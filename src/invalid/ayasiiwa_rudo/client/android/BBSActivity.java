package invalid.ayasiiwa_rudo.client.android;

import invalid.ayasiiwa_rudo.client.BBS;
import invalid.ayasiiwa_rudo.client.HttpSource;
import invalid.ayasiiwa_rudo.client.MemoryLogMerchant;
import invalid.ayasiiwa_rudo.client.android.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

class EndlessScrollListener implements OnScrollListener {
    private boolean enabled = true;
    private int visibleThreshold = 3;
    private int previousTotal = 0;
    private boolean loading = true;
    private BBSActivity bbs;

    public EndlessScrollListener(BBSActivity b) {
        bbs = b;
    }

    public EndlessScrollListener(BBSActivity b, int visibleThreshold) {
        bbs = b;
        this.visibleThreshold = visibleThreshold;
    }

    void clear() {
        previousTotal = 0;
        loading = true;
    }

    void setEnabled(boolean b) {
        enabled = b;
        clear();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (!enabled)
            return;
        if (totalItemCount <= visibleThreshold)
            return;

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            loading = true;
            bbs.nextPageAuto();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
}

public class BBSActivity extends PostListActivity {
    public static final String TEMP_FILE = "bbstmp";
    private static final int LOAD = 0;
    private static final int LOAD_UNREAD = LOAD + 1;
    private static final int LOAD_NEXT = LOAD_UNREAD + 1;
    private static final int LOAD_NEXT_AUTO = LOAD_NEXT + 1;
    private static final int LOAD_UNREAD_TOP = LOAD_NEXT_AUTO + 1;

    private final static int FlingThreshold = 1000;

    private int loadMode =  LOAD;
    private EndlessScrollListener autopager = null;
    private ListView listView = null;
    private boolean firstUpdate;
    private GestureDetector gestureDetector;
    private boolean flingEnabled = true;
    private boolean forceFlingStop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gestureDetector = new GestureDetector(this, simpleOnGestureListener);
        listView = (ListView) findViewById(R.id.PostList);

        findViewById(R.id.NextPage).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextPage();
                    }
                });
        findViewById(R.id.ReloadButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMode = LOAD;
                        loadWithProgress(-1, 0);
                    }
                });
        findViewById(R.id.ReloadUnreadButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMode = LOAD_UNREAD;
                        loadWithProgress(0, 1);
                    }
                });
        findViewById(R.id.PostButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String postid = null;
                        if (getBBS().getShortName().equals("ii"))
                            postid = getBBS().getPosts().get(0).getName();
                        startPostActivity("", postid);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        BBS bbs = getBBS();

        if (bbs == null) {
            MemoryLogMerchant log = new MemoryLogMerchant();
            HttpSource ssource = new HttpSource(getBbsUrl());
            // debug use. load local raw resource.
            // StreamSource ssource = new StreamSource(bbsUrl,
            // getResources().openRawResource(R.raw.zantei));
            log.setSource(ssource);
            bbs = new BBS(getBbsName(), getBbsUrl(), getBbsShortName(), isUseNextPageMessageID());
            setBBS(bbs);
            bbs.setLog(log);
            loadMode = LOAD;

            String fl[] = fileList();
            for (String f : fl) {
                if (f.contains(TEMP_FILE)) {
                    loadTemp();
                    return;
                }
            }
            loadWithProgress(-1, 0);
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
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    void restoreList() {
        if (getBBS() == null) {
            loadMode = LOAD;
        }
        super.restoreList();
    }

    @Override
    protected void asyncLoad() throws Exception {
        switch (loadMode) {
        case LOAD:
            getBBS().load();
            break;
        case LOAD_UNREAD:
            getBBS().loadUnread();
            break;
        case LOAD_UNREAD_TOP:
            getBBS().loadUnreadToHead();
            break;
        case LOAD_NEXT:
        case LOAD_NEXT_AUTO:
            getBBS().loadNext();
            break;
        }
    }

    void nextPage() {
        loadMode = LOAD_NEXT;
        loadWithProgress(-1, 0);
    }

    void nextPageAuto() {
        loadMode = LOAD_NEXT_AUTO;
        loadWithProgress(-1, 0);
    }

    @Override
    public String getTempFileName() {
        return TEMP_FILE;
    }

    @Override
    protected int getActivityLayoutId() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean(ViewPrefActivity.KEY_MENUBUTTON_ON_TOP, false))
            return R.layout.bbs_top;
        else
            return R.layout.bbs;
    }

    @Override
    protected void loadViewPref(SharedPreferences pref) {
        listView = (ListView) findViewById(R.id.PostList);
        super.loadViewPref(pref);
        boolean auto = pref.getBoolean(ViewPrefActivity.KEY_AUTO_PAGE, false);
        if (auto) {
            autopager = new EndlessScrollListener(this);
            listView.setOnScrollListener(autopager);
        } else {
            autopager = null;
            listView.setOnScrollListener(null);
        }
        forceFlingStop = !pref.getBoolean(ViewPrefActivity.KEY_FLING_LOAD, true);
    }

    @Override
    protected void preAsyncLoad() {
        firstUpdate = true;
        flingEnabled = false;
        if (autopager != null)
            autopager.setEnabled(false);
        PostAdapter adp = (PostAdapter)listView.getAdapter();
        if (loadMode == LOAD_UNREAD) {
            adp.clear();
            adp.notifyDataSetChanged();
            getBBS().clearLog();
        } else if (loadMode == LOAD) {
            adp.clear();
            adp.notifyDataSetChanged();
            getBBS().clearLog();
        }
    }

    @Override
    protected void updateAsyncLoad() {
        if (firstUpdate) {
            firstUpdate = false;
            if (loadMode == LOAD_NEXT) {
                ListAdapter adp = listView.getAdapter();
                int pos = adp.getCount();
                listView.setSelectionFromTop(pos -1, 0);
            }
        }
    }

    @Override
    protected void postAsyncLoad(boolean canceled) {
        if (autopager != null)
            autopager.setEnabled(true);
        flingEnabled = true;
    }

/*    class ScrollList extends Thread {
        int position = 0;
        int yPos = 0;
        int currentPosition = 0;
        int delta = -5;
        int p = 0;

        ScrollList(int position) {
            this.position = position;
        }

        public void run() {
            Log.d("STRANGE_WORLD", "child " + String.valueOf(listView.getChildCount()));
            currentPosition = listView.getFirstVisiblePosition();
            if (currentPosition == position)
                return;
            View itemView = listView.getChildAt(0);
            if (position < currentPosition)
                delta = 5;
            if (itemView != null)
                yPos = itemView.getTop();
            //Log.d("STRANGE_WORLD", "scroll " + String.valueOf(yPos));
            //while (yPos >= 20) {
            p = currentPosition;
            while (position != p) {
                try {

                    yPos += delta;
                    sleep(20);
                    listView.post(new Runnable() {
                        public void run() {
                            listView.setSelectionFromTop(currentPosition, yPos);
                            //currentPosition += delta;
                        }
                    });
                    p = listView.getFirstVisiblePosition();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };*/
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
        //return true;/// super.onTouchEvent(event);
    }
*/
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return onTouchEvent(event);
    }

    private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            doFling(velocityX, velocityY);
            return super.onFling(event1, event2, velocityX, velocityY);
        }
    };

    private void doFling(float velocityX, float velocityY) {
        if (forceFlingStop)
            return;
        if (!flingEnabled)
            return;
        if (velocityY < 0) { // foot
            if (velocityY > -FlingThreshold) return;
            int p = listView.getLastVisiblePosition();
            if (p == listView.getAdapter().getCount() - 1) {
                View v = listView.getChildAt(listView.getChildCount() - 1);
                if (v == null)
                    return;
                int h = listView.getHeight();
                int b = v.getBottom();
                if (b > h)
                    return; // not display all.
                TranslateAnimation translate = new TranslateAnimation(0, 0, -100, 0);
                translate.setDuration(700);
                translate.setInterpolator(new AccelerateInterpolator());
                listView.startAnimation(translate);
                nextPage();
            }
        } else { // head
            if (velocityY < FlingThreshold) return;
            int p = listView.getFirstVisiblePosition();
            if (p == 0) {
                View v = listView.getChildAt(0);
                if (v == null)
                    return;
                if (v.getTop() != 0)
                    return;

                TranslateAnimation translate = new TranslateAnimation(0, 0, 100, 0);
                translate.setDuration(700);
                translate.setInterpolator(new AccelerateInterpolator());
                listView.startAnimation(translate);
                loadMode = LOAD_UNREAD_TOP;
                loadWithProgress(0, 1);
            }
        }
    }
 }
