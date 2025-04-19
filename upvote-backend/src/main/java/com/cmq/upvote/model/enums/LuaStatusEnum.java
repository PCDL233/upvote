package com.cmq.upvote.model.enums;

import lombok.Getter;

/**
 * 标识lua脚本执行状态
 */
@Getter
public enum LuaStatusEnum {
    // 成功  
    SUCCESS(1L),
    // 失败  
    FAILED(-1L);

    private final long value;

    LuaStatusEnum(long value) {
        this.value = value;
    }
}
