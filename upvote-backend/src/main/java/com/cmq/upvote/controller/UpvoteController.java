package com.cmq.upvote.controller;

import com.cmq.upvote.common.BaseResponse;
import com.cmq.upvote.common.ResultUtils;
import com.cmq.upvote.model.dto.DoUpvoteRequest;
import com.cmq.upvote.service.UpvoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "点赞模块", description = "点赞相关接口")
@RestController
@RequestMapping("upvote")
public class UpvoteController {
    @Resource
    private UpvoteService upvoteService;

    @Operation(summary = "点赞")
    @PostMapping("/do")
    public BaseResponse<Boolean> doUpvote(@RequestBody DoUpvoteRequest doUpvoteRequest, HttpServletRequest request) {
        Boolean success = upvoteService.doUpvote(doUpvoteRequest, request);
        return ResultUtils.success(success);
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/undo")
    public BaseResponse<Boolean> undoUpvote(@RequestBody DoUpvoteRequest doUpvoteRequest, HttpServletRequest request) {
        Boolean success = upvoteService.undoUpvote(doUpvoteRequest, request);
        return ResultUtils.success(success);
    }
}
