package com.github.tartaricacid.netmusic.api.lyric;

import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicLyric;
import com.github.tartaricacid.netmusic.api.pojo.VipLyric;
import com.google.gson.Gson;

/**
 * VIP歌词转换工具
 * 将VIP API的歌词格式转换为标准的NetEaseMusicLyric格式
 */
public class VipLyricConverter {
    private static final Gson GSON = new Gson();

    /**
     * 将VIP歌词API响应转换为标准JSON格式
     * @param vipLyricJson VIP歌词API的JSON响应
     * @return 标准格式的歌词JSON字符串
     */
    public static String convertToStandardFormat(String vipLyricJson) {
        if (vipLyricJson == null || vipLyricJson.isEmpty()) {
            return createEmptyLyricJson();
        }

        try {
            VipLyric vipLyric = GSON.fromJson(vipLyricJson, VipLyric.class);
            return convertToStandardFormat(vipLyric);
        } catch (Exception e) {
            return createEmptyLyricJson();
        }
    }

    /**
     * 将VipLyric对象转换为标准格式的JSON
     * @param vipLyric VIP歌词对象
     * @return 标准格式的歌词JSON字符串
     */
    public static String convertToStandardFormat(VipLyric vipLyric) {
        if (vipLyric == null) {
            return createEmptyLyricJson();
        }

        // 构建符合NetEaseMusicLyric格式的JSON
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"code\":200,");
        
        // 原始歌词
        json.append("\"lrc\":{");
        if (vipLyric.hasLyric()) {
            json.append("\"lyric\":").append(GSON.toJson(vipLyric.getLyric()));
        } else {
            json.append("\"lyric\":\"\"");
        }
        json.append("},");
        
        // 翻译歌词
        json.append("\"tlyric\":{");
        if (vipLyric.hasTransLyric()) {
            json.append("\"lyric\":").append(GSON.toJson(vipLyric.getTransLyric()));
        } else {
            json.append("\"lyric\":\"\"");
        }
        json.append("}");
        
        json.append("}");
        
        return json.toString();
    }

    /**
     * 创建空歌词的标准JSON
     */
    private static String createEmptyLyricJson() {
        return "{\"code\":200,\"lrc\":{\"lyric\":\"\"},\"tlyric\":{\"lyric\":\"\"}}";
    }

    /**
     * 验证转换后的JSON是否可以正常解析
     */
    public static boolean validateConvertedJson(String json) {
        try {
            NetEaseMusicLyric lyric = GSON.fromJson(json, NetEaseMusicLyric.class);
            return lyric != null && lyric.code() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
