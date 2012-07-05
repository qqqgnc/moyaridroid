package invalid.ayasiiwa_rudo.client;

public class NozomiParser extends BBSParser {
    @Override
    public PostParams createPostParams() {
        return new NozomiPostParams();
    }
}
