package my.jedis;

/**
 * 带缓存的服务示例
 */
public class NormalService extends AbstractService {
    public NormalService() {
        super(new Cache(), new Database());
    }

    public NormalService(Database database) {
        super(new Cache(), database);
    }

    /**
     * 先查缓存，命中返回结果
     * 未命中，查数据库，命中，缓存结果，返回结果
     * 未命中，返回空
     *
     * @param key
     * @return
     */
    public String get(String key) {
        String value = cache.get(key);
        if (value != null) {
            return value;
        }

        value = database.get(key);
        if (value != null) {
            cache.set(key, value);
        }
        return value;
    }
}
