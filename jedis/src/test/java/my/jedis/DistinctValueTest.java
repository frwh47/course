package my.jedis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DistinctValueTest extends StandAloneBase {
    private static Set<Integer> ids;

    @BeforeClass
    public static void beforeClass() {
        StandAloneBase.beforeClass();
        System.out.println("DistinctTest.beforeClass");
        ids = new HashSet<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            ids.add(random.nextInt(100000));
        }
    }

    @Before
    public void before() {
        super.before();
    }

    @Test
    public void set() {
        final String key = "set";
        try (Jedis jedis = pool.getResource()) {
            for (Integer id : ids) {
                jedis.sadd(key, String.valueOf(id));
            }
            Assert.assertEquals(ids.size(), jedis.scard(key).longValue());
        }
    }

    @Test
    public void bitmap() {
        final String key = "bitmap";
        try (Jedis jedis = pool.getResource()) {
            for (Integer id : ids) {
                jedis.setbit(key, id, true);
            }
            Assert.assertEquals(ids.size(), jedis.bitcount(key).longValue());
        }
    }

    @Test
    public void hyperloglog() {
        final String key = "hyperloglog";
        try (Jedis jedis = pool.getResource()) {
            for (Integer id : ids) {
                jedis.pfadd(key, String.valueOf(id));
            }
            int low = ids.size() * 98 / 100;
            int high = ids.size() * 102 / 100;
            long count = jedis.pfcount(key);
            String msg = String.format("[%d %d %d] %d", low, ids.size(), high, count);
            System.out.println(msg);
            Assert.assertTrue(msg, low <= count);
            Assert.assertTrue(msg, count <= high);
        }
    }
}
