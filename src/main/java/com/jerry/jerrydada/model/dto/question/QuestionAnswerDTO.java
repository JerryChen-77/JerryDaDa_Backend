package com.jerry.jerrydada.model.dto.question;

import lombok.Data;

@Data
/**
 * 题目答案 AI评分用
 */
public class QuestionAnswerDTO {

    /**
     * 题目
     */
    private String title;

    /**
     * 用户答案
     */
    private String userAnswer;
}
