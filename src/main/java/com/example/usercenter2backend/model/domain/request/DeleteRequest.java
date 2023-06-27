package com.example.usercenter2backend.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = -1005109282099913415L;

    /**
     * id
     */
    private Long id;
}
