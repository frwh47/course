package my.jedis;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.SetParams;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StringTest extends BaseTest {
    private static JedisPoolAbstract pool;

    @BeforeClass
    public static void beforeClass() {
        pool = JedisPoolFactory.createPool();
    }

    @AfterClass
    public static void afterClass() {
        if (pool != null) {
            pool.close();
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

    @Test
    public void expire() {
        final String KEY = UUID.randomUUID().toString();
        final String VALUE = UUID.randomUUID().toString();

        try (Jedis jedis = pool.getResource()) {
            Assert.assertFalse(jedis.exists(KEY));
            Assert.assertEquals(NOT_EXISTS, jedis.ttl(KEY));

            Assert.assertEquals(OK, jedis.set(KEY, KEY));
            Assert.assertEquals(NONE, jedis.ttl(KEY));

            Assert.assertEquals(ONE, jedis.expire(KEY, SECONDS_60));
            Assert.assertTrue(0 < jedis.ttl(KEY));
            Assert.assertTrue(jedis.ttl(KEY) <= SECONDS_60);

            Assert.assertEquals(OK, jedis.set(KEY, VALUE));
            Assert.assertEquals(NONE, jedis.ttl(KEY));
            Assert.assertEquals(ONE, jedis.del(KEY));
        }
    }

    @Test
    public void setNxEx() {
        final String KEY = UUID.randomUUID().toString();
        final String VALUE = UUID.randomUUID().toString();
        SetParams nxEx = SetParams.setParams().nx().ex(SECONDS_60);

        try (Jedis jedis = pool.getResource()) {
            Assert.assertNull(jedis.get(KEY));

            Assert.assertEquals(OK, jedis.set(KEY, KEY, nxEx));
            Assert.assertEquals(KEY, jedis.get(KEY));
            Assert.assertTrue(0 < jedis.ttl(KEY));
            Assert.assertTrue(jedis.ttl(KEY) <= SECONDS_60);

            Assert.assertNull(jedis.set(KEY, VALUE, nxEx));
            Assert.assertEquals(KEY, jedis.get(KEY));
            Assert.assertEquals(ONE, jedis.del(KEY));
        }
    }

    @Test
    public void incr() {
        final String KEY = "counter";
        final long TIMES = 10;
        try (Jedis jedis = pool.getResource()) {
            for (long num = 1; num <= TIMES; num++) {
                Assert.assertEquals(num, jedis.incr(KEY).longValue());
            }
            Assert.assertEquals(String.valueOf(TIMES), jedis.get(KEY));

            jedis.del(KEY);
            for (long num = 1; num <= TIMES; num++) {
                jedis.incrBy(KEY, num);
            }
            long expected = TIMES * (TIMES + 1) / 2;
            Assert.assertEquals(expected, Long.parseLong(jedis.get(KEY)));
        }
    }

    @Test
    public void bit() {
        String key = "bit";
        final int TIMES = 100;
        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < TIMES; i++) {
                //setbit 返回的是该位修改之前的值
                Assert.assertFalse(jedis.setbit(key, i, true));
                Assert.assertEquals(i + 1, jedis.bitcount(key).longValue());
            }

            for (int i = 0; i < TIMES; i++) {
                Assert.assertTrue(jedis.setbit(key, i, false));
                Assert.assertEquals(TIMES - 1 - i, jedis.bitcount(key).longValue());
            }
        }
    }

    @Test
    public void hyper() {
        String tom = "tom";
        String mike = "mike";
        String tomAndMike = tom + mike;
        try (Jedis jedis = pool.getResource()) {
            jedis.del(tom, mike);
            jedis.pfadd(tom, "Java", "Redis");
            Assert.assertEquals(2, jedis.pfcount(tom));

            jedis.pfadd(mike, "Redis", "Golang", "Python", "Python");
            Assert.assertEquals(3, jedis.pfcount(mike));

            Assert.assertEquals(4, jedis.pfcount(tom, mike));
            jedis.pfmerge(tomAndMike, tom, mike);
            Assert.assertEquals(4, jedis.pfcount(tomAndMike));
        }
    }


    interface MinArray {
        void put(int index, int value);

        int get(int index);

        int min();

        int[] array();
    }

    @Test
    public void keepWriting() throws InterruptedException {
        try (Jedis jedis = pool.getResource()) {
            while (true) {
                String key = UUID.randomUUID().toString();
                jedis.set(key, key);
                TimeUnit.MILLISECONDS.sleep(100);
            }
        }
    }

    @Test
    public void writeNew() {
        final int RUN_TIMES = 100;
        final int BATCH_SIZE = 100;

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

    //    @Test
    public void updateExists() throws InterruptedException {
        final int BATCH_SIZE = 3;
        final String key = String.valueOf(1);
        int counter = 1;
        try (Jedis jedis = pool.getResource();
             Pipeline pipeline = jedis.pipelined()) {
            while (true) {
                Response<String> response = pipeline.multi();
                System.out.println(response);
                for (int j = 1; j <= BATCH_SIZE; j++) {
                    response = pipeline.set(key, UUID.randomUUID().toString());
                    System.out.println(response);
                }
                pipeline.exec();
                System.out.println(new Date() + ", " + counter);
                counter++;
                TimeUnit.SECONDS.sleep(1);
            }
        }
    }

    //    @Test
    public void update() throws InterruptedException {
        final int BATCH_SIZE = 3;

        try (Jedis jedis = pool.getResource();
             Pipeline pipeline = jedis.pipelined()) {
            while (true) {
                pipeline.multi();
                for (int j = 1; j <= BATCH_SIZE; j++) {
                    String key = String.valueOf(j);
                    String value = UUID.randomUUID().toString();
                    pipeline.set(key, value);
                }
                pipeline.exec();
                System.out.println(new Date());
                TimeUnit.MILLISECONDS.sleep(100);
            }
        }
    }
}
