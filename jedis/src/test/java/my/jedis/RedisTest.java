package my.jedis;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisTest {
    private static final String host = "10.105.23.70";
    private static final int port = 7005;

    @Test
    public void connectRedis() throws InterruptedException {
        String key = "a";
        while (true) {
            try (Jedis jedis = new Jedis(host, port)) {
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
        final int RUN_TIMES = 2000;
        final int BATCH_SIZE = 500;

        try (Jedis jedis = new Jedis(host, port);
             Pipeline pipeline = jedis.pipelined()) {
            for (int i = 0; i < RUN_TIMES; i++) {
                pipeline.multi();
                for (int j = 0; j < BATCH_SIZE; j++) {
                    String key = UUID.randomUUID().toString();
                    pipeline.set(key, key);
                }
                pipeline.exec();
                System.out.println(new Date() + ", " + i);
            }
        }
    }


    @Test
    public void update1() throws InterruptedException {
        final int BATCH_SIZE = 1000;
        final String key = String.valueOf(1);
        int counter = 1;
        try (Jedis jedis = new Jedis(host, port);
             Pipeline pipeline = jedis.pipelined()) {
            while (true) {
                pipeline.multi();
                for (int j = 1; j <= BATCH_SIZE; j++) {
                    pipeline.set(key, UUID.randomUUID().toString());
                }
                pipeline.exec();
                System.out.println(new Date() + ", " + counter);
                counter++;
            }
        }
    }

    @Test
    public void update() throws InterruptedException {
        final int BATCH_SIZE = 3;

        try (Jedis jedis = new Jedis(host, port);
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
