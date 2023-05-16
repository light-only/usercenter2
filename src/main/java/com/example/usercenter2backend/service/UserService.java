package com.example.usercenter2backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter2backend.model.domain.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author Z
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-05-15 11:29:37
*/
public interface UserService extends IService<User> {

    /**
     * 注册账号
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 返回用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 账号登录
     * @param userAccount 登录账号
     * @param userPassword 登录密码
     * @param request 请求接口，判断当前是否登录
     * @return 返回脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 脱敏用户信息
     * @param user 需要脱敏的用户信息
     * @return 返回脱敏后的用户信息
     */
    User getSafeUser(User user);
}
