package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BBSManager {
    private ArrayList<BBSInfo> bbsList = new ArrayList<BBSInfo>();

    public BBSManager() {
    }

    public List<BBSInfo> getBBSList() {
        return bbsList;
    }

    public void loadFromStream(InputStream s) throws IOException {
        if (s == null)
            return;
        BufferedReader r = new BufferedReader(new InputStreamReader(s, "UTF-8"), 1024 * 8);
        String line;
        while ((line = r.readLine()) != null) {
            String[] sl = line.split(",");
            if (sl.length != 4)
                break;
            bbsList.add(new BBSInfo(sl[0].trim(), sl[1].trim(), sl[2].trim(),
                    sl[3].trim().equals("useid")));
        }
    }

    public BBSInfo getBBS(String shortName) {
        for (BBSInfo i : bbsList) {
            if (i.getShortName().equals(shortName))
                return i;
        }
        return null;
    }
}
