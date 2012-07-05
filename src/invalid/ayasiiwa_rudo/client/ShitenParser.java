package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.IOException;

public class ShitenParser extends BBSParser {
    @Override
    protected void readText(BufferedReader r, Post p) throws IOException {
        super.readText(r, p);
        p.setQuotationText(replaceBBSLink(p.getQuotationText()));
        p.setBodyText(replaceBBSLink(p.getBodyText()));
    }

    private String replaceBBSLink(String s) {
        return s.replaceAll("<A href=\"\\./r\\.cgi\\?", "<A href=\"");
    }
}
