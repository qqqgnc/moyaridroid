package invalid.ayasiiwa_rudo.client.android;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputType;

public class ViewPrefActivity extends PreferenceActivity {
    public static final String INTENT_FONTS = "FONTS";
    public static final String KEY_HEADER = PostAdapter.VIEW_HEADER;
    public static final String KEY_QUOTATION = PostAdapter.VIEW_QUOTATION;
    public static final String KEY_BODY = PostAdapter.VIEW_BODY;
    public static final String SUFFIX_FONT = "_font";
    public static final String SUFFIX_SIZE = "_size";
    public static final String SUFFIX_VISIBLE = "_visible";
    public static final String KEY_AUTO_PAGE = "autpage";
    public static final String KEY_BACKSCROLL = "backscroll";
    public static final String KEY_SCROLL_COUNT = "backscrollcount";
    public static final String KEY_USE_PROXY = "useproxy";
    public static final String KEY_PROXY = "proxy";
    public static final String KEY_BBS_BANNER = "bbsbanner";
    public static final String KEY_FLING_LOAD = "flingload";
    public static final String KEY_MENUBUTTON_ON_TOP = "menubuttonontop";
    public static final String KEY_DESC_THREAD= "descthread";

    private String fonts[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        fonts = i.getStringArrayExtra(INTENT_FONTS);
        setPreferenceScreen(createPreferenceScreen());
    }

    private PreferenceScreen createPreferenceScreen() {
        PreferenceScreen screen = getPreferenceManager()
                .createPreferenceScreen(this);
        screen.removeAll();
        PreferenceCategory preferenceCategory = new PreferenceCategory(this);
        preferenceCategory.setTitle("Page");
        screen.addPreference(preferenceCategory);

        CheckBoxPreference check = new CheckBoxPreference(this);
        check.setTitle("AutoPage");
        check.setSummary("auto load next page.");
        check.setChecked(false);
        check.setKey(KEY_AUTO_PAGE);
        screen.addPreference(check);

        check = new CheckBoxPreference(this);
        check.setTitle("BackScroll");
        check.setSummary("back scroll with back button.");
        check.setChecked(false);
        check.setKey(KEY_BACKSCROLL);
        screen.addPreference(check);

        EditTextPreference edit = new EditTextPreference(this);
        edit.setTitle("ScrollCount");
        edit.setSummary("count of scroll.");
        edit.setText("5");
        edit.setKey(KEY_SCROLL_COUNT);
        edit.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        screen.addPreference(edit);

        check = new CheckBoxPreference(this);
        check.setTitle("Proxy");
        check.setSummary("using proxy, if checked");
        check.setChecked(false);
        check.setKey(KEY_USE_PROXY);
        screen.addPreference(check);

        edit = new EditTextPreference(this);
        edit.setTitle("Proxy Address");
        edit.setSummary("HTTP proxy. HostAddress:Port");
        edit.setText("");
        edit.setKey(KEY_PROXY);
        screen.addPreference(edit);

        check = new CheckBoxPreference(this);
        check.setTitle("Show BBS Banner");
        check.setSummary("Effect after a restart");
        check.setChecked(true);
        check.setKey(KEY_BBS_BANNER);
        screen.addPreference(check);

        check = new CheckBoxPreference(this);
        check.setTitle("Flick load");
        check.setSummary("Enable flick load");
        check.setChecked(true);
        check.setKey(KEY_FLING_LOAD);
        screen.addPreference(check);

        check = new CheckBoxPreference(this);
        check.setTitle("Desc sort thread");
        check.setSummary("newer post is top");
        check.setChecked(true);
        check.setKey(KEY_DESC_THREAD);
        screen.addPreference(check);
        
        check = new CheckBoxPreference(this);
        check.setTitle("Top Menu");
        check.setSummary("Menu button on top");
        check.setChecked(false);
        check.setKey(KEY_MENUBUTTON_ON_TOP);
        screen.addPreference(check);

        createFontPreference(screen, KEY_HEADER);
        createFontPreference(screen, KEY_QUOTATION);
        createFontPreference(screen, KEY_BODY);
        return screen;
    }

    private void createFontPreference(PreferenceScreen screen, String name) {
        PreferenceCategory preferenceCategory = new PreferenceCategory(this);
        preferenceCategory.setTitle(name);
        screen.addPreference(preferenceCategory);

        CheckBoxPreference check = new CheckBoxPreference(this);
        check.setTitle("Visibility");
        check.setSummary("Visibility of text");
        check.setChecked(true);
        check.setKey(name + SUFFIX_VISIBLE);
        screen.addPreference(check);

        ListPreference list = new ListPreference(this);
        list.setDialogTitle("Font");
        list.setTitle("Font");
        list.setSummary(name + " font");
        if (fonts == null)
            fonts = new String[] { "serif", "sans serif", "monospace" };
        list.setEntries(fonts);
        list.setEntryValues(fonts);
        list.setKey(name + SUFFIX_FONT);
        screen.addPreference(list);

        list = new ListPreference(this);
        list.setDialogTitle("Size");
        list.setTitle("Size");
        list.setSummary(name + " font size");
        final int num = 11;
        String ent[] = new String[num];
        String ient[] = new String[num];
        for (int i = 0; i < num; ++i) {
            ent[i] = String.format("%+d", i - 5);
            ient[i] = String.valueOf(i - 5);
        }
        list.setEntries(ent);
        list.setEntryValues(ient);
        list.setKey(name + SUFFIX_SIZE);
        screen.addPreference(list);
    }
}
