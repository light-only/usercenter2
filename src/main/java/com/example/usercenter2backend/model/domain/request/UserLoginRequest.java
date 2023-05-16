package com.example.usercenter2backend.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -5786848304564000096L;

    private String userAccount;
    private String userPassword;
}
