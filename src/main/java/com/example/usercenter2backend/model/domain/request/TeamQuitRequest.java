package com.example.usercenter2backend.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 吕晓亮
 * @version 1.0
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = -7052053300971430226L;

    private Long teamId;
}
