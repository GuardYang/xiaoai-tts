package com.ruoyi.tts.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public interface TtsService {
    /**
     * 账号密码校验
     *
     * @param username
     * @param password
     * @return
     */
    JSONObject serviceAuth(String username, String password);

    /**
     * 获取身份凭证
     *
     * @param authInfo
     * @return
     */
    JSONObject loginMiAi(JSONObject authInfo);

    /**
     * 获取在线设备列表
     *
     * @param session
     * @return
     */
    JSONArray getDevice(JSONObject session);

    /**
     * 朗读文本
     *
     * @param session
     * @param text
     * @return
     */
    JSONObject say(JSONObject session, String text);
}
