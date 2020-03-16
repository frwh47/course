package my.jedis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LuaApp {
    public static void main(String[] args) throws IOException {
        LuaApp app = new LuaApp();
        app.singleNode();
        app.cluster();
    }

    private void singleNode() throws IOException {
        Jedis jedis = new Jedis("10.5.72.54", 6379);
        jedis.auth("123456");

        String path = this.getClass().getClassLoader().getResource("set.lua").getPath();
        System.out.println(path);
        if (isWindows()) {
            path = path.substring(1);
        }
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        System.out.println(new String(bytes));

        byte[] sha1 = jedis.scriptLoad(bytes);
        String key = "key-" + UUID.randomUUID().toString();
        String value = "value-" + UUID.randomUUID().toString();
        String ttl = "100";
        // 使用返回的标识执行，1表示有1个键
        Object obj = jedis.evalsha(sha1, 1, key.getBytes(), value.getBytes(), ttl.getBytes());
        System.out.println("ttl : " + ttl + " == " + obj);
    }

    private void cluster() throws IOException {
        Set<HostAndPort> hps = new HashSet<>();
        for (int port = 7001; port <= 7006; port++) {
            hps.add(new HostAndPort("39.100.242.198", port));
//            hps.add(new HostAndPort("192.168.0.97", port));
        }

        String password = "f75B1X609aJ1";
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(20);
        config.setMinIdle(2);
        config.setMaxWaitMillis(3000);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        config.setBlockWhenExhausted(false);

        JedisCluster jc = new JedisCluster(hps, 5000, 5000, 5,
                password, config);

        String path = this.getClass().getClassLoader().getResource("set.lua").getPath();
        System.out.println(path);
        if (isWindows()) {
            path = path.substring(1);
        }
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        System.out.println(new String(bytes));

        String key = "key-" + UUID.randomUUID().toString();
        String value = "value-" + UUID.randomUUID().toString();
        String ttl = "100";
        String sha1 = jc.scriptLoad(new String(bytes), key);
        // 使用返回的标识执行，1表示有1个键
        Object obj = jc.evalsha(sha1, 1, key, value, ttl);
        System.out.println("ttl : " + ttl + " == " + obj);
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        System.out.println(os);
        return os.toLowerCase().contains("windows");
    }
}
