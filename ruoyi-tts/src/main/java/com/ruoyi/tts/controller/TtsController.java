package com.ruoyi.tts.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.tts.dto.AuthInfo;
import com.ruoyi.tts.dto.LoginParam;
import com.ruoyi.tts.dto.SayParam;
import com.ruoyi.tts.dto.Session;
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
        String token = UUID.fastUUID().toString();
        redisCache.setCacheObject(token, session);
        return AjaxResult.success().put(Constants.TOKEN, token);
    }

    @RequestMapping("/say")
    public AjaxResult say(@RequestBody SayParam sayParam) {
        String token = ServletUtils.getRequest().getHeader(Constants.TOKEN);
        Session session = redisCache.getCacheObject(token);
        JSONObject jsonObject = ttsService.say(session, sayParam.getText());
        if (jsonObject.getInteger("code") != 0) {
            return AjaxResult.error("我猜你没输入内容！");
        }
        return AjaxResult.success(jsonObject);
    }
}
