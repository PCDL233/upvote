package com.cmq.upvote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmq.upvote.model.dto.DoThumbRequest;
import com.cmq.upvote.model.entity.Thumb;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author CMQ233
 * @description 针对表【thumb】的数据库操作Service
 * @createDate 2025-04-17 22:23:20
 */
public interface ThumbService extends IService<Thumb> {

    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);
}
