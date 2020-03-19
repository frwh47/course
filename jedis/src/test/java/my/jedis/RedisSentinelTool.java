package my.jedis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

import java.io.Closeable;
import java.util.*;

public class RedisSentinelTool implements Closeable {
    private String masterName;
    private String masterPassword;
    private Set<HostAndPort> sentinels;

    public RedisSentinelTool(String masterName, Set<HostAndPort> sentinels, String masterPassword) {
        Objects.requireNonNull(masterName);
        if (sentinels == null || sentinels.isEmpty()) {
            throw new IllegalArgumentException("sentinel is empty");
        }
        this.masterName = masterName;
        this.sentinels = Collections.unmodifiableSet(sentinels);
        this.masterPassword = masterPassword;
    }

    public Jedis getResource() {
        return getMaster();
    }

    public Jedis getMaster() {
        HostAndPort master = null;
        for (HostAndPort hp : sentinels) {
            try (Jedis jedis = new Jedis(hp.getHost(), hp.getPort())) {
                List<String> ipPort = jedis.sentinelGetMasterAddrByName(masterName);
                String ip = ipPort.get(0);
                int port = Integer.parseInt(ipPort.get(1));
                master = new HostAndPort(ip, port);
                break;
            }
        }
        Jedis jedis = new Jedis(master);
        jedis.auth(masterPassword);
        return jedis;
    }


    public Jedis getSalve() {
        for (HostAndPort hp : sentinels) {
            try (Jedis jedis = new Jedis(hp.getHost(), hp.getPort())) {
                List<Map<String, String>> masterIpPort = jedis.sentinelSlaves(masterName);
                for (Map<String, String> map : masterIpPort) {
                    String ip = map.get("ip");
                    int port = Integer.parseInt(map.get("port"));
                    Jedis cli = new Jedis(ip, port);
                    cli.auth(masterPassword);
                    return cli;
                }
            }
        }
        return null;
    }

    @Override
    public void close() {
    }
}
