import thirdpart.checksum.impl.CheckSumImpl;

public class CheckSumTest {
    public static void main(String[] args) {
        String a = "test1";
        String b = "test2";
        String c = "test1";
        CheckSumImpl checkSum = new CheckSumImpl();
        System.out.println(checkSum.getCheckSum(a.getBytes()));
        System.out.println(checkSum.getCheckSum(b.getBytes()));
        System.out.println(checkSum.getCheckSum(c.getBytes()));
    }
}
