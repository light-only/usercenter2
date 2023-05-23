create table if not exists leo2.user
(
    userName varchar(256) null comment '用户名称',
    userAccount varchar(256) null comment '用户账号',
    avatarUrl varchar(1024) null comment '用户头像',
    id bigint auto_increment comment 'id'
    primary key,
    gender tinyint null comment '性别',
    userPassword varchar(512) not null comment '用户密码',
    email varchar(512) null comment '邮箱',
    userStatus int default 0 null comment '用户状态 0-正常',
    phone varchar(128) null comment '手机号',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete tinyint default 0 not null comment '逻辑删除 0-正常 1-删除',
    userRole int default 0 not null comment '用户角色 0-普通用户  1-管理员',
    planetCode varchar(512) null comment '星球编号',
    tags varchar(1024) null comment ' 标签 json 列表',
    profile varchar(516) null comment '个人简介'
    )
    comment '用户表';


create table if not exists leo2.tag
(
    id bigint auto_increment comment 'id'
    primary key,
    tag_name varchar(256) null comment '标签名',
    user_id bigint null comment '用户 id',
    parent_id bigint null comment '父标签 id',
    is_parent tinyint null comment '是否为父标签 0-否 1-是',
    gmt_create datetime default CURRENT_TIMESTAMP null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    is_deleted tinyint default 0 null comment '逻辑删除',
    constraint uniIdx_tageName
    unique (tag_name)
    )
    comment '标签';

create index idx_userId
	on leo2.tag (user_id);
