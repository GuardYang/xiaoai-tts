package com.ruoyi.tts.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.tts.dto.AuthInfo;
import com.ruoyi.tts.dto.Session;

public interface TtsService {
    /**
     * 账号密码校验
     *
     * @param username
     * @param password
     * @return
     */
    AuthInfo serviceAuth(String username, String password);

    /**
     * 获取身份凭证
     *
     * @param authInfo
     * @return
     */
    Session loginMiAi(AuthInfo authInfo);

    /**
     * 获取在线设备列表
     *
     * @param session
     * @return
     */
    JSONArray getDevice(Session session);

    /**
     * 朗读文本
     *
     * @param session
     * @param text
     * @return
     */
    JSONObject say(Session session, String text);
}
