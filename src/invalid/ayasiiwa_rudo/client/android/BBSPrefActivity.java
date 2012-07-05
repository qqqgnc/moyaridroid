package invalid.ayasiiwa_rudo.client.android;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class BBSPrefActivity extends PreferenceActivity {
    public static final String KEY_VISIBILITY = "visibility";
    public static final String INTENT_BBS_NAMES = "bbs_names";
    public static final String INTENT_BBS_KEYS = "bbs_keys";

    private ArrayList<String> bbsNames;
    private ArrayList<String> bbsKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        bbsNames = i.getStringArrayListExtra(INTENT_BBS_NAMES);
        bbsKeys = i.getStringArrayListExtra(INTENT_BBS_KEYS);
        if (bbsNames != null && bbsKeys != null)
            setPreferenceScreen(createPreferenceScreen());
    }

    private PreferenceScreen createPreferenceScreen() {
        PreferenceScreen screen = getPreferenceManager()
        .createPreferenceScreen(this);
        PreferenceCategory preferenceCategory = new PreferenceCategory(this);
        preferenceCategory.setTitle("BBS");
        screen.addPreference(preferenceCategory);

        for (int i = 0; i < bbsNames.size(); ++i) {
            CheckBoxPreference check = new CheckBoxPreference(this);
            check.setTitle(bbsNames.get(i));
            check.setSummary("");
            check.setChecked(true);
            check.setKey(KEY_VISIBILITY + bbsKeys.get(i));
            screen.addPreference(check);
        }
        return screen;
    }
}
