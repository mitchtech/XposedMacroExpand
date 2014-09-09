
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
    private ArrayList<MacroEntry> mMacroList;
    private MacroAdapter mMacroAdapter;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_macros);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());     
        
        mListview = (ListView) findViewById(R.id.listview);
        mListEmptyTextView = (TextView) findViewById(R.id.listEmptyText);
        mMacroList = MacroUtils.loadMacroList(mPrefs);
        
////        mMacroList = new ArrayList<MacroEntry>();
//        String json = mPrefs.getString("json", "");
//        Type type = new TypeToken<List<MacroEntry>>() { }.getType();
////        List<MacroEntry> replacements = new Gson().fromJson(json, type);
//        mMacroList = new Gson().fromJson(json, type);
//        if (replacements == null || replacements.isEmpty()) {
        
//        if (mMacroList == null || mMacroList.isEmpty()) {
//            mListEmptyTextView.setVisibility(View.VISIBLE);
//        } else {
//            mListEmptyTextView.setVisibility(View.GONE);
////            for (MacroEntry replacement : replacements) {
////                mList.add(replacement);
////            }
//        }

        mMacroAdapter = new MacroAdapter(this, mMacroList);
        mListview.setAdapter(mMacroAdapter);
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
                removeMacro(position);
                return true;
            }
        });
    }
    
    @Override
    protected void onResume() {
//        mMacroList = MacroUtils.loadMacroList(mPrefs);
        if (mMacroList == null || mMacroList.isEmpty()) {
            mListEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mListEmptyTextView.setVisibility(View.GONE);
        }
//        mMacroAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // MacroUtils.saveMacroList(mMacroList, mPrefs);
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
//                        if (isTextRegexFree(actualText) && isTextRegexFree(replacementText)) {
                            if (position > -1) {
                                mMacroList.remove(mListview.getItemAtPosition(position));
                            }
                            mMacroList.add(new MacroEntry(actualText, replacementText));
                            mListEmptyTextView.setVisibility(View.GONE);
                            mMacroAdapter.notifyDataSetChanged();
                            MacroUtils.saveMacroList(mMacroList, mPrefs);
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

    private void removeMacro(final int position) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(EditMacrosActivity.this);
        alert.setIcon(R.drawable.ic_launcher).setTitle("Delete Macro?")
                .setMessage("Are you sure you want to delete this macro?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mMacroList.remove(mListview.getItemAtPosition(position));
                        if (mMacroList.isEmpty()) {
                            mListEmptyTextView.setVisibility(View.VISIBLE);
                        }
                        mMacroAdapter.notifyDataSetChanged();
                        MacroUtils.saveMacroList(mMacroList, mPrefs);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }

}
