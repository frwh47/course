package my.jedis;

abstract class AbstractService implements Service {
    protected Cache cache;
    protected Database database;

    AbstractService(Cache cache, Database database) {
        this.cache = cache;
        this.database = database;
    }

    public int getTimesOfReadCache() {
        return cache == null ? 0 : cache.getReadTimes();
    }

    public int getTimesOfWriteCache() {
        return cache == null ? 0 : cache.getWriteTimes();
    }

    public int getTimesOfReadDatabase() {
        return database == null ? 0 : database.getReadTimes();
    }

    public int getTimesOfWriteDatabase() {
        return database == null ? 0 : database.getWriteTimes();
    }
}
