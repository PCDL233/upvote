package com.cmq.upvote.utils;


import com.cmq.upvote.constant.UpvoteConstant;

/**
 * @author pine
 */
public class RedisKeyUtil {

    /**
     * 获取用户点赞记录 key
     *
     * @param userId 用户id
     * @return 点赞记录 key
     */
    public static String getUserUpvoteKey(Long userId) {
        return UpvoteConstant.USER_UPVOTE_KEY_PREFIX + userId;
    }

    /**
     * 获取 临时点赞记录 key
     */
    public static String getTempUpvoteKey(String time) {
        return UpvoteConstant.TEMP_UPVOTE_KEY_PREFIX.formatted(time);
    }

}
