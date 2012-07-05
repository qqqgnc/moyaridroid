package invalid.ayasiiwa_rudo.client.android;

import java.util.Collections;

import invalid.ayasiiwa_rudo.client.BBS;
import invalid.ayasiiwa_rudo.client.HttpSource;
import invalid.ayasiiwa_rudo.client.MemoryLogMerchant;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;

public class ThreadListActivity extends PostListActivity {
    public static final String INTENT_THREAD_URL = "THREAD_URL";
    public static final String INTENT_BG_COLOR = "BG_COLOR";
    public static final String INTENT_PROTECT_CODE = "PROTECT_CODE";
    public static final String TEMP_FILE = "theradtmp";
    public static final String STR_RELOAD = "Reload";
    private static final int MENU_ID_RELOAD = Menu.FIRST + 100;

    private String threadUrl;
    private String protectCode;
    private String bgColor;
    private boolean sort_desc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        threadUrl = i.getStringExtra(INTENT_THREAD_URL);
        protectCode = i.getStringExtra(INTENT_PROTECT_CODE);
        bgColor = i.getStringExtra(INTENT_BG_COLOR);
        if (bgColor != null && bgColor.length() != 0) {
            try {
                ((LinearLayout) findViewById(R.id.BaseLayout))
                        .setBackgroundColor(Color.parseColor(bgColor));
            } catch (IllegalArgumentException e) {
                // ignore error.
            }
        }
        registerForContextMenu(findViewById(R.id.PostList));
    }

    @Override
    protected void onStart() {
        super.onStart();

        BBS bbs = getBBS();
        if (getBBS() == null) {
            MemoryLogMerchant log = new MemoryLogMerchant();
            HttpSource ssource = new HttpSource(getBbsUrl());
            log.setSource(ssource);
            bbs = new BBS(getBbsName(), getBbsUrl(), getBbsShortName(), isUseNextPageMessageID());
            setBBS(bbs);
            bbs.setLog(log);
            bbs.setBgColor(bgColor);
            bbs.setProtectCode(protectCode);
            String fl[] = fileList();
            for (String f : fl) {
                if (f.contains(TEMP_FILE)) {
                    loadTemp();
                    return;
                }
            }
            if (sort_desc) {
            	loadWithProgress(-1, 0);
            } else {
            	loadWithProgress(0, 0);
            }
            
        } else {
            restoreList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String pc = data.getStringExtra(PostActivity.INTENT_PROTECT_CODE);
            if (pc != null && pc.length() != 0) {
                getBBS().setProtectCode(pc);
                protectCode = pc;
                Intent i = new Intent();
                i.putExtra(INTENT_PROTECT_CODE, pc);
                setResult(Activity.RESULT_OK, i);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        if (ret)
            menu.add(Menu.NONE, MENU_ID_RELOAD, Menu.NONE, STR_RELOAD);
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_RELOAD:
            PostAdapter adp = (PostAdapter)((ListView)findViewById(R.id.PostList)).getAdapter();
            adp.clear();
            adp.notifyDataSetChanged();
            getBBS().clearLog();
            loadWithProgress(0, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.removeItem(MENU_ID_THREAD);
    }

    @Override
    public String getTempFileName() {
        return TEMP_FILE;
    }

    @Override
    protected void asyncLoad() throws Exception {
        getBBS().loadThread(threadUrl);
    }

    @Override
    protected int getActivityLayoutId() {
        return R.layout.threadlist;
    }

    @Override
    protected void postAsyncLoad(boolean canceled) {
        if (!canceled) {
        	if (sort_desc) {
        		getBBS().getPosts();
        	} else {
        		Collections.reverse(getBBS().getPosts());
        	}
        }
    }

    @Override
    protected void loadViewPref(SharedPreferences pref) {
        super.loadViewPref(pref);
        sort_desc = pref.getBoolean(ViewPrefActivity.KEY_DESC_THREAD, true);
    }

}
