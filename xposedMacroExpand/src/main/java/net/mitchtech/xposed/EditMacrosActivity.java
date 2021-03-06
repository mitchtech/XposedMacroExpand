
package net.mitchtech.xposed;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;

import net.mitchtech.utils.MacroUtils;
import net.mitchtech.xposed.macroexpand.R;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

public class EditMacrosActivity extends BaseActivity {

    private static final String TAG = EditMacrosActivity.class.getSimpleName();

    private static final int MACRO_SIZE_WARN = 100;

    private CardListView mListView;
    private TextView mListEmptyTextView;
    private ArrayList<MacroEntry> mMacroList;
    private CardArrayAdapter mCardArrayAdapter;
    private ArrayList<Card> mCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_macros);

        initToolbar();
        initDrawer();

        mListView = (CardListView) findViewById(R.id.listview);
        mListEmptyTextView = (TextView) findViewById(R.id.listEmptyText);
        mMacroList = MacroUtils.loadMacroList(mPrefs);

//        if (mMacroList.size() >= MACRO_SIZE_WARN) {
//            macroListLengthWarningDialog(mMacroList.size());
//        }

        mCards = new ArrayList<Card>();
        for (MacroEntry macroEntry : mMacroList) {
            mCards.add(macroEntryToCard(macroEntry));
        }

        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        if (mListView != null) {
            mListView.setAdapter(mCardArrayAdapter);
        }

        Drawable plus = new IconicsDrawable(this, FontAwesome.Icon.faw_plus).color(Color.DKGRAY).sizeDp(20);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add);
        addButton.setImageDrawable(plus);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMacro();
            }
        });
    }

    @Override
    protected void onResume() {
        if (mMacroList == null || mMacroList.isEmpty()) {
            mListEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mListEmptyTextView.setVisibility(View.GONE);
        }
        super.onResume();
    }


    private void addMacro() {
        editMacro(-1);
    }

    //    private void editMacro(MacroEntry entry, final int position) {
    private void editMacro(final int position) {
        MacroEntry entry;
        LayoutInflater factory = LayoutInflater.from(EditMacrosActivity.this);
        final View textEntryView = factory.inflate(R.layout.dialog_edit_macro, null);
        final EditText actual = (EditText) textEntryView.findViewById(R.id.actual);
        final EditText replacement = (EditText) textEntryView.findViewById(R.id.replacement);
        if (position > -1) {
            entry = mMacroList.get(position);
            actual.setText(entry.actual, TextView.BufferType.EDITABLE);
            replacement.setText(entry.replacement, TextView.BufferType.EDITABLE);
        }

        new MaterialDialog.Builder(EditMacrosActivity.this)
                .title("Define Macro")
                .customView(textEntryView, true)
                .positiveText("Save")
                .negativeText("Cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String actualText = actual.getText().toString();
                        String replacementText = replacement.getText().toString();
                        // if (isTextRegexFree(actualText)
                        if (position > -1) {
                            mMacroList.remove(position);
                            mCards.remove(position);
                        }
                        MacroEntry macroEntry = new MacroEntry(actualText, replacementText);
                        mMacroList.add(macroEntry);
                        mCards.add(macroEntryToCard(macroEntry));
                        mListEmptyTextView.setVisibility(View.GONE);
                        mCardArrayAdapter.notifyDataSetChanged();
                        MacroUtils.saveMacroList(mMacroList, mPrefs);
                    }
                }).show();
    }

    private void removeMacro(final int position) {
        new MaterialDialog.Builder(EditMacrosActivity.this)
                .title("Delete Macro?")
                .content("Are you sure you want to delete this macro?")
                .positiveText("Confirm")
                .negativeText("Cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mMacroList.remove(position);
                        mCards.remove(position);
                        if (mMacroList.isEmpty()) {
                            mListEmptyTextView.setVisibility(View.VISIBLE);
                        }
                        mCardArrayAdapter.notifyDataSetChanged();
                        MacroUtils.saveMacroList(mMacroList, mPrefs);
                    }
                }).show();
    }

    private void macroListLengthWarningDialog(int length) {
        new MaterialDialog.Builder(EditMacrosActivity.this)
                .title("Size Warning")
                .content("Waring, macro list contains " + length
                        + " entries. This is permitted, but performance my degrade as a result")
                .positiveText("OK")
                .show();
    }

    private Card macroEntryToCard(MacroEntry macroEntry) {
        Card card = new Card(this);
        CardHeader header = new CardHeader(this);

        header.setTitle(macroEntry.toString());
        card.setTitle(macroEntry.isEnabled());

        header.setPopupMenu(R.menu.macro, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard baseCard, MenuItem menuItem) {
                int index = mCards.indexOf(baseCard);
                switch (menuItem.getItemId()) {

                    case R.id.action_enable:
                        baseCard.setTitle("Disabled");
                        break;

                    case R.id.action_edit:
                        editMacro(index);
                        break;

                    case R.id.action_delete:
                        removeMacro(index);
                        break;
                }
                mCardArrayAdapter.notifyDataSetChanged();
            }
        });
//        header.setButtonOverflowVisible(true);
//        header.setButtonExpandVisible(true);
        card.addCardHeader(header);

        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                int index = mCards.indexOf(card);
                editMacro(index);

            }
        });

        card.setOnLongClickListener(new Card.OnLongCardClickListener() {
            @Override
            public boolean onLongClick(Card card, View view) {
                int index = mCards.indexOf(card);
                removeMacro(index);
                return true;
            }
        });
        return card;
    }
}
