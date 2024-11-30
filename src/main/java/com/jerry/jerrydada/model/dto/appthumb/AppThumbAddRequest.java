package com.jerry.jerrydada.model.dto.appthumb;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子点赞请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class AppThumbAddRequest implements Serializable {

    /**
     * 帖子 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}