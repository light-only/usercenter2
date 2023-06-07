package com.example.usercenter2backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.usercenter2backend.common.ErrorCode;
import com.example.usercenter2backend.constant.TeamStatusEnum;
import com.example.usercenter2backend.exception.BusinessException;
import com.example.usercenter2backend.mapper.TeamMapper;
import com.example.usercenter2backend.model.domain.Team;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.model.domain.UserTeam;
import com.example.usercenter2backend.model.domain.dto.TeamQuery;
import com.example.usercenter2backend.model.domain.request.TeamJoinRequest;
import com.example.usercenter2backend.model.domain.request.TeamUpdateRequest;
import com.example.usercenter2backend.model.domain.vo.TeamUserVO;
import com.example.usercenter2backend.model.domain.vo.UserVO;
import com.example.usercenter2backend.service.TeamService;
import com.example.usercenter2backend.service.UserService;
import com.example.usercenter2backend.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author Z
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2023-06-01 10:26:57
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1.请求参数为空
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.是否登录，未登录不允许创建
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        final long userId = loginUser.getId();
        //检验信息
        //(1)队伍人数 >1 && <=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);//如果为空，直接赋值为0
        if(maxNum < 1  || maxNum >= 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //(2)队伍标题 <=20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题长度不满足要求");
        }
        //(3)队伍描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isBlank(description) || description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述长度不满足要求");
        }

        //status 是否公开 不传默认0
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum =  TeamStatusEnum.getEnumByValue(status);

        if(statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足需求");
        }
        //(5)如果status是加密状态，一定要加密，且密码<=32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(password) || password.length()>32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍密码不满足需求");
            }
        }
        //(6) 超出时间 > 当前时间
        Date expireTime = team.getExpireTime();
        //判断当前时间是否晚于过期时间
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超出时间 > 当前时间");
        }
        //(7)检验用户最多创建5个队伍
        //todo:有bug，可能同时创建100个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(queryWrapper);
        if(hasTeamNum >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        //(8)插入队伍消息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        //(9)插入用户 =>队伍关系到 关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVO> getTeamList(TeamQuery teamQuery,boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if( id!=null && id>0 ){
                queryWrapper.eq("id",id);
            }
        }
        String searchText = teamQuery.getSearchText();
        if(StringUtils.isNotBlank(searchText)){
            queryWrapper.and(qw->qw.like("name",searchText).or().like("expireTime",searchText));
        }
        String name = teamQuery.getName();
        if(StringUtils.isNotBlank(name)){
            queryWrapper.eq("name",name);
        }
        String description = teamQuery.getDescription();
        if(StringUtils.isNotBlank(description)){
            queryWrapper.like("description",description);
        }
        Integer maxNum = teamQuery.getMaxNum();
        //查询最大人数相等
        if(maxNum != null && maxNum>0){
            queryWrapper.eq("maxNum",maxNum);
        }
        Long userId = teamQuery.getUserId();
        //根据创建人来查询
        if(userId != null && userId>0){
            queryWrapper.eq("userId",userId);
        }
        //根据状态查询
        Integer status = teamQuery.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if(statusEnum == null){
            statusEnum = TeamStatusEnum.PUBLIC;
        }
        if(!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        queryWrapper.eq("status",statusEnum.getValue());
        //不展示过期的队伍
        queryWrapper.and(qw->qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        for(Team team:teamList){
            Long userIds = team.getUserId();
            if(userIds == null){
                continue;
            }
            User user = userService.getById(userIds);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team,teamUserVO);

            //脱敏用户信息
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if(id ==null || id<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //检验只有管理员或者是队伍创建者才能修改
        Long createUserId = oldTeam.getUserId();
        if(!createUserId.equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //如果队伍状态改为加密，必须要有密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍密码不能为空");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        //todo:待完善
        return false;
    }
}




