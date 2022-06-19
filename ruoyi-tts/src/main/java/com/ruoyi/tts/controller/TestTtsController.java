package com.ruoyi.tts.controller;

import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.core.domain.AjaxResult;
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

    @RequestMapping("/hello")
    public AjaxResult hello() {
        return AjaxResult.success();
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
}
