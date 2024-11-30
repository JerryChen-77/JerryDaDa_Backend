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
import com.jerry.jerrydada.manager.AiManager;
import com.jerry.jerrydada.model.dto.question.AiGenerateQuestionRequest;
import com.jerry.jerrydada.model.dto.question.QuestionContentDTO;
import com.jerry.jerrydada.model.dto.scoringResult.*;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.ScoringResult;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.enums.AppTypeEnum;
import com.jerry.jerrydada.model.vo.ScoringResultVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.ScoringResultService;
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
@RequestMapping("/scoringResult")
@Slf4j
public class ScoringResultController {

    @Resource
    private ScoringResultService scoringResultService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Resource
    private AiManager aiManager;
    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param scoringResultAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addScoringResult(@RequestBody ScoringResultAddRequest scoringResultAddRequest, HttpServletRequest request) {
        if (scoringResultAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtils.copyProperties(scoringResultAddRequest, scoringResult);
        // 转换集合JSON
        scoringResult.setResultProp(JSONUtil.toJsonStr(scoringResultAddRequest.getResultProp()));
        // 校验
        scoringResultService.validScoringResult(scoringResult, true);
        User loginUser = userService.getLoginUser(request);
        scoringResult.setUserId(loginUser.getId());
        boolean result = scoringResultService.save(scoringResult);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newScoringResultId = scoringResult.getId();
        return ResultUtils.success(newScoringResultId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteScoringResult(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ScoringResult oldScoringResult = scoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldScoringResult.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = scoringResultService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param scoringResultUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateScoringResult(@RequestBody ScoringResultUpdateRequest scoringResultUpdateRequest) {
        if (scoringResultUpdateRequest == null || scoringResultUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtils.copyProperties(scoringResultUpdateRequest, scoringResult);
// 对象转JSON
        scoringResult.setResultProp(JSONUtil.toJsonStr(scoringResultUpdateRequest.getResultProp()));
        //  参数校验
        scoringResultService.validScoringResult(scoringResult, false);
        long id = scoringResultUpdateRequest.getId();
        // 判断是否存在
        ScoringResult oldScoringResult = scoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = scoringResultService.updateById(scoringResult);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ScoringResultVO> getScoringResultVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ScoringResult scoringResult = scoringResultService.getById(id);
        if (scoringResult == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(scoringResultService.getScoringResultVO(scoringResult, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param scoringResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listScoringResultVOByPage(@RequestBody ScoringResultQueryRequest scoringResultQueryRequest,
                                                                         HttpServletRequest request) {
        long current = scoringResultQueryRequest.getCurrent();
        long size = scoringResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ScoringResult> scoringResultPage = scoringResultService.page(new Page<>(current, size),
                scoringResultService.getQueryWrapper(scoringResultQueryRequest));
        return ResultUtils.success(scoringResultService.getScoringResultVOPage(scoringResultPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param scoringResultQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ScoringResultVO>> listMyScoringResultVOByPage(@RequestBody ScoringResultQueryRequest scoringResultQueryRequest,
                                                                           HttpServletRequest request) {
        if (scoringResultQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        scoringResultQueryRequest.setUserId(loginUser.getId());
        long current = scoringResultQueryRequest.getCurrent();
        long size = scoringResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<ScoringResult> scoringResultPage = scoringResultService.page(new Page<>(current, size),
                scoringResultService.getQueryWrapper(scoringResultQueryRequest));
        return ResultUtils.success(scoringResultService.getScoringResultVOPage(scoringResultPage, request));
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param scoringResultEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editScoringResult(@RequestBody ScoringResultEditRequest scoringResultEditRequest, HttpServletRequest request) {
        if (scoringResultEditRequest == null || scoringResultEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ScoringResult scoringResult = new ScoringResult();
        BeanUtils.copyProperties(scoringResultEditRequest, scoringResult);

        // 对象转JSON
        scoringResult.setResultProp(JSONUtil.toJsonStr(scoringResultEditRequest.getResultProp()));

        // 参数校验
        scoringResultService.validScoringResult(scoringResult, false);
        User loginUser = userService.getLoginUser(request);
        long id = scoringResultEditRequest.getId();
        // 判断是否存在
        ScoringResult oldScoringResult = scoringResultService.getById(id);
        ThrowUtils.throwIf(oldScoringResult == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldScoringResult.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = scoringResultService.updateById(scoringResult);
        return ResultUtils.success(result);
    }


    private static final String GENERATE_SCORING_RESULT_SYSTEM_PROMPT = "你是一位严谨的专家，能够给出题目评分结果信息，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来给出评分结果，注意！是对给你的应用，给出你对评分结果：\n" +
            "1. 要求：评分结果名称尽量简短，结果描述尽量完整，且生动\n" +
            "2. 严格按照下面的 json 格式输出，不要包含其他的字段！\n" +
            "```\n" +
            "    [\n" +
            "        {\n" +
            "        resultName:\"结果名称\",\n" +
            "        resultDesc:\"结果描述\",\n" +
            "        resultScoringRange:\"评分范围\",\n" +
            "        }\n" +
            "    ]\n" +
            "```\n" +
            "resultName 是结果名称，resultDesc 是结果描述，resultScoringRange是评分范围\n" +
            "3. 返回的评分范围在0—100之间,均匀分布，四个或者五个结果，评分范围填写最大值，如0-25则填写25" +
            "4. 返回的题目列表格式必须为 JSON 字符串\n";


    private String getGenerateScoringResultUserMessage(App app) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        userMessage.append(AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类").append("\n");
        return userMessage.toString();
    }

    @PostMapping("/ai_generate_scoring_result")
    public BaseResponse<List<ScoringResultVO>> aiGenerateScoringResult(@RequestBody AiGenerateScoringResultRequest aiGenerateScoringResultRequest) {
        if (aiGenerateScoringResultRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        Long appId = aiGenerateScoringResultRequest.getAppId();

        App app = appService.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        // 封装用户参数
        String userMessage = getGenerateScoringResultUserMessage(app);
        // AI生成题目
        String json = aiManager.doSyncUNSTABLERequest(GENERATE_SCORING_RESULT_SYSTEM_PROMPT, userMessage);
        int start = json.indexOf("[");
        int end = json.lastIndexOf("]");
        String result = json.substring(start, end + 1);
        List<ScoringResultVO> list = JSONUtil.toList(result, ScoringResultVO.class);
        // 插入数据库中
        log.info("AI生成的评分" + list.toString());
        return ResultUtils.success(list);
    }


}
