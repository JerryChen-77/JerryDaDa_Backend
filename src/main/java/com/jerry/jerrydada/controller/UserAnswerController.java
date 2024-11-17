package com.jerry.jerrydada.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.jerry.jerrydada.annotation.AuthCheck;
import com.jerry.jerrydada.common.BaseResponse;
import com.jerry.jerrydada.common.DeleteRequest;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.common.ResultUtils;
import com.jerry.jerrydada.constant.UserConstant;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.exception.ThrowUtils;
import com.jerry.jerrydada.model.dto.userAnswer.UserAnswerAddRequest;
import com.jerry.jerrydada.model.dto.userAnswer.UserAnswerEditRequest;
import com.jerry.jerrydada.model.dto.userAnswer.UserAnswerQueryRequest;
import com.jerry.jerrydada.model.dto.userAnswer.UserAnswerUpdateRequest;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.UserAnswer;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.enums.ReviewStateEnum;
import com.jerry.jerrydada.model.vo.UserAnswerVO;
import com.jerry.jerrydada.scoring.ScoringStrategyExecutor;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.UserAnswerService;
import com.jerry.jerrydada.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/JerryChen-77">JerryChen</a>
 */
@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;
    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;


    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param userAnswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request){
        if (userAnswerAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerAddRequest, userAnswer);
        // 数组转JSON
        List<String> choices = userAnswerAddRequest.getChoices();
        userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerAddRequest.getChoices()));
        // 判断app是否存在
        App app = appService.getById(userAnswer.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        if(!ReviewStateEnum.getEnumByValue(app.getReviewStatus()).equals(ReviewStateEnum.PASS)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"该应用未通过审核");
        }
        //  填充默认值用户
        userAnswerService.validUserAnswer(userAnswer, true);
        User loginUser = userService.getLoginUser(request);
        userAnswer.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = userAnswerService.save(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 新写入数据库的数据
        long newUserAnswerId = userAnswer.getId();
        // 调用评分模块
        try {
            // 测评一下，并且更新
            UserAnswer newAnswer = scoringStrategyExecutor.doScore(choices, app);
            newAnswer.setId(newUserAnswerId);
            userAnswerService.updateById(newAnswer);
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"评分错误");
        }
        return ResultUtils.success(newUserAnswerId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userAnswerService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param userAnswerUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest userAnswerUpdateRequest) {
        if (userAnswerUpdateRequest == null || userAnswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerUpdateRequest, userAnswer);
        // 数组转JSON
        userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerUpdateRequest.getChoices()));
        //  参数校验
        userAnswerService.validUserAnswer(userAnswer, false);
        long id = userAnswerUpdateRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = userAnswerService.updateById(userAnswer);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserAnswer userAnswer = userAnswerService.getById(id);
        if (userAnswer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(userAnswerService.getUserAnswerVO(userAnswer, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
                                                                   HttpServletRequest request) {
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
                                                                     HttpServletRequest request) {
        if (userAnswerQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        userAnswerQueryRequest.setUserId(loginUser.getId());
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserAnswer> userAnswerPage = userAnswerService.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param userAnswerEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request) {
        if (userAnswerEditRequest == null || userAnswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerEditRequest, userAnswer);
        // 数组转JSON
        userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerEditRequest.getChoices()));
        // 参数校验
        userAnswerService.validUserAnswer(userAnswer, false);
        User loginUser = userService.getLoginUser(request);
        long id = userAnswerEditRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = userAnswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userAnswerService.updateById(userAnswer);
        return ResultUtils.success(result);
    }

}
