
package net.mitchtech.xposed;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;

import com.ipaulpro.afilechooser.utils.FileUtils;

import net.mitchtech.xposed.macroexpand.R;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MacroPreferenceActivity extends PreferenceActivity {

    private static final String TAG = MacroPreferenceActivity.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";
    private static final int FORMAT_AHK = 0;
    private static final int FORMAT_JSON = 1;
    
    private SharedPreferences mPrefs;
    
    private Preference mPrefImportMacros;
    private Preference mPrefExportMacros;
    private Preference mPrefAboutModule;
    private Preference mPrefAboutXposed;
    private Preference mPrefDonatePaypal;
    private Preference mPrefGithub;
    private Preference mPrefHelp;
    private Preference mPrefChangeLog;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        
        // this is important since settings executed in the context of the hooked package
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mPrefs = getPreferenceScreen().getSharedPreferences();
        
        mPrefImportMacros = findPreference("prefImportMacros");
        mPrefExportMacros = findPreference("prefExportMacros");        
        mPrefAboutModule = findPreference("prefAboutModule");
        mPrefAboutXposed = findPreference("prefAboutXposed");
        mPrefDonatePaypal = findPreference("prefDonatePaypal");
        mPrefGithub = findPreference("prefGithub");
        mPrefHelp = findPreference("prefHelp");
        mPrefChangeLog = findPreference("prefChangeLog");

        String version = MacroUtils.getVersion(this);
        mPrefAboutModule.setTitle(this.getTitle() + version);
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen prefScreen, Preference pref) {
        Intent intent = null;
        
        if (pref == mPrefImportMacros) {
            importFormatDialog();
        } else if (pref == mPrefExportMacros) {
            exportFormatDialog();
        } else if (pref == mPrefAboutModule) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_xda)));
        } else if (pref == mPrefAboutXposed) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_xposed)));
        } else if (pref == mPrefDonatePaypal) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_paypal)));
        } else if (pref == mPrefGithub) {
        } else if (pref == mPrefHelp) {
        } else if (pref == mPrefChangeLog) {
            changelogDialog();
        }
        
        if (intent != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onPreferenceTreeClick(prefScreen, pref);
    }
    
    private void importFileChooser(int format) {
        Intent target = FileUtils.createGetContentIntent();
        // target.setType(FileUtils.MIME_TYPE_TEXT);
        Intent intent = Intent.createChooser(target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, format);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FORMAT_AHK:
            case FORMAT_JSON:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final Uri uri = data.getData();
                        // Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            final String path = FileUtils.getPath(this, uri);
                            // Log.i(TAG, "path = " + path);
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
                        // setProgressBarIndeterminateVisibility(true);
                        new ImportMacroListTask(format).execute(path);
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
    
    private void importResultDialog(final String log) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MacroPreferenceActivity.this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Import Result")
                .setMessage(log)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        MacroUtils.reloadLauncherActivity(MacroPreferenceActivity.this);
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
                        setProgressBarIndeterminateVisibility(true);
                        switch (which) {
                            case FORMAT_AHK:
                                // exportMacros(FORMAT_AHK);
                                new ExportMacroListTask(FORMAT_AHK).execute();
                                break;
                            case FORMAT_JSON:
                                // exportMacros(FORMAT_JSON);
                                new ExportMacroListTask(FORMAT_JSON).execute();
                                break;
                        }
                        dialog.dismiss();
                    }
                });
        alert.show();
    }
    
    private void exportResultDialog(final String log) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MacroPreferenceActivity.this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Export Result")
                .setMessage(log)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }
    
    private void changelogDialog() {
        WebView webView = new WebView (this);
        webView.loadUrl("file:///android_asset/changelog.html");
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Changelog")
                .setView(webView)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }
    
    class ImportMacroListTask extends AsyncTask<String, Void, String> {

        int mFormat;
        
        public ImportMacroListTask(int format) {
            mFormat = format;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(true);
        }
        
        @Override
        protected String doInBackground(String... params) {
            StringBuilder json = new StringBuilder();
            StringBuilder log = new StringBuilder();
            String path = params[0];
            ArrayList<MacroEntry> macroList = new ArrayList<MacroEntry>();
            String line;
            
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
                while ((line = bufferedReader.readLine()) != null) {
                    if (mFormat == FORMAT_JSON) {
                        json.append(line); // json.append(line + "\n");
                        log.append("Import json: " + line + "\n");
                    } else if (mFormat == FORMAT_AHK) {
                        if (!MacroUtils.isPureAscii(line)) {
                            log.append("Skipping non-ascii line:: [" + line + "]\n");
                        } else if (line.startsWith("::")) {
                            String[] split = line.split("::");
                            if (split.length != 3) {
                                log.append("Invalid format. Skipping line: [" + line + "]\n");
                            } else {
                                String macro = split[1];
                                String replacement = split[2];
                                if (replacement.contains(";")) {
                                    String[] removeComment = replacement.split(";");
                                    replacement = removeComment[0].trim();
                                }
                                // MacroEntry macroEntry = new MacroEntry(split[1], split[2]);
                                MacroEntry macroEntry = new MacroEntry(macro, replacement);
                                log.append("Import Macro: [" + macroEntry.toString() + "]\n");
                                macroList.add(macroEntry);
                            }
                        } else {
                            log.append("Invalid format. Skipping line: [" + line + "]\n");
                        }
                    }
                }
                bufferedReader.close();
                         
                if (mFormat == FORMAT_JSON) {
                    macroList = MacroUtils.jsonToMacroArrayList(json.toString());
                }
                
                String result;
                if (macroList.size() > 0) {
                    result = "Complete. Imported: " + macroList.size() + " macros from " + path; 
                            // + "\n\nSoft reboot to activate";
                    MacroUtils.saveMacroList(macroList, mPrefs);                
                } else {
                    result = "Complete. No macros found in file " + path;
                }
                
                if (mPrefs.getBoolean("prefImportDebug", false)) {
                    FileOutputStream fileOutputStream = new FileOutputStream(path + ".log");
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                    outputStreamWriter.append(log);
                    outputStreamWriter.close();
                    fileOutputStream.close();
                    result = result + "\n\nDebug log output to " + path + ".log";
                }
                
                return result;
                
            } catch (Exception e) {
                Log.e(TAG, "File import error:", e);
                return "File import error:" + e;
            }
        }
        
        protected void onPostExecute(String result) {
            setProgressBarIndeterminateVisibility(false);
            final String output = result.toString();
            importResultDialog(output);
        }
    }
    
    class ExportMacroListTask extends AsyncTask<String, Void, String> {

        int mFormat;
        
        public ExportMacroListTask(int format) {
            mFormat = format;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(true);
        }
        
        @Override
        protected String doInBackground(String... params) {
            ArrayList<MacroEntry> macroList = MacroUtils.loadMacroList(mPrefs);
            
            if (macroList == null || macroList.isEmpty()) { 
                return "Macro list empty. No file was exported.";
            }
            
            try {
                String path = "";
                FileOutputStream fileOutputStream = null;
                OutputStreamWriter outputStreamWriter = null;
                
                switch (mFormat) {
                    case FORMAT_AHK:
                        path = Environment.getExternalStorageDirectory() + "/macros.ahk";
                        fileOutputStream = new FileOutputStream(path);
                        outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                        for (MacroEntry macro : macroList) {              
                            outputStreamWriter.append("::" + macro.actual + "::" + macro.replacement + "\n");
                        }
                        break;
                        
                    case FORMAT_JSON:
                        path = Environment.getExternalStorageDirectory() + "/macros.json";
                        fileOutputStream = new FileOutputStream(path);
                        outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                        outputStreamWriter.append(MacroUtils.macroArrayListToJson(macroList));
                        break;

                    default:
                        break;
                }

                outputStreamWriter.close();
                fileOutputStream.close();
                return "Complete. Exported " + macroList.size() + " macros to " + path;
            } 
            catch (Exception e) {
                Log.e(TAG, "File export error: ", e);
                return "File export error: " + e;
            }
        }
        
        protected void onPostExecute(String result) {
            setProgressBarIndeterminateVisibility(false);
            final String output = result.toString();
            exportResultDialog(output);
        }
    }
}
