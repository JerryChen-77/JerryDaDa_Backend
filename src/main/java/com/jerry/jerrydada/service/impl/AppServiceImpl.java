package com.jerry.jerrydada.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.constant.CommonConstant;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.exception.ThrowUtils;
import com.jerry.jerrydada.mapper.AppMapper;
import com.jerry.jerrydada.model.dto.app.AppQueryRequest;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.enums.AppScoringStrategyEnum;
import com.jerry.jerrydada.model.enums.AppTypeEnum;
import com.jerry.jerrydada.model.enums.ReviewStateEnum;
import com.jerry.jerrydada.model.vo.AppVO;
import com.jerry.jerrydada.model.vo.UserVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.UserService;
import com.jerry.jerrydada.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Jerry Chen
* @description 针对表【app(应用)】的数据库操作Service实现
* @createDate 2024-11-08 15:43:46
*/
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
    implements AppService {

    @Resource
    private UserService userService;

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        Integer reviewStatus = app.getReviewStatus();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(appName, appDesc), ErrorCode.PARAMS_ERROR);
            AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(appType);
            if(appTypeEnum == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "测试类型非法");
            }
            AppScoringStrategyEnum appScoringStrategyEnum = AppScoringStrategyEnum.getEnumByValue(scoringStrategy);
            ThrowUtils.throwIf(appScoringStrategyEnum==null, ErrorCode.PARAMS_ERROR, "测试评分策略非法");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(appName) && appName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "测试名称过长");
        }
        if (StringUtils.isNotBlank(appDesc) && appDesc.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "测试描述过长");
        }
        if(reviewStatus != null){
            ReviewStateEnum reviewStateEnum = ReviewStateEnum.getEnumByValue(reviewStatus);
            ThrowUtils.throwIf(reviewStateEnum == null, ErrorCode.PARAMS_ERROR, "审核状态非法");
        }
    }

    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String searchText = appQueryRequest.getSearchText();
        String appDesc = appQueryRequest.getAppDesc();
        String appIcon = appQueryRequest.getAppIcon();
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        String reviewMessage = appQueryRequest.getReviewMessage();
        Long reviewerId = appQueryRequest.getReviewerId();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        // 同时从多个字段
        if(StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper -> wrapper.like("appName", searchText).or().like("appDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        queryWrapper.like(StringUtils.isNotBlank(reviewMessage),"reviewMessage",reviewMessage);
        // 精确查询
        queryWrapper.eq(StringUtils.isNotBlank(appIcon), "appIcon", appIcon);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType),"appType",appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy),"scoringStrategy",scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus),"reviewStatus",reviewStatus);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId),"reviewerId",reviewerId);

        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app,appVO);
        // 1. 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);

        return appVO;
    }

    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        Page<AppVO> appVOPage = new Page<>();
        BeanUtils.copyProperties(appPage, appVOPage);
        List<AppVO> appVOList = appPage.getRecords().stream().map(app -> getAppVO(app, request)).collect(Collectors.toList());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }
}




