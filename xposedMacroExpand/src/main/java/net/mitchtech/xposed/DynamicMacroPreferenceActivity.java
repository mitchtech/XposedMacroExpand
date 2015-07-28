
package net.mitchtech.xposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.mitchtech.xposed.macroexpand.R;

public class DynamicMacroPreferenceActivity extends BaseActivity {

    private static final String TAG = DynamicMacroPreferenceActivity.class.getSimpleName();

    private Preference mPrefDynamicDateKeyword;
    private Preference mPrefDynamicTimeKeyword;
    private Preference mPrefDynamicWeekdayKeyword;

    protected SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
//        addPreferencesFromResource(R.xml.settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content, new SettingsFragment()).commit();
        }
    }

    public class SettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // this is important since settings executed in the context of the hooked package
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.dynamic_settings);
//            getActionBar().setDisplayHomeAsUpEnabled(true);

            mPrefs = getPreferenceScreen().getSharedPreferences();

            mPrefDynamicDateKeyword = findPreference("prefDynamicDateKeyword");
            mPrefDynamicTimeKeyword = findPreference("prefDynamicTimeKeyword");
            mPrefDynamicWeekdayKeyword = findPreference("prefDynamicWeekdayKeyword");
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen prefScreen, Preference pref) {
            if ((pref == mPrefDynamicDateKeyword) ||
                    (pref == mPrefDynamicTimeKeyword) ||
                    (pref == mPrefDynamicWeekdayKeyword)) {
                setDynamicMacroKeyword(pref);
            }
            return super.onPreferenceTreeClick(prefScreen, pref);
        }
    }

    private void setDynamicMacroKeyword(final Preference pref) {
        LayoutInflater factory = LayoutInflater.from(DynamicMacroPreferenceActivity.this);
        View view = factory.inflate(R.layout.dialog_edit_dynamic_macro, null);
        TextView textView = (TextView) view.findViewById(R.id.description);
        final EditText editText = (EditText) view.findViewById(R.id.input);
        textView.setText("Enter text keyword or phrase to trigger dynamic macro expansion." +
                "\n\nChange requires soft reboot to activate.");
        editText.setText(mPrefs.getString(pref.getKey(), "default"));

        new MaterialDialog.Builder(DynamicMacroPreferenceActivity.this)
                .title("Set Macro Keyword")
                .customView(view, true)
                .positiveText("OK")
                .negativeText("Cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String keyword = editText.getText().toString();
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putString(pref.getKey(), keyword);
                        editor.commit();
                    }
                }).show();
    }


}
