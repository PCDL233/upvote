package com.cmq.upvote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmq.upvote.model.dto.DoUpvoteRequest;
import com.cmq.upvote.model.entity.Upvote;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author CMQ233
 * @description 针对表【upvote】的数据库操作Service
 * @createDate 2025-04-17 22:23:20
 */
public interface UpvoteService extends IService<Upvote> {

    Boolean doUpvote(DoUpvoteRequest doUpvoteRequest, HttpServletRequest request);

    Boolean undoUpvote(DoUpvoteRequest doUpvoteRequest, HttpServletRequest request);

    Boolean hasUpvote(Long blogId, Long userId);
}
