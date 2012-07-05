package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class BBSParser {
    private InputStream stream = null;
    private String charset = "Shift-JIS";

    public BBSParser() {
    }

    public BBSParser(InputStream s, String Charset) {
        stream = s;
        charset = Charset;
    }

    public ArrayList<Post> parse(Map<String, String> altParams, LoadPostHandler handler)
            throws IOException {
        ArrayList<Post> ret = new ArrayList<Post>(30);
        if (stream == null)
            return ret;
        BufferedReader r = new BufferedReader(new InputStreamReader(stream,
                charset), 1024 * 8);
        try {
            if (altParams != null) {
                String title = getTitle(r);
                if (title.length() != 0)
                    altParams.put("title", title);
                altParams.put("bgcolor", parseBodySetting(r));
                altParams.put("protectcode", parseProtectCode(r));
                altParams.put("participants", getParticipants(r));
            }
            String line = SkipToPost(r);
            if (line == null)
                return ret;

            while (line != null) {
                if (line.length() == 0)
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

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    protected String parseBodySetting(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            line = line.toLowerCase();
            if (line.contains("<body")) {
                int start = line.indexOf("bgcolor=\"") + 9;
                int end = line.indexOf('"', start);
                if (start < 9 || end == -1)
                    return "";
                return line.substring(start, end);
            }
        }
        return "";
    }

    /*
     * find valid protect code. uncomment line is valid. <INPUT type="hidden"
     * name="pc" value="127393540058"> <!-- <INPUT type="hidden" name="pc"
     * value="127393540054"> --> <!-- <INPUT type="hidden" name="pc"
     * value="127393540162"> -->
     */
    public String parseProtectCode(BufferedReader r) throws IOException {
        String line, last = "";
        final String pctag = getProtectCodeTag();
        while ((line = r.readLine()) != null) {
            if (line.toLowerCase().contains(pctag)) {
                // Log.d("DEBUG", line);
                if (!last.contains("<!--")) {
                    int start = line.indexOf("value=\"");
                    int end = line.lastIndexOf("\">");
                    if (start == -1 || end == -1)
                        return "";
                    return line.substring(start + 7, end);
                }
            }
            last = line;
        }
        return "";
    }

    protected String getTitle(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            if (line.toLowerCase().startsWith("<title>")) {
                int p = line.toLowerCase().indexOf("</title>");
                if (p != -1)
                    return line.substring(7, p);
            }
        }
        return "";
    }

    protected String getProtectCodeTag() {
        return "<input type=\"hidden\" name=\"pc\"";
    }

    protected String getParticipants(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            int pos = line.indexOf("現在の参加者 :");
            if (pos > -1) {
                int e = line.indexOf("名", pos + 8);
                if (e < 0)
                    e = line.indexOf("人", pos + 8);
                if (e < 0)
                    return "";
                return line.substring(pos, e + 1);
            }
        }
        return "";
    }

    protected String SkipToPost(BufferedReader r) throws IOException {
        String line = r.readLine();
        while (line != null) {
            if (line.toLowerCase().startsWith("<a name=")) {
                if (!line.contains("bottom"))
                    return line;
            }
            line = r.readLine();
        }
        return null;
    }

    protected Post ParsePost(String a_name, BufferedReader r)
            throws IOException {
        if (a_name == null)
            return null;
        String name = extractAName(a_name);
        if (name.length() == 0)
            return null;
        Post p = new Post();
        p.setName(name);
        skipToTextHead(r);
        p.setTitle(extractTitle(r.readLine()));
        p.setAuthor(extractAuthor(r.readLine()));
        p.setPostTime(extractPostTime(r.readLine()));
        p.setFollowLink(extractFollowLink(r.readLine()));
        String s = r.readLine();
        if (s.contains("★"))
            s = r.readLine();
        p.setThreadLink(extractThreadLink(s));
        skipToTextBody(r);
        readText(r, p);
        skipToTextFoot(r);
        return p;
    }

    protected void skipToTextHead(BufferedReader r) throws IOException {
        r.readLine();
    }

    protected void skipToTextBody(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            if (line.toUpperCase().contains("<PRE"))
                return;
        }
    }

    protected void skipToTextFoot(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            if (line.contains("<!-- -->"))
                break;
        }
    }

    // <A name="9999999"></A> -> 9999999
    protected String extractAName(String s) {
        int start = s.indexOf("name=\"");
        int end = s.lastIndexOf('"');
        if (start == -1 || end == -1)
            return "";
        String r = s.substring(start + 6, end);
        if (r.matches("\\d+"))
            return r;
        return "";
    }

    // <FONT size="+1" color="#fffffe"><B>＞hoge</B></FONT> -> hoge
    protected String extractTitle(String s) {
        String ups = s.toUpperCase();
        int start = ups.indexOf("<B>");
        int end = ups.lastIndexOf("</B>");
        if (start == -1 || end == -1)
            return "";
        String ret = s.substring(start + 3, end);
        if (ret.equals(" "))
            return "";
        return ret;
    }

    protected String extractAuthor(String s) {
        return extractTitle(s);
    }

    protected String extractPostTime(String s) {
        int start = s.indexOf('>');
        if (start == -1)
            return "";
        return s.substring(start + 1, s.length() - 1);
    }

    // 投稿者などの空白はそのままにするのがくずはスクリプト準拠。本文だけ置き換える
    protected void readText(BufferedReader r, Post p) throws IOException {
        StringBuilder quote = new StringBuilder(300);
        StringBuilder body = new StringBuilder(300);
        String line;
        boolean inquote = true;
        while ((line = r.readLine()) != null) {
            if (line.toUpperCase().contains("</PRE"))
                break;
            if (inquote) {
                if (line.startsWith("&gt;") || line.startsWith("<FONT")) {
                    quote.append(line.replaceAll("  ", "&nbsp;&nbsp;"));
                    quote.append("<br>");
                } else {
                    inquote = false;
                    body.append(line.replaceAll("  ", "&nbsp;&nbsp;"));
                    body.append("<br>");
                }
            } else {
                // skip ref link
                if (isReflinkLine(line))
                    break;
                body.append(line.replaceAll("  ", "&nbsp;&nbsp;"));
                body.append("<br>");
            }
        }
        deleteLastBR(quote, body);
        if (quote.length() != 0) {
            p.setQuotationText(quote.substring(0, quote.length() - 4));
        }
        p.setBodyText(body.toString());
    }

    protected String extractFollowLink(String s) {
        int start = s.indexOf("href=\"");
        int end = s.indexOf('"', start + 6);
        if (start == -1 || end == -1)
            return "";
        return s.substring(start + 6, end);
    }

    protected String extractThreadLink(String s) {
        if (!s.contains("m=t"))
            return "";
        int start = s.indexOf("href=\"");
        int end = s.indexOf('"', start + 6);
        if (start == -1 || end == -1)
            return "";
        return s.substring(start + 6, end);
    }

    public PostParams parseFollowPost() throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream,
                charset));
        PostParams pp = createPostParams();
        try {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.toLowerCase().contains("<form"))
                    break;
            }
            if (line == null)
                return pp;
            splitInputTag(r.readLine(), pp, false);
            line = r.readLine();
            if (!line.toLowerCase().contains("<input"))
                line = r.readLine();
            splitInputTag(line, pp, false);
            splitInputTag(r.readLine(), pp, false);
            splitInputTag(r.readLine(), pp, false);

            while ((line = r.readLine()) != null) {
                if (line.toLowerCase().contains("<textarea")) {
                    if (line.endsWith(">"))
                        line = r.readLine(); // II
                    StringBuffer b = new StringBuffer();
                    do {
                        if (line.toLowerCase().contains("</textarea>"))
                            break;
                        b.append(line.substring(line.indexOf('>') + 1)
                                .replaceAll("  ", "&nbsp;&nbsp;"));
                        b.append("<br>");
                    } while ((line = r.readLine()) != null);
                    pp.setText(b.toString());
                    break;
                }
            }
            pp.setProtectCode(parseProtectCode(r));
            while ((line = r.readLine()) != null) {
                if (line.toLowerCase().contains("<input")) {
                    splitInputTag(line, pp, false);
                }
            }
        } finally {
            r.close();
        }
        return pp;
    }

    protected void splitInputTag(String s, PostParams pp, boolean readPC) {
        String s1 = s.substring(0, s.indexOf('>'));
        int start = s1.indexOf("name=\"") + 6;
        int end = s1.indexOf('"', start);
        if (start < 6 || end == -1)
            return;
        String n = s1.substring(start, end);
        if (!readPC && n.equals(pp.getProtectCodeKey())) // skip protect code
            return;

        start = s1.indexOf("value=\"") + 7;
        end = s1.indexOf('"', start);
        if (start < 7 || end == -1)
            return;
        String v;
        if (start == end)
            v = "";
        else
            v = s1.substring(start, end);
        pp.put(n, v);
    }

    public ArrayList<Post> parseThread(LoadPostHandler postHandler) throws IOException {
        ArrayList<Post> ret = new ArrayList<Post>(30);
        BufferedReader r = new BufferedReader(new InputStreamReader(stream,
                charset));
        String line = SkipToPost(r);
        if (line == null)
            return ret;

        try {
            while (line != null) {
                if (line.length() == 0)
                    break;
                Post p = ParsePost(line, r);
                if (p != null) {
                    ret.add(p);
                    if (postHandler != null)
                        postHandler.add(p);
                }
                line = r.readLine();

            }
        } finally {
            if (postHandler != null)
                postHandler.add(null);
        }
        return ret;
    }

    public PostParams createPostParams() {
        return new PostParams();
    }

    protected void deleteLastBR(StringBuilder quote, StringBuilder body) {
        final int BR_LEN = 4;
        StringBuilder target = null;
        if (body.length() == 0)
            target = quote;
        else
            target = body;
        while (true) {
            int len = target.length();
            int lastbr = target.lastIndexOf("<br>");
            if (lastbr == -1 || lastbr != len - BR_LEN)
                break;
            target.delete(len - BR_LEN,  len);
        }
    }

    protected boolean isReflinkLine(final String s) {
        if (s.startsWith("<A href") || s.startsWith("<a href")) {
            if (s.contains(">参考："))
                return true;
        }
        return false;
    }
}
