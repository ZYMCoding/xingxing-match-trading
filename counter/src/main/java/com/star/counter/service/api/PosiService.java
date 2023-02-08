package com.star.counter.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.PosiInfo;

import java.util.List;

public interface PosiService {

    List<PosiInfo> getPosiListByUid(long uid) throws JsonProcessingException;
}
