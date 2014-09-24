
package net.mitchtech.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class XposedMacroExpand implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = XposedMacroExpand.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";

    private XSharedPreferences prefs;

    private ArrayList<MacroEntry> mMacroList;
    
	private final String[] preMacro = { ":ld:", // ":ld:" local date
			":\\d+\\+ld:", // ":x+ld:" local date + number of days
			":lt:", // ":lt:" local time
			":\\d+\\+lt:", // ":x+lt:" local time + number of hours
	};
	
    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        loadPrefs();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        
        // don't replace text in this package, otherwise can't edit macros
        if (lpparam.packageName.equals(PKG_NAME)) {
            return;
        }

        // public void setText(CharSequence text, BufferType type)
        findAndHookMethod(EditText.class, "setText", CharSequence.class, TextView.BufferType.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam)
                            throws Throwable {
                        CharSequence actualText = (CharSequence) methodHookParam.args[0];
                        if (actualText != null) {
                            // XposedBridge.log(TAG + ": setText(): " + actualText);
                            String replacementText = replaceText(actualText.toString());
                            methodHookParam.args[0] = replacementText;
                        } 
                    }
            });
        
        // common hook method for all edittext constructors
        XC_MethodHook editTextMethodHook = new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                final EditText editText = (EditText) methodHookParam.thisObject;
//                if (editText instanceof MultiAutoCompleteTextView) {
//                    // XposedBridge.log(TAG + ": MultiAutoCompleteTextView");
//                    return;
//                }
                editText.setOnFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (isEnabled("prefIgnorePassword") && editText.getInputType() 
                                == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                         // XposedBridge.log(TAG + ": password box & ignore password enabled");
                        } else {
                            if (!hasFocus) {
                                String actualText = editText.getText().toString();
                                // XposedBridge.log(TAG + ": onFocusChange(): " + actualText);
                                String replacementText = replaceText(actualText);
                                // prevent stack overflow, only set text if modified
                                if (!actualText.equals(replacementText)) {
                                    editText.setText(replacementText);
                                    editText.setSelection(editText.getText().length());
                                }
                            }
                        }
                    }
                });

                editText.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence text, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (isEnabled("prefIgnorePassword") && editText.getInputType() 
                                == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            // XposedBridge.log(TAG + ": password box & ignore password enabled");
                        } else {
                            String actualText = editable.toString();
                            // XposedBridge.log(TAG + ": afterTextChanged(): " + actualText);
                            String replacementText = replaceText(actualText);
                            // prevent stack overflow, only set text if modified
                            if (!actualText.equals(replacementText)) {
                                editText.setText(replacementText);
                                // dont move cursor if field is MultiAutoCompleteTextView
                                if (editText instanceof MultiAutoCompleteTextView) {
                                    // XposedBridge.log(TAG + ": MultiAutoCompleteTextView");
                                } else {
                                    if (isEnabled("prefMoveCursor")) {
                                        editText.setSelection(editText.getText().length());
                                    }
                                }
                            }
                        }
                    }
                });
            }
        };
        
        // public EditText(Context context) 
        findAndHookConstructor(EditText.class, Context.class, editTextMethodHook);
        
        // public EditText(Context context, AttributeSet attrs)
        findAndHookConstructor(EditText.class, Context.class, AttributeSet.class, editTextMethodHook);
        
        // public EditText(Context context, AttributeSet attrs, int defStyle)
        findAndHookConstructor(EditText.class, Context.class, AttributeSet.class, int.class, editTextMethodHook);        
    }

    private String replaceText(String actualText) {
        String replacementText = actualText.toString();
        if (mMacroList != null && !mMacroList.isEmpty()) {           
            for (MacroEntry replacement : mMacroList) {
                if (isEnabled("prefIgnoreCase")) {
                    // "(?i)" used for case insensitive replace
                    replacementText = replacementText.replaceAll(("(?i)" + Pattern.quote(replacement.actual)),
                            replacement.replacement);
                } else {
                    // case sensitive replacement
                    replacementText = replacementText.replaceAll(Pattern.quote(replacement.actual),
                            replacement.replacement);
                }
            }
        }
		if (isEnabled("prefEnablePreload"))
			replacementText = preloadedReplace(replacementText);
		return replacementText;
	}

	private String preloadedReplace(String actualText) {
		String replacementText = actualText.toString();
		for (String replacement : preMacro) {
			Matcher matcher = Pattern.compile(replacement).matcher(
					replacementText);
			int num = 0;
			String type = "";
			try {
				if (matcher.find()) {
					type = matcher.group();
					matcher = Pattern.compile("(\\d+)").matcher(type);
					if (matcher.find())
						num = Integer.parseInt(matcher.group());
				}
			} catch (Exception ex) {
				// Avoid empty ones
			}
			if (type != null && !type.isEmpty())
				replacementText = replacementText.replaceAll(
						("(?i)" + replacement), fxConvert(type, num));
		}
		return replacementText;
	}

	private String fxConvert(String type, Integer num) {
		if (type == null)
			return "";
		if (type.contains("ld")) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, num);
			String date = new SimpleDateFormat("dd/MM/yyyy",
					Locale.getDefault()).format(c.getTime());
			return date;
		}
		if (type.contains("lt")) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR, num);
			String date = new SimpleDateFormat("h:mm a", Locale.getDefault())
					.format(c.getTime());
			return date;
		}
		return "";
	}

    private boolean isEnabled(String pkgName) {
        prefs.reload();
        return prefs.getBoolean(pkgName, false);
    }

    private void loadPrefs() {
        prefs = new XSharedPreferences(PKG_NAME);
        prefs.makeWorldReadable();
        mMacroList = MacroUtils.loadMacroList(prefs);
        XposedBridge.log(TAG + ": prefs loaded.");
//        String json = prefs.getString("json", "");
//        Type type = new TypeToken<List<MacroEntry>>() { }.getType();
//        mMacroList = new Gson().fromJson(json, type);
    }

}
