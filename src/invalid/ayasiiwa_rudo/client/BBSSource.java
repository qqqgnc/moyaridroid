package invalid.ayasiiwa_rudo.client;

import java.util.HashMap;
import java.util.List;

public interface BBSSource {
    public List<Post> load(int PostNo, String PostId) throws Exception;

    public List<Post> loadUnread(String lastname) throws Exception;

    public List<Post> loadThread(String threadlink) throws Exception;

    public HashMap<String, String> getAltParams();

    public void setLoadPostHandler(LoadPostHandler postHandler);
}
