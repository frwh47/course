package my.jedis;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SentinelTest {
    private static final String host = "10.105.23.70";
    private static final int port = 7001;
    private static JedisSentinelPool pool;

    @BeforeClass
    public static void createJedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1024);
        config.setMaxIdle(8);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setTestOnReturn(true);
        String masterName = "mymaster";
        Set<String> sentinels = new HashSet<String>();
        sentinels.add(new HostAndPort(host, port).toString());
        sentinels.add(new HostAndPort(host, port + 1).toString());
        sentinels.add(new HostAndPort(host, port + 2).toString());
        String password = "1234@abcd";
        pool = new JedisSentinelPool(masterName, sentinels, config, 3000);
    }

    @Test
    public void connectSentinel() throws InterruptedException {
        String key = "a";
        Assert.assertNotNull(pool);
        while (true) {
            String value = "a1 @ " + new Date();
            try (Jedis jedis = pool.getResource()) {
                jedis.set(key, value);
                String pong = jedis.ping();
                String msg = String.format("%s : %s %s = %s", new Date(), pong, key, jedis.get(key));
                System.out.println(msg);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            TimeUnit.SECONDS.sleep(3);
        }
    }
}
