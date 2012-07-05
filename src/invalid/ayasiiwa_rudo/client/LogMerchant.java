package invalid.ayasiiwa_rudo.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class LogMerchant {
    private BBSSource bbsSource = null;
    private String unreadMessageID = "";
    private String nextPageMessageID = "";

    public BBSSource getSource() {
        return bbsSource;
    }

    public void setSource(BBSSource bbsSource) {
        this.bbsSource = bbsSource;
    }

    public int logging() throws Exception {
        if (bbsSource == null)
            return 0;
        List<Post> ps = bbsSource.load(0, null);
        if (ps == null || ps.isEmpty()) {
            unreadMessageID = "";
            nextPageMessageID = "";
        } else {
            unreadMessageID = ps.get(0).getName();
            nextPageMessageID = unreadMessageID;
        }
        return storeLog(ps, -1);
    }

    public int loggingNext(int PostNo) throws Exception {
        if (bbsSource == null)
            return 0;
        List<Post> ps = bbsSource.load(PostNo, nextPageMessageID);
        return storeLog(ps, -1);
    }

    public int loggingUnread(boolean UpdateNextPageID) throws Exception {
        if (bbsSource == null)
            return 0;
        if (unreadMessageID.length() == 0)
            return logging();
        List<Post> ps = bbsSource.loadUnread(unreadMessageID);
        if (!ps.isEmpty()) {
            String s = ps.get(0).getName();
            if (s.length() != 0) {
                unreadMessageID = s;
                if (UpdateNextPageID)
                    nextPageMessageID = s;
            }
        }
        return storeLog(ps, 0);
    }

    public void saveTemp(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(unreadMessageID);
        oos.writeUTF(nextPageMessageID);
        List<Post> pl = getLogs(0, -1);
        if (pl != null)
            oos.writeUnshared(pl);
    }

    @SuppressWarnings("unchecked")
    public void loadTemp(ObjectInputStream ois) throws Exception {
        unreadMessageID = ois.readUTF();
        nextPageMessageID = ois.readUTF();
        List<Post> ps = (ArrayList<Post>) ois.readUnshared();
        if (ps != null)
            storeLog(ps, -1);
    }

    public int loadThread(String threadlink) throws Exception {
        if (bbsSource == null)
            return 0;
        List<Post> ps = bbsSource.loadThread(threadlink);
        if (ps != null)
            return storeLog(ps, -1);
        return 0;
    }

    public abstract Post getLog(String name);

    public abstract List<Post> getLogs(int start, int count);

    public abstract int size();

    public abstract int storeLog(List<Post> log, int index);

    public abstract void clear();
}
