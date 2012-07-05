package invalid.ayasiiwa_rudo.client;

public class BBSParserFactory {
    public static BBSParser createParser(String url) {
        if (url.contains("http://zangzang.poox360.net/cgi-bin/bbs.cgi"))
            return new ZanteiParser();
        else if (url.contains("http://210.150.243.7/cgi-bin/bbs.cgi"))
            return new IIParser();
        else if (url.contains("http://yotsuba.saiin.net/~testcard/swr/bbs.cgi"))
            return new TetsudoParser();
        else if (url.contains("http://hontena.s231.xrea.com/index.cgi"))
            return new ShitenParser();
        else if (url.contains("http://amari.ath.cx/bbs/bbs.cgi")) {
            return new NozomiParser();
        } else if (url.contains("http://www.strangeworld.ne.jp/cgi-bin/bbs/bbs.cgi")) {
            return new CxParser();
        }
        return new BBSParser();
    }
}
