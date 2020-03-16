package my.jedis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AccessRedisCluster {
    public static void main(String[] args) {
        Set<HostAndPort> hps = new HashSet<>();
        for (int port = 7001; port <= 7006; port++) {
            hps.add(new HostAndPort("39.100.242.198", port));
//            hps.add(new HostAndPort("192.168.0.97", port));
        }
        access(hps, "f75B1X609aJ1");
    }

    private static void access(Set<HostAndPort> hps, String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(20);
        config.setMinIdle(2);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setBlockWhenExhausted(false);

        JedisCluster jc = new JedisCluster(hps, 5000, 5000, 5,
                password, config);

        for (int i = 0; i < 100; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            jc.set(key, value, SetParams.setParams().ex(60));
            System.out.println(jc.get(key));
        }
        System.out.println("Access succeed, " + Arrays.toString(hps.toArray()));
    }
}
