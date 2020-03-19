package my.jedis.sentinel;

import my.jedis.BaseTest;
import my.jedis.JedisPoolFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Pipeline;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SentinelTest extends BaseTest {
    protected static JedisSentinelPool pool;
//    protected static RedisSentinelTool pool;

    @BeforeClass
    public static void beforeClass() {
//        pool = JedisPoolFactory.createRedisSentinelTool();
        pool = JedisPoolFactory.createSentinelPool();
    }

    @AfterClass
    public static void afterClass() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }

    @Test
    public void set() {
        final String KEY = UUID.randomUUID().toString();
        final String VALUE = UUID.randomUUID().toString();

        try (Jedis jedis = pool.getResource()) {
            Assert.assertFalse(jedis.exists(KEY));
            Assert.assertNull(jedis.get(KEY));
            Assert.assertEquals(ZERO, jedis.del(KEY));

            Assert.assertEquals(OK, jedis.set(KEY, VALUE));
            Assert.assertEquals(VALUE, jedis.get(KEY));

            Assert.assertEquals(ONE, jedis.del(KEY));
            Assert.assertFalse(jedis.exists(KEY));
        }
    }

    //    @Test
    public void write() {
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                writeNew();
            } catch (Exception ex) {
                System.out.println(new Date() + ", " + ex.getMessage());
            }
        }
    }

    @Test
    public void writeNew() {
        final int RUN_TIMES = 5;
        final int BATCH_SIZE = 20;

        try (Jedis jedis = pool.getResource();
             Pipeline pipeline = jedis.pipelined()) {
            for (int i = 0; i < RUN_TIMES; i++) {
                pipeline.multi();
                for (int j = 0; j < BATCH_SIZE; j++) {
                    String key = UUID.randomUUID().toString();
                    pipeline.set(key, key);
                }
                pipeline.exec();
            }
        }
    }
}