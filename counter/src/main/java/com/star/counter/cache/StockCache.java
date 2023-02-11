package com.star.counter.cache;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.star.counter.bean.StockInfo;
import com.star.counter.mapper.StockMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
@Data
public class StockCache {

    private StockMapper stockMapper;

    //倒排索引
    //Map<String,List<StockInfo>>
    // 6 --> 600086,600025...
    private Map<String, List<StockInfo>> invertIndex = new HashMap<>();

    @Autowired
    public StockCache(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
        System.out.println("加载股票数据...");
        long st = System.currentTimeMillis();
        List<StockInfo> stockInfos = stockMapper.queryAllStockInfo();
        if (CollectionUtils.isEmpty(stockInfos)) {
            System.out.println("未找到股票数据");
            return;
        }
        for (StockInfo stockInfo : stockInfos){
            int code = stockInfo.getCode();
            String abbrname = stockInfo.getAbbrName();
            List<String> codeMetas = splitData(String.format("%06d", code));
            List<String> abbrNameMetas = splitData(abbrname);
            //将股票代码索引和缩写索引合并
            codeMetas.addAll(abbrNameMetas);
            //去重
            Set<String> set = new HashSet<>(codeMetas);
            List<String> newCodeMetas = new ArrayList<>(set);
            for (String key : newCodeMetas) {
                if (!invertIndex.containsKey(key)) {
                    invertIndex.put(key, new ArrayList<>());
                }
                //限制索引数据列表长度(防止索引对应股票太长，例如以6开头的都是上证股票)
                List<StockInfo> stockList = invertIndex.get(key);
                if (!CollectionUtils.isEmpty(stockList) && stockList.size() > 10) {
                    continue;
                }
                stockList.add(stockInfo);
            }
        }
        long time = System.currentTimeMillis() - st;
        System.out.println("股票索引初始化花费时间为：" + time);
    }

    /**
     * 拆分股票代码/缩写对应的所有索引
     * @param code 股票代码或缩写
     * @return 索引列表
     */
    private List<String> splitData(String code) {
        // payh -->
        // p pa pay payh
        // a ay ayh
        // y yh
        // h
        List<String> list = Lists.newArrayList();
        int outLength = code.length();
        for (int i = 0; i < outLength; i++) {
            int inLength = outLength + 1;
            for (int j = i + 1; j < inLength; j++) {
                list.add(code.substring(i, j));
            }
        }
        return list;
    }

    public List<StockInfo> getStocksByKey(String key) {
        return invertIndex.get(key);
    }
}
