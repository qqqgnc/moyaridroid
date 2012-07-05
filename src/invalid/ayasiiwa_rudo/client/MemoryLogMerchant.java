package invalid.ayasiiwa_rudo.client;

import java.util.ArrayList;
import java.util.List;

public class MemoryLogMerchant extends LogMerchant {
    private ArrayList<Post> log = new ArrayList<Post>();

    @Override
    public Post getLog(String name) {
        for (Post p : log) {
            if (p.getName() == name)
                return p;
        }
        return null;
    }

    @Override
    public List<Post> getLogs(int start, int count) {
        if (start == 0 && count < 1)
            return log;
        int len = log.size();
        if (start > len)
            return new ArrayList<Post>();
        int last = start + count > len ? len : start + count;
        return log.subList(start, last);
    }

    @Override
    public int size() {
        return log.size();
    }

    @Override
    public int storeLog(List<Post> log, int index) {
        if (log == null)
            return 0;
        if (index < 0)
            this.log.addAll(log);
        else
            this.log.addAll(index, log);
        return log.size();
    }

    @Override
    public void clear() {
        log.clear();
    }
}
