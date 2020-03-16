package my.jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带缓存的服务示例
 * 防穿透，防击穿，防雪崩
 */
public class AgainstFloodService extends AbstractService {
    private final String NONE = "NONE";
    private final int LIFE_TIME_FOR_NONE_TEN_SECONDS = 10;
    private final Map<String, String> keys = new ConcurrentHashMap<>();

    public AgainstFloodService() {
        super(new Cache(), new Database());
    }

    public String get(String key) {
        if (!valid(key)) {
            return NONE;
        }

        String value = cache.get(key);
        if (value != null) {
            return value;
        }

        keys.putIfAbsent(key, key);
        String lock = keys.get(key);
        synchronized (lock) {
            value = cache.get(key);
            if (value != null) {
                return value;
            }

            value = database.get(key);
            if (value == null) {
                cache.set(key, NONE, LIFE_TIME_FOR_NONE_TEN_SECONDS);
                return NONE;
            }
        }
        return value;
    }

    private boolean valid(String key) {
        return (key != null) && (key.length() > 0) && ('-' != key.charAt(0));
    }
}
