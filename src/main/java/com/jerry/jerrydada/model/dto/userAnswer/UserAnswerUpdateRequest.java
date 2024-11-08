package com.jerry.jerrydada.model.dto.userAnswer;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserAnswerUpdateRequest {
    private Long id;
    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;
}
