package com.jerry.jerrydada.model.dto.question;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class QuestionUpdateRequest {

    private Long id;

    /**
     * 题目内容（json格式）
     */
    private List<QuestionContentDTO> questionContent;

    /**
     * 应用 id
     */
    private Long appId;

}
