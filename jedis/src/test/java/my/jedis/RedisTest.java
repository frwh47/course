package my.jedis;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisTest {
    private static final String HOST = "10.105.23.70";
    private static final int PORT = 6379;
    /**
     * connection timeout and socket timeout, in milliseconds
     */
    private static final int TIMEOUT = 3000;
    private static final String PASSWORD = "yourpassword";
    private static final String OK = "OK";
    private static final int SECONDS_60 = 60;
    private static final Long ONE = 1L;
    private static final Long ZERO = 0L;
    private static final Long NONE = -1L;
    private static final Long NOT_EXISTS = -2L;

    private static JedisPool pool;

    @BeforeClass
    public static void beforeClass() {
        createPool();
    }

    private static void createPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1024);
        config.setMaxIdle(8);
        config.setMinIdle(4);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(false);
        config.setTestOnReturn(false);
        pool = new JedisPool(config, HOST, PORT, TIMEOUT, PASSWORD, false);
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

    interface MinArray {
        void put(int index, int value);

        int get(int index);

        int min();

        int[] array();
    }

    @Test
    public void keepPing() throws InterruptedException {
        String key = "a";
        while (true) {
            try (Jedis jedis = pool.getResource()) {
                String pong = jedis.ping();
                String msg = String.format("%s %s %s = %s", new Date(), pong, key, jedis.get(key));
                System.out.println(msg);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            TimeUnit.SECONDS.sleep(3);
        }
    }

    @Test
    public void writeNew() {
        final int RUN_TIMES = 1000 * 10;
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


    @Test
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

    @Test
    public void update() throws InterruptedException {
        final int BATCH_SIZE = 3;

        try (Jedis jedis = new Jedis(HOST, PORT);
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
