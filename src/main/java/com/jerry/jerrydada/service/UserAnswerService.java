package com.jerry.jerrydada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jerry.jerrydada.model.dto.userAnswer.UserAnswerQueryRequest;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.jerry.jerrydada.model.vo.UserAnswerVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Jerry Chen
* @description 针对表【user_answer(用户答题记录)】的数据库操作Service
* @createDate 2024-11-08 15:43:46
*/
public interface UserAnswerService extends IService<UserAnswer> {
    /**
     * 校验
     *
     * @param userAnswer
     * @param add
     */
    void validUserAnswer(UserAnswer userAnswer, boolean add);

    /**
     * 获取查询条件
     *
     * @param userAnswerQueryRequest
     * @return
     */
    QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest);


    /**
     * 获取帖子封装
     *
     * @param userAnswer
     * @param request
     * @return
     */
    UserAnswerVO getUserAnswerVO(UserAnswer userAnswer, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param userAnswerPage
     * @param request
     * @return
     */
    Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> userAnswerPage, HttpServletRequest request);
}
