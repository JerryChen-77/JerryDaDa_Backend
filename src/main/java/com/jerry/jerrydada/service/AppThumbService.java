package com.jerry.jerrydada.service;

import com.jerry.jerrydada.model.entity.AppThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.vo.AppThumbVO;

/**
* @author Jerry Chen
* @description 针对表【AppThumb(帖子点赞)】的数据库操作Service
* @createDate 2024-12-01 00:31:42
*/
public interface AppThumbService extends IService<AppThumb> {

    int doAppThumb(long postId, User loginUser);

    int doAppThumbInner(long userId, long appId);

    AppThumbVO hasThumbed(long appId, User loginUser);
}
