
package net.mitchtech.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class XposedMacroExpand implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = XposedMacroExpand.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";

    private XSharedPreferences prefs;

    public ArrayList<MacroEntry> replacements;

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
                if (editText instanceof MultiAutoCompleteTextView) {
                    // XposedBridge.log(TAG + ": MultiAutoCompleteTextView");
                    return;
                }
                editText.setOnFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
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
                        String actualText = editable.toString();
                        // XposedBridge.log(TAG + ": afterTextChanged(): " + actualText);
                        String replacementText = replaceText(actualText);
                        // prevent stack overflow, only set text if modified
                        if (!actualText.equals(replacementText)) {
                            editText.setText(replacementText);
                            editText.setSelection(editText.getText().length());
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
        if (replacements != null && !replacements.isEmpty()) {           
            for (MacroEntry replacement : replacements) {
                if (isEnabled("prefCaseSensitive")) {
                    // case sensitive replacement
                    replacementText = replacementText.replaceAll(replacement.actual,
                            replacement.replacement);
                } else {
                    // "(?i)" used for case insensitive replace
                    replacementText = replacementText.replaceAll(("(?i)" + replacement.actual),
                            replacement.replacement);
                }
            }
        }
        return replacementText;
    }

    private boolean isEnabled(String pkgName) {
        prefs.reload();
        return prefs.getBoolean(pkgName, false);
    }

    private void loadPrefs() {
        prefs = new XSharedPreferences(PKG_NAME);
        prefs.makeWorldReadable();     
        String json = prefs.getString("json", "");
        Type type = new TypeToken<List<MacroEntry>>() { }.getType();
        replacements = new Gson().fromJson(json, type);
        XposedBridge.log(TAG + ": prefs loaded.");
    }

}
