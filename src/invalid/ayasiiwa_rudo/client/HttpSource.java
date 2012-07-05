package invalid.ayasiiwa_rudo.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/*	kuzuha script post parameter
 * 	m: mode
 * 		n=next page, l=topic list, c=customize bbs, g=bbs log, etc..
 *  p: post id. post.name.
 *  d: count of display.
 *  g: gzip flag.
 */

public class HttpSource extends StreamSource {

    public HttpSource(String Url) {
        super(Url, null);
    }

    @Override
    public List<Post> load(int PostNo, String PostId) throws Exception {
        DefaultHttpClient hc = HttpUtil.buildHttpClient();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (PostNo > 0) {
            params.add(new BasicNameValuePair("m", "n"));
            params.add(new BasicNameValuePair("b", String.valueOf(PostNo)));
            params.add(new BasicNameValuePair("d", "30"));
            params.add(new BasicNameValuePair("g", "checked"));
            // II
            params.add(new BasicNameValuePair("mode", "page"));
            params.add(new BasicNameValuePair("gzip", "checked"));
            params.add(new BasicNameValuePair("bmsg", String.valueOf(PostNo)));
            params.add(new BasicNameValuePair("msgdisp", "30"));
        }
        if (PostId != null && PostId.length() != 0) {
            params.add(new BasicNameValuePair("p", PostId));
        }
        params.add(new BasicNameValuePair("g", "checked"));
        HttpPost post = new HttpPost(getUrl());
        post.setEntity(new UrlEncodedFormEntity(params, "Shift-JIS"));

        HttpResponse res = hc.execute(post);

        int code = res.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK) {
            throw new RuntimeException(res.getStatusLine().toString());
        }
        setStream(res.getEntity().getContent());
        return super.load(PostNo, PostId);
    }

    @Override
    public List<Post> loadUnread(String lastname) throws Exception {
        DefaultHttpClient hc = HttpUtil.buildHttpClient();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("midokureload", "midokureload"));
        params.add(new BasicNameValuePair("reload0", "reload0")); // for honten
        params.add(new BasicNameValuePair("p", lastname));
        params.add(new BasicNameValuePair("g", "checked"));
        params.add(new BasicNameValuePair("d", "0"));

        params.add(new BasicNameValuePair("postid", lastname));
        params.add(new BasicNameValuePair("gzip", "checked"));
        params.add(new BasicNameValuePair("msgdisp", "0"));
        HttpPost m = new HttpPost(getUrl());
        m.setEntity(new UrlEncodedFormEntity(params, "Shift-JIS"));
        HttpResponse res = hc.execute(m);
        int code = res.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK) {
            throw new RuntimeException(res.getStatusLine().toString());
        }
        setStream(res.getEntity().getContent());
        return super.load(0, lastname);
    }

    @Override
    public List<Post> loadThread(String threadlink) throws Exception {
        DefaultHttpClient hc = HttpUtil.buildHttpClient();
        HttpGet m = new HttpGet(threadlink);
        HttpResponse res = hc.execute(m);
        int code = res.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK) {
            throw new RuntimeException(res.getStatusLine().toString());
        }
        setStream(res.getEntity().getContent());
        return super.loadThread(threadlink);
    }

}
