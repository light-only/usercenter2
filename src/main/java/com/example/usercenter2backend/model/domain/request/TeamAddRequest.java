package com.example.usercenter2backend.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = -4331863657102184478L;

    /**
     * 队伍名称
     */
    private String name;
    /**
     * 队伍描述
     */
    private String description;
    /**
     * 队伍最大人数
     */
    private Integer maxNum;
    /**
     * 过期时间
     */
    private Date expireTime;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 队伍状态
     */
    private Integer status;
    /**
     * 加密队伍密码
     */
    private String password;
}
