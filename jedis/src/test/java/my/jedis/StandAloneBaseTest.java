package my.jedis;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.JedisPoolConfig;

public abstract class StandAloneBaseTest extends BaseTest {
    private static final String HOST = "10.105.23.70";
    private static final int PORT = 6379;
    protected static JedisPoolAbstract pool;

    @BeforeClass
    public static void beforeClass() {
        JedisPoolConfig config = createPoolConfig();
        pool = new JedisPool(config, HOST, PORT, CONNECT_TIMEOUT, SO_TIMEOUT,
                PASSWORD, 0, "UT");
    }

    @AfterClass
    public static void afterClass() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

}
