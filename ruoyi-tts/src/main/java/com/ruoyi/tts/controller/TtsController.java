package com.ruoyi.tts.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.tts.dto.*;
import com.ruoyi.tts.service.TtsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tts")
public class TtsController {
    @Autowired
    private TtsService ttsService;
    @Autowired
    private RedisCache redisCache;

    @RequestMapping("/login")
    public AjaxResult login(@RequestBody LoginParam loginParam) {
        AuthInfo authInfo = ttsService.serviceAuth(loginParam.getUsername(), loginParam.getPassword());
        Session session = ttsService.loginMiAi(authInfo);
        JSONArray devices = ttsService.getDevice(session);
        if (devices.size() < 1) {
            return AjaxResult.error(201, "没有在线设备");
        }
        session.setDeviceId(devices.getJSONObject(0).getString("deviceID"));

        redisCache.setCacheObject(Constants.TTS_TOKEN_KEY + session.getDeviceId(), session);
        return AjaxResult.success().put(Constants.TOKEN, session.getDeviceId());
    }

    @RequestMapping("/say")
    public AjaxResult say(@RequestBody SayParam sayParam) {
        JSONObject jsonObject = ttsService.say(getSession(), sayParam.getText());
        if (jsonObject.getInteger("code") != 0) {
            return AjaxResult.error("我猜你没输入内容");
        }
        return AjaxResult.success(jsonObject);
    }

    @RequestMapping("/share")
    public AjaxResult share() {
        String token = UUID.fastUUID().toString();
        redisCache.setCacheObject(Constants.TTS_SHARE_TOKEN_KEY + token, getSession());
        return AjaxResult.success().put(Constants.TOKEN, token);
    }

    @RequestMapping("/praise")
    public AjaxResult praise(@RequestBody PraiseParam praiseParam) {
        Session session = redisCache.getCacheObject(Constants.TTS_SHARE_TOKEN_KEY + praiseParam.getToken());
        if (session == null) {
            return AjaxResult.error("链接已失效");
        }
        JSONObject jsonObject = ttsService.say(session, praiseParam.getText());
        if (jsonObject.getInteger("code") != 0) {
            return AjaxResult.error("我猜你没输入内容");
        }
        return AjaxResult.success(jsonObject);
    }

    private Session getSession() {
        String token = ServletUtils.getRequest().getHeader(Constants.TOKEN);
        Session session = redisCache.getCacheObject(Constants.TTS_TOKEN_KEY + token);
        if (session == null) {
            throw new ServiceException("token过期", 401);
        }
        return session;
    }
}
