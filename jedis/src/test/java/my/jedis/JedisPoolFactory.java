package my.jedis;

import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JedisPoolFactory {
    protected static final String PASSWORD = "yourpassword";
    /**
     * timeout in milliseconds
     */
    protected static final int CONNECT_TIMEOUT = 3000;
    protected static final int SO_TIMEOUT = 3000;

    protected static final String OK = "OK";
    protected static final int SECONDS_60 = 60;
    protected static final Long ONE = 1L;
    protected static final Long ZERO = 0L;
    protected static final Long NONE = -1L;
    protected static final Long NOT_EXISTS = -2L;

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

    public static JedisPoolAbstract createPool() {
        final String HOST = "10.105.23.70";
        final int PORT = 6379;

        JedisPoolConfig config = createPoolConfig();
        return new JedisPool(config, HOST, PORT, CONNECT_TIMEOUT, SO_TIMEOUT,
                PASSWORD, 0, "UT");
    }

    public static JedisPoolAbstract createSentinelPool() {
        final String masterName = "mymaster";
        Set<String> sentinels = new HashSet<String>();
        sentinels.add("ip:port");
        sentinels.add("ip:port");
        sentinels.add("ip:port");

        JedisPoolConfig config = createPoolConfig();
        return new JedisSentinelPool(masterName, sentinels, config,
                CONNECT_TIMEOUT, SO_TIMEOUT,
                PASSWORD, 0, "UT");
    }

    public static ShardedJedisPool createJedis() {
        JedisPoolConfig config = createPoolConfig();
        List<JedisShardInfo> shards = new ArrayList<>();
        shards.add(new JedisShardInfo("127.0.0.1", 6379));
        return new ShardedJedisPool(config, shards);
    }

    public static JedisCluster create() {
        Set<HostAndPort> hps = new HashSet<>();
        hps.add(new HostAndPort("127.0.0.1", 6379));
        JedisPoolConfig config = createPoolConfig();
        return new JedisCluster(hps, CONNECT_TIMEOUT, SO_TIMEOUT,
                3, "UT", config);
    }
}