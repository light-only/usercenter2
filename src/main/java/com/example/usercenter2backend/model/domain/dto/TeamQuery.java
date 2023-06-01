package com.example.usercenter2backend.model.domain.dto;

import com.example.usercenter2backend.common.PageRequest;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Transactional(rollbackFor  = Exception.class)
 * 要么数据操作都成功，要么都失败
 */

@Transactional(rollbackFor  = Exception.class)
@Data
public class TeamQuery extends PageRequest {

    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1-加密，2-私有。
     */
    private Integer status;

}
