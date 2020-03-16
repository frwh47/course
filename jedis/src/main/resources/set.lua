redis.call('set', KEYS[1], ARGV[1])
redis.call('expire', KEYS[1], ARGV[2])
return redis.call('ttl', KEYS[1])
