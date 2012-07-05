package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class CxParser extends IIParser {

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
                altParams.put("title", getTitle(r));
                altParams.put("bgcolor", parseBodySetting(r));
                altParams.put("protectcode", parseProtectCode(r));
            }
            setPostId(getPostId(r));
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
    public String parseProtectCode(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("<input type=\"hidden\" name=\"pc\"")) {
                int start = line.indexOf("value=\"");
                int end = line.lastIndexOf("\">");
                if (start == -1 || end == -1)
                    return "";
                return line.substring(start + 7, end);
            }
        }
        return "";
    }

    @Override
    protected String getParticipants(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            int pos = line.indexOf("現在の参加者 : ");
            if (pos > -1) {
                int e = line.indexOf("<br>", pos + 8);
                if (e < 0)
                    return "";
                return line.substring(pos, e);
            }
        }
        return "";
    }

    @Override
    protected String SkipToPost(BufferedReader r) throws IOException {
        String line = r.readLine();
        while (line != null) {
            if (line.startsWith("<hr></font><input type=submit")) {
                return line;
            } else if (line.startsWith("<body ")) // thread
                return r.readLine();
            line = r.readLine();
        }
        return null;
    }

    @Override
    protected String extractPostTime(String s) {
        int start = s.indexOf('>');
        if (start == -1)
            return "";
        return s.substring(start + 2, s.length() - 1);
    }

    @Override
    protected void skipToTextFoot(BufferedReader r) throws IOException {
        super.skipToTextFoot(r);
        r.readLine();
    }


    @Override
    public PostParams createPostParams() {
        return new CxPostParams();
    }

    @Override
    public PostParams parseFollowPost() throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(getStream(),
                getCharset()));
        PostParams pp = createPostParams();
        try {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("<form"))
                    break;
            }
            if (line == null)
                return pp;
            splitInputTag(r.readLine(), pp, true);
            splitInputTag(r.readLine(), pp, true);
            splitInputTag(r.readLine(), pp, true);
            splitInputTag(r.readLine(), pp, true);

            while ((line = r.readLine()) != null) {
                if (line.contains("<textarea")) {
                    if (line.endsWith(">"))
                        line = r.readLine(); // II
                    StringBuffer b = new StringBuffer();
                    do {
                        if (line.contains("</textarea>"))
                            break;
                        b.append(line.substring(line.indexOf('>') + 1)
                                .replaceAll("  ", "&nbsp;&nbsp;"));
                        b.append("<br>");
                    } while ((line = r.readLine()) != null);
                    pp.setText(b.toString());
                    break;
                }
            }
            r.readLine();
            while ((line = r.readLine()) != null) {
                if (line.contains("<input")) {
                    splitInputTag(line, pp, true);
                }
            }
        } finally {
            r.close();
        }
        return pp;
    }
}
