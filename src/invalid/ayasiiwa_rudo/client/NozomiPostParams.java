package invalid.ayasiiwa_rudo.client;

import java.util.Random;

public class NozomiPostParams extends PostParams {
    //private static final String KEY = "";
    //private static final String URL =
    //    "https://www.googleapis.com/language/translate/v2?source=ja&target=en&key=";

    @Override
    public void setLink(String s) {
        if (s == null || s.length() == 0) {
            Random r = new Random();
            int n = r.nextInt(10);
            if (n == 0) {
                super.setLink("っふ");
                return;
            } else if (n == 1) {
                super.setLink("阻止");
                return;
            }
        }
        super.setLink(s);
    }
/*
    @Override
    public void prepare() {
        try {
            if (KEY.length() == 0)
                return;
            // only one liner.
            if (getText().contains("\n"))
                return;
            Random r = new Random();
            int n = r.nextInt(7);
            if (n != 0)
                return;
            DefaultHttpClient hc = HttpUtil.buildHttpClient();
            StringBuilder url = new StringBuilder(500);
            url.append(URL);
            url.append(KEY);
            url.append("&q=");
            url.append(URLEncoder.encode(getText(),"UTF-8"));
            // sometime rise UnknownHostException, in emulator.
            HttpGet m = new HttpGet(url.toString());
            HttpResponse res = hc.execute(m);
            int code = res.getStatusLine().getStatusCode();
            if (code != HttpStatus.SC_OK) {
                //Log.d("STRANGE_WORLD", res.getStatusLine().toString());
                return;
            }
            String entity = EntityUtils.toString(res.getEntity());
            JSONObject root = new JSONObject(entity);
            JSONObject data = root.getJSONObject("data");
            JSONArray trans = data.getJSONArray("translations");
            if (trans.length() < 1)
                return;
            String text = trans.getJSONObject(0).getString("translatedText");
            if (text != null && text.length() != 0)
                setText(text);
        } catch (Exception e){
            //Log.d("STRANGE_WORLD", e.toString());
            //ignore errors.
        }
    }
*/
}
