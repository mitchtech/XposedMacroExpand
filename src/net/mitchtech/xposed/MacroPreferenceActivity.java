
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
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ipaulpro.afilechooser.utils.FileUtils;

import net.mitchtech.xposed.macroexpand.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MacroPreferenceActivity extends PreferenceActivity {

    private static final String TAG = MacroPreferenceActivity.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";
    private static final int FORMAT_JSON = 0;
    private static final int FORMAT_AHK = 1;
    
    private static final int REQUEST_CODE = 6384; // onActivityResult request
    
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
                        importFileChooser();
                        return false;
                    }
                });
        
        findPreference("prefExportMacros").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // exportMacros(FORMAT_JSON);
                        exportMacros(FORMAT_AHK);
                        return false;
                    }
                });
    }
    
    private void exportMacros(int format) {
        String json = getPreferenceScreen().getSharedPreferences().getString("json", "");
        String path = Environment.getExternalStorageDirectory() + "/macros.txt";
        List<MacroEntry> replacements = null;
        
        if (format == FORMAT_AHK) {
            Type type = new TypeToken<List<MacroEntry>>() { }.getType();
            replacements = new Gson().fromJson(json, type);
            
            if (replacements == null || replacements.isEmpty()) { 
                Toast.makeText(this, "Macro list empty, file not exported", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            
            if (format == FORMAT_AHK) {
                for (MacroEntry macro : replacements) {              
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
    

    private void importFileChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        target.setType(FileUtils.MIME_TYPE_TEXT);
        Intent intent = Intent.createChooser(target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Log.i(TAG, "path = " + path);
//                            SharedPreferences.Editor editor = getPreferenceScreen()
//                                    .getSharedPreferences().edit();
//                            editor.putString("prefSoundFile", path);
//                            editor.commit();
                            importConfirmDialog();
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void importConfirmDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MacroPreferenceActivity.this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Append or Overwrite?")
                .setMessage("Do you want to overwrite your macro list or append imported entries? This operation cannot be undone!")
                .setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
//                        mList.remove(mListview.getItemAtPosition(position));
//                        if (mList.isEmpty()) {
//                            mListEmptyTextView.setVisibility(View.VISIBLE);
//                        }
//                        mAdapter.notifyDataSetChanged();
//                        saveMacroList();
                    }
                }).setNeutralButton("Append", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }
    
}
