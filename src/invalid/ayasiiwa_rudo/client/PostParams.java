package invalid.ayasiiwa_rudo.client;

import java.util.HashMap;

public class PostParams {
    private HashMap<String, String> params = new HashMap<String, String>();

    public PostParams() {
        params.put("m", "p");
        params.put("k", "あ"); // kanji
        params.put("g", "checked");
        params.put("d", "0");
        params.put("a", "checked");
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public String getTitle() {
        return params.get(getTitleKey());
    }

    public String getText() {
        return params.get(getTextKey());
    }

    public String getName() {
        return params.get(getNameKey());
    }

    public String getMail() {
        return params.get(getMailKey());
    }

    public String getLink() {
        return params.get(getLinkKey());
    }

    public String getProtectCode() {
        return params.get(getProtectCodeKey());
    }

    public void setTitle(String s) {
        params.put(getTitleKey(), s);
    }

    public void setText(String s) {
        params.put(getTextKey(), s);
    }

    public void setName(String s) {
        params.put(getNameKey(), s);
    }

    public void setMail(String s) {
        params.put(getMailKey(), s);
    }

    public void setLink(String s) {
        params.put(getLinkKey(), s);
    }

    public void setProtectCode(String s) {
        params.put(getProtectCodeKey(), s);
    }

    public void put(String key, String val) {
        params.put(key, val);
    }

    public String getTitleKey() {
        return "t";
    }

    public String getTextKey() {
        return "v";
    }

    public String getMailKey() {
        return "i";
    }

    public String getNameKey() {
        return "u";
    }

    public String getLinkKey() {
        return "l";
    }

    public String getProtectCodeKey() {
        return "pc";
    }

    public void prepare() {
    }
}
