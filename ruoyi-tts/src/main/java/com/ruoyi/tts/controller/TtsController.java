package com.ruoyi.tts.controller;

import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.tts.dto.AuthInfo;
import com.ruoyi.tts.dto.LoginParam;
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

    @RequestMapping("/login")
    public AjaxResult login(@RequestBody LoginParam loginParam) {
        AuthInfo authInfo = ttsService.serviceAuth(loginParam.getUsername(), loginParam.getPassword());
        Session session = ttsService.loginMiAi(authInfo);
        JSONArray devices = ttsService.getDevice(session);
        if (devices.size() == 0) {
            return AjaxResult.error(201, "没有在线设备");
        }
        return AjaxResult.success(devices);
    }
}
