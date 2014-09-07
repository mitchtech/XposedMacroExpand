
package net.mitchtech.xposed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.mitchtech.xposed.macroexpand.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EditMacrosActivity extends Activity {

    private static final String TAG = EditMacrosActivity.class.getSimpleName();
    private static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";

    private ListView mListview;
    private TextView mListEmptyTextView;
    private ArrayList<MacroEntry> mList;
    private MacroAdapter mAdapter;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_macros);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());     
        
        mListview = (ListView) findViewById(R.id.listview);
        mListEmptyTextView = (TextView) findViewById(R.id.listEmptyText);
        mList = new ArrayList<MacroEntry>();

        String json = mPrefs.getString("json", "");
        Type type = new TypeToken<List<MacroEntry>>() { }.getType();
        List<MacroEntry> replacements = new Gson().fromJson(json, type);

        if (replacements == null || replacements.isEmpty()) {
            mListEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mListEmptyTextView.setVisibility(View.GONE);
            for (MacroEntry replacement : replacements) {
                mList.add(replacement);
            }
        }

        mAdapter = new MacroAdapter(this, mList);
        mListview.setAdapter(mAdapter);
        mListview.setTextFilterEnabled(true);
        mListview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editMacro((MacroEntry) parent.getItemAtPosition(position), position);
            }
        });

        mListview.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeReplacement(position);
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        saveMacroList();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            
            case R.id.action_add:
                addMacro();
                return true;

            case R.id.action_settings:
                Intent settings = new Intent(this, MacroPreferenceActivity.class);
                startActivity(settings);
                return true;

            case R.id.action_exit:
                this.finish();
                return true;
        }

        return false;
    }

    private void addMacro() {
        editMacro(new MacroEntry("", ""), -1);
    }

    private void editMacro(MacroEntry entry, final int position) {
        LayoutInflater factory = LayoutInflater.from(EditMacrosActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_edit_macro, null);
        final EditText actual = (EditText) textEntryView.findViewById(R.id.actual);
        final EditText replacement = (EditText) textEntryView.findViewById(R.id.replacement);
        actual.setText(entry.actual, TextView.BufferType.EDITABLE);
        replacement.setText(entry.replacement, TextView.BufferType.EDITABLE);

        final AlertDialog.Builder alert = new AlertDialog.Builder(EditMacrosActivity.this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Define Macro").setView(textEntryView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        
                        String actualText = actual.getText().toString();
                        String replacementText = replacement.getText().toString();
                        
                        // check for regex in text ($, ^, +, *, ., !, ?, |, \, (), {}, [])
//                        if (isTextRegexFree(actualText) && isTextRegexFree(replacementText)) {
                            if (position > -1) {
                                mList.remove(mListview.getItemAtPosition(position));
                            }
                            mList.add(new MacroEntry(actualText, replacementText));
                            mListEmptyTextView.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                            saveMacroList();
//                        } else {    
//                            Toast.makeText(
//                                    EditMacrosActivity.this,
//                                    "Macros cannot contain regular expression characters ($, ^, +, *, ., !, ?, |, \\, (), {}, [])",
//                                    Toast.LENGTH_SHORT).show();
//                            editMacro(new MacroEntry(actualText, replacementText), position);
//                        }
                        
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }

    private void removeReplacement(final int position) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(EditMacrosActivity.this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Delete Macro?")
                .setMessage("Are you sure you want to delete this macro?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mList.remove(mListview.getItemAtPosition(position));
                        if (mList.isEmpty()) {
                            mListEmptyTextView.setVisibility(View.VISIBLE);
                        }
                        mAdapter.notifyDataSetChanged();
                        saveMacroList();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }
    
    private boolean isTextRegexFree(String text) {
        // ($, ^, +, *, ., !, ?, |, \, (), {}, [])
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
    
    private void saveMacroList() {
        String json = new Gson().toJson(mList);
        Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString("json", json);
        prefsEditor.commit();
    }

    public static String getVersion(Context context) {
        String version = "1.0";
        try {
            PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }

}
