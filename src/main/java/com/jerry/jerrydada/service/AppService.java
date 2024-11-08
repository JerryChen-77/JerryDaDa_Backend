package com.jerry.jerrydada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jerry.jerrydada.model.dto.app.AppQueryRequest;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.entity.App;
import com.jerry.jerrydada.model.vo.AppVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Jerry Chen
* @description 针对表【app(应用)】的数据库操作Service
* @createDate 2024-11-08 15:43:46
*/
public interface AppService extends IService<App> {
    /**
     * 校验
     *
     * @param app
     * @param add
     */
    void validApp(App app, boolean add);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);
    

    /**
     * 获取帖子封装
     *
     * @param app
     * @param request
     * @return
     */
    AppVO getAppVO(App app, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param appPage
     * @param request
     * @return
     */
    Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request);

}
