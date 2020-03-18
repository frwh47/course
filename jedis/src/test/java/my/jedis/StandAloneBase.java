package my.jedis;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolAbstract;

public abstract class StandAloneBase {
    protected static final String OK = "OK";
    protected static final int SECONDS_60 = 60;
    protected static final Long ONE = 1L;
    protected static final Long ZERO = 0L;
    protected static final Long NONE = -1L;
    protected static final Long NOT_EXISTS = -2L;

    protected static JedisPoolAbstract pool;

    @BeforeClass
    public static void beforeClass() {
        System.out.println("StandAloneBase.beforeClass");
        pool = JedisPoolFactory.createPool();
    }

    @AfterClass
    public static void afterClass() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    @Before
    public void before() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushAll();
        }
    }
}
