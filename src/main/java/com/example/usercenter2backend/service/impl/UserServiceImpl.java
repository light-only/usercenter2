package com.example.usercenter2backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter2backend.common.ErrorCode;
import com.example.usercenter2backend.exception.BusinessException;
import com.example.usercenter2backend.mapper.UserMapper;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.service.UserService;
import com.example.usercenter2backend.utils.AlgorithmUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.channels.Pipe;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.usercenter2backend.constant.UserConstant.ADMIN_ROLE;
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

    /**
     * 根据标签查询用户（内存版本）
     * @param tagNameList 标签列表
     * @return
     */
    @Override
    public List<User> getUserByTags(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.先查询所有的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2.在内存中查询是否有符合条件的标签
        return userList.stream().filter(user->{
            String tagStr = user.getTags();
            if(StringUtils.isBlank(tagStr)){
                return false;
            }
            /**
             * new TypeToken<Set<String>>(){}.getType()用于获取一个Type对象，表示Set<String>类型。
             * 然后，gson.fromJson方法将tagStr字符串解析成一个Set<String>类型的对象tempTagNameSet。
             */
            Set<String> tempTagNameSet = gson.fromJson(tagStr,new TypeToken<Set<String>>(){}.getType());
            //判断是否为空
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for(String tagName:tagNameList){
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafeUser).collect(Collectors.toList());
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if(userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验信息：
        //1.管理员可以修改任何人的信息
        //2.普通用户只能修改自己的信息
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = this.getById(user.getId());
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //触发更新
        return this.baseMapper.updateById(user);
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags,new TypeToken<List<String>>(){}.getType());
        //用户列表的下标->相似度
        List<Pair<User,Long>> list = new ArrayList<>();
        //依次计算所有用户和当前用户的相似度
        for(int i=0;i<userList.size();i++){
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签或者当前用户自己
            if(StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())){
                continue;
            }
            List<String > usersTagList = gson.fromJson(userTags,new TypeToken<List<String>>(){}.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList,usersTagList);
            list.add(new Pair<>(user,distance));
        }
        //按编辑距离由大到小排序
        List<Pair<User,Long>> toUserPairList = list.stream().sorted((a,b)->(int) (a.getValue() -b.getValue()))
                .limit(num).collect(Collectors.toList());
        //原本顺序的userId 列表
        List<Long> userIdList = toUserPairList.stream().map(pair->pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id",userIdList);
        Map<Long,List<User>> userIdUserListMap = this.list(userQueryWrapper).stream().map(user->getSafeUser(user)).collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for(Long userId: userIdList){
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签查询用户（SQL版本）
     * @param tagNameList
     * @return
     */
    @Deprecated
    private List<User> searchUserByTags(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName:tagNameList){
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafeUser).collect(Collectors.toList());
    }
}




