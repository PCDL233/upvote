package com.cmq.upvote.controller;

import com.cmq.upvote.common.BaseResponse;
import com.cmq.upvote.common.ResultUtils;
import com.cmq.upvote.model.entity.Blog;
import com.cmq.upvote.model.vo.BlogVO;
import com.cmq.upvote.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "博客模块", description = "博客相关接口")
@RestController
@RequestMapping("blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "获取博客详情", description = "根据博客ID获取博客详情")
    @GetMapping("/get")
    public BaseResponse<BlogVO> get(long blogId, HttpServletRequest request) {
        BlogVO blogVO = blogService.getBlogVOById(blogId, request);
        return ResultUtils.success(blogVO);
    }

    @Operation(summary = "获取博客列表", description = "获取所有博客列表")
    @GetMapping("/list")
    public BaseResponse<List<BlogVO>> list(HttpServletRequest request) {
        List<Blog> blogList = blogService.list();
        List<BlogVO> blogVOList = blogService.getBlogVOList(blogList, request);
        return ResultUtils.success(blogVOList);
    }

}
