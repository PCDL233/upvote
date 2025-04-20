package com.cmq.upvote.manager.cache;

/**
 * @param expelledKey 被挤出的 key
 * @param isHotKey    当前 key 是否进入 TopK
 * @param currentKey  当前操作的 key
 */ // 新增返回结果类
public record AddResult(String expelledKey, boolean isHotKey, String currentKey) {
}