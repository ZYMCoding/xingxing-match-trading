package thirdpart.uuid;

public class GudyUuid {

    private static GudyUuid ourInstance = new GudyUuid();

    public static GudyUuid getInstance() {
        return ourInstance;
    }

    private GudyUuid() {
    }

    public void init(int centerId, int workerId) {
        idWorker = new SnowflakeIdWorker(workerId, centerId);
    }

    private SnowflakeIdWorker idWorker;

    public long getUUID() {
        return idWorker.nextId();
    }


}
