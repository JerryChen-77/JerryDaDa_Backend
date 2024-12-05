package com.jerry.jerrydada.model.dto.appthumb;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用点赞
 *

 */
@Data
public class AppThumbAddRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}