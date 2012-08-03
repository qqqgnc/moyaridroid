package invalid.ayasiiwa_rudo.client.android;

import invalid.ayasiiwa_rudo.client.BBS;
import invalid.ayasiiwa_rudo.client.HttpUtil;
import invalid.ayasiiwa_rudo.client.Post;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

class PostComparator implements Comparator<Post> {
    @Override
    public int compare(Post object1, Post object2) {
        return object1.getPostTime().compareTo(object2.getPostTime());
    }
}

public abstract class PostListActivity extends Activity {

    protected class SaveData {
        public BBS bbs = null;
        int position = 0;
        int top = 0;
    }

    //public static final String TAG = "STRANGE_WORLD";
    private static final String LIST_TOP = "listTop";
    private static final String LIST_POSITION = "listPosition";
    public static final String STR_FOLLOW = "フォロー投稿";
    public static final String STR_THREAD = "スレッド表示";
    public static final String STR_SHARE = "共有";
    public static final String STR_GO_TOP = "Go Top";
    public static final String INTENT_BBS_URL = "BBS_URL";
    public static final String INTENT_BBS_NAME = "BBS_NAME";
    public static final String INTENT_BBS_SHORT_NAME = "BBS_SHORT_NAME";
    public static final String INTENT_BBS_MESSAGE_ID = "BBS_MESSAGE_ID";
    public static final String INTENT_DELETE_TEMP_FILE = "DELETE_TEMP_FILE";
    protected static final int MENU_ID_FOLLOW = (Menu.FIRST + 1);
    protected static final int MENU_ID_THREAD = (MENU_ID_FOLLOW + 1);
    protected static final int MENU_ID_SHARE = (MENU_ID_THREAD + 1);
    protected static final int MENU_ID_VIEWPREF = (MENU_ID_SHARE + 1);
    protected static final int MENU_ID_CLOSE = (MENU_ID_VIEWPREF + 1);
    protected static final int MENU_ID_OPEN_URL = (MENU_ID_CLOSE + 1);
    protected static final int MENU_ID_GO_TOP = (MENU_ID_OPEN_URL + 1);

    private static final String STR_VIEWPREF = "Setting";
    private static final String STR_CLOSE = "Close";

    private static int REQUEST_VIEWPREF = 2;
    private static int DISPLAY_WIDTH = 0;

    private String bbsName = "";
    private String bbsUrl = "";
    private String bbsShortName = "";
    private boolean useNextPageMessageID;
    private BBS currentBBS = null;
    private Handler handler = new Handler();
    private int listTop = 0;
    private int listPosition = 0;
    private int backScrollCount = 10;
    private boolean backScrollEnabled = true;
    private LoadTask loadTask = null;
    private WaitAnime waitAnime;
    private Semaphore loadLock = new Semaphore(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(getActivityLayoutId());
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.bbs_titlebar);
        TextView t = (TextView) findViewById(R.id.TextTitle);
        Intent i = getIntent();
        bbsName = i.getStringExtra(INTENT_BBS_NAME);
        bbsUrl = i.getStringExtra(INTENT_BBS_URL);
        bbsShortName = i.getStringExtra(INTENT_BBS_SHORT_NAME);
        useNextPageMessageID = i.getBooleanExtra(INTENT_BBS_MESSAGE_ID, true);
        if (i.getBooleanExtra(INTENT_DELETE_TEMP_FILE, false)) {
            deleteTempFile();
        }
        t.setText(bbsName);
        ListView lv = (ListView)findViewById(R.id.PostList);
        TextView empty = new TextView(this);
        empty.setText(getResources().getString(R.string.end_post));
        empty.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        empty.setVisibility(View.GONE);
        ViewGroup vg = (ViewGroup)lv.getParent();
        vg.addView(empty);
        lv.setEmptyView(empty);

        lv.setDivider(new ColorDrawable(Color.WHITE));
        lv.setDividerHeight(1);

        lv.setBackgroundColor(Color.TRANSPARENT);
        lv.setCacheColorHint(Color.TRANSPARENT);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        PostAdapter.scaledDensity = metrics.scaledDensity;
        DISPLAY_WIDTH = metrics.widthPixels;
        registerForContextMenu(findViewById(R.id.PostList));
        if (savedInstanceState != null) {
            listPosition = savedInstanceState.getInt(LIST_POSITION);
            listTop = savedInstanceState.getInt(LIST_TOP);
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        loadViewPref(pref);
        PostAdapter adp = new PostAdapter(this, R.layout.list_entry, new ArrayList<Post>(30));
        adp.setNotifyOnChange(false);
        lv.setAdapter(adp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SaveData sd = (SaveData) getLastNonConfigurationInstance();
        if (sd != null) {
            listPosition = sd.position;
            listTop = sd.top;
            currentBBS = sd.bbs;
            findViewById(R.id.BaseLayout).setBackgroundColor(
                    Color.parseColor(currentBBS.getBgColor()));
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        SaveData s = new SaveData();
        s.bbs = currentBBS;
        saveViewPosition();
        s.position = listPosition;
        s.top = listTop;
        return s;
    }

    @Override
    protected void onPause() {
        super.onPause();

        OutputStream s = null;
        String temp = getTempFileName();
        try {
            s = openFileOutput(temp, Context.MODE_PRIVATE);
            if (s == null)
                return;
            ObjectOutputStream oos = new ObjectOutputStream(s);
            saveViewPosition();
            oos.writeInt(listPosition);
            oos.writeInt(listTop);
            currentBBS.saveTemp(oos);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            deleteFile(temp);
        }
        try {
            if (s != null)
                s.close();
        } catch (IOException e) {
            //ignore errors
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveViewPosition();
        outState.putInt(LIST_POSITION, listPosition);
        outState.putInt(LIST_TOP, listTop);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIEWPREF) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            loadViewPref(pref);
            saveViewPosition();
            restoreList();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void startPostActivity(String fl, String postid) {
        Intent i = new Intent(getApplicationContext(), PostActivity.class);
        i.putExtra(PostActivity.INTENT_BBS_URL, bbsUrl);
        i.putExtra(PostActivity.INTENT_PROTECT_CODE,
                currentBBS.getProtectCode());
        i.putExtra(PostActivity.INTENT_FOLLOW_LINK, fl);
        i.putExtra(PostActivity.INTENT_BG_COLOR, currentBBS.getBgColor());
        if (postid != null)
            i.putExtra(PostActivity.INTENT_II_POSTID, postid);
        startActivityForResult(i, 1);
    }

    void startThreadActivity(String threadLink) {
        if (threadLink.length() == 0) {
            Toast.makeText(this, "No threads", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(getApplicationContext(), ThreadListActivity.class);
        i.putExtra(PostListActivity.INTENT_BBS_URL, bbsUrl);
        i.putExtra(PostListActivity.INTENT_BBS_NAME, bbsName);
        i.putExtra(PostListActivity.INTENT_BBS_SHORT_NAME, bbsShortName);
        i.putExtra(ThreadListActivity.INTENT_THREAD_URL, threadLink);
        i.putExtra(ThreadListActivity.INTENT_BG_COLOR, currentBBS.getBgColor());
        i.putExtra(ThreadListActivity.INTENT_PROTECT_CODE,
                currentBBS.getProtectCode());
        deleteFile(ThreadListActivity.TEMP_FILE);
        startActivityForResult(i, 1);
    }

    protected void saveViewPosition() {
        ListView lv = (ListView) findViewById(R.id.PostList);
        listPosition = lv.getFirstVisiblePosition();
        View v = lv.getChildAt(0);
        listTop = (v == null) ? 0 : v.getTop();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();
        Post p = currentBBS.getPosts().get((int) info.id);
        switch (item.getItemId()) {
        case MENU_ID_FOLLOW:
            startPostActivity(p.getFollowLink(), null);
            return true;
        case MENU_ID_THREAD:
            startThreadActivity(p.getThreadLink());
            return true;
        case MENU_ID_SHARE:
            sharePost(p);
            return true;
        case MENU_ID_OPEN_URL:
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(item.getTitle().toString()));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e){
                Toast.makeText(this, "Invalid Link", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (loadTask != null)
            return;
        menu.add(Menu.NONE, MENU_ID_FOLLOW, Menu.NONE, STR_FOLLOW);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        Post p = getPostAdapter().getItem((int)info.id);
        if (p.getThreadLink().length() != 0)
            menu.add(Menu.NONE, MENU_ID_THREAD, Menu.NONE, STR_THREAD);
        menu.add(Menu.NONE, MENU_ID_SHARE, Menu.NONE, STR_SHARE);
        Spanned span = Html.fromHtml(p.getQuotationText());
        URLSpan[] urls = span.getSpans(0, span.length(), URLSpan.class);
        for (URLSpan u: urls) {
            menu.add(Menu.NONE, MENU_ID_OPEN_URL, Menu.NONE, u.getURL());
        }
        span = Html.fromHtml(p.getBodyText());
        urls = span.getSpans(0, span.length(), URLSpan.class);
        for (URLSpan u: urls) {
            menu.add(Menu.NONE, MENU_ID_OPEN_URL, Menu.NONE, u.getURL());
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    protected void loadWithProgress(int insertPosition, int insertDelta) {
    	if (!loadLock.tryAcquire()) {
            Toast.makeText(this, "other thread now loading.", Toast.LENGTH_SHORT).show();
            return;
    	}
        if (loadTask != null) {
            loadTask.cancel(true);
        }
        if (waitAnime != null)
            waitAnime.finish(null);
        waitAnime = new WaitAnime((TextView)findViewById(R.id.TextParticipants));
        waitAnime.start();
        loadTask = new LoadTask(this, insertPosition, insertDelta);
        PostAdapter adp = getPostAdapter();
        loadTask.execute(adp);
    }

    void restoreList() {
        if (currentBBS != null) {
            PostAdapter adp = getPostAdapter();
            adp.clear();
            adp.addAll(currentBBS.getPosts());
            adp.notifyDataSetChanged();
            ListView list = (ListView) findViewById(R.id.PostList);
            list.setSelectionFromTop(listPosition, listTop);
            String numPart = currentBBS.getParticipants();
            if (numPart != null) {
                TextView t = (TextView) findViewById(R.id.TextParticipants);
                t.setText(numPart);
            }
        } else {
            loadWithProgress(-1, 0);
        }
    }

    protected void loadTemp() {
        String temp = getTempFileName();
        InputStream s = null;
        try {
            s = openFileInput(temp);
            if (s == null)
                return;
            ObjectInputStream ois = new ObjectInputStream(s);
            listPosition = ois.readInt();
            listTop = ois.readInt();
            currentBBS.loadTemp(ois);
            PostAdapter adp = getPostAdapter();
            adp.clear();
            adp.addAll(currentBBS.getPosts());
            adp.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        } finally {
            deleteTempFile();
        }
    }

    protected void deleteTempFile() {
        String temp = getTempFileName();
        deleteFile(temp);
    }

    protected void sharePost(Post p) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        StringBuilder b = new StringBuilder();
        b.append(currentBBS.getName()).append("\n題名： ")
                .append(Html.fromHtml(p.getTitle())).append("\n投稿者： ")
                .append(Html.fromHtml(p.getAuthor())).append("\n")
                .append(p.getPostTime()).append("\n\n")
                .append(Html.fromHtml(p.getQuotationText() + p.getBodyText()));
        intent.putExtra(Intent.EXTRA_TEXT, b.toString());
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "共有できるアプリケーションがありません", Toast.LENGTH_LONG)
                    .show();
        }
    }

    protected String getBbsName() {
        return bbsName;
    }

    protected void setBbsName(String bbsName) {
        this.bbsName = bbsName;
    }

    protected String getBbsUrl() {
        return bbsUrl;
    }

    protected void setBbsUrl(String bbsUrl) {
        this.bbsUrl = bbsUrl;
    }

    protected String getBbsShortName() {
        return bbsShortName;
    }

    protected void setBbsShortName(String bbsShortName) {
        this.bbsShortName = bbsShortName;
    }

    protected boolean isUseNextPageMessageID() {
        return useNextPageMessageID;
    }

    protected BBS getBBS() {
        return currentBBS;
    }

    protected void setBBS(BBS currentBBS) {
        this.currentBBS = currentBBS;
    }

    protected int getListTop() {
        return listTop;
    }

    protected void setListTop(int listTop) {
        this.listTop = listTop;
    }

    protected int getListPosition() {
        return listPosition;
    }

    protected void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    protected Handler getHandler() {
        return handler;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_ID_VIEWPREF, Menu.NONE, STR_VIEWPREF);
        menu.add(Menu.NONE, MENU_ID_CLOSE, Menu.NONE, STR_CLOSE);
        menu.add(Menu.NONE, MENU_ID_GO_TOP, Menu.NONE, STR_GO_TOP);
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_VIEWPREF:
            showViewPrefActivity();
            return true;
        case MENU_ID_CLOSE:
            finish();
            return true;
        case MENU_ID_GO_TOP:
            ListView lv = (ListView)findViewById(R.id.PostList);
            lv.setSelectionFromTop(0, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showViewPrefActivity() {
        ArrayList<String> lst = new ArrayList<String>();
        lst.add("serif");
        lst.add("sans serif");
        lst.add("monospace");
        final ArrayList<String> path = new ArrayList<String>();

        if (isExternalStorageReadable()) {
            File dir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/fonts");
            File fl[] = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".ttf"))
                        return true;
                    else if (name.endsWith(".otf"))
                        return true;
                    return false;
                }
            });
            if (fl != null) {
                Arrays.sort(fl, new Comparator<File>() {
                    @Override
                    public int compare(File arg0, File arg1) {
                        return arg0.getName().compareTo(arg1.getName());
                    }
                });
                for (File f : fl) {
                    lst.add(f.getName());
                    path.add(f.getAbsolutePath());
                }
            }
        }

        final String[] items = lst.toArray(new String[0]);
        Intent i = new Intent(this, ViewPrefActivity.class);
        i.putExtra(ViewPrefActivity.INTENT_FONTS, items);
        startActivityForResult(i, REQUEST_VIEWPREF);
    }

    protected void loadViewPref(SharedPreferences pref) {
        try {
            setViewSetting(pref, PostAdapter.header);
            setViewSetting(pref, PostAdapter.quotation);
            setViewSetting(pref, PostAdapter.body);
            backScrollEnabled = pref.getBoolean(ViewPrefActivity.KEY_BACKSCROLL, false);
            String c = pref.getString(ViewPrefActivity.KEY_SCROLL_COUNT, "5");
            try {
                backScrollCount = Integer.parseInt(c);
            } catch (NumberFormatException e) {
                backScrollCount = 5;
            }
            boolean use_proxy = pref.getBoolean(ViewPrefActivity.KEY_USE_PROXY, false);
            if (use_proxy) {
                String proxy = pref.getString(ViewPrefActivity.KEY_PROXY, "");
                String ps[] = proxy.split(":");
                if (ps.length != 2) {
                    HttpUtil.PROXY_HOST = null;
                } else {
                    HttpUtil.PROXY_HOST = ps[0];
                    try {
                        HttpUtil.PROXY_PORT = Integer.parseInt(ps[1]);
                    } catch (NumberFormatException e) {
                        HttpUtil.PROXY_PORT = -1;
                    }
                }
            } else {
                HttpUtil.PROXY_HOST = null;
                HttpUtil.PROXY_PORT = -1;
            }
        } catch (ClassCastException e) {
            Toast.makeText(this, "Config error " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void setViewSetting(SharedPreferences pref, ViewSetting s) {
        s.font = createFont(pref.getString(s.name
                + ViewPrefActivity.SUFFIX_FONT, ""));
        s.fontDelta = Integer.parseInt(pref.getString(s.name
                + ViewPrefActivity.SUFFIX_SIZE, "0"));
        s.visibility = pref.getBoolean(
                s.name + ViewPrefActivity.SUFFIX_VISIBLE, true);
    }

    private Typeface createFont(String font) {
        Typeface f = null;
        if (font.length() != 0) {
            if (font.equals("serif")) {
                f = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
            } else if (font.equals("sans serif")) {
                f = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
            } else if (font.equals("monospace")) {
                f = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
            } else {
                if (isExternalStorageReadable()) {
                    f = Typeface.createFromFile(Environment
                            .getExternalStorageDirectory().getAbsolutePath()
                            + "/fonts/" + font);
                }
            }
        }
        return f;
    }

    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       int sign = 0;
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            if (!backScrollEnabled)
                return super.onKeyDown(keyCode, event);
            sign = -1;
            break;
        case KeyEvent.KEYCODE_VOLUME_UP:
            sign = -1;
            break;
        case KeyEvent.KEYCODE_SEARCH:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            sign = 1;
            break;
        default:
            return super.onKeyDown(keyCode, event);
        }
        ListView lv = (ListView) findViewById(R.id.PostList);
        int pos = lv.getFirstVisiblePosition();
        View v = lv.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        if (top < 0)
            pos += 1;
        if (pos == 0 && keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }

        pos = pos + (backScrollCount * sign);
        if (pos < 0)
            pos = 0;
        lv.setSelectionFromTop(pos, 0);
        return true;
    }

    public int getDisplayWidth() {
        return DISPLAY_WIDTH;
    }

    protected void finishLoadTask() {
        loadTask = null;
        loadLock.release();
    }

    protected PostAdapter getPostAdapter() {
        return (PostAdapter) ((ListView) findViewById(R.id.PostList)).getAdapter();
    }

    abstract protected String getTempFileName();

    abstract protected void asyncLoad() throws Exception;
    protected void preAsyncLoad() {}
    protected void updateAsyncLoad() {}
    protected void postAsyncLoad(boolean canceled) {}

    abstract protected int getActivityLayoutId();

    public void finsihWaitAnime(String s) {
        if (waitAnime != null)
            waitAnime.finish(s);
        waitAnime = null;
    }

    class WaitAnime extends Thread {
        boolean stoped = false;
        TextView text;
        String endText;

        WaitAnime(TextView tv) {
            text = tv;
        }

        public void finish(String s) {
            endText = s;
            stoped = true;
        }

        @Override
        public void run() {
            final String[][] texts = {
                    {"ヽ(´ー` )ノ", "　(-` ノ )　", "ヽ(　    )ノ", "　(ヽ ´-)　"},
                    {"ヽ(´ー` )ノ", "ヽ(`Д´)ノ "},
                    {"(ﾟДﾟ)   ", "(　ﾟДﾟ　)  "},
                    {"(","(ﾟ","(ﾟｰ","(ﾟｰﾟ","ｸｸｸ(ﾟーﾟ"},
                    {"(;ﾟДﾟ)", "(´Д`)"},
                    {"(´ｰ`)y-~~","(;ﾟДﾟ)y_~~"},
                    {"('ｰ'*)ｸｽｸｽ   ", "('ｰ'*)ﾊｽﾞｶｼｰ"},
                };
            int r = new Random().nextInt(texts.length);
            int index = 0;
            final String[] texts2 = texts[r];
            int max = texts2.length;
            while (!stoped) {
                try {
                    final int i = index;
                    sleep(300);
                    text.post(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(texts2[i]);
                        }
                    });
                    ++index;
                    if (index == max)
                        index = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            text.post(new Runnable() {
                @Override
                public void run() {
                    if (endText != null)
                        text.setText(endText);
                    else
                        text.setText("");
                }
            });
        }
    };
}
