package my.jedis;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class BaseTest {
    private static final String HOST = "10.105.23.70";
    private static final int PORT = 6379;
    /**
     * connection timeout and socket timeout, in milliseconds
     */
    private static final int TIMEOUT = 3000;
    private static final String PASSWORD = "yourpassword";
    protected static final String OK = "OK";
    protected static final int SECONDS_60 = 60;
    protected static final Long ONE = 1L;
    protected static final Long ZERO = 0L;
    protected static final Long NONE = -1L;
    protected static final Long NOT_EXISTS = -2L;

    protected static JedisPool pool;

    @BeforeClass
    public static void beforeClass() {
        createPool();
    }

    private static void createPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1024);
        config.setMaxIdle(8);
        config.setMinIdle(4);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(false);
        config.setTestOnReturn(false);
        pool = new JedisPool(config, HOST, PORT, TIMEOUT, PASSWORD, false);
    }

    @AfterClass
    public static void afterClass() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

}
