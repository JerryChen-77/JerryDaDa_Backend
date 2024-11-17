package com.jerry.jerrydada.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jerry.jerrydada.model.dto.question.QuestionContentDTO;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.Question;
import com.jerry.jerrydada.model.entity.ScoringResult;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.jerry.jerrydada.model.vo.QuestionVO;
import com.jerry.jerrydada.service.QuestionService;
import com.jerry.jerrydada.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 自定义得分类应用打分机制
 */
@ScoringStrategyConfig(appType = 0,scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy {
    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;


    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        // 根据id 查询到题目结果信息（按分数降序）
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class)
                        .eq(Question::getAppId, app.getId()));

        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, app.getId()).orderByDesc(ScoringResult::getResultScoreRange));

        // 统计用户总分
// 2.统计用户每个选择对应的个数
        QuestionVO questionVO = QuestionVO.objToVo(question);
        int totalScore = 0;
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
        for (QuestionContentDTO questionContentDTO : questionContent) {
            for (String answer : choices) {
                for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                    if(option.getKey().equals(answer)){
                        // 答案对，取出属性
                        int score = Optional.ofNullable(option.getScore()).orElse(0);
                        totalScore+=score;
                    }
                }
            }
        }
        ScoringResult maxScoringResult = scoringResultList.get(0);
        // 根据用户总分，查询到对应的分数区间
        for (ScoringResult scoringResult : scoringResultList) {
            if(totalScore>scoringResult.getResultScoreRange()){
                // 填充对象返回
                maxScoringResult = scoringResult;
                break;
            }
        }

        // 填充对象返回
        // 4构造返回值，填充答案对象属性
        UserAnswer userAnswer = new UserAnswer();

        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setResultId(maxScoringResult.getId());
        userAnswer.setResultName(maxScoringResult.getResultName());
        userAnswer.setResultDesc(maxScoringResult.getResultDesc());
        userAnswer.setResultPicture(maxScoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}
