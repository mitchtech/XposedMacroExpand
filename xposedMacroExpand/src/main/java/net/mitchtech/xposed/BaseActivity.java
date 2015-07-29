package net.mitchtech.xposed;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.tsengvn.typekit.TypekitContextWrapper;

import net.mitchtech.xposed.macroexpand.R;

/**
 * Created by michael on 7/15/15.
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    protected static final String PKG_NAME = "net.mitchtech.xposed.macroexpand";

    public static final int MENU_ABOUT = Menu.FIRST + 1;
    public static final int MENU_HELP = Menu.FIRST + 2;
    public static final int MENU_SETTINGS = Menu.FIRST + 3;
    public static final int MENU_THEME = Menu.FIRST + 4;
    public static final int MENU_UPGRADE = Menu.FIRST + 5;
    public static final int MENU_DONATE = Menu.FIRST + 6;
    public static final int MENU_SHARE = Menu.FIRST + 7;
    public static final int MENU_RATE = Menu.FIRST + 8;
    public static final int MENU_MORE_APPS = Menu.FIRST + 9;
    public static final int MENU_SUPPORT_URL = Menu.FIRST + 10;
    public static final int MENU_CONTACT = Menu.FIRST + 11;
    public static final int MENU_SOURCE_CODE = Menu.FIRST + 12;
    public static final int MENU_LICENSE = Menu.FIRST + 13;
    public static final int MENU_PRIVACY = Menu.FIRST + 14;
    public static final int MENU_CHANGE_LOG = Menu.FIRST + 15;
    public static final int MENU_EXIT = Menu.FIRST + 16;
    public static final int MENU_DYNAMIC_MACROS = Menu.FIRST + 20;

    public static final String MENU_TEXT_ABOUT = "About";
    public static final String MENU_TEXT_HELP = "Help";
    public static final String MENU_TEXT_SETTINGS = "Settings";
    public static final String MENU_TEXT_THEME = "Theme";
    public static final String MENU_TEXT_UPGRADE = "Upgrade";
    public static final String MENU_TEXT_DONATE = "Donate";
    public static final String MENU_TEXT_SHARE = "Share App";
    public static final String MENU_TEXT_RATE = "Rate App";
    public static final String MENU_TEXT_MORE_APPS = "More Apps";
    public static final String MENU_TEXT_SUPPORT_URL = "Support URL";
    public static final String MENU_TEXT_CONTACT = "Contact Developer";
    public static final String MENU_TEXT_SOURCE_CODE = "Source Code";
    public static final String MENU_TEXT_LICENSE = "Software License";
    public static final String MENU_TEXT_PRIVACY = "Privacy Policy";
    public static final String MENU_TEXT_CHANGE_LOG = "Change Log";
    public static final String MENU_TEXT_EXIT = "Exit";
    public static final String MENU_TEXT_DYNAMIC_MACROS = "Dynamic Macros";

    protected SharedPreferences mPrefs;
    protected static Drawer mDrawer = null;
    protected static Toolbar mToolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_base);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    protected void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    protected void initDrawer() {
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withTranslucentActionBarCompatibility(true)
                .withCloseOnClick(true)
                .withAnimateDrawerItems(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(MENU_TEXT_DYNAMIC_MACROS).withIcon(FontAwesome.Icon.faw_android).withIdentifier(MENU_DYNAMIC_MACROS),
                        new PrimaryDrawerItem().withName(MENU_TEXT_SETTINGS).withIcon(FontAwesome.Icon.faw_gear).withIdentifier(MENU_SETTINGS),
                        new PrimaryDrawerItem().withName(MENU_TEXT_ABOUT).withIcon(FontAwesome.Icon.faw_info).withIdentifier(MENU_ABOUT),
                        new PrimaryDrawerItem().withName(MENU_TEXT_HELP).withIcon(FontAwesome.Icon.faw_question).withIdentifier(MENU_HELP),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(MENU_TEXT_THEME).withIcon(FontAwesome.Icon.faw_paint_brush).withIdentifier(MENU_THEME),
                        new SecondaryDrawerItem().withName(MENU_TEXT_UPGRADE).withIcon(FontAwesome.Icon.faw_shopping_cart).withIdentifier(MENU_UPGRADE),
                        new SecondaryDrawerItem().withName(MENU_TEXT_DONATE).withIcon(FontAwesome.Icon.faw_dollar).withIdentifier(MENU_DONATE),
                        new SecondaryDrawerItem().withName(MENU_TEXT_SHARE).withIcon(FontAwesome.Icon.faw_share).withIdentifier(MENU_SHARE),
                        new SecondaryDrawerItem().withName(MENU_TEXT_RATE).withIcon(FontAwesome.Icon.faw_star).withIdentifier(MENU_RATE),
                        new SecondaryDrawerItem().withName(MENU_TEXT_MORE_APPS).withIcon(FontAwesome.Icon.faw_image).withIdentifier(MENU_MORE_APPS),
                        new SecondaryDrawerItem().withName(MENU_TEXT_SUPPORT_URL).withIcon(FontAwesome.Icon.faw_bookmark).withIdentifier(MENU_SUPPORT_URL),
                        new SecondaryDrawerItem().withName(MENU_TEXT_CONTACT).withIcon(FontAwesome.Icon.faw_bullhorn).withIdentifier(MENU_CONTACT),
                        new SecondaryDrawerItem().withName(MENU_TEXT_SOURCE_CODE).withIcon(FontAwesome.Icon.faw_github).withIdentifier(MENU_SOURCE_CODE),
                        new SecondaryDrawerItem().withName(MENU_TEXT_LICENSE).withIcon(FontAwesome.Icon.faw_copyright).withIdentifier(MENU_LICENSE),
                        new SecondaryDrawerItem().withName(MENU_TEXT_PRIVACY).withIcon(FontAwesome.Icon.faw_video_camera).withIdentifier(MENU_PRIVACY),
                        new SecondaryDrawerItem().withName(MENU_TEXT_CHANGE_LOG).withIcon(FontAwesome.Icon.faw_list).withIdentifier(MENU_CHANGE_LOG),
                        new SecondaryDrawerItem().withName(MENU_TEXT_EXIT).withIcon(FontAwesome.Icon.faw_power_off).withIdentifier(MENU_EXIT)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            performAction(drawerItem.getIdentifier());
                            return true;
                        }
                        return false;
                    }
                })
                .withActionBarDrawerToggle(true)
                .withShowDrawerOnFirstLaunch(true)
                .build();
    }

    public void performAction(int itemId) {
        // http://stackoverflow.com/questions/9092712/switch-case-statement-error-case-expressions-must-be-constant-expression
        Intent intent = null;
        if (itemId == MENU_ABOUT) {
        } else if (itemId == MENU_HELP) {
        } else if (itemId == MENU_DYNAMIC_MACROS) {
            intent = new Intent(this, DynamicMacroPreferenceActivity.class);
        } else if (itemId == MENU_SETTINGS) {
            intent = new Intent(this, MacroPreferenceActivity.class);
        } else if (itemId == MENU_THEME) {
        } else if (itemId == MENU_UPGRADE) {
        } else if (itemId == MENU_DONATE) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_paypal)));
        } else if (itemId == MENU_SHARE) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_github));
            intent.setType("text/plain");
            intent = Intent.createChooser(intent, "Share");
        } else if (itemId == MENU_RATE) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.market_uri_app)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        } else if (itemId == MENU_MORE_APPS) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.market_uri_mitchtech)));
        } else if (itemId == MENU_SUPPORT_URL) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_xda)));
        } else if (itemId == MENU_CONTACT) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.mailto)));
        } else if (itemId == MENU_SOURCE_CODE) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github)));
        } else if (itemId == MENU_LICENSE) {
        } else if (itemId == MENU_PRIVACY) {
        } else if (itemId == MENU_CHANGE_LOG) {
            showChangeLogDialog();
        } else if (itemId == MENU_EXIT) {
            this.finish();
        }

//        mDrawer.resetDrawerContent();
//        mDrawer.closeDrawer();
//        mDrawer.setSelection(-1);

        if (intent != null) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    protected void showChangeLogDialog() {
        WebView webView = new WebView(this);
        webView.loadUrl("file:///android_asset/changelog.html");
        new MaterialDialog.Builder(this)
                .title("Change Log")
                .customView(webView, false)
                .cancelable(false)
                .positiveText("OK")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }
                }).show();
    }

}
