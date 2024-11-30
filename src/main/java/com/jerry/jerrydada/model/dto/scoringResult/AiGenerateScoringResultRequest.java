package com.jerry.jerrydada.model.dto.scoringResult;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author JerryDada
 * @Date 2022/5/25 15:43
 */
@Data
public class AiGenerateScoringResultRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;

    private String resultName;

    private String resultDesc;

    private String resultProp;

    private Integer resultScoreRange;

}
