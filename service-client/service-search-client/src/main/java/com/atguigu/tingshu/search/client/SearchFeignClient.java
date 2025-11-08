package com.atguigu.tingshu.search.client;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.search.client.impl.SearchDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-search", fallback = SearchDegradeFeignClient.class)
public interface SearchFeignClient {

    /**
     * 更新排行榜
     *
     * @return
     */
    @GetMapping("api/search/albumInfo/updateLatelyAlbumRanking")
    Result updateLatelyAlbumRanking();
}