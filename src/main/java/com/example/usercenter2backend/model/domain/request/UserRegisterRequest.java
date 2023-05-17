package com.example.usercenter2backend.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 6585955365201840889L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;
}
