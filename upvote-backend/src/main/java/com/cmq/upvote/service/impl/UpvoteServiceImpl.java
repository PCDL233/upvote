package com.cmq.upvote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmq.upvote.constant.UpvoteConstant;
import com.cmq.upvote.exception.BusinessException;
import com.cmq.upvote.exception.ErrorCode;
import com.cmq.upvote.mapper.UpvoteMapper;
import com.cmq.upvote.model.dto.DoUpvoteRequest;
import com.cmq.upvote.model.entity.Blog;
import com.cmq.upvote.model.entity.Upvote;
import com.cmq.upvote.model.entity.User;
import com.cmq.upvote.service.BlogService;
import com.cmq.upvote.service.UpvoteService;
import com.cmq.upvote.service.UserService;
import com.cmq.upvote.utils.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
public class UpvoteServiceImpl extends ServiceImpl<UpvoteMapper, Upvote> implements UpvoteService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     *
     * @param doUpvoteRequest 点赞请求
     * @param request         HttpServletRequest
     * @return 是否点赞成功
     */
    @Override
    public Boolean doUpvote(DoUpvoteRequest doUpvoteRequest, HttpServletRequest request) {
        if (doUpvoteRequest == null || doUpvoteRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {
            return transactionTemplate.execute(status -> {
                // 先查询是否存在
                Long blogId = doUpvoteRequest.getBlogId();
                //从redis中查询是否点赞
                Boolean exists = this.hasUpvote(blogId, loginUser.getId());
                if (exists) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
                }
                //点赞数+1
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Upvote upvote = new Upvote();
                upvote.setUserId(loginUser.getId());
                upvote.setBlogId(blogId);
                // 更新成功才执行
                boolean success = update && this.save(upvote);
                if (success) {
                    // 将点赞记录存入redis
                    redisTemplate.opsForHash().put(UpvoteConstant.USER_UPVOTE_KEY_PREFIX + loginUser.getId().toString(),
                            blogId.toString(), upvote.getId());
                    return true;
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "点赞失败");
                }
            });
        }
    }

    /**
     * 取消点赞
     *
     * @param doUpvoteRequest 取消点赞请求
     * @param request         HttpServletRequest
     * @return 是否取消点赞成功
     */
    @Override
    public Boolean undoUpvote(DoUpvoteRequest doUpvoteRequest, HttpServletRequest request) {
        if (doUpvoteRequest == null || doUpvoteRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {
            return transactionTemplate.execute(status -> {
                Long blogId = doUpvoteRequest.getBlogId();
                //从redis中查询是否点赞
                Long upvoteId = null;
                Object value = redisTemplate.opsForHash()
                        .get(UpvoteConstant.USER_UPVOTE_KEY_PREFIX + loginUser.getId().toString(), blogId.toString());
                if (value instanceof Integer) {
                    upvoteId = Long.valueOf((Integer) value);
                } else if (value instanceof Long) {
                    upvoteId = (Long) value;
                }
                if (upvoteId == null) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                // 更新成功才执行
                boolean success = update && this.removeById(upvoteId);
                if (success) {
                    // 将点赞记录从redis中删除
                    redisTemplate.opsForHash().delete(UpvoteConstant.USER_UPVOTE_KEY_PREFIX + loginUser.getId().toString(),
                            blogId.toString());
                    return true;
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消点赞失败");
                }
            });
        }
    }

    /**
     * 在redis中查询是否点赞
     *
     * @param blogId 博客id
     * @param userId 用户id
     * @return 是否点赞
     */
    @Override
    public Boolean hasUpvote(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserUpvoteKey(userId), blogId.toString());
    }
}





