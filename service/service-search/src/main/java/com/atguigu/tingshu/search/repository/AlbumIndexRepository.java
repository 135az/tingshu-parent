package com.atguigu.tingshu.search.repository;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author atguigu-mqx
 * @ClassName AlbumIndexRepository
 * @description: TODO
 * @date 2023年08月08日
 * @version: 1.0
 */
public interface AlbumIndexRepository extends ElasticsearchRepository<AlbumInfoIndex, Long> {
}