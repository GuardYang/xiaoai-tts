package com.ruoyi.tts.service;

import com.alibaba.fastjson2.JSONObject;

public interface TtsService {
    JSONObject serviceAuth(String username, String password);

    String loginMiAi(JSONObject authInfo);
}
