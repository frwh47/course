package my.jedis;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServiceTest {
    private final int USER_COUNT = 1000 * 10;
    private final int REQUEST_TIMES = 1000 * 10;
    private final int FEW_KEYS = 100;
    private AbstractService service;
    private ExecutorService pool;

    @Before
    public void before() {
        pool = Executors.newFixedThreadPool(64);
    }

    @After
    public void after() {
        await();
        service = null;
        pool = null;
    }

    private void await() {
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    /**
     * 缓存穿透，大流量请求的key，不在缓存中，也不在数据库中
     */
    @Test
    public void penetration() {
        service = new NormalService();
        Runnable task = () -> {
            for (int i = 0, key = 0; i < REQUEST_TIMES; i++, key++) {
                if (key >= FEW_KEYS) {
                    key = 0;
                }
                service.get(String.valueOf(key));
            }
        };
        for (int i = 0; i < USER_COUNT; i++) {
            pool.submit(task);
        }

        await();
        Assert.assertEquals(USER_COUNT * REQUEST_TIMES, service.getTimesOfReadCache());
        Assert.assertEquals(0, service.getTimesOfWriteCache());
        Assert.assertEquals(USER_COUNT * REQUEST_TIMES, service.getTimesOfReadDatabase());
        Assert.assertEquals(0, service.getTimesOfWriteDatabase());
    }

    @Test
    public void againstFlood() {
        service = new AgainstFloodService();
        Runnable task = () -> {
            //invalid key
            for (int i = 0, key = -1; i < REQUEST_TIMES; i++, key--) {
                if (key < -FEW_KEYS) {
                    key = -1;
                }
                service.get(String.valueOf(key));
            }
            for (int i = 0, key = 1; i < REQUEST_TIMES; i++, key++) {
                if (key > FEW_KEYS) {
                    key = 1;
                }
                service.get(String.valueOf(key));
            }
        };
        for (int i = 0; i < USER_COUNT; i++) {
            pool.submit(task);
        }
        await();
        Assert.assertTrue(USER_COUNT * REQUEST_TIMES + FEW_KEYS <= service.getTimesOfReadCache());
        Assert.assertEquals(FEW_KEYS, service.getTimesOfWriteCache());
        Assert.assertEquals(FEW_KEYS, service.getTimesOfReadDatabase());
        Assert.assertEquals(0, service.getTimesOfWriteDatabase());
    }

    /**
     * 缓存击穿，大流量请求的key，key不在缓存中，在数据库中
     */
    @Test
    public void breakdown() {
        Database db = new Database();
        for (int i = 1; i <= FEW_KEYS; i++) {
            String key = String.valueOf(i);
            db.set(key, key);
        }
        service = new NormalService(db);
        String msg = String.format("write database, %d = %d", FEW_KEYS, service.getTimesOfWriteDatabase());
        Assert.assertEquals(msg, FEW_KEYS, service.getTimesOfWriteDatabase());

        Runnable task = () -> {
            for (int i = 0, key = 1; i < REQUEST_TIMES; i++, key++) {
                if (key > FEW_KEYS) {
                    key = 1;
                }
                service.get(String.valueOf(key));
            }
        };
        for (int i = 0; i < USER_COUNT; i++) {
            pool.submit(task);
        }
        await();
        msg = String.format("read cache, %d = %d", USER_COUNT * REQUEST_TIMES, service.getTimesOfReadCache());
        Assert.assertEquals(msg, USER_COUNT * REQUEST_TIMES, service.getTimesOfReadCache());

        msg = String.format("write cache, %d <= %d", FEW_KEYS, service.getTimesOfWriteCache());
        Assert.assertTrue(msg, FEW_KEYS <= service.getTimesOfWriteCache());

        msg = String.format("read database, %d <= %d", FEW_KEYS, service.getTimesOfReadDatabase());
        Assert.assertTrue(msg, FEW_KEYS <= service.getTimesOfReadDatabase());

        msg = String.format("write database, %d = %d", FEW_KEYS, service.getTimesOfWriteDatabase());
        Assert.assertEquals(msg, FEW_KEYS, service.getTimesOfWriteDatabase());
    }
}