package invalid.ayasiiwa_rudo.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StreamSource implements BBSSource {
    private InputStream stream = null;
    private String url;
    private HashMap<String, String> altParams;
    private LoadPostHandler postHandler = null;

    public StreamSource(String Url, InputStream Stream) {
        url = Url;
        stream = Stream;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public HashMap<String, String> getAltParams() {
        return altParams;
    }

    @Override
    public List<Post> load(int PostNo, String PostId) throws Exception {
        if (stream == null)
            return new ArrayList<Post>();
        BBSParser parser = BBSParserFactory.createParser(url);
        if (parser == null)
            return new ArrayList<Post>();
        parser.setStream(stream);
        HashMap<String, String> alt = new HashMap<String, String>();
        List<Post> ret = parser.parse(alt, postHandler);
        altParams = alt;
        return ret;
    }

    @Override
    public List<Post> loadUnread(String lastname) throws Exception {
        /* do not　load */
        return new ArrayList<Post>();
    }

    @Override
    public List<Post> loadThread(String threadlink) throws Exception {
        BBSParser parser = BBSParserFactory.createParser(url);
        if (parser == null)
            return new ArrayList<Post>();
        parser.setStream(stream);
        List<Post> ret = parser.parseThread(postHandler);
        return ret;
    }

    @Override
    public void setLoadPostHandler(LoadPostHandler postHandler) {
        this.postHandler = postHandler;
    }
}
