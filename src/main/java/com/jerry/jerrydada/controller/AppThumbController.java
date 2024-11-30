package com.jerry.jerrydada.controller;

import com.jerry.jerrydada.common.BaseResponse;
import com.jerry.jerrydada.common.ErrorCode;
import com.jerry.jerrydada.common.ResultUtils;
import com.jerry.jerrydada.exception.BusinessException;
import com.jerry.jerrydada.model.dto.appthumb.AppThumbAddRequest;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.User;
import com.jerry.jerrydada.model.vo.AppThumbVO;
import com.jerry.jerrydada.service.AppThumbService;
import com.jerry.jerrydada.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/appThumb")
@Slf4j
public class AppThumbController {

    @Resource
    private AppThumbService appThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param appThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody AppThumbAddRequest appThumbAddRequest,
                                         HttpServletRequest request) {
        if (appThumbAddRequest == null || appThumbAddRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long appId = appThumbAddRequest.getAppId();
        int result = appThumbService.doAppThumb(appId, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/hasThumbed")
    public BaseResponse<AppThumbVO> hasThumbed(@RequestBody AppThumbAddRequest appThumbAddRequest, HttpServletRequest request) {
        if (appThumbAddRequest == null || appThumbAddRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long appId = appThumbAddRequest.getAppId();
        AppThumbVO appThumbVO = appThumbService.hasThumbed(appId, loginUser);
        return ResultUtils.success(appThumbVO);
    }

}
