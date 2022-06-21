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

    // ————————————————  媒体控制  ————————————————

    JSONObject setVolume(Session session, Integer volume); // 设置音量，最低 6

    JSONObject getVolume(Session session); // 获取音量

    JSONObject play(Session session); // 继续播放

    JSONObject pause(Session session); // 暂停播放

    JSONObject togglePlayState(Session session); // 切换播放状态(播放/暂停)

    JSONObject prev(Session session); // 播放上一曲

    JSONObject next(Session session); // 播放下一曲

    JSONObject getSongInfo(String songId); // 查询歌曲信息

    JSONObject getMyPlaylist(String listId); // 获取用户自建歌单，当指定 listId 时，将返回目标歌单内的歌曲列表

    JSONObject playUrl(String url); // 播放在线音频
}
