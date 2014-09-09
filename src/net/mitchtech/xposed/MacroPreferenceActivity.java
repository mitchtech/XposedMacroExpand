
package net.mitchtech.xposed;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ipaulpro.afilechooser.utils.FileUtils;

import net.mitchtech.xposed.macroexpand.R;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MacroPreferenceActivity extends PreferenceActivity {

    private static final String TAG = MacroPreferenceActivity.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";
    private static final int FORMAT_AHK = 0;
    private static final int FORMAT_JSON = 1;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        findPreference("prefImportMacros").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        importFormatDialog(); // importFileChooser();
                        return false;
                    }
                });
        
        findPreference("prefExportMacros").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        exportFormatDialog();
                        return false;
                    }
                });
    }
    
    private void exportMacros(int format) {
        String json = getPreferenceScreen().getSharedPreferences().getString("json", "");
        String path = "";
        List<MacroEntry> macroList = null;
        
        if (format == FORMAT_AHK) {
            path = Environment.getExternalStorageDirectory() + "/macros.ahk";
            Type type = new TypeToken<List<MacroEntry>>() { }.getType();
            macroList = new Gson().fromJson(json, type);
            
            if (macroList == null || macroList.isEmpty()) { 
                Toast.makeText(this, "Macro list empty, file not exported", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (format == FORMAT_JSON) {
            path = Environment.getExternalStorageDirectory() + "/macros.json";
        }
        
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            
            if (format == FORMAT_AHK) {
                for (MacroEntry macro : macroList) {              
                    outputStreamWriter.append("::" + macro.actual + "::" + macro.replacement + "\n");
                }
            } else if (format == FORMAT_JSON) {                
                outputStreamWriter.append(json);
            }
            
            outputStreamWriter.close();
            fileOutputStream.close();
            Toast.makeText(this, "Macro list exported: " + path, Toast.LENGTH_SHORT).show();
        } 
        catch (Exception e) {
            Log.e(TAG, "File export error:", e);
        }
    }
    
    private void importMacros(String path, int format) {
        StringBuilder json = new StringBuilder();
        ArrayList<MacroEntry> macroList = new ArrayList<MacroEntry>();
        String line;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, line);
                if (format == FORMAT_JSON) {
                    json.append(line); // json.append(line + "\n"); 
                } else if (format == FORMAT_AHK) {
                    if (line.startsWith("::")) {
                        String[] split = line.split("::");
                        if (split.length != 3) {
                            Log.e(TAG, "Bad line format. split.length[" + split.length + " !=3]");
                        } else {
                            MacroEntry macro = new MacroEntry(split[1], split[2]);
                            Log.i(TAG, "macro:" + macro.toString());
                            macroList.add(macro);
                        }
                    }
                }
            }
            bufferedReader.close();
            SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
            
                       
            if (format == FORMAT_JSON) {
                editor.putString("json", json.toString());                
            } else if (format == FORMAT_AHK) {
                editor.putString("json", MacroUtils.macroArrayListToJson(macroList));
            }
            editor.commit();
            
            Toast.makeText(this, "Macro list imported: " + path, Toast.LENGTH_SHORT).show();
            
            MacroUtils.reloadLauncherActivity(this);
            
        } catch (Exception e) {
            Log.e(TAG, "File import error:", e);
        }
    }
    
    private void importFileChooser(int format) {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // target.setType(FileUtils.MIME_TYPE_TEXT);
        Intent intent = Intent.createChooser(target, getString(R.string.chooser_title));
        try {
//            startActivityForResult(intent, REQUEST_CODE);
            startActivityForResult(intent, format);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
//            case REQUEST_CODE:
            case FORMAT_AHK:
            case FORMAT_JSON:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            final String path = FileUtils.getPath(this, uri);
                            Log.i(TAG, "path = " + path);
                            importConfirmDialog(path, requestCode);
                        } catch (Exception e) {
                            Log.e(TAG, "File select error:", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void importConfirmDialog(final String path, final int format) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MacroPreferenceActivity.this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Overwrite Macro List?")
//                .setMessage("Do you want to overwrite your macro list or append imported entries? \n\nThis operation cannot be undone!")
                  .setMessage("Are you sure you want to overwrite your macro list with imported entries? \n\nThis operation cannot be undone!")
                .setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        importMacros(path, format); // importMacros(path, FORMAT_AHK);
                    }
//                }).setNeutralButton("Append", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }
    
    private void importFormatDialog() {
        final CharSequence[] items = {"AutoHotKey", "JSON"};
        final AlertDialog.Builder alert = new AlertDialog.Builder(MacroPreferenceActivity.this);
        alert.setIcon(R.drawable.ic_launcher)
                .setTitle("Select Import Format")
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case FORMAT_AHK:
                                importFileChooser(FORMAT_AHK);
                                break;
                            case FORMAT_JSON:
                                importFileChooser(FORMAT_JSON);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        alert.show();
    }
    
    private void exportFormatDialog() {
        final CharSequence[] items = {"AutoHotKey", "JSON"};
        final AlertDialog.Builder alert = new AlertDialog.Builder(MacroPreferenceActivity.this);
        alert.setIcon(R.drawable.ic_launcher)
                .setTitle("Select Export Format")
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case FORMAT_AHK:
                                exportMacros(FORMAT_AHK);
                                break;
                            case FORMAT_JSON:
                                exportMacros(FORMAT_JSON);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        alert.show();
    }
    
}
