package com.cmq.upvote.model.dto;

import lombok.Data;

/**
 * 点赞请求
 */
@Data
public class DoUpvoteRequest {

    /**
     * 博客id
     */
    private Long blogId;
}
