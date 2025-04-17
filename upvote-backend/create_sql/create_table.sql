-- 用户表
CREATE TABLE IF NOT EXISTS USER
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(128) NOT NULL
);

-- 博客表
CREATE TABLE IF NOT EXISTS blog
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId     BIGINT                             NOT NULL,
    title      VARCHAR(512)                       NULL COMMENT '标题',
    coverImg   VARCHAR(1024)                      NULL COMMENT '封面',
    content    TEXT                               NOT NULL COMMENT '内容',
    thumbCount INT      DEFAULT 0                 NOT NULL COMMENT '点赞数',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
CREATE INDEX idx_userId ON blog (userId);


-- 点赞记录表
CREATE TABLE IF NOT EXISTS thumb
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    userId     BIGINT                             NOT NULL,
    blogId     BIGINT                             NOT NULL,
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间'
);
CREATE UNIQUE INDEX idx_userId_blogId ON thumb (userId, blogId);
