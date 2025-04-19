package com.cmq.upvote.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmq.upvote.constant.RedisLuaScriptConstant;
import com.cmq.upvote.exception.BusinessException;
import com.cmq.upvote.exception.ErrorCode;
import com.cmq.upvote.mapper.UpvoteMapper;
import com.cmq.upvote.model.dto.DoUpvoteRequest;
import com.cmq.upvote.model.entity.Upvote;
import com.cmq.upvote.model.entity.User;
import com.cmq.upvote.model.enums.LuaStatusEnum;
import com.cmq.upvote.service.UpvoteService;
import com.cmq.upvote.service.UserService;
import com.cmq.upvote.utils.RedisKeyUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 点赞服务实现类
 * <p>
 * 该类实现了 UpvoteService 接口，提供了点赞和取消点赞的功能
 * 点赞和取消点赞的操作是通过 Redis 的 Lua 脚本来实现的
 * <p>
 * 点赞和取消点赞的操作是原子性的，避免了并发问题
 */
@Service("upvoteService")
@Slf4j
@RequiredArgsConstructor
public class UpvoteServiceRedisImpl extends ServiceImpl<UpvoteMapper, Upvote> implements UpvoteService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     *
     * @param doUpvoteRequest 点赞请求
     * @param request         HttpServletRequest
     * @return 布尔值
     */
    @Override
    public Boolean doUpvote(DoUpvoteRequest doUpvoteRequest, HttpServletRequest request) {
        if (doUpvoteRequest == null || doUpvoteRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        Long blogId = doUpvoteRequest.getBlogId();
        // 计算时间片
        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempUpvoteKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserUpvoteKey(loginUser.getId());

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );
        //执行失败
        if (LuaStatusEnum.FAILED.getValue() == result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
        }

        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    /**
     * 取消点赞
     *
     * @param doUpvoteRequest 点赞请求
     * @param request         HttpServletRequest
     * @return 布尔值
     */
    @Override
    public Boolean undoUpvote(DoUpvoteRequest doUpvoteRequest, HttpServletRequest request) {
        if (doUpvoteRequest == null || doUpvoteRequest.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        Long blogId = doUpvoteRequest.getBlogId();
        // 计算时间片
        String timeSlice = getTimeSlice();
        // Redis Key
        String tempThumbKey = RedisKeyUtil.getTempUpvoteKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserUpvoteKey(loginUser.getId());

        // 执行 Lua 脚本
        long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );
        // 执行失败
        if (result == LuaStatusEnum.FAILED.getValue()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
        }
        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    /**
     * 获取当前时间片
     *
     * @return 时间片
     */
    private String getTimeSlice() {
        DateTime nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

    /**
     * 是否点赞
     *
     * @param blogId 博客ID
     * @param userId 用户ID
     * @return 布尔值
     */
    public Boolean hasUpvote(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserUpvoteKey(userId), blogId.toString());
    }
}



