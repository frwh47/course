package my.jedis;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.UUID;

public class ShardTest {
    private static ShardedJedisPool pool;

    @BeforeClass
    public static void beforeClass() {
        pool = JedisPoolFactory.createShardedJedisPool();
    }

    @AfterClass
    public static void afterClass() {
        if (pool != null) {
            pool.close();
        }
    }

    @Test
    public void writeNew() {
        final int RUN_TIMES = 5;
        final int BATCH_SIZE = 20;

        try (ShardedJedis jedis = pool.getResource()) {
            for (int i = 0; i < RUN_TIMES; i++) {
                for (int j = 0; j < BATCH_SIZE; j++) {
                    String key = UUID.randomUUID().toString();
                    jedis.set(key, key);
                }
            }
        }
    }
}
