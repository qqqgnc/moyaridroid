package invalid.ayasiiwa_rudo.client.android;

import java.util.ArrayList;
import java.util.Random;

import invalid.ayasiiwa_rudo.client.BBSInfo;
import invalid.ayasiiwa_rudo.client.BBSManager;
import invalid.ayasiiwa_rudo.client.HttpUtil;
import invalid.ayasiiwa_rudo.client.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BBSListActivity extends Activity {
    // public static final String TAG = "STRANGE_WORLD";
    private static final int MENU_SETTING = Menu.FIRST + 1;
    private static final int MENU_BBS = MENU_SETTING + 1;

    private BBSManager bbsManager = new BBSManager();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        loadBBSList();
        showButtons();;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            showButtons();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadBBSList() {
        try {
            bbsManager.loadFromStream(getResources().openRawResource(
                    R.raw.bbslist));
        } catch (Exception e) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("Error");
            dlg.setMessage(e.toString());
            dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg.create();
            dlg.show();
        }
    }

    private void showButtons() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.MainLayout);
        removeChildViews(layout);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean show_banner = pref.getBoolean(ViewPrefActivity.KEY_BBS_BANNER, true);
        for (BBSInfo i : bbsManager.getBBSList()) {
            if (!pref.getBoolean(BBSPrefActivity.KEY_VISIBILITY + i.getShortName(), true))
                continue;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(6, 10, 6, 10);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            int id = 0;
            if (show_banner)
                id = getRandomBannerId(i.getShortName());
            if (id != 0) {
                LinearLayout banner = new LinearLayout(this);
                banner.setOrientation(LinearLayout.VERTICAL);
                TextView txt = new TextView(this);
                txt.setText(i.getName());
                txt.setTextColor(Color.WHITE);
                banner.addView(txt);
                ImageView image = new ImageView(this);
                image.setImageDrawable(getResources().getDrawable(id));
                image.setTag(i);
                image.setOnClickListener(createClickListener());
                image.setMaxWidth(metrics.widthPixels);
                image.setAdjustViewBounds(true);
                banner.addView(image);
                banner.setLayoutParams(params);
                layout.addView(banner);
            } else {
                Button b = new Button(this);
                b.setText(i.getName());
                b.setOnClickListener(createClickListener());
                b.setTag(i);
                b.setLayoutParams(params);
                layout.addView(b);
            }
        }
        TextView tv = new TextView(this);
        tv.setText(HttpUtil.USER_AGENT);
        tv.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(6, 10, 6, 10);
        params.gravity = Gravity.RIGHT;
        tv.setLayoutParams(params);
        layout.addView(tv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_SETTING, Menu.NONE, "Setting");
        menu.add(Menu.NONE, MENU_BBS, Menu.NONE, "BBS");
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_SETTING: {
            Intent i = new Intent(this, ViewPrefActivity.class);
            startActivity(i);
            return true;
        }
        case MENU_BBS: {
            ArrayList<String> names = new ArrayList<String>();
            ArrayList<String> keys = new ArrayList<String>();
            for (BBSInfo info : bbsManager.getBBSList()) {
                names.add(info.getName());
                keys.add(info.getShortName());
            }
            Intent i = new Intent(this, BBSPrefActivity.class);
            i.putExtra(BBSPrefActivity.INTENT_BBS_NAMES, names);
            i.putExtra(BBSPrefActivity.INTENT_BBS_KEYS, keys);
            startActivityForResult(i, 1);
            return true;
        }
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeChildViews(LinearLayout layout) {
        for (int i = layout.getChildCount() - 1; i >= 0; --i) {
            if (layout.getChildAt(i).getId() != R.id.TextAppTitle)
                layout.removeViewAt(i);
        }
    }

    private View.OnClickListener createClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile(BBSActivity.TEMP_FILE);
                BBSInfo info = (BBSInfo)v.getTag();
                Intent i = new Intent(getApplicationContext(),
                        BBSActivity.class);
                i.putExtra(PostListActivity.INTENT_BBS_NAME, info.getName());
                i.putExtra(PostListActivity.INTENT_BBS_URL, info.getUrl());
                i.putExtra(PostListActivity.INTENT_BBS_SHORT_NAME, info.getShortName());
                i.putExtra(PostListActivity.INTENT_BBS_MESSAGE_ID, info.isUseNextPageMessageID());
                startActivity(i);
            }
        };
    }

    private int getRandomBannerId(String prefix) {
        int maxnum = 0, id = 0;
        for (; ; ++maxnum) {
            int newid = getResources().getIdentifier(prefix + String.valueOf(maxnum + 1), "drawable", getPackageName());
            if (newid == 0)
                break;
            else
                id = newid;
        }
        if (maxnum == 0)
            return 0;
        if (maxnum == 1)
            return id;
        Random r = new Random();
        int num = r.nextInt(maxnum) + 1;
        return getResources().getIdentifier(prefix + String.valueOf(num), "drawable", getPackageName());
    }
}