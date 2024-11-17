package com.jerry.jerrydada.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author JerryDada
 * @Date 2022/5/25 15:43
 */
@Data
public class AiGenerateQuestionRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 题目数量
     */
    private int questionNumber=10;


    /**
     * 选项数量
     */
    private int optionNumber=2;


}
