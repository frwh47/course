package my.jedis;

import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.JedisCluster;

import java.util.UUID;

public class ClusterTest extends BaseTest {
    @Test
    public void writeNew() {
        final int RUN_TIMES = 10;

        try (JedisCluster jedis = JedisPoolFactory.createJedisCluster()) {
            for (int i = 0; i < RUN_TIMES; i++) {
                String key = UUID.randomUUID().toString();
                Assert.assertEquals(OK, jedis.set(key, key));
            }
        }
    }
}
