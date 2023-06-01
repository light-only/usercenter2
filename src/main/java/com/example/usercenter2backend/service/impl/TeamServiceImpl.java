package com.example.usercenter2backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.usercenter2backend.mapper.TeamMapper;
import com.example.usercenter2backend.model.domain.Team;
import com.example.usercenter2backend.service.TeamService;
import org.springframework.stereotype.Service;

/**
* @author Z
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2023-06-01 10:26:57
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

}




