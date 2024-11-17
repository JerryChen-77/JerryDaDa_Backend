package com.jerry.jerrydada.mapper;

import com.jerry.jerrydada.model.dto.statistic.AppAnswerCountDTO;
import com.jerry.jerrydada.model.dto.statistic.AppAnswerResultDTO;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Jerry Chen
 * @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
 * @createDate 2024-11-08 15:29:17
 * @Entity com.jerry.jerrydada.model.entity.UserAnswer
 */
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    /**
     * 使用情况最多的APP（前八）
     *
     * @return
     */
    @Select("select appId, count(userId) as answerCount\n" +
            "from user_answer\n" +
            "group by appId\n" +
            "order by answerCount desc\n" +
            "limit 8;")
    List<AppAnswerCountDTO> doAppAnswerCount();

    @Select("select resultName, count(resultName) as resultCount\n" +
            "from user_answer where appId=#{appId}\n" +
            "group by resultName\n" +
            "order by resultCount desc;")
    List<AppAnswerResultDTO> doAppAnswerResultCount(Long appId);

}




