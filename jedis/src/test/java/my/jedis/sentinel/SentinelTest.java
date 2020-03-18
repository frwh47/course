package my.jedis.sentinel;

import my.jedis.BaseTest;
import org.junit.BeforeClass;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

public class SentinelTest extends BaseTest {
    protected static JedisSentinelPool pool;

    @BeforeClass
    public void beforeClass() {
        JedisPoolConfig config = createPoolConfig();
        String masterName = "mmm";
        Set<String> sentinels = new HashSet<>();

        pool = new JedisSentinelPool(masterName, sentinels, config,
                CONNECT_TIMEOUT, SO_TIMEOUT,
                PASSWORD, 0, "UT");
    }
}
