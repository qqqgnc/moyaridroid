package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class IIParser extends BBSParser {
    private String postid;

    @Override
    public ArrayList<Post> parse(Map<String, String> altParams, LoadPostHandler handler)
            throws IOException {
        ArrayList<Post> ret = new ArrayList<Post>(30);
        if (getStream() == null)
            return ret;
        BufferedReader r = new BufferedReader(new InputStreamReader(
                getStream(), getCharset()), 1024 * 8);
        try {
            if (altParams != null) {
                altParams.put("bgcolor", parseBodySetting(r));
                altParams.put("protectcode", parseProtectCode(r));
            }
            postid = getPostId(r);
            if (altParams != null)
                altParams.put("participants", getParticipants(r));

            String line = SkipToPost(r);
            if (line == null)
                return ret;

            while (line != null) {
                if (line.length() == 0)
                    break;
                if (line.startsWith("<p>"))
                    break;
                Post p = ParsePost(line, r);
                if (p != null) {
                    ret.add(p);
                    if (handler != null)
                        handler.add(p);
                }
                line = r.readLine();

            }
        } finally {
            r.close();
            if (handler != null)
                handler.add(null);
        }
        return ret;
    }

    @Override
    protected String getProtectCodeTag() {
        return "<input type=\"hidden\" name=\"protect\"";
    }

    protected String getPostId(BufferedReader r) throws IOException {
        String line;
        final String s = "name=\"postid\" value=\"";
        while ((line = r.readLine()) != null) {
            int idx = line.indexOf(s);
            if (idx != -1) {
                return line.substring(idx + s.length(), line.lastIndexOf('"'));
            }
        }
        return "";
    }

    @Override
    protected Post ParsePost(String a_name, BufferedReader r)
            throws IOException {
        if (!a_name.contains("<hr"))
            return null;
        String line = r.readLine();
        if (line == null || !line.startsWith("<font"))
            return null;
        Post p = new Post();
        p.setName(postid);
        p.setTitle(extractTitle(line));
        p.setAuthor(extractAuthor(r.readLine()));
        p.setPostTime(extractPostTime(r.readLine()));
        p.setFollowLink(extractFollowLink(r.readLine()));
        r.readLine(); // search link
        String s = r.readLine();
        if (s.contains("◆")) {
            p.setThreadLink(extractThreadLink(s));
            r.readLine();
        }
        readText(r, p);
        skipToTextFoot(r);
        return p;
    }

    @Override
    protected String extractPostTime(String s) {
        int start = s.indexOf(">　");
        if (start == -1)
            return "";
        return s.substring(start + 2, s.length() - 1);
    }

    @Override
    protected String SkipToPost(BufferedReader r) throws IOException {
        String line = r.readLine();
        while (line != null) {
            if (line.contains("■ : ")) {
                return r.readLine();
            } else if (line.startsWith("<body ")) // thread
                return r.readLine();
            line = r.readLine();
        }
        return null;
    }

    @Override
    protected String extractThreadLink(String s) {
        if (!s.contains("mode=thread"))
            return "";
        int start = s.indexOf("href=\"");
        int end = s.indexOf('"', start + 6);
        if (start == -1 || end == -1)
            return "";
        return s.substring(start + 6, end);
    }

    @Override
    protected void readText(BufferedReader r, Post p) throws IOException {
        StringBuilder quote = new StringBuilder(300);
        StringBuilder body = new StringBuilder(300);
        String line = r.readLine();
        if (line == null)
            return;
        line = line.substring(line.indexOf("<pre>") + 5);
        while (line != null) {
            int pos = line.indexOf("</pre");
            if (pos != -1) {
                if (pos != 0) {
                    if (isReflinkLine(line))
                        break;
                    line = line.substring(0, pos);
                    body.append(line.replaceAll("  ", "&nbsp;&nbsp;"));
                    body.append("<br>");
                }
                break;
            }

            if (line.startsWith("&gt;")) {
                quote.append(line.replaceAll("  ", "&nbsp;&nbsp;"));
                quote.append("<br>");
            } else {
                body.append(line.replaceAll("  ", "&nbsp;&nbsp;"));
                body.append("<br>");
            }
            line = r.readLine();
        }
        deleteLastBR(quote, body);
        if (quote.length() != 0) {
            p.setQuotationText(quote.substring(0, quote.length() - 4));
        }
        p.setBodyText(body.toString());
    }

    @Override
    protected void skipToTextFoot(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            if (line.contains("</blockquote>"))
                break;
        }
    }

    @Override
    protected String getParticipants(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            int pos = line.indexOf("通過人数");
            if (pos > -1) {
                int e = line.indexOf("人", pos + 4);
                if (e < 0)
                    return "";
                return line.substring(pos, e + 1).replace("</a>", "");
            }
        }
        return "";
    }

    @Override
    public PostParams createPostParams() {
        return new IIPostParams();
    }

    protected void setPostId(String pid) {
        postid = pid;
    }
}
