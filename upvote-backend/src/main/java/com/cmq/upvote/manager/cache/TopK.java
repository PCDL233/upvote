package com.cmq.upvote.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * TopK 接口定义了一个用于管理和维护 Top K 项的集合。
 * 该接口提供了添加项、获取项列表、获取被驱逐项的队列等方法。
 */
public interface TopK {

    /**
     * 添加一个具有指定键和增量的项到集合中。
     * 如果键已存在，其计数将增加指定的增量值。
     *
     * @param key       要添加的项的键
     * @param increment 增加的计数值
     * @return 添加结果，包括项是新增还是更新
     */
    AddResult add(String key, int increment);

    /**
     * 获取当前集合中所有元素的列表。
     *
     * @return 包含所有项的列表
     */
    List<Item> list();

    /**
     * 获取被挤出TopK的元素的队列。
     */
    BlockingQueue<Item> expelled();

    /**
     * 对所有元素进行衰减处理。
     */
    void fading();

    /**
     * 获取当前集合中元素的总数。
     */
    long total();
}