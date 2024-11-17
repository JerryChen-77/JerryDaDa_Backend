package com.jerry.jerrydada.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jerry.jerrydada.model.dto.question.QuestionContentDTO;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.Question;
import com.jerry.jerrydada.model.entity.ScoringResult;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.jerry.jerrydada.model.vo.QuestionVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.QuestionService;
import com.jerry.jerrydada.service.ScoringResultService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义测评类应用的打分机制
 */
@Slf4j
@ScoringStrategyConfig(appType = 1, scoringStrategy = 0)
public class CustomTestScoringStrategy implements ScoringStrategy {
    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {

        Long appId = app.getId();
        // 1. 根据id查询题目和题目结果信息
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class)
                        .eq(Question::getAppId, appId));

        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class)
                        .eq(ScoringResult::getAppId, appId));

        // 2.统计用户每个选择对应的个数
        Map<String, Integer> optionCount = new HashMap<>();
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
        for (int i = 0; i < questionContent.size(); i++) {
            QuestionContentDTO questionContentDTO = questionContent.get(i);
            String answer = choices.get(i);
            for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                if (option.getKey().equals(answer)) {
                    // 答案对，取出属性
                    String result = option.getResult();
                    if (!optionCount.containsKey(result)) {
                        optionCount.put(result, 0);
                    }
                    optionCount.put(result, optionCount.get(result) + 1);
                }
            }

        }

        log.info(optionCount.toString());
        int maxScore = 0;
        ScoringResult maxScoringResult = scoringResultList.get(0);
        //3.遍历每种评分结果，计算那个得分更高
        for (ScoringResult scoringResult : scoringResultList) {
            List<String> resultProp = JSONUtil.toList(scoringResult.getResultProp(), String.class);
            int score = resultProp.stream()
                    .mapToInt(prop -> optionCount.getOrDefault(prop, 0))
                    .sum();
            log.info(scoringResult.getResultName() + ":" + score + "");
            if (score > maxScore) {
                maxScore = score;
                maxScoringResult = scoringResult;
            }
        }
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

        return userAnswer;
    }
}
