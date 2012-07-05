package invalid.ayasiiwa_rudo.client;

import java.io.BufferedReader;
import java.io.IOException;

public class TetsudoParser extends BBSParser {
    @Override
    protected String getParticipants(BufferedReader r) throws IOException {
        String line;
        while ((line = r.readLine()) != null) {
            int pos = line.indexOf("現在の乗客 : ");
            if (pos > -1) {
                int e = line.indexOf("名", pos + 8);
                if (e < 0)
                    return "";
                return line.substring(pos, e + 1);
            }
        }
        return "";
    }
}
