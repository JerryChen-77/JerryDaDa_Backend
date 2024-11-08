package com.jerry.jerrydada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jerry.jerrydada.model.dto.scoringResult.ScoringResultQueryRequest;
import com.jerry.jerrydada.model.entity.ScoringResult;
import com.jerry.jerrydada.model.entity.ScoringResult;
import com.jerry.jerrydada.model.vo.ScoringResultVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Jerry Chen
* @description 针对表【scoring_result(评分结果)】的数据库操作Service
* @createDate 2024-11-08 15:43:46
*/
public interface ScoringResultService extends IService<ScoringResult> {

    /**
     * 校验
     *
     * @param scoringResult
     * @param add
     */
    void validScoringResult(ScoringResult scoringResult, boolean add);

    /**
     * 获取查询条件
     *
     * @param scoringResultQueryRequest
     * @return
     */
    QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest scoringResultQueryRequest);


    /**
     * 获取帖子封装
     *
     * @param scoringResult
     * @param request
     * @return
     */
    ScoringResultVO getScoringResultVO(ScoringResult scoringResult, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param scoringResultPage
     * @param request
     * @return
     */
    Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> scoringResultPage, HttpServletRequest request);
}
