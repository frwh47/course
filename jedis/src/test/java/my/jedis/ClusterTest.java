package my.jedis;

import org.junit.Test;
import redis.clients.jedis.JedisCluster;

import java.util.UUID;

public class ClusterTest {
    @Test
    public void writeNew() {
        final int RUN_TIMES = 50;
        final int BATCH_SIZE = 20;

        try (JedisCluster jedis = JedisPoolFactory.createJedisCluster()) {
            for (int i = 0; i < RUN_TIMES; i++) {
                for (int j = 0; j < BATCH_SIZE; j++) {
                    String key = UUID.randomUUID().toString();
                    jedis.set(key, key);
                }
            }
        }
    }
}
