package com.example.usercenter2backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.usercenter2backend.mapper.UserTeamMapper;
import com.example.usercenter2backend.model.domain.UserTeam;
import com.example.usercenter2backend.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author Z
* @description 针对表【user_team(用户队伍表)】的数据库操作Service实现
* @createDate 2023-06-01 10:42:19
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




