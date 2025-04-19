package com.cmq.upvote.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.cmq.upvote.constant.UpvoteConstant;
import com.cmq.upvote.utils.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 定时将 Redis 中的临时点赞数据同步到数据库的补偿措施
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SyncThumb2DBCompensatoryJob {

    private final RedisTemplate<String, Object> redisTemplate;

    private final SyncUpvote2DBJob syncUpvote2DBJob;

    /**
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        log.info("开始补偿数据");
        Set<String> thumbKeys = redisTemplate.keys(RedisKeyUtil.getTempUpvoteKey("") + "*");
        Set<String> needHandleDataSet = new HashSet<>();
        thumbKeys.stream().filter(ObjUtil::isNotNull)
                .forEach(thumbKey -> needHandleDataSet
                        .add(thumbKey.replace(UpvoteConstant.TEMP_UPVOTE_KEY_PREFIX.formatted(""), ""))
                );

        if (CollUtil.isEmpty(needHandleDataSet)) {
            log.info("没有需要补偿的临时数据");
            return;
        }
        // 补偿数据
        for (String date : needHandleDataSet) {
            syncUpvote2DBJob.syncThumb2DBByDate(date);
        }
        log.info("临时数据补偿完成");
    }
}
