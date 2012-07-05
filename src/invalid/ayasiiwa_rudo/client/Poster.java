package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class Poster {
    private static final String NOBREAK_SPACE = "\u00A0"; // no-break space

    public class BBSPostError extends Exception {
        public BBSPostError(String string) {
            super(string);
        }

        private static final long serialVersionUID = -8931638332463613225L;
    }

    private String postUrl;

    Poster(String PostUrl) {
        this.postUrl = PostUrl;
    }

    public String post(PostParams pp) throws Exception {
        pp.prepare();
        DefaultHttpClient hc = HttpUtil.buildHttpClient();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        String[] needrep = new String[] { pp.getNameKey(), pp.getTitleKey(),
                pp.getTextKey(), pp.getMailKey(), pp.getLinkKey() };
        Arrays.sort(needrep);
        Set<Map.Entry<String, String>> set = pp.getParams().entrySet();
        for (Map.Entry<String, String> it : set) {
            if (Arrays.binarySearch(needrep, it.getKey()) > 0)
                params.add(new BasicNameValuePair(it.getKey(), it.getValue()
                        .replaceAll(NOBREAK_SPACE, " ")));
            else
                params.add(new BasicNameValuePair(it.getKey(), it.getValue()));
        }
        HttpPost m = new HttpPost(postUrl);
        m.addHeader("Referer", postUrl);
        m.addHeader("Content-Type", "application/x-www-form-urlencoded");
        m.setEntity(new UrlEncodedFormEntity(params, "Shift-JIS"));
        HttpResponse res = hc.execute(m);
        int code = res.getStatusLine().getStatusCode();
        if (code != HttpStatus.SC_OK)
            throw new BBSPostError("Post Error: " + res.getStatusLine().toString());
        return checkError(res.getEntity().getContent());
    }

    private String checkError(InputStream stream) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream,
                "Shift-JIS"), 1024 * 8);
        String s;
        while ((s = r.readLine()) != null) {
            if (s.toLowerCase().contains("<title>")) {
                if (!s.contains("(エラー)")) {
                    return BBSParserFactory.createParser(postUrl)
                            .parseProtectCode(r);
                } else {
                    while ((s = r.readLine()) != null) {
                        // find <h1> (zante) or <H3> (kuzuha 0.1 prev9)
                        if (s.toLowerCase().contains("<h")) {
                            throw new BBSPostError(findErrorBody(s));
                        }
                    }
                }
            }
        }
        return "";
    }

    private String findErrorBody(String s) {
        int start = 0, end = s.length(), i = 0;
        char cs[] = s.toCharArray();
        for (i = 0; i < cs.length - 1; ++i) {
            if (cs[i] == '>') {
                if (cs[i + 1] != '<') {
                    start = ++i;
                }
            } else if (start != 0 && cs[i] == '<') {
                end = i;
                break;
            }
        }
        return s.substring(start, end);
    }
}
