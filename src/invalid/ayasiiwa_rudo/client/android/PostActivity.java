package invalid.ayasiiwa_rudo.client.android;

import java.util.List;
import java.util.concurrent.Semaphore;

import invalid.ayasiiwa_rudo.client.BBS;
import invalid.ayasiiwa_rudo.client.BBSInfo;
import invalid.ayasiiwa_rudo.client.BBSManager;
import invalid.ayasiiwa_rudo.client.BBSParserFactory;
import invalid.ayasiiwa_rudo.client.HttpSource;
import invalid.ayasiiwa_rudo.client.PostParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PostActivity extends Activity implements Runnable {
    public static final String INTENT_BBS_URL = "BBS_URL";
    public static final String INTENT_PROTECT_CODE = "PROTECT_CODE";
    public static final String INTENT_BG_COLOR = "BG_COLOR";
    public static final String INTENT_FOLLOW_LINK = "follow_link";
    public static final String INTENT_II_POSTID = "postid";

    private String url;
    private String protectCode;
    private String baseText;
    private String title;
    private String link;
    private String followLink;
    private EditText titleEdit;
    private EditText bodyEdit;
    private EditText nameEdit;
    private EditText mailEdit;
    private EditText linkEdit;
    private ProgressDialog progressDialog = null;
    private Handler handler = new Handler();
    private int mode = 0;
    private PostParams postParams;
    private Semaphore postLock = new Semaphore(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);

        Intent i = getIntent();
        if (Intent.ACTION_SEND.equals(i.getAction())) {
            protectCode = "";
            followLink = "";
            Bundle extras = i.getExtras();
            if (extras != null) {
                baseText = extras.getString(Intent.EXTRA_TEXT);
                baseText.replaceAll("\n", "<br>");
                baseText.replaceAll(" ", "&nbsp;");
            } else
                baseText = "";
            showBBSDialog();
        } else {
            url = i.getStringExtra(INTENT_BBS_URL);
            protectCode = i.getStringExtra(INTENT_PROTECT_CODE);
            followLink = i.getStringExtra(INTENT_FOLLOW_LINK);
            String bg = i.getStringExtra(INTENT_BG_COLOR);
            baseText = "";
            if (bg != null && bg.length() != 0) {
                try {
                    ((LinearLayout) findViewById(R.id.BaseLayout))
                            .setBackgroundColor(Color.parseColor(bg));
                } catch (IllegalArgumentException e) {
                    // ignore error.
                }
            }
        }
        title = "";
        link = "";
        titleEdit = (EditText) findViewById(R.id.TitleEdit);
        bodyEdit = (EditText) findViewById(R.id.BodyEdit);
        nameEdit = (EditText) findViewById(R.id.NameEdit);
        mailEdit = (EditText) findViewById(R.id.MailEdit);
        linkEdit = (EditText) findViewById(R.id.LinkEdit);

        Typeface f = PostAdapter.body.font;
        if (f != null) {
            bodyEdit.setTypeface(f);
            titleEdit.setTypeface(f);
            nameEdit.setTypeface(f);
            mailEdit.setTypeface(f);
            linkEdit.setTypeface(f);
        }
        clear();

        findViewById(R.id.PostButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        post();
                    }
                });

        findViewById(R.id.EraseButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clear();
                    }
                });

        if (savedInstanceState != null) {
            String a[] = savedInstanceState.getStringArray("items");
            titleEdit.setText(a[0]);
            bodyEdit.setText(a[1]);
            nameEdit.setText(a[2]);
            mailEdit.setText(a[3]);
            linkEdit.setText(a[4]);
            url = a[5];
            protectCode = a[6];
            baseText = a[7];
            title = a[8];
            link = a[9];
            followLink = a[10];
        } else if (followLink.length() != 0) {
            mode = 1;
            progressDialog = ProgressDialog.show(this, "", "Loading...", true);
            Thread th = new Thread(this);
            th.start();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String a[] = savedInstanceState.getStringArray("items");
        titleEdit.setText(a[0]);
        bodyEdit.setText(a[1]);
        nameEdit.setText(a[2]);
        mailEdit.setText(a[3]);
        linkEdit.setText(a[4]);
        url = a[5];
        protectCode = a[6];
        baseText = a[7];
        title = a[8];
        link = a[9];
        followLink = a[10];
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String a[] = new String[11];
        a[0] = titleEdit.getText().toString();
        a[1] = bodyEdit.getText().toString();
        a[2] = nameEdit.getText().toString();
        a[3] = mailEdit.getText().toString();
        a[4] = linkEdit.getText().toString();
        a[5] = url;
        a[6] = protectCode;
        a[7] = baseText;
        a[8] = title;
        a[9] = link;
        a[10] = followLink;
        outState.putStringArray("items", a);
    }

    private void post() {
        if (url == null || url.length() == 0) {
            Toast.makeText(this, "投稿先が未指定です。", Toast.LENGTH_LONG).show();
            showBBSDialog();
            return;
        }
        if (postLock.tryAcquire()) {
        	progressDialog = ProgressDialog.show(this, "", "Loading...", true);
        	Thread th = new Thread(this);
        	th.start();
        }
    }

    private void clear() {
        titleEdit.setText(Html.fromHtml(title.replaceAll(" ", "&nbsp;")));
        bodyEdit.setText(Html.fromHtml(baseText));
        nameEdit.setText("");
        mailEdit.setText("");
        linkEdit.setText(link);
        bodyEdit.requestFocus();
        bodyEdit.setSelection(bodyEdit.getText().length());
    }

    private void loadFollowLink() throws Exception {
        postParams = BBS.getFollowParams(followLink);
        if (postParams == null)
            return;
        handler.post(new Thread(new Runnable() {
            @Override
            public void run() {
                title = postParams.getTitle();
                baseText = postParams.getText();
                link = postParams.getLink();
                protectCode = postParams.getProtectCode();
                clear();
                progressDialog.dismiss();
                mode = 0;
            }
        }));
    }

    @Override
    public void run() {
        try {
            if (mode == 0) {
                if (postParams == null)
                    postParams = BBSParserFactory.createParser(url)
                            .createPostParams();
                if (protectCode == null || protectCode.length() == 0) {
                    protectCode = loadProtectCode(url);
                    if (protectCode == null || protectCode.length() == 0)
                        throw new RuntimeException("Can't load protect code");
                }
                postParams.setProtectCode(protectCode);
                postParams.setName(nameEdit.getText().toString());
                postParams.setTitle(titleEdit.getText().toString());
                postParams.setMail(mailEdit.getText().toString());
                postParams.setText(bodyEdit.getText().toString());
                postParams.setLink(linkEdit.getText().toString());
                String pc = BBS.post(url, postParams);
                if (pc.length() != 0)
                    protectCode = pc;
                progressDialog.dismiss();
                Intent i = new Intent();
                i.putExtra(INTENT_PROTECT_CODE, pc);
                setResult(Activity.RESULT_OK, i);
                finish();
            } else {
                loadFollowLink();
            }
        } catch (final Exception e) {
            final Activity a = this;
            handler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(a);
                    dlg.setTitle("Error");
                    dlg.setMessage(e.toString());
                    dlg.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                    if (mode != 0)
                                        finish();
                                }
                            });
                    dlg.create();
                    dlg.show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            }));
        } finally {
        	postLock.release();
        }
    }

    private void showBBSDialog() {
        BBSManager manager = new BBSManager();
        try {
            manager.loadFromStream(getResources()
                    .openRawResource(R.raw.bbslist));
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        final List<BBSInfo> bbs = manager.getBBSList();
        final CharSequence[] items = new CharSequence[bbs.size()];
        int idx = 0;
        for (BBSInfo info : bbs) {
            items[idx++] = info.getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("投稿する板を選んでください。");
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                url = "";
                for (BBSInfo info : bbs) {
                    if (info.getName().equals(items[item].toString()))
                        url = info.getUrl();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private String loadProtectCode(String url) throws Exception {
        HttpSource ss = new HttpSource(url);
        ss.load(0, null);
        return ss.getAltParams().get("protectcode");
    }

}
