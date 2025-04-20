package com.cmq.upvote.constant;

public interface UpvoteConstant {

    /**
     * 用户点赞 hash key
     */
    String USER_UPVOTE_KEY_PREFIX = "upvote:";

    /**
     * 用户点赞记录 key
     */
    Long UN_UPVOTE_CONSTANT = 0L;

    /**
     * 临时 点赞记录 key
     */
    String TEMP_UPVOTE_KEY_PREFIX = "upvote:temp:%s";
}
