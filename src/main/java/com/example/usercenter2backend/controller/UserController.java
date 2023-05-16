package com.example.usercenter2backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.model.domain.request.UserLoginRequest;
import com.example.usercenter2backend.model.domain.request.UserRegisterRequest;
import com.example.usercenter2backend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.usercenter2backend.constant.UserConstant.ADMIN_ROLE;
import static com.example.usercenter2backend.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long doRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        //这里的处理仅仅是对参数的校验，没有业务的处理
        if(userRegisterRequest == null){
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public User doLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        //这里的处理仅仅是对参数的校验，没有业务的处理
        if(userLoginRequest == null){
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        return userService.userLogin(userAccount, userPassword,request);
    }
    @GetMapping("/search")
    public List<User> userSearch( String userName,HttpServletRequest request){
        //这个地方基本上没有业务逻辑校验，所以直接在这写。

        //判断是否是管理员
        if(!isAdmin(request)){
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(userName)){
            queryWrapper.like("userName",userName);
        }
        List<User> userList = userService.list(queryWrapper);
        //遍历获取的用户，并进行脱敏后的数据。
        return userList.stream().map(user-> userService.getSafeUser(user)).collect(Collectors.toList());
    }
    @PostMapping("/delete")
    public boolean deleteUser(long id,HttpServletRequest request){

        //判断是否是管理员
        if(!isAdmin(request)){
            return false;
        }
        if(id <= 0){
            return false;
        }
        return userService.removeById(id);
    }

    public boolean isAdmin(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
