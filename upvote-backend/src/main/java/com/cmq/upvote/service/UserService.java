package com.cmq.upvote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmq.upvote.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author CMQ233
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-17 20:50:34
*/
public interface UserService extends IService<User> {
    User getLoginUser(HttpServletRequest request);
}
