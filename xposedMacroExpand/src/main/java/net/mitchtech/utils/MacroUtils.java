package net.mitchtech.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.mitchtech.xposed.MacroEntry;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

public class MacroUtils {
    
    private static final String TAG = MacroUtils.class.getSimpleName();
    public static final String DYNAMIC_MACRO_WILDCARD = "@";

    // Date and time dynamic macros
    public static final int MACRO_DATE = 0;
    public static final int MACRO_TIME = 1;
    public static final int MACRO_WEEKDAY = 2;
    public static String MACRO_DATE_TEXT = "@date";
    public static String MACRO_TIME_TEXT = "@time";
    public static String MACRO_WEEKDAY_TEXT = "@day";

    // Network dynamic macros
    public static final int MACRO_MAC_ADDRESS = 10;
    public static final int MACRO_LAN_IP_ADDRESS = 11;
    public static final int MACRO_WAN_IP_ADDRESS = 12;
    public static final int MACRO_SSID = 13;
    public static String MACRO_MAC_ADDRESS_TEXT = "@macaddr";
    public static String MACRO_LAN_IP_ADDRESS_TEXT = "@lanip";
    public static String MACRO_WAN_IP_ADDRESS_TEXT = "@wanip";
    public static String MACRO_SSID_TEXT = "@ssid";

    // Battery and power dynamic macros
    public static final int MACRO_BATTERY_LEVEL = 20;
    public static final int MACRO_BATTERY_CHARGING = 21;
    public static String MACRO_BATTERY_LEVEL_TEXT = "@batt";
    public static String MACRO_BATTERY_CHARGING_TEXT = "@plugged";

    // Storage dynamic macros
    public static final int MACRO_INTERNAL_MB_FREE = 30;
    public static final int MACRO_EXTERNAL_MB_FREE = 31;
    public static String MACRO_INTERNAL_MB_FREE_TEXT = "@imbfree";
    public static String MACRO_EXTERNAL_MB_FREE_TEXT = "@embfree";


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
      if (macroList == null) {
          macroList = new ArrayList<MacroEntry>();
      }
      return macroList;
    }

    public static ArrayList<MacroEntry> loadDynamicMacroList(SharedPreferences prefs) {
        ArrayList<MacroEntry> macroList = new ArrayList<MacroEntry>();

        if (prefs.getBoolean("prefDynamicDate", false))
            macroList.add(new MacroEntry(prefs.getString("prefDynamicDateKeyword", MACRO_DATE_TEXT), "" + MACRO_DATE));
        if (prefs.getBoolean("prefDynamicTime", false))
            macroList.add(new MacroEntry(prefs.getString("prefDynamicTimeKeyword", MACRO_TIME_TEXT), "" + MACRO_TIME));
        if (prefs.getBoolean("prefDynamicWeekday", false))
            macroList.add(new MacroEntry(prefs.getString("prefDynamicWeekdayKeyword", MACRO_WEEKDAY_TEXT), "" + MACRO_WEEKDAY));

        if (prefs.getBoolean(MACRO_MAC_ADDRESS_TEXT, true))
            macroList.add(new MacroEntry(MACRO_MAC_ADDRESS_TEXT, "" + MACRO_MAC_ADDRESS));
        if (prefs.getBoolean(MACRO_LAN_IP_ADDRESS_TEXT, true))
            macroList.add(new MacroEntry(MACRO_LAN_IP_ADDRESS_TEXT, "" + MACRO_LAN_IP_ADDRESS));
        if (prefs.getBoolean(MACRO_WAN_IP_ADDRESS_TEXT, true))
            macroList.add(new MacroEntry(MACRO_WAN_IP_ADDRESS_TEXT, "" + MACRO_WAN_IP_ADDRESS));
        if (prefs.getBoolean(MACRO_SSID_TEXT, true))
            macroList.add(new MacroEntry(MACRO_SSID_TEXT, "" + MACRO_SSID));

        if (prefs.getBoolean("prefDynamicBatteryLevel", false))
            macroList.add(new MacroEntry(prefs.getString("prefDynamicBatteryLevelKeyword", MACRO_BATTERY_LEVEL_TEXT), "" + MACRO_BATTERY_LEVEL));
        if (prefs.getBoolean("prefDynamicBatteryState", false))
            macroList.add(new MacroEntry(prefs.getString("prefDynamicBatteryStateKeyword", MACRO_BATTERY_CHARGING_TEXT), "" + MACRO_BATTERY_CHARGING));

        if (prefs.getBoolean(MACRO_INTERNAL_MB_FREE_TEXT, true))
            macroList.add(new MacroEntry(MACRO_INTERNAL_MB_FREE_TEXT, "" + MACRO_INTERNAL_MB_FREE));
        if (prefs.getBoolean(MACRO_EXTERNAL_MB_FREE_TEXT, true))
            macroList.add(new MacroEntry(MACRO_EXTERNAL_MB_FREE_TEXT, "" + MACRO_EXTERNAL_MB_FREE));
        return macroList;
    }
    
    // or "ISO-8859-1" for ISO Latin 1
    public static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

    public static boolean isPureAscii(String string) {
        return asciiEncoder.canEncode(string);
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
    
}
