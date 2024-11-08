package com.jerry.jerrydada.service.impl;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.constant.CommonConstant;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.exception.ThrowUtils;
import com.jerry.jerrydada.mapper.ScoringResultMapper;
import com.jerry.jerrydada.model.dto.scoringResult.ScoringResultQueryRequest;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.Question;
import com.jerry.jerrydada.model.entity.ScoringResult;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.vo.QuestionVO;
import com.jerry.jerrydada.model.vo.ScoringResultVO;
import com.jerry.jerrydada.model.vo.UserVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.ScoringResultService;
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
 * @description 针对表【scoring_result(评分结果)】的数据库操作Service实现
 * @createDate 2024-11-08 15:43:46
 */
@Service
public class ScoringResultServiceImpl extends ServiceImpl<ScoringResultMapper, ScoringResult>
        implements ScoringResultService {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    public void validScoringResult(ScoringResult scoringResult, boolean add) {


        if (scoringResult == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String resultName = scoringResult.getResultName();
        String resultDesc = scoringResult.getResultDesc();
        Long appId = scoringResult.getAppId();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(resultName, resultDesc), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId非法");
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(resultName) && resultName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结果名称过长");
        }
        if (StringUtils.isNotBlank(resultDesc) && resultDesc.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结果描述过长");
        }
        // 判断app是否存在
        if (appId != null) {
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "app不存在");
        }
    }

    @Override
    public QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest scoringResultQueryRequest) {
        QueryWrapper<ScoringResult> queryWrapper = new QueryWrapper<>();
        if (scoringResultQueryRequest == null) {
            return queryWrapper;
        }
// TODO 后期完善条件搜索
        Long id = scoringResultQueryRequest.getId();
        String resultName = scoringResultQueryRequest.getResultName();
        String resultDesc = scoringResultQueryRequest.getResultDesc();
        String resultPicture = scoringResultQueryRequest.getResultPicture();
        String resultProp = scoringResultQueryRequest.getResultProp();
        Integer resultScoreRange = scoringResultQueryRequest.getResultScoreRange();
        Long appId = scoringResultQueryRequest.getAppId();
        Long userId = scoringResultQueryRequest.getUserId();
        String searchText = scoringResultQueryRequest.getSearchText();
        String sortField = scoringResultQueryRequest.getSortField();
        String sortOrder = scoringResultQueryRequest.getSortOrder();

        // 同时从多个字段
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(wrapper -> wrapper.like("resultName", searchText).or().like("resultDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(resultPicture), "resultPicture", resultPicture);
        queryWrapper.like(StringUtils.isNotBlank(resultProp), "resultProp", resultProp);
        // 精确查询
        queryWrapper.eq(resultScoreRange != null, "resultScoreRange", resultScoreRange);
        queryWrapper.eq(appId != null, "appId", appId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(id != null, "id", id);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        return queryWrapper;
    }

    @Override
    public ScoringResultVO getScoringResultVO(ScoringResult scoringResult, HttpServletRequest request) {
        ScoringResultVO scoringResultVO = ScoringResultVO.objToVo(scoringResult);
        Long userId = scoringResult.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        scoringResultVO.setUser(userVO);
        return scoringResultVO;
    }

    @Override
    public Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> scoringResultPage, HttpServletRequest request) {
        Page<ScoringResultVO> scoringResultVOPage = new Page<>();
        BeanUtils.copyProperties(scoringResultPage, scoringResultVOPage);
        List<ScoringResultVO> questionVOList = scoringResultPage.getRecords().stream().map(scoringResult -> getScoringResultVO(scoringResult, request)).collect(Collectors.toList());
        scoringResultVOPage.setRecords(questionVOList);
        return scoringResultVOPage;
    }
}




