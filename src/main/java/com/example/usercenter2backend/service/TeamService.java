package com.example.usercenter2backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter2backend.model.domain.Team;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.model.domain.dto.TeamQuery;
import com.example.usercenter2backend.model.domain.vo.TeamUserVO;

import java.util.List;


/**
* @author Z
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2023-06-01 10:26:57
*/
public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     * @param team 队伍
     * @param loginUser 当前登录用户
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 获取队伍列表
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> getTeamList(TeamQuery teamQuery,boolean isAdmin);


}
