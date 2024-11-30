package com.jerry.jerrydada.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.AppThumb;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.vo.AppThumbVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.AppThumbService;
import com.jerry.jerrydada.mapper.AppThumbMapper;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
* @author Jerry Chen
* @description 针对表【AppThumb(帖子点赞)】的数据库操作Service实现
* @createDate 2024-12-01 00:31:42
*/
@Service
public class appThumbServiceImpl extends ServiceImpl<AppThumbMapper, AppThumb>
    implements AppThumbService {

    @Resource
    private AppService appService;
    @Override
    public int doAppThumb(long appId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        AppThumbService appThumbService = (AppThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return appThumbService.doAppThumbInner(userId, appId);
        }
    }



    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param appId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doAppThumbInner(long userId, long appId) {
        AppThumb appThumb = new AppThumb();
        appThumb.setUserId(userId);
        appThumb.setAppId(appId);
        QueryWrapper<AppThumb> thumbQueryWrapper = new QueryWrapper<>(appThumb);
        AppThumb oldAppThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldAppThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = appService.update()
                        .eq("id", appId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(appThumb);
            if (result) {
                // 点赞数 + 1
                result = appService.update()
                        .eq("id", appId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    @Override
    public AppThumbVO hasThumbed(long appId, User loginUser) {
        AppThumb appThumb = new AppThumb();
        appThumb.setAppId(appId);
        QueryWrapper<AppThumb> thumbQueryWrapper = new QueryWrapper<>(appThumb);
        appThumb.setUserId(loginUser.getId());
        thumbQueryWrapper = new QueryWrapper<>(appThumb);
        AppThumb hasThumbed = this.getOne(thumbQueryWrapper);

        AppThumbVO appThumbVO = new AppThumbVO();
        appThumbVO.setHasThumbed(hasThumbed != null);
        appThumbVO.setAppThumbNum(appService.getById(appId).getThumbNum());
        return appThumbVO;
    }
}




