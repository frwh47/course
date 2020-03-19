package my.jedis;

import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;

public class RedisSentinelToolTest {
    private static RedisSentinelTool pool;

    @BeforeClass
    public static void beforeClass() {
        final String masterName = "mymaster";
        final String masterPassword = "yourpassword";
        final String HOST = "10.105.23.70";
        Set<HostAndPort> sentinels = new HashSet<>();
        for (int port = 27001; port <= 27003; port++) {
            sentinels.add(new HostAndPort(HOST, port));
        }
        pool = new RedisSentinelTool(masterName, sentinels, masterPassword);
    }

    @Test
    public void t1() {
        String key = "a";
        try (Jedis jedis = pool.getMaster()) {
            jedis.del(key);
        }
        try (Jedis jedis = pool.getSalve()) {
            jedis.exists(key);
        }
    }
}