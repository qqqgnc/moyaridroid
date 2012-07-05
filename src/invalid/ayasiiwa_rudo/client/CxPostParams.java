package invalid.ayasiiwa_rudo.client;

import java.util.HashMap;

public class CxPostParams extends IIPostParams {
    CxPostParams() {
        super();
        HashMap<String, String> p = getParams();
        p.put("image", "-1"); //右下なんて無かった
    }

    @Override
    public String getProtectCodeKey() {
        return "pc";
    }
}
