package com.cmq.upvote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmq.upvote.model.entity.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author CMQ233
 * @description 针对表【blog】的数据库操作Mapper
 * @createDate 2025-04-17 22:21:31
 * @Entity generator.domain.Blog
 */
public interface BlogMapper extends BaseMapper<Blog> {

    /**
     * 批量更新点赞数
     *
     * @param countMap Map<Long, Long>，key为博客ID，value为点赞数
     */
    void batchUpdateUpvoteCount(@Param("countMap") Map<Long, Long> countMap);
}




