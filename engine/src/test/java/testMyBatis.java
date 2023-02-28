import com.star.engine.bean.DBUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class testMyBatis {
    public static void main(String[] args) {
        List<Map<String, Object>> maps = DBUtil.getInstance().queryAllBalance();
        for (Map<String, Object> map : maps){
            for (String key : map.keySet()) {
                System.out.println("key is: " + key);
                System.out.println("value is: " + map.get(key));
                System.out.println("the type of value is: " + map.get(key).getClass());
            }
        }
        HashSet<Integer> allStockCode = DBUtil.getInstance().queryAllStockCode();
        System.out.println(allStockCode);
        List<Integer> allMemberIds = DBUtil.getInstance().queryAllMemberIds();
        System.out.println(allMemberIds);
    }
}