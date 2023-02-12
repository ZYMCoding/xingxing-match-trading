package com.star.counter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.star.counter.bean.PosiInfo;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.mapper.PosiMapper;
import com.star.counter.service.api.PosiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class PosiServiceImpl implements PosiService {

    @Autowired
    RedisStringCache redisStringCache;

    @Autowired
    PosiMapper posiMapper;

    @Override
    public List<PosiInfo> getPosiListByUid(long uid) throws JsonProcessingException {
        String suid = Long.toString(uid);
        String posiS = redisStringCache.get(suid, CacheType.POSI);
        if (StringUtils.isEmpty(posiS)) {
            //缓存中没有查到
            List<PosiInfo> posiInfos = posiMapper.queryPosiByUid(uid);
            List<PosiInfo> result = CollectionUtils.isEmpty(posiInfos) ? Lists.newArrayList() : posiInfos;
            //将持仓信息转换为json并写入缓存
            ObjectMapper objectMapper = new ObjectMapper();
            String resultJson = objectMapper.writeValueAsString(result);
            redisStringCache.cache(suid, resultJson, CacheType.POSI);
            return posiInfos;
        } else {
            //命中缓存
            ObjectMapper objectMapper = new ObjectMapper();
            List<PosiInfo> posiInfos = objectMapper.readValue(posiS, new TypeReference<List<PosiInfo>>() {});
            return posiInfos;
        }
    }

    @Override
    public void addPosi(long uid, int code, long volume, long price) {
        //判断持仓是否已经存在
        PosiInfo posiInfo = posiMapper.queryOnePosi(uid, code);
        if (posiInfo == null) {
            //新增持仓
            posiMapper.insetPosi(uid, code, volume, price);
        } else {
            //修改持仓
            posiMapper.addPosi(uid, code, volume, price);
        }
    }

    @Override
    public void minusPosi(long uid, int code, long volume, long price) {
        addPosi(uid, code, -volume, price);
    }
}
