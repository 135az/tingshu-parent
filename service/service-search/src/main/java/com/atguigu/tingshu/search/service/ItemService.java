package com.atguigu.tingshu.search.service;

import java.util.Map;

public interface ItemService {
    
    /**
     * 根据专辑Id查询专辑信息
     *
     * @param albumId
     * @return
     */
    Map<String, Object> getItem(Long albumId);
}
