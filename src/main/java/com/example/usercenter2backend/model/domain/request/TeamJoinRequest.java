package com.example.usercenter2backend.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = -1853077719616627410L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
