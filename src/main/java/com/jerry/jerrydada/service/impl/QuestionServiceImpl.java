package com.jerry.jerrydada.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.constant.CommonConstant;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.exception.ThrowUtils;
import com.jerry.jerrydada.mapper.QuestionMapper;
import com.jerry.jerrydada.model.dto.question.QuestionQueryRequest;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.Question;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.vo.QuestionVO;
import com.jerry.jerrydada.model.vo.UserVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.QuestionService;
import com.jerry.jerrydada.service.UserService;
import com.jerry.jerrydada.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Jerry Chen
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2024-11-08 15:43:46
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;
    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = question.getQuestionContent();
        Long appId = question.getAppId();
        // TODO 补充校验逻辑
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(content), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(appId == null||appId<=0, ErrorCode.PARAMS_ERROR,"appId非法");
        }
        // 判断app是否存在
        if(appId!= null){
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR,"app不存在");
        }
    }

    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        // TODO 后期完善条件搜索

        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }

        Long id = questionQueryRequest.getId();
        String questionContent = questionQueryRequest.getQuestionContent();
        Long appId = questionQueryRequest.getAppId();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank((questionContent)), "questionContent", questionContent);

        // 精确查询
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(appId != null, "appId", appId);
        queryWrapper.eq(userId != null, "userId", userId);

        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);

        return queryWrapper;
    }

    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        Page<QuestionVO> questionVOPage = new Page<>();
        BeanUtils.copyProperties(questionPage,questionVOPage);
        List<QuestionVO> questionVOList = questionPage.getRecords().stream().map(question -> getQuestionVO(question, request)).collect(Collectors.toList());
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }
}




