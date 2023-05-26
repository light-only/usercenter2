package com.example.usercenter2backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.usercenter2backend.common.BaseResponse;
import com.example.usercenter2backend.common.ErrorCode;
import com.example.usercenter2backend.common.ResultUtils;
import com.example.usercenter2backend.exception.BusinessException;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.model.domain.request.UserLoginRequest;
import com.example.usercenter2backend.model.domain.request.UserRegisterRequest;
import com.example.usercenter2backend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.usercenter2backend.constant.UserConstant.ADMIN_ROLE;
import static com.example.usercenter2backend.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> doRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        //这里的处理仅仅是对参数的校验，没有业务的处理
        if(userRegisterRequest == null){
            //return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            //return null;
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> doLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        //这里的处理仅仅是对参数的校验，没有业务的处理
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword,request);
        return ResultUtils.success(user);
    }
    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch( String userName,HttpServletRequest request){
        //这个地方基本上没有业务逻辑校验，所以直接在这写。
        //判断是否是管理员
        if(!isAdmin(request)){
            //return new ArrayList<>();
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(userName)){
            queryWrapper.like("userName",userName);
        }
        List<User> userList = userService.list(queryWrapper);
        //遍历获取的用户，并进行脱敏后的数据。
        List<User> list = userList.stream().map(user-> userService.getSafeUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(long id,HttpServletRequest request){
        //判断是否是管理员
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safeUser =  userService.getSafeUser(user);
        return ResultUtils.success(safeUser);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> doLogout(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.getUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 判断是否是管理员账号
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
