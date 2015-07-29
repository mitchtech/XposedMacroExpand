
package net.mitchtech.xposed;

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

import net.mitchtech.utils.DateTimeUtils;
import net.mitchtech.utils.MacroUtils;
import net.mitchtech.utils.NetworkUtils;
import net.mitchtech.utils.PowerUtils;
import net.mitchtech.utils.StorageUtils;

import java.util.ArrayList;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class XposedMacroExpand implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = XposedMacroExpand.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";

    private XSharedPreferences prefs;

    private ArrayList<MacroEntry> mMacroList;
    private ArrayList<MacroEntry> mDynamicMacroList;
    private Context mContext;

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
                mContext = (Context) methodHookParam.args[0];
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
        String replacementText = actualText;

        // process replacements in user assigned macro list
        if (mMacroList != null && !mMacroList.isEmpty()) {           
            for (MacroEntry macroEntry : mMacroList) {
                if (isEnabled("prefIgnoreCase")) {
                    // "(?i)" used for case insensitive replace
                    replacementText = replacementText.replaceAll(("(?i)" + Pattern.quote(macroEntry.actual)),
                            macroEntry.replacement);
                } else {
                    // case sensitive replacement
                    replacementText = replacementText.replaceAll(Pattern.quote(macroEntry.actual),
                            macroEntry.replacement);
                }
            }
        }

        // process replacements in dynamically generated macro list
        if (isEnabled("prefEnableDynamicMacros")) {
            if (mDynamicMacroList != null && !mDynamicMacroList.isEmpty()) {

                for (MacroEntry macroEntry : mDynamicMacroList) {

                    if (replacementText.contains(macroEntry.actual)) {

                        String dynamicText = "";
                        switch (Integer.parseInt(macroEntry.replacement)) {
                            case MacroUtils.MACRO_DATE:
                                dynamicText = DateTimeUtils.getDate();
                                break;

                            case MacroUtils.MACRO_TIME:
                                dynamicText = DateTimeUtils.getCurrentTime();
                                break;

                            case MacroUtils.MACRO_WEEKDAY:
                                dynamicText = DateTimeUtils.getDayOfWeek();
                                break;

                            case MacroUtils.MACRO_MAC_ADDRESS:
                                dynamicText = NetworkUtils.getMacAddress(mContext);
                                break;

                            case MacroUtils.MACRO_LAN_IP_ADDRESS:
                                dynamicText = NetworkUtils.getIpAddress(mContext);
                                break;

                            case MacroUtils.MACRO_WAN_IP_ADDRESS:
                                dynamicText = NetworkUtils.getPublicIpAddress();
                                break;

                            case MacroUtils.MACRO_SSID:
                                dynamicText = NetworkUtils.getWifiSsid(mContext);
                                break;

                            case MacroUtils.MACRO_BATTERY_LEVEL:
                                dynamicText = "" + PowerUtils.getBatteryLevel(mContext);
                                break;

                            case MacroUtils.MACRO_BATTERY_CHARGING:
                                dynamicText = "" + PowerUtils.getBatteryState(mContext);
                                break;

                            case MacroUtils.MACRO_INTERNAL_MB_FREE:
                                dynamicText = "" + StorageUtils.getInternalAvailableSpace();
                                break;

                            case MacroUtils.MACRO_EXTERNAL_MB_FREE:
                                dynamicText = "" + StorageUtils.getExternalAvailableSpace();
                                break;

                            default:
                                break;
                        }
                        replacementText = replacementText.replaceAll(Pattern.quote(macroEntry.actual),
                                dynamicText);
                    }
                }
            }
        }

        return replacementText;
    }

    private boolean isEnabled(String pkgName) {
        prefs.reload();
        return prefs.getBoolean(pkgName, true);
    }

    private void loadPrefs() {
        prefs = new XSharedPreferences(PKG_NAME);
        prefs.makeWorldReadable();
        mMacroList = MacroUtils.loadMacroList(prefs);
        mDynamicMacroList = MacroUtils.loadDynamicMacroList(prefs);
        XposedBridge.log(TAG + ": prefs loaded.");
//        String json = prefs.getString("json", "");
//        Type type = new TypeToken<List<MacroEntry>>() { }.getType();
//        mMacroList = new Gson().fromJson(json, type);
    }

}
