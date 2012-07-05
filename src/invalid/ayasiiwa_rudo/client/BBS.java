package invalid.ayasiiwa_rudo.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class BBS {
    private String url;
    private String name;
    private String shortName;
    private LogMerchant log = null;
    private int currentPostNo = 0;
    private HashMap<String, String> altParams = new HashMap<String, String>();
    private LoadPostHandler postHandler = null;
    private boolean useNextPageMessageID = true;

    public BBS(String Name, String Url, String ShortName, boolean useNextPageMessageID) {
        setName(Name);
        setUrl(Url);
        setShortName(ShortName);
        altParams.put("bgcolor", "#004040");
        this.useNextPageMessageID = useNextPageMessageID;
    }

    public LogMerchant getLog() {
        return log;
    }

    public void setLog(LogMerchant log) {
        this.log = log;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setShortName(String name) {
        this.shortName = name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getProtectCode() {
        if (altParams != null)
            return altParams.get("protectcode");
        return null;
    }

    public void setProtectCode(String pc) {
        if (altParams != null)
            altParams.put("protectcode", pc);
    }

    public String getBgColor() {
        if (altParams != null)
            return altParams.get("bgcolor");
        return null;
    }

    public void setBgColor(String bg) {
        if (altParams != null)
            altParams.put("bgcolor", bg);
    }

    public String getParticipants() {
        if (altParams != null)
            return altParams.get("participants");
        return null;
    }

    public String getTitle() {
        if (altParams != null)
            return altParams.get("title");
        return null;
    }

    public List<Post> getPosts() {
        return log.getLogs(0, -1);
    }

    public void load() throws Exception {
        currentPostNo = 0;
        if (log == null)
            return;
        log.getSource().setLoadPostHandler(postHandler);
        currentPostNo = log.logging();
        altParams = log.getSource().getAltParams();
    }

    public void loadUnread() throws Exception {
        if (log == null)
            return;
        log.getSource().setLoadPostHandler(postHandler);
        currentPostNo = log.loggingUnread(true);
        altParams = log.getSource().getAltParams();
    }

    public void loadUnreadToHead() throws Exception {
        if (log == null)
            return;
        log.getSource().setLoadPostHandler(postHandler);
        if (useNextPageMessageID) {
            log.loggingUnread(false);
        } else {
            currentPostNo += log.loggingUnread(false);
        }
        altParams = log.getSource().getAltParams();
    }

    public void loadNext() throws Exception {
        if (log == null)
            return;
        log.getSource().setLoadPostHandler(postHandler);
        List<Post> ps = getPosts();
        if (ps.isEmpty())
            currentPostNo = log.logging();
        else {
            currentPostNo += log.loggingNext(currentPostNo);
        }
        altParams = log.getSource().getAltParams();
    }

    @SuppressWarnings("unchecked")
    public void loadTemp(ObjectInputStream ois) throws Exception {
        name = ois.readUTF();
        url = ois.readUTF();
        shortName = ois.readUTF();
        altParams = (HashMap<String, String>) ois.readUnshared();
        log.loadTemp(ois);
    }

    public void saveTemp(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(name);
        oos.writeUTF(url);
        oos.writeUTF(shortName);
        oos.writeUnshared(altParams);
        log.saveTemp(oos);
    }

    public void loadThread(String threadLink) throws Exception {
        log.getSource().setLoadPostHandler(postHandler);
        log.loadThread(threadLink);
    }

    static public String post(String url, PostParams pp) throws Exception {
        Poster p = new Poster(url);
        return p.post(pp);
    }

    static public PostParams getFollowParams(String url) throws Exception {
        DefaultHttpClient hc = HttpUtil.buildHttpClient();
        HttpGet m = new HttpGet(url);
        HttpResponse res = hc.execute(m);
        int code = res.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK)
            return null;
        BBSParser parser = BBSParserFactory.createParser(url);
        parser.setStream(res.getEntity().getContent());
        return parser.parseFollowPost();
    }

    public LoadPostHandler getPostHandler() {
        return postHandler;
    }

    public void setLoadPostHandler(LoadPostHandler postHandler) {
        this.postHandler = postHandler;
    }

    public void clearLog() {
        currentPostNo = 0;
        if (log != null)
            log.clear();
    }
 }
