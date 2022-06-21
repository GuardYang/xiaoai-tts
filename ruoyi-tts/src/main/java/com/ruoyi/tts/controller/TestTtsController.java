package com.ruoyi.tts.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.tts.dto.AuthInfo;
import com.ruoyi.tts.dto.Session;
import com.ruoyi.tts.service.TtsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test/tts")
public class TestTtsController {
    @Autowired
    private TtsService ttsService;
    @Autowired
    private RedisCache redisCache;
    @RequestMapping("/setVolume")
    public AjaxResult setVolume(Integer volume) {
        JSONObject jsonObject = ttsService.setVolume(getSession(),volume);
        return AjaxResult.success(jsonObject);
    }

    @RequestMapping("/getVolume")
    public AjaxResult getVolume() {
        JSONObject jsonObject = ttsService.getVolume(getSession());
        return AjaxResult.success(jsonObject);
    }

    @RequestMapping("/serviceAuth")
    public AjaxResult serviceAuth(String username, String password) {
        AuthInfo authInfo = ttsService.serviceAuth(username, password);
        return AjaxResult.success(authInfo);
    }

    @RequestMapping("/loginMiAi")
    public AjaxResult loginMiAi(String username, String password) {
        AuthInfo authInfo = ttsService.serviceAuth(username, password);
        Session session = ttsService.loginMiAi(authInfo);
        return AjaxResult.success(session);
    }

    @RequestMapping("/getDevice")
    public AjaxResult getDevice(String username, String password) {
        AuthInfo authInfo = ttsService.serviceAuth(username, password);
        Session session = ttsService.loginMiAi(authInfo);
        JSONArray devices = ttsService.getDevice(session);
        return AjaxResult.success(devices);
    }

    @RequestMapping("/say")
    public AjaxResult say(String username, String password) {
        AuthInfo authInfo = ttsService.serviceAuth(username, password);
        Session session = ttsService.loginMiAi(authInfo);
        JSONArray devices = ttsService.getDevice(session);
        System.out.println(devices);
        session.setDeviceId(devices.getJSONObject(0).getString("deviceID"));
        return AjaxResult.success(ttsService.say(session, "专门为你选的"));
    }
    private Session getSession() {
        String token = ServletUtils.getRequest().getHeader(Constants.TOKEN);
        if (StrUtil.isEmpty(token)) {
            token = ServletUtils.getRequest().getParameter(Constants.TOKEN);
        }
        if (StrUtil.isEmpty(token)) {
            throw new ServiceException("token不存在", 401);
        }
        Session session = redisCache.getCacheObject(Constants.TTS_TOKEN_KEY + token);
        if (session == null) {
            throw new ServiceException("token过期", 401);
        }
        return session;
    }
}
