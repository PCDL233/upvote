package com.cmq.upvote.controller;

import com.cmq.upvote.common.BaseResponse;
import com.cmq.upvote.common.ResultUtils;
import com.cmq.upvote.constant.UserConstant;
import com.cmq.upvote.model.entity.User;
import com.cmq.upvote.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户模块", description = "用户模块相关接口")
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "登录")
    @GetMapping("/login")
    public BaseResponse<User> login(long userId, HttpServletRequest request) {
        User user = userService.getById(userId);
        request.getSession().setAttribute(UserConstant.USER_LOGIN, user);
        return ResultUtils.success(user);
    }

    @Operation(summary = "获取登录用户")
    @GetMapping("/get/login")
    public BaseResponse<User> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(loginUser);
    }
}
