package my.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Administrator on 2019/9/27.
 */
public class JedisClusterTest {
    private Logger logger = LoggerFactory.getLogger(JedisClusterTest.class);

    @Test
    public void testCluster1() {
        String serverInfo = "10.5.72.54:7001";
        String password = "123456";
        testCluster(serverInfo, password);
    }

    private void testCluster(String serverInfo, String password) {
        Set<HostAndPort> set = new HashSet<HostAndPort>();
        String ipPort[] = serverInfo.split(",");
        int len = ipPort.length;
        for (int i = 0; i < len; i++) {
            String server[] = ipPort[i].split(":");
            HostAndPort hp = new HostAndPort(server[0], Integer.parseInt(server[1]));
            set.add(hp);
        }
        logger.info("Redis Cluster SET:" + serverInfo);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(100); //最大空闲数
        poolConfig.setMinIdle(2); //最小空闲数
        poolConfig.setMaxTotal(1000);

        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(1000 * 5); //最大建立连接等待时间(毫秒)

        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(10 * 1000);
        poolConfig.setTimeBetweenEvictionRunsMillis(10 * 1000);

        int timeout = 5 * 1000; //
        int maxAttempts = 5; //节点跳转次数，就是重试次数

        try (JedisCluster jc = new JedisCluster(set, timeout, timeout, maxAttempts, password, poolConfig)) {
            System.out.println(jc.getClusterNodes().keySet());

            for (int i = 0; i < 100; i++) {
                String key = "key-" + UUID.randomUUID().toString();
                String value = "value-" + UUID.randomUUID().toString();
                Assert.assertEquals("OK", jc.setex(key, 60, value));
                Assert.assertEquals(value, jc.get(key));
            }
        }
    }
}
