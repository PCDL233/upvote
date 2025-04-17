package com.cmq.upvote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmq.upvote.constant.UserConstant;
import com.cmq.upvote.exception.BusinessException;
import com.cmq.upvote.exception.ErrorCode;
import com.cmq.upvote.mapper.UserMapper;
import com.cmq.upvote.model.entity.User;
import com.cmq.upvote.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * @author CMQ233
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2025-04-17 20:50:34
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //获取当前登录用户
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户未登录");
        }
        return (User) attribute;
    }
}




