package invalid.ayasiiwa_rudo.client;

import java.util.HashMap;

public class IIPostParams extends PostParams {
    public IIPostParams() {
        HashMap<String, String> p = getParams();
        p.put("mode", "post");
        p.put("gzip", "checked");
        p.put("autolink", "checked");
        p.put("linkurl", "http://");
        p.put("msgdisp", "1");
    }

    @Override
    public String getTitleKey() {
        return "title";
    }

    @Override
    public String getTextKey() {
        return "message";
    }

    @Override
    public String getMailKey() {
        return "mailaddr";
    }

    @Override
    public String getNameKey() {
        return "username";
    }

    @Override
    public String getLinkKey() {
        return "linkurl";
    }

    @Override
    public String getProtectCodeKey() {
        return "protect";
    }
}
