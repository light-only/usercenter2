package com.example.usercenter2backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.usercenter2backend.model.domain.Team;
import com.example.usercenter2backend.model.domain.User;
import com.example.usercenter2backend.model.domain.dto.TeamQuery;
import com.example.usercenter2backend.model.domain.request.TeamJoinRequest;
import com.example.usercenter2backend.model.domain.request.TeamQuitRequest;
import com.example.usercenter2backend.model.domain.request.TeamUpdateRequest;
import com.example.usercenter2backend.model.domain.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @author Z
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2023-06-01 10:26:57
*/
public interface TeamService extends IService<Team> {

    /**
     * @description:新增队伍
     * @param team 队伍
     * @param loginUser 当前登录用户
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * @description:获取队伍列表
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> getTeamList(TeamQuery teamQuery,boolean isAdmin);

    /**
     * @description:更新队伍信息
     * @param teamUpdateRequest 请求体
     * @param loginUser 当前登录用户
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * @description:加入队伍
     * @param teamJoinRequest 请求体
     * @param loginUser 当前登录用户
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * @description:退出队伍
     * @param teamQuitRequest 退出请求体
     * @param loginUser 当前登录用户
     * @return
     */
    boolean quitTeams(TeamQuitRequest teamQuitRequest,User loginUser);

    /**
     * @description:删除队伍
     * @param id 队伍id
     * @param loginUser 当前登录用户
     * @return
     */
    boolean deleteTeams(Long id , User loginUser);
}
