package invalid.ayasiiwa_rudo.client;

import java.io.Serializable;

public class Post implements Serializable {
    /**
	 *
	 */
    private static final long serialVersionUID = 6816788735835570985L;
    private String name = "";
    private String author = "";
    private String title = "";
    private String quotationText = "";
    private String bodyText = "";
    private String postTime = "";
    private String followLink = "";
    private String threadLink = "";
    private transient CharSequence quotationTextBuffer = null;
    private transient CharSequence bodyTextBuffer = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setQuotationText(String quotationText) {
        this.quotationText = quotationText;
    }

    public String getQuotationText() {
        return quotationText;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getFollowLink() {
        return followLink;
    }

    public void setFollowLink(String followLink) {
        this.followLink = followLink;
    }

    public String getThreadLink() {
        return threadLink;
    }

    public void setThreadLink(String threadLink) {
        this.threadLink = threadLink;
    }

    public CharSequence getQuotationTextBuffer() {
        return quotationTextBuffer;
    }

    public CharSequence getBodyTextBuffer() {
        return bodyTextBuffer;
    }

    public void setQuotationTextBuffer(CharSequence quotationTextBuffer) {
        this.quotationTextBuffer = quotationTextBuffer;
    }

    public void setBodyTextBuffer(CharSequence bodyTextBuffer) {
        this.bodyTextBuffer = bodyTextBuffer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o instanceof Post) {
            return ((Post) o).name.equals(name);
        }
        return false;
    }
}

