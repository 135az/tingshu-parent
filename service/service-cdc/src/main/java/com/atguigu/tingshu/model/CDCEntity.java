package com.atguigu.tingshu.model;

import lombok.Data;

import javax.persistence.Column;

/**
 * cdc 实体类
 *
 * @author yjz
 * @Date 2025/11/5 14:34
 */
@Data
public class CDCEntity {
    // 注意Column 注解必须是persistence包下的
    @Column(name = "id")
    private Long id;
}