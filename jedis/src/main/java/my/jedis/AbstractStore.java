package my.jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AbstractStore {
    protected Map<String, String> map = new ConcurrentHashMap<>();
    protected AtomicInteger readTimes = new AtomicInteger();
    protected AtomicInteger writeTimes = new AtomicInteger();

    public String get(String key) {
        readTimes.incrementAndGet();
        return map.get(key);
    }

    public void set(String key, String value) {
        set(key, value, Integer.MAX_VALUE);
    }

    public void set(String key, String value, int lifeTimeInSeconds) {
        if (lifeTimeInSeconds > 0) {
            writeTimes.incrementAndGet();
            map.put(key, value);
        }
    }

    public int getReadTimes() {
        return readTimes.intValue();
    }

    public int getWriteTimes() {
        return writeTimes.intValue();
    }
}
