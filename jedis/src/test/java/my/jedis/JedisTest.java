package my.jedis;

import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

public class JedisTest {
    private static final String host = "10.5.72.54";
    private static final int port = 6379;
    private static final int timeout = 3000;
    private static final String password = "123456";

    private void validate(Jedis jedis) {
        Assert.assertEquals("OK", jedis.auth(password));
        Assert.assertEquals("PONG", jedis.ping());

        String key = "key-" + UUID.randomUUID().toString();
        String value = "value-" + UUID.randomUUID().toString();
        Assert.assertEquals("OK", jedis.set(key, value, SetParams.setParams().ex(60)));
        Assert.assertEquals(value, jedis.get(key));
    }

    @Test
    public void t1() {
        try (Jedis jedis = new Jedis(host, port)) {
            validate(jedis);
        }
    }

    @Test
    public void testPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(20);
        config.setMinIdle(2);
        config.setMaxWaitMillis(3000);
        config.setTestWhileIdle(true);
        config.setBlockWhenExhausted(false);
        JedisPool pool = new JedisPool(config, host, port, timeout, password);

        for (int i = 0; i < 100; i++) {
            try (Jedis jedis = pool.getResource()) {
                validate(jedis);
            }
        }
    }
}
