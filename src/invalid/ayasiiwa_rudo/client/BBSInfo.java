package invalid.ayasiiwa_rudo.client;

public class BBSInfo {
    private String name;
    private String url;
    private String shortName;
    private boolean useNextPageMessageID;

    BBSInfo() {
    }

    BBSInfo(String name, String url, String shortName, boolean useNextPageMessageID) {
        this.name = name;
        this.url = url;
        this.shortName = shortName;
        this.useNextPageMessageID = useNextPageMessageID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isUseNextPageMessageID() {
        return useNextPageMessageID;
    }

    public void setUseNextPageMessageID(boolean useNextPageMessageID) {
        this.useNextPageMessageID = useNextPageMessageID;
    }
}
