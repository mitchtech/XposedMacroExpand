package net.mitchtech.xposed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MacroUtils {
    
    private static final String TAG = MacroUtils.class.getSimpleName();
    
    public static ArrayList<MacroEntry> jsonToMacroArrayList(String json) {
        ArrayList<MacroEntry> macroList = new ArrayList<MacroEntry>();
        Type type = new TypeToken<List<MacroEntry>>() { }.getType();
        macroList = new Gson().fromJson(json, type);
        return macroList;
    }
    
    public static String macroArrayListToJson(ArrayList<MacroEntry> macroList) {
        return new Gson().toJson(macroList);
    }
    
    public static void saveMacroList(ArrayList<MacroEntry> macroList, SharedPreferences prefs) {
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString("json", macroArrayListToJson(macroList));
        prefsEditor.commit();
    }
    
    public static ArrayList<MacroEntry> loadMacroList(SharedPreferences prefs) {
      String json = prefs.getString("json", "");
      Type type = new TypeToken<List<MacroEntry>>() { }.getType();
      ArrayList<MacroEntry> macroList = new Gson().fromJson(json, type);
      return macroList;
    }
        
    // check for regex in text ($, ^, +, *, ., !, ?, |, \, (), {}, [])
    public static boolean isTextRegexFree(String text) {
        if (text.contains("$") || text.contains("^") || text.contains("+") || text.contains("*")
                || text.contains(".") || text.contains("!") || text.contains("?")
                || text.contains("$") || text.contains("|") || text.contains("\\")
                || text.contains("(") || text.contains(")") || text.contains("{")
                || text.contains("}") || text.contains("[") || text.contains("]")) {
            return false;
        } else {
            return true;
        }
    }
    
    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = " v" + pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }
    
    public static Class<?> getLauncherClass(Context context) {
        String className = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName())
                .getComponent().getClassName();
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        return clazz;
    }
    
    public static void reloadLauncherActivity(Context context) {
        Class<?> clazz = getLauncherClass(context);
        Intent intent = new Intent(context, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
}
