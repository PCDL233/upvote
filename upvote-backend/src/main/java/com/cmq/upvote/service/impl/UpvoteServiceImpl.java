package com.cmq.upvote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmq.upvote.constant.UpvoteConstant;
import com.cmq.upvote.exception.BusinessException;
import com.cmq.upvote.exception.ErrorCode;
import com.cmq.upvote.manager.cache.CacheManager;
import com.cmq.upvote.mapper.UpvoteMapper;
import com.cmq.upvote.model.dto.DoUpvoteRequest;
import com.cmq.upvote.model.entity.Blog;
import com.cmq.upvote.model.entity.Upvote;
import com.cmq.upvote.model.entity.User;
import com.cmq.upvote.service.BlogService;
import com.cmq.upvote.service.UpvoteService;
import com.cmq.upvote.service.UserService;
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
@Service("upvoteService")
@Slf4j
@RequiredArgsConstructor
public class UpvoteServiceImpl extends ServiceImpl<UpvoteMapper, Upvote> implements UpvoteService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    //缓存管理
    private final CacheManager cacheManager;

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
                    String hashKey = UpvoteConstant.USER_UPVOTE_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    Long upvoteId = upvote.getId();
                    // 将点赞记录存入 Redis
                    redisTemplate.opsForHash().put(hashKey, fieldKey, upvoteId);
                    //如果本地缓存中已有该数据，则更新本地缓存
                    cacheManager.putIfPresent(hashKey, fieldKey, upvoteId);
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
                //从多级缓存中查询是否点赞
                Object upvoteObj = cacheManager.get(UpvoteConstant.USER_UPVOTE_KEY_PREFIX + loginUser.getId(), blogId.toString());
                if (upvoteObj == null || upvoteObj.equals(UpvoteConstant.UN_UPVOTE_CONSTANT)) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                // 更新成功才执行
                boolean success = update && this.removeById((Long) upvoteObj);
                if (success) {
                    // 将点赞记录从缓存中删除
                    String hashKey = UpvoteConstant.USER_UPVOTE_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    redisTemplate.opsForHash().delete(hashKey, fieldKey);
                    //如果本地缓存中已有该数据，则更新本地缓存
                    cacheManager.putIfPresent(hashKey, fieldKey, UpvoteConstant.UN_UPVOTE_CONSTANT);
                    return true;
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消点赞失败");
                }
            });
        }
    }

    /**
     * 使用多级缓存机制
     * <p>
     * 先从本地缓存中查询是否点赞
     * 如果没有，则从 Redis 中查询
     * 如果 Redis 中也没有，则返回 false
     *
     * @param blogId 博客id
     * @param userId 用户id
     * @return 是否点赞
     */
    @Override
    public Boolean hasUpvote(Long blogId, Long userId) {
        Object upvoteObj = cacheManager.get(UpvoteConstant.USER_UPVOTE_KEY_PREFIX + userId, blogId.toString());
        if (upvoteObj == null) {
            return false;
        }
        //如果点赞记录为0，则说明没有点赞
        Long upvoteId = (Long) upvoteObj;
        return !UpvoteConstant.UN_UPVOTE_CONSTANT.equals(upvoteId);
    }
}





