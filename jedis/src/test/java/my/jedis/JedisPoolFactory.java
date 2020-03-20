package my.jedis;

import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JedisPoolFactory {
    protected static final String HOST = "10.105.23.70";
    protected static final String PASSWORD = "yourpassword";
    /**
     * timeout in milliseconds
     */
    protected static final int CONNECT_TIMEOUT = 3000;
    protected static final int SO_TIMEOUT = 3000;

    private static JedisPoolConfig createPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1024);
        config.setMaxIdle(8);
        config.setMinIdle(4);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(false);
        config.setTestOnReturn(false);
        return config;
    }

    public static JedisPool createPool() {
        final int PORT = 7001;

        JedisPoolConfig config = createPoolConfig();
        return new JedisPool(config, HOST, PORT, CONNECT_TIMEOUT, SO_TIMEOUT,
                PASSWORD, 0, "UT");
    }

    public static JedisSentinelPool createSentinelPool() {
        final String masterName = "mymaster";
        Set<String> sentinels = new HashSet<>();
        for (int port = 27001; port <= 27003; port++) {
            sentinels.add(HOST + ":" + port);
        }
        return createSentinelPool(masterName, sentinels);
    }

    public static JedisSentinelPool createSentinelPool(String masterName, Set<String> sentinels) {
        JedisPoolConfig config = createPoolConfig();
        return new JedisSentinelPool(masterName, sentinels, config,
                CONNECT_TIMEOUT, SO_TIMEOUT,
                PASSWORD, 0, "UT");
    }

    public static RedisSentinelTool createRedisSentinelTool() {
        final String masterName = "mymaster";
        Set<HostAndPort> sentinels = new HashSet<>();
        for (int port = 27001; port <= 27003; port++) {
            sentinels.add(new HostAndPort(HOST, port));
        }
        return new RedisSentinelTool(masterName, sentinels, PASSWORD);
    }

    public static ShardedJedisPool createShardedJedisPool() {
        List<JedisShardInfo> shards = new ArrayList<>();
        for (int port = 7001; port <= 7003; port++) {
            JedisShardInfo shard = new JedisShardInfo(HOST, port);
            shard.setPassword(PASSWORD);
            shards.add(shard);
        }
        JedisPoolConfig config = createPoolConfig();
        return new ShardedJedisPool(config, shards);
    }

    public static JedisCluster createJedisCluster() {
        Set<HostAndPort> hps = new HashSet<>();
        for (int port = 6001; port <= 6006; port++) {
            hps.add(new HostAndPort(HOST, port));
        }
        JedisPoolConfig config = createPoolConfig();
        return new JedisCluster(hps, CONNECT_TIMEOUT, SO_TIMEOUT,
                10, PASSWORD, "UT", config);
    }
}