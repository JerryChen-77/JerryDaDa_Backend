package com.jerry.jerrydada.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.jerry.jerrydada.annotation.AuthCheck;
import com.jerry.jerrydada.common.BaseResponse;
import com.jerry.jerrydada.common.DeleteRequest;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.common.ResultUtils;
import com.jerry.jerrydada.config.VipSchedulerConfig;
import com.jerry.jerrydada.constant.UserConstant;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.exception.ThrowUtils;
import com.jerry.jerrydada.manager.AiManager;
import com.jerry.jerrydada.model.dto.question.*;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.Question;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.enums.AppTypeEnum;
import com.jerry.jerrydada.model.vo.QuestionVO;
import com.jerry.jerrydada.service.AppService;
import com.jerry.jerrydada.service.QuestionService;
import com.jerry.jerrydada.service.UserService;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.esf.SPuri;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/JerryChen-77">JerryChen</a>
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private AppService appService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private Scheduler vipScheduler;


    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 转换
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<QuestionContentDTO> questionContentDTO = questionAddRequest.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
        // 填充默认值
        // TODO 添加应用的校验
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        // 填充默认值
        question.setUserId(loginUser.getId());
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        // 转换 JSON和对象

        List<QuestionContentDTO> questionContentDTO = questionUpdateRequest.getQuestionContent();

        question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
        // TODO 参数校验
//        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }


    @GetMapping("/get/vo/byAppId")
    public BaseResponse<QuestionVO> getQuestionVOByAppId(long appId, HttpServletRequest request) {
        if (appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getByAppId(appId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        // 转换 JSON和对象
        List<QuestionContentDTO> questionContentDTO = questionEditRequest.getQuestionContent();

        question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }


    @GetMapping("/generate/id")
    public BaseResponse<Long> generateUniqueUserAnswerId() {
        return ResultUtils.success(IdUtil.getSnowflakeNextId());
    }

    // region AI生成功能
    private static final String GENERATE_QUESTION_SYSTEM_PROMPT = "你是一位严谨的出题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "要生成的题目数，\n" +
            "每个题目的选项数\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来出题：\n" +
            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
            "2. 严格按照下面的 json 格式输出题目和选项\n" +
            "```\n" +
            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
            "```\n" +
            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
            "4. 返回的题目列表格式必须为 JSON 数组\n";




    private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        userMessage.append(AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类").append("\n");
        userMessage.append(questionNumber).append("\n");
        userMessage.append(optionNumber);
        return userMessage.toString();
    }

    @PostMapping("/ai_generate_question")
    public BaseResponse<List<QuestionContentDTO>> aiGenerateQuestion(@RequestBody AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        if(aiGenerateQuestionRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
        App app = appService.getById(appId);
        if(app == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        // 封装用户参数
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
        // AI生成题目
        String json = aiManager.doSyncUNSTABLERequest(GENERATE_QUESTION_SYSTEM_PROMPT, userMessage);
        int start = json.indexOf("[");
        int end = json.lastIndexOf("]");
        String result = json.substring(start, end + 1);
        List<QuestionContentDTO> questionList = JSONUtil.toList(result, QuestionContentDTO.class);
        log.info("AI生成的题目"+questionList.toString());
        return ResultUtils.success(questionList);
    }


    @GetMapping("/ai_generate/sse")
    public SseEmitter aiGenerateQuestionSSE(AiGenerateQuestionRequest aiGenerateQuestionRequest) {
        if(aiGenerateQuestionRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
        App app = appService.getById(appId);
        if(app == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        // 封装用户参数
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
        // 建立SSE 连接对象,0表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);
        // AI生成题目,SSE流式返回
        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(GENERATE_QUESTION_SYSTEM_PROMPT, userMessage,null);
        // 左括号计数器，除了默认值外，当回归为 0 时，表示左括号等于右括号，可以截取
        AtomicInteger flag = new AtomicInteger(0);
        // 拼接完整题目
        StringBuilder contentBuilder = new StringBuilder();
        // 流式处理题目
        modelDataFlowable
                .observeOn(Schedulers.io())
                .map(chunk ->chunk.getChoices().get(0).getDelta().getContent())
                .map(message ->message.replaceAll("\\s",""))
                .filter(StrUtil::isNotBlank)
                .flatMap(message ->{
                    List<Character> charList = new ArrayList<>();
                    for(char c: message.toCharArray()){
                        charList.add(c);
                    }
                    return Flowable.fromIterable(charList);
                })//分流
                .doOnNext(c -> {
                    {
                        // 识别第一个 [ 表示开始 AI 传输 json 数据，打开 flag 开始拼接 json 数组
                        if (c == '{') {
                            flag.addAndGet(1);
                        }
                        if (flag.get() > 0) {
                            contentBuilder.append(c);
                        }
                        if (c == '}') {
                            flag.addAndGet(-1);
                            if (flag.get() == 0) {
                                // 累积单套题目满足 json 格式后，sse 推送至前端
                                // sse 需要压缩成当行 json，sse 无法识别换行
                                sseEmitter.send(JSONUtil.toJsonStr(contentBuilder.toString()));
                                // 清空 StringBuilder
                                contentBuilder.setLength(0);
                            }
                        }
                    }
                })
                .doOnError((e)->{
                    log.error("AI生成题目失败");
                })
                .doOnComplete(sseEmitter::complete)
                .subscribe();
        return sseEmitter;
    }

    // endregion
    @Deprecated
    @GetMapping("/ai_generate/sse/test")
    public SseEmitter aiGenerateQuestionSSETest(AiGenerateQuestionRequest aiGenerateQuestionRequest,boolean isVip) {
        if(aiGenerateQuestionRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        Long appId = aiGenerateQuestionRequest.getAppId();
        int questionNumber = aiGenerateQuestionRequest.getQuestionNumber();
        int optionNumber = aiGenerateQuestionRequest.getOptionNumber();
        App app = appService.getById(appId);
        if(app == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        // 封装用户参数
        String userMessage = getGenerateQuestionUserMessage(app, questionNumber, optionNumber);
        // 建立SSE 连接对象,0表示永不超时
        SseEmitter sseEmitter = new SseEmitter(0L);
        // AI生成题目,SSE流式返回
        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(GENERATE_QUESTION_SYSTEM_PROMPT, userMessage,null);
        // 左括号计数器，除了默认值外，当回归为 0 时，表示左括号等于右括号，可以截取
        AtomicInteger flag = new AtomicInteger(0);
        // 拼接完整题目
        StringBuilder contentBuilder = new StringBuilder();
        // 流式处理题目
        // 默认全局线程池
        Scheduler scheduler = Schedulers.single();
        // 如果是vip用户，使用vip线程池
        if (isVip) {
             scheduler= vipScheduler;
        }
        modelDataFlowable
                .observeOn(scheduler)
                .map(chunk ->chunk.getChoices().get(0).getDelta().getContent())
                .map(message ->message.replaceAll("\\s",""))
                .filter(StrUtil::isNotBlank)
                .flatMap(message ->{
                    List<Character> charList = new ArrayList<>();
                    for(char c: message.toCharArray()){
                        charList.add(c);
                    }
                    return Flowable.fromIterable(charList);
                })//分流
                .doOnNext(c -> {
                    {
                        // 识别第一个 [ 表示开始 AI 传输 json 数据，打开 flag 开始拼接 json 数组
                        if (c == '{') {
                            flag.addAndGet(1);
                        }
                        if (flag.get() > 0) {
                            contentBuilder.append(c);
                        }
                        if (c == '}') {
                            flag.addAndGet(-1);
                            if (flag.get() == 0) {
                                // 累积单套题目满足 json 格式后，sse 推送至前端
                                // sse 需要压缩成当行 json，sse 无法识别换行
                                sseEmitter.send(JSONUtil.toJsonStr(contentBuilder.toString()));
                                // 清空 StringBuilder
                                contentBuilder.setLength(0);
                            }
                        }
                    }
                })
                .doOnError((e)->{
                    log.error("AI生成题目失败");
                })
                .doOnComplete(sseEmitter::complete)
                .subscribe();
        return sseEmitter;
    }

}
