package com.cmq.upvote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmq.upvote.model.entity.Blog;
import com.cmq.upvote.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author CMQ233
 * @description 针对表【blog】的数据库操作Service
 * @createDate 2025-04-17 22:21:31
 */
public interface BlogService extends IService<Blog> {

    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request);

}
