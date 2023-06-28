package com.example.usercenter2backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter2backend.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * @param planetCode
     * @return 返回用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String planetCode);

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

    /**
     * 注销登录
     * @param request 移除session就默认未登录
     * @return 返回一个状态码
     */
    int userLogout(HttpServletRequest request);

    /**
     * 通过标签查询用户
     * @param tagNameList 标签列表
     * @return
     */
    List<User> getUserByTags(List<String> tagNameList);

    /**
     * @Description:判断是否是管理员
     * @param request 请求接口
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * @Description:判断是否是管理员
     * @param loginUser 当前登录的用户信息
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * @Description:获取当前登录用户信息
     * @param request 请求接口
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * @Description:更新当前用户信息
     * @param user 要更新的用户信息
     * @param loginUser 当前登录的用户信息
     * @return
     */
    int updateUser(User user,User loginUser);

    /**
     * @param num
     * @param loginUser
     * @return
     * @description:匹配用户
     */
    List<User> matchUsers(long num, User loginUser);
}
