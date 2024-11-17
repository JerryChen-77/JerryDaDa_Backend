package com.jerry.jerrydada.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.constant.CommonConstant;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.exception.ThrowUtils;
import com.jerry.jerrydada.mapper.UserAnswerMapper;
import com.jerry.jerrydada.model.dto.userAnswer.UserAnswerQueryRequest;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.jerry.jerrydada.model.vo.UserAnswerVO;
import com.jerry.jerrydada.model.vo.UserVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.UserAnswerService;
import com.jerry.jerrydada.service.UserService;
import com.jerry.jerrydada.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Jerry Chen
* @description 针对表【user_answer(用户答题记录)】的数据库操作Service实现
* @createDate 2024-11-08 15:43:46
*/
@Service
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer>
    implements UserAnswerService {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    public void validUserAnswer(UserAnswer userAnswer, boolean add) {
        if (userAnswer == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long appId = userAnswer.getAppId();
        Long id = userAnswer.getId();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "新增时id不能为空");
            ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR,"appId非法");
        }
        // 判断app是否存在
        if(appId!= null){
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR,"app不存在");
        }
    }

    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest) {

        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (userAnswerQueryRequest == null) {
            return queryWrapper;
        }
        Long id = userAnswerQueryRequest.getId();
        Long appId = userAnswerQueryRequest.getAppId();
        Integer appType = userAnswerQueryRequest.getAppType();
        Integer scoringStrategy = userAnswerQueryRequest.getScoringStrategy();
        String choices = userAnswerQueryRequest.getChoices();
        Long resultId = userAnswerQueryRequest.getResultId();
        String resultName = userAnswerQueryRequest.getResultName();
        String resultDesc = userAnswerQueryRequest.getResultDesc();
        String resultPicture = userAnswerQueryRequest.getResultPicture();
        Integer resultScore = userAnswerQueryRequest.getResultScore();
        Long userId = userAnswerQueryRequest.getUserId();
        String searchText = userAnswerQueryRequest.getSearchText();

        String sortField = userAnswerQueryRequest.getSortField();
        String sortOrder = userAnswerQueryRequest.getSortOrder();

        // TODO 后期完善条件搜索
        // 同时从多个字段
        if(StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper -> wrapper.like("resultName", searchText).or().like("resultDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(resultPicture), "resultPicture", resultPicture);
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);
        queryWrapper.like(StringUtils.isNotBlank(searchText), "searchText", searchText);
        // 精确查询
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(appId != null, "appId", appId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(appType != null, "appType", appType);
        queryWrapper.eq(scoringStrategy != null, "scoringStrategy", scoringStrategy);
        queryWrapper.eq(resultId != null, "resultId", resultId);
        queryWrapper.eq(resultScore != null, "resultScore", resultScore);

        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer userAnswer, HttpServletRequest request) {
        UserAnswerVO userAnswerVO = UserAnswerVO.objToVo(userAnswer);
        // 1. 关联查询用户信息
        Long userId = userAnswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userAnswerVO.setUser(userVO);
        return userAnswerVO;
    }

    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> userAnswerPage, HttpServletRequest request) {
        Page<UserAnswerVO> userAnswerVOPage = new Page<>();
        BeanUtils.copyProperties(userAnswerPage,userAnswerVOPage);
        List<UserAnswerVO> questionVOList = userAnswerPage.getRecords().stream().map(userAnswer -> getUserAnswerVO(userAnswer, request)).collect(Collectors.toList());
        userAnswerVOPage.setRecords(questionVOList);
        return userAnswerVOPage;
    }
}




