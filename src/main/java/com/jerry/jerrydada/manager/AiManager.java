package com.jerry.jerrydada.manager;

import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiManager {
    @Resource
    private ClientV4 clientV4;

    //稳定
    private static final float STABLE_TEMPERATURE = 0.05f;
    //不稳定
    private static final float UNSTABLE_TEMPERATURE = 0.95f;


    /**
     * 稳定
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncSTABLERequest(String systemMessage,String userMessage){
        return doSyncRequest(systemMessage,userMessage,false,STABLE_TEMPERATURE);
    }

    /**
     * 随机
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncUNSTABLERequest(String systemMessage,String userMessage){
        // 构造请求
        return doSyncRequest(systemMessage,userMessage,false,UNSTABLE_TEMPERATURE);
    }

    public String doSyncRequest(String systemMessage,String userMessage,Boolean stream,Float temperature){
        // 构造请求
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messages.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(userChatMessage);
        return doRequest(messages,stream,temperature);
    }

    public Flowable<ModelData> doStreamSTABLERequest(String systemMessage, String userMessage){
        return doStreamRequest(systemMessage,userMessage,STABLE_TEMPERATURE);
    }

    public Flowable<ModelData> doStreamUNSTABLERequest(String systemMessage, String userMessage){
        return doStreamRequest(systemMessage,userMessage,UNSTABLE_TEMPERATURE);
    }

    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature){
        // 构造请求
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messages.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(userChatMessage);
        return doStreamRequest(messages,temperature);
    }

    /**
     * 流式请求
     * @param messages
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages ,Float temperature){
        // 构造请求

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(true)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        // 调用
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getFlowable();
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI调用失败");
        }
    }

    /**
     * 通用请求
     * @param messages
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(List<ChatMessage> messages,Boolean stream,Float temperature){
        // 构造请求

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        // 调用
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).toString();
        }catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI调用失败");
        }
    }

}
