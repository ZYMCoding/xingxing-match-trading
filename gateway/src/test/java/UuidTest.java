import thirdpart.uuid.GudyUuid;

public class UuidTest {

    public static void main(String[] args) {
        GudyUuid uuid = GudyUuid.getInstance();
        uuid.init(0, 0);
        for (int i = 0; i < 1000; i++) {
            System.out.println(uuid.getUUID());
        }
    }
}
