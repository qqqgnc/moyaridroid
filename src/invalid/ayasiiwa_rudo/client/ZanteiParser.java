package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class ZanteiParser extends BBSParser {
    private static final String ZANTEI_HOST = "http://zangzang.poox360.net";

    public ZanteiParser() {
        super();
    }

    public ZanteiParser(InputStream s, String Charset) {
        super(s, Charset);
    }

    @Override
    protected void skipToTextHead(BufferedReader r) throws IOException {
        r.readLine();
        r.readLine();
        r.readLine();
    }

    private String extractAuthorInner(String s, String start_tag) {
        int start = s.indexOf(start_tag);
        int end = s.lastIndexOf("</b>");
        if (start == -1 || end == -1)
            return "";
        String ret = s.substring(start + start_tag.length(), end);
        if (ret.equals(" "))
            return "";
        return ret;
    }

    // <font size="+1" color="#fffffe"><b class="ZANGZANGKijiTitle">＞hoge</b></font> -> hoge
    @Override
    protected String extractTitle(String s) {
        return extractAuthorInner(s, "ZANGZANGKijiTitle\">");
    }

    @Override
    protected String extractAuthor(String s) {
        return extractAuthorInner(s, "ZANGZANGKijiAuthor\">");
    }

    @Override
    protected void readText(BufferedReader r, Post p) throws IOException {
        super.readText(r, p);
        p.setQuotationText(replaceBBSLink(p.getQuotationText()));
        p.setBodyText(replaceBBSLink(p.getBodyText()));
    }

    @Override
    protected String extractFollowLink(String s) {
        String r = super.extractFollowLink(s);
        if (r.length() == 0)
            return r;
        return ZANTEI_HOST + r;
    }

    @Override
    protected String extractThreadLink(String s) {
        String r = super.extractThreadLink(s);
        if (r.length() == 0)
            return r;
        return ZANTEI_HOST + r;
    }

    private String replaceBBSLink(String s) {
        return s.replaceAll("<a href=\"/cgi-bin/bbs\\.cgi\\?",
                "<a href=\"http://zangzang.poox360.net/cgi-bin/bbs.cgi?");
    }
}
