package com.example.usercenter2backend.common;

import java.io.Serializable;

public class PageRequest implements Serializable {

    private static final long serialVersionUID = 6270734210699667449L;

    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前是第几页
     */
    protected  int pageNum;
}
