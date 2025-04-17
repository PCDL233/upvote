package com.cmq.upvote.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmq.upvote.mapper.BlogMapper;
import com.cmq.upvote.model.entity.Blog;
import com.cmq.upvote.model.entity.Thumb;
import com.cmq.upvote.model.entity.User;
import com.cmq.upvote.model.vo.BlogVO;
import com.cmq.upvote.service.BlogService;
import com.cmq.upvote.service.ThumbService;
import com.cmq.upvote.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author CMQ233
 * @description 针对表【blog】的数据库操作Service实现
 * @createDate 2025-04-17 22:21:31
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;

    /**
     * 获取博客详情
     *
     * @param blogId  博客id
     * @param request 请求
     * @return {@link BlogVO}
     */
    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    /**
     * 获取博客列表
     *
     * @param blogList 博客列表
     * @param request  请求
     * @return {@link List}<{@link BlogVO}>
     */
    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if (ObjUtil.isNotEmpty(loginUser)) {
            // 获取当前用户点赞的博客id集合
            Set<Long> blogIdSet = blogList.stream().map(Blog::getId).collect(Collectors.toSet());
            // 获取点赞记录
            List<Thumb> thumbList = thumbService.lambdaQuery()
                    .eq(Thumb::getUserId, loginUser.getId())
                    .in(Thumb::getBlogId, blogIdSet)
                    .list();

            thumbList.forEach(blogThumb -> blogIdHasThumbMap.put(blogThumb.getBlogId(), true));
        }

        return blogList.stream()
                .map(blog -> {
                    BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                    blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                    return blogVO;
                })
                .toList();
    }


    /**
     * 实体对象转换
     *
     * @param blog      博客
     * @param loginUser 登录用户
     * @return {@link BlogVO}
     */
    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog, blogVO);

        if (loginUser == null) {
            return blogVO;
        }

        // 获取点赞状态
        Thumb thumb = thumbService.lambdaQuery()
                .eq(Thumb::getUserId, loginUser.getId())
                .eq(Thumb::getBlogId, blog.getId())
                .one();
        blogVO.setHasThumb(thumb != null);

        return blogVO;
    }
}




