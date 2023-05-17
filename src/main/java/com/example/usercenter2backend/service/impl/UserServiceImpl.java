package com.example.usercenter2backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter2backend.common.ErrorCode;
import com.example.usercenter2backend.exception.BusinessException;
import com.example.usercenter2backend.mapper.UserMapper;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.usercenter2backend.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Z
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2023-05-15 11:29:37
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //不能为空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //账户长度不能小于4，
        if(userAccount.length()<4 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户长度不能小于4");
        }
        //密码长度不能小于8
        if(userPassword.length()<8 || checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }
        if(planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号长度不能大于5");
        }
        //账户不能存在特殊字符
        String pattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(pattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能存在特殊字符");
        }
        //密码和校验密码不同
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验密码不同");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        Long count = userMapper.selectCount(queryWrapper);
        if(count >0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }
        //星球编号不能重复
        QueryWrapper<User> queryWrapper2 = new QueryWrapper();
        queryWrapper2.eq("planetCode",planetCode);
        Long count2 = userMapper.selectCount(queryWrapper2);
        if(count2 >0){
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号已存在");
        }
        //加密
        String encryptPassWord = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(userPassword);
        user.setPlanetCode(planetCode);
        boolean result = this.save(user);
        if(!result){
           throw new BusinessException(ErrorCode.NULL_ERROR,"注册失败，请重试");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword,HttpServletRequest request) {
        //不能为空
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        //账户长度不能小于4，
        if(userAccount.length()<4 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户长度不能小于4");
        }
        //密码长度不能小于8
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }
        //账户不能存在特殊字符
        String pattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(pattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能存在特殊字符");
        }
        //加密
        String encryptPassWord = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //是否有这个用户
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",userPassword);
        User user = userMapper.selectOne(queryWrapper);
        if(user  == null){
            log.info("user login faild,userAcoount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户不存在，请重试");
        }

        //用户脱敏  脱敏就是不能返回所有的信息，比如密码，只返回可以展示出来的信息，比如账号，头像等。
        User safeUser = getSafeUser(user);

        //记录用户的登录状态 ->通过这个来判断用户是否登录
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);
        return safeUser;
    }

    /**
     * 返回脱敏后的用户信息
     * @param user
     * @return
     */
    @Override
    public User getSafeUser(User user) {
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUserName(user.getUserName());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setPhone(user.getPhone());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setPlanetCode(user.getPlanetCode());
        safeUser.setTags(user.getTags());
        safeUser.setProfile(user.getProfile());
        return safeUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




