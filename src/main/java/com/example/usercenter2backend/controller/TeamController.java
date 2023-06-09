package com.example.usercenter2backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter2backend.common.BaseResponse;
import com.example.usercenter2backend.common.ErrorCode;
import com.example.usercenter2backend.common.ResultUtils;
import com.example.usercenter2backend.exception.BusinessException;
import com.example.usercenter2backend.model.domain.Team;

import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.model.domain.UserTeam;
import com.example.usercenter2backend.model.domain.dto.TeamQuery;
import com.example.usercenter2backend.model.domain.request.*;
import com.example.usercenter2backend.model.domain.vo.TeamUserVO;
import com.example.usercenter2backend.service.TeamService;
import com.example.usercenter2backend.service.UserService;
import com.example.usercenter2backend.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 用户接口
 */
@Slf4j
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://localhost:3000",allowCredentials = "true")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        Long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);
    }
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam long id){
        if(id <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(team);
    }
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getTeamList(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.getTeamList(teamQuery,isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        //判断用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try{
            User loginUser = userService.getCurrentUser(request);
            //查询当前的用户id
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            //查询teamId
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入队伍的id集合
            Set<Long> hasJoinTeamSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team->{
                boolean hasJoin = hasJoinTeamSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        }catch (Exception e){}
        if(teamIdList != null && !teamIdList.isEmpty()){
            QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            //队伍id->加入这个队伍的用户列表
            Map<Long,List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
            teamList.forEach(team->team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size()));
        }
        return ResultUtils.success(teamList);
    }
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamPage( TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page,queryWrapper);
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(TeamQuitRequest teamQuitRequest,HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean result = teamService.quitTeams(teamQuitRequest,loginUser);
        return ResultUtils.success(result);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam( DeleteRequest deleteRequest, HttpServletRequest request){
        if(deleteRequest ==  null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean result = teamService.deleteTeams(deleteRequest.getId(),loginUser);
        return ResultUtils.success(result);
    }
    @GetMapping("list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean isTrue = userService.isAdmin(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.getTeamList(teamQuery,isTrue);
        return ResultUtils.success(teamList);
    }

    @GetMapping("list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeam(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        boolean isTrue = userService.isAdmin(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //取出不重复的队伍id
        Map<Long,List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.getTeamList(teamQuery,isTrue);
        return ResultUtils.success(teamList);
    }

}
