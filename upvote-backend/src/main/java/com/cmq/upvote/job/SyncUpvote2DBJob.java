package com.cmq.upvote.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmq.upvote.mapper.BlogMapper;
import com.cmq.upvote.model.entity.Upvote;
import com.cmq.upvote.model.enums.UpvoteTypeEnum;
import com.cmq.upvote.service.UpvoteService;
import com.cmq.upvote.utils.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时将 Redis 中的临时点赞数据同步到数据库
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SyncUpvote2DBJob {

    private final UpvoteService upvoteService;

    private final BlogMapper blogMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每10秒将 Redis 中的临时点赞数据同步到数据库
     * 10秒的时间是为了避免 Redis 中的数据过多，导致内存占用过高
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行");
        DateTime nowDate = DateUtil.date();
        String date = DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10 - 1) * 10;
        //将redis中的数据同步到数据库
        syncThumb2DBByDate(date);
        log.info("临时数据同步完成");
    }


    /**
     * 同步点赞数据到数据库
     *
     * @param date 时间戳
     */
    public void syncThumb2DBByDate(String date) {
        // 获取到临时点赞和取消点赞数据  
        String tempUpvoteKey = RedisKeyUtil.getTempUpvoteKey(date);
        // 获取到所有的临时点赞数据
        Map<Object, Object> allTempUpvoteMap = redisTemplate.opsForHash().entries(tempUpvoteKey);
        boolean upvoteMapEmpty = CollUtil.isEmpty(allTempUpvoteMap);
        if (upvoteMapEmpty) {
            return;
        }

        // 同步 点赞 到数据库  
        // 构建插入列表并收集blogId  
        Map<Long, Long> blogThumbCountMap = new HashMap<>();
        ArrayList<Upvote> upvoteList = new ArrayList<>();
        LambdaQueryWrapper<Upvote> wrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;
        // 遍历临时点赞数据，构建插入列表
        for (Object userIdBlogIdObj : allTempUpvoteMap.keySet()) {
            String userIdBlogId = (String) userIdBlogIdObj;
            String[] userIdAndBlogId = userIdBlogId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndBlogId[0]);
            Long blogId = Long.valueOf(userIdAndBlogId[1]);
            // -1 取消点赞，1 点赞  
            Integer upvoteType = Integer.valueOf(allTempUpvoteMap.get(userIdBlogId).toString());
            if (upvoteType == UpvoteTypeEnum.INCR.getValue()) {
                Upvote upvote = new Upvote();
                upvote.setUserId(userId);
                upvote.setBlogId(blogId);
                upvoteList.add(upvote);
            } else if (upvoteType == UpvoteTypeEnum.DECR.getValue()) {
                // 拼接查询条件，批量删除  
                needRemove = true;
                wrapper.or().eq(Upvote::getUserId, userId).eq(Upvote::getBlogId, blogId);
            } else {
                if (upvoteType != UpvoteTypeEnum.NON.getValue()) {
                    log.warn("数据异常：{}", userId + "," + blogId + "," + upvoteType);
                }
                continue;
            }
            // 计算点赞增量  
            blogThumbCountMap.put(blogId, blogThumbCountMap.getOrDefault(blogId, 0L) + upvoteType);
        }
        // 批量插入  
        upvoteService.saveBatch(upvoteList);
        // 批量删除  
        if (needRemove) {
            upvoteService.remove(wrapper);
        }
        // 批量更新博客点赞量  
        if (!blogThumbCountMap.isEmpty()) {
            blogMapper.batchUpdateUpvoteCount(blogThumbCountMap);
        }
        // 使用虚拟线程异步删除 Redis 中的临时点赞数据
        Thread.startVirtualThread(() -> {
            redisTemplate.delete(tempUpvoteKey);
        });
    }
}
