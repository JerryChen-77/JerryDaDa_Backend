package com.jerry.jerrydada.score;

import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.jerry.jerrydada.scoring.ScoringStrategyExecutor;
import com.jerry.jerrydada.service.AppService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.RecordApplicationEvents;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ScoringTest {

    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;

    @Resource
    private AppService appService;
    @Test
    public void test() throws Exception {
        App app = appService.getById(1);
        List<String> choices = new ArrayList<>();
        choices.add("A");
        choices.add("B");
        choices.add("A");
        choices.add("A");
        choices.add("A");
        choices.add("A");
        choices.add("A");
        choices.add("A");
        choices.add("A");
        choices.add("A");
        UserAnswer userAnswer = scoringStrategyExecutor.doScore(choices, app);
        System.out.println(userAnswer);
    }
}
