package com.cmq.upvote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmq.upvote.exception.BusinessException;
import com.cmq.upvote.exception.ErrorCode;
import com.cmq.upvote.mapper.ThumbMapper;
import com.cmq.upvote.model.dto.DoThumbRequest;
import com.cmq.upvote.model.entity.Blog;
import com.cmq.upvote.model.entity.Thumb;
import com.cmq.upvote.model.entity.User;
import com.cmq.upvote.service.BlogService;
import com.cmq.upvote.service.ThumbService;
import com.cmq.upvote.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author CMQ233
 * @description 针对表【thumb】的数据库操作Service实现
 * @createDate 2025-04-17 22:23:20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;


    /**
     * 点赞
     *
     * @param doThumbRequest 点赞请求
     * @param request        HttpServletRequest
     * @return 是否点赞成功
     */
    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {
            return transactionTemplate.execute(status -> {
                // 先查询是否存在
                Long blogId = doThumbRequest.getBlogId();
                boolean exists = this.lambdaQuery()
                        .eq(Thumb::getUserId, loginUser.getId())
                        .eq(Thumb::getBlogId, blogId)
                        .exists();
                if (exists) {
                    throw new RuntimeException("用户已点赞");
                }
                //点赞数+1
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                // 更新成功才执行
                return update && this.save(thumb);
            });
        }
    }

    /**
     * 取消点赞
     *
     * @param doThumbRequest 取消点赞请求
     * @param request        HttpServletRequest
     * @return 是否取消点赞成功
     */
    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Thumb thumb = this.lambdaQuery()
                        .eq(Thumb::getUserId, loginUser.getId())
                        .eq(Thumb::getBlogId, blogId)
                        .one();
                if (thumb == null) {
                    throw new RuntimeException("用户未点赞");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                // 更新成功才执行
                return update && this.removeById(thumb.getId());
            });
        }
    }

}





