package invalid.ayasiiwa_rudo.client.android;

import java.util.List;

import invalid.ayasiiwa_rudo.client.BBSInfo;
import invalid.ayasiiwa_rudo.client.BBSManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BBSManager bbsManager = new BBSManager();
        try {
            bbsManager.loadFromStream(getResources().openRawResource(
                    R.raw.bbslist));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
            finish();
        }
        final List<BBSInfo> bbs = bbsManager.getBBSList();
        final String[] names = new String[bbs.size()];
        int idx = 0;
        for (BBSInfo i: bbs)
            names[idx++] = i.getName();

        new AlertDialog.Builder(this)
            .setTitle("Select shortcut")
            .setItems(names,
                new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialoginterface,int i) {
                    BBSInfo info = bbs.get(i);
                    Intent shortcut = new Intent("invalid.ayasiiwa_rudo.client.android.shortcut");
                    shortcut.setClass(getApplicationContext(), BBSActivity.class);
                    shortcut.putExtra(PostListActivity.INTENT_BBS_NAME, info.getName());
                    shortcut.putExtra(PostListActivity.INTENT_BBS_URL, info.getUrl());
                    shortcut.putExtra(PostListActivity.INTENT_BBS_SHORT_NAME, info.getShortName());
                    shortcut.putExtra(PostListActivity.INTENT_BBS_MESSAGE_ID, info.isUseNextPageMessageID());
                    shortcut.putExtra(PostListActivity.INTENT_DELETE_TEMP_FILE, true);
                    Intent.ShortcutIconResource icon =
                        Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon);
                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut);
                    String name = info.getName();
                    int index = name.indexOf("＠");
                    if (index != -1)
                        name = name.substring(index, name.length());
                    else {
                        name = name.replace("あやしいわーるど", "＠");
                    }
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
                    setResult(RESULT_OK, intent);
                    finish();
            }
         }).show();
    }
}
