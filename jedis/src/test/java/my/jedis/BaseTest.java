package my.jedis;

import redis.clients.jedis.JedisPoolConfig;

public abstract class BaseTest {
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

    protected static JedisPoolConfig createPoolConfig() {
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
}
