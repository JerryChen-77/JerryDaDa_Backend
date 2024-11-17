package com.jerry.jerrydada.controller;

import com.jerry.jerrydada.common.BaseResponse;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.common.ResultUtils;
import com.jerry.jerrydada.exception.ThrowUtils;
import com.jerry.jerrydada.mapper.UserAnswerMapper;
import com.jerry.jerrydada.model.dto.statistic.AppAnswerCountDTO;
import com.jerry.jerrydada.model.dto.statistic.AppAnswerResultDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/statistic")
@Slf4j
public class StatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    /**
     * top 8 的应用回答量
     * @return
     */
    @GetMapping("/answerCount")
    public BaseResponse<List<AppAnswerCountDTO>> top8AnswerCount(){
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }

    /**
     * 某应用回答结果统计
     * @param appId
     * @return
     */
    @GetMapping("/answerResultCount")
    public BaseResponse<List<AppAnswerResultDTO>> answerResultCount(Long appId){
        ThrowUtils.throwIf(appId==null||appId<0, ErrorCode.PARAMS_ERROR, "appId不能为空");
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }
}
