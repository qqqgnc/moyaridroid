package invalid.ayasiiwa_rudo.client.android;

import java.util.List;

import invalid.ayasiiwa_rudo.client.LoadPostHandler;
import invalid.ayasiiwa_rudo.client.Post;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoadTask extends AsyncTask<PostAdapter, Post, PostAdapter> {
    class PostHandler implements LoadPostHandler
    {
        public PostAdapter adapter = null;
        LoadTask task = null;

        @Override
        public void add(Post post) {
            if (post == null) {
                task = null;
                return;
            }
            if (task == null)
                return;
            synchronized(task) {
                task.publishProgress(post);
            }
        }
    }

    private PostListActivity activity;
    private PostAdapter adapter;
    private Exception threadException = null;
    private int insertPosition = -1;
    private int insertDelta = 0;
    private int updateCount = 0;
    private int notifyTiming = 1;

    LoadTask(PostListActivity act, int insertPos, int insertDelta) {
        activity = act;
        insertPosition = insertPos;
        this.insertDelta = insertDelta;
    }

    @Override
    protected PostAdapter doInBackground(PostAdapter... params) {
        adapter = params[0];
        PostHandler handler = new PostHandler();
        handler.adapter = adapter;
        handler.task = this;
        activity.getBBS().setLoadPostHandler(handler);
        try {
            activity.asyncLoad();
        } catch (Exception e) {
            threadException = e;
        }
        return params[0];
    }

    @Override
    protected void onPreExecute() {
        activity.preAsyncLoad();
        TextView t = (TextView) activity.findViewById(R.id.TextParticipants);
        changeTitleText(activity, "ÅR(ÅLÅ[` )Ém");
        t.setText("");
        t.setTextColor(Color.YELLOW);
     }

    @Override
    protected void onPostExecute(PostAdapter result) {
        activity.finishLoadTask();
        if (isCancelled ()) {
            activity.finsihWaitAnime(null);
            changeTitleText(activity, "");
            activity.postAsyncLoad(true);
            return;
        }
        if (threadException != null) {
            Toast.makeText(activity, threadException.toString(), Toast.LENGTH_LONG).show();
        }
        adapter.notifyDataSetChanged();
        String title = activity.getBBS().getTitle();
        if (title != null && title.length() != 0) {
            TextView t = (TextView) activity.findViewById(R.id.TextTitle);
            t.setText(title);
        }
        changeTitleText(activity, activity.getBBS().getParticipants());
        activity.finsihWaitAnime(activity.getBBS().getParticipants());
        String bg = activity.getBBS().getBgColor();
        View layout = activity.findViewById(R.id.BaseLayout);
        if (layout != null && bg != null && bg.length() != 0) {
            try {
                int color = Color.parseColor(bg);
                layout.setBackgroundColor(color);
            } catch (IllegalArgumentException e) {
                // ignore error.
            }
        }
        activity.postAsyncLoad(false);
    }

    @Override
    protected void onProgressUpdate(Post... values) {
        List<Post> posts = adapter.getPosts();
        if (isCancelled ())
            return;
        if (values[0] != null) {
            if (insertPosition == -1)
                posts.add(values[0]);
            else {
                posts.add(insertPosition, values[0]);
                insertPosition += insertDelta;
            }
            ++updateCount;
            if (updateCount >= notifyTiming) {
                adapter.notifyDataSetChanged();
                notifyTiming = notifyTiming * 2;
                updateCount = 0;
            }
            activity.updateAsyncLoad();
        }
    }

    private void changeTitleText(PostListActivity act, String s) {
        TextView t = (TextView) act.findViewById(R.id.TextParticipants);
        TextView title = (TextView) act.findViewById(R.id.TextTitle);
        t.setTextColor((title).getCurrentTextColor());
        float width = 0;
        if (s != null) {
            t.setText(s);
            width = t.getPaint().measureText(s) + 10.0f;
        } else {
            t.setText("");
        }
        title.setMaxWidth(act.getDisplayWidth() - (int)width);
    }
}
