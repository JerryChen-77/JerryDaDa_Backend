package com.jerry.jerrydada.model.vo;

import cn.hutool.json.JSONUtil;
import com.jerry.jerrydada.model.dto.question.QuestionContentDTO;
import com.jerry.jerrydada.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

@Data
public class QuestionVO {
    private Long id;

    /**
     * 题目内容（json格式）
     */
    private List<QuestionContentDTO> questionContent;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private UserVO userVO;

    public static Question voToObj(QuestionVO questionVO){
        if(questionVO == null){
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContent));
        return question;
    }

    public static QuestionVO objToVo(Question question){
        if(question == null){
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        if(question.getQuestionContent() != null){
            questionVO.setQuestionContent(JSONUtil.toList(question.getQuestionContent(), QuestionContentDTO.class));
        }
        return questionVO;
    }
}
