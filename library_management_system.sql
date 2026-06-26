/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 90500 (9.5.0)
 Source Host           : localhost:3306
 Source Schema         : library_management_system

 Target Server Type    : MySQL
 Target Server Version : 90500 (9.5.0)
 File Encoding         : 65001

 Date: 24/02/2026 04:47:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book_info
-- ----------------------------
DROP TABLE IF EXISTS `book_info`;
CREATE TABLE `book_info`  (
  `book_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `isbn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ISBN编号',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '书名',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作者',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类',
  `location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图书馆位置',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` int NOT NULL DEFAULT 0 COMMENT '可用库存',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图书封面URL',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `publication_date` datetime NULL DEFAULT NULL COMMENT '出版日期',
  `publisher` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出版社',
  `rating` decimal(3, 2) NULL DEFAULT NULL COMMENT '评分（0-5）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图书描述',
  PRIMARY KEY (`book_id`) USING BTREE,
  UNIQUE INDEX `isbn`(`isbn` ASC) USING BTREE,
  INDEX `idx_category_title_author`(`category` ASC, `title` ASC, `author` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_info
-- ----------------------------
INSERT INTO `book_info` VALUES (1, '9787111544661', 'Java核心技术卷I', 'Cay S. Horstmann', '计算机科学', 'A区1层01架', 8, 5, NULL, '2025-08-08 03:13:27', '2017-01-15 00:00:00', '机械工业出版社', 4.80, 'Java经典教材，深入浅出讲解核心概念');
INSERT INTO `book_info` VALUES (2, '9787115467225', 'Python编程从入门到实践', 'Eric Matthes', '计算机科学', 'A区1层02架', 10, 7, NULL, '2025-08-18 03:13:27', '2018-03-20 00:00:00', '人民邮电出版社', 4.70, '零基础Python入门首选');
INSERT INTO `book_info` VALUES (3, '9787121358654', 'Spring Boot实战', '汪云飞', '计算机科学', 'A区1层03架', 6, 4, NULL, '2025-09-27 03:13:27', '2019-06-10 00:00:00', '电子工业出版社', 4.50, 'Spring Boot企业级应用开发');
INSERT INTO `book_info` VALUES (4, '9787115429452', 'MySQL必知必会', 'Ben Forta', '计算机科学', 'A区1层04架', 7, 5, NULL, '2025-08-28 03:13:27', '2018-08-01 00:00:00', '人民邮电出版社', 4.60, '快速掌握SQL语法');
INSERT INTO `book_info` VALUES (5, '9787121267337', '算法导论(第3版)', 'Thomas H. Cormen', '计算机科学', 'A区1层05架', 5, 3, NULL, '2025-07-19 03:13:27', '2016-11-20 00:00:00', '机械工业出版社', 4.90, '算法领域的巨著');
INSERT INTO `book_info` VALUES (6, '9787302457589', '深入理解计算机系统', 'Randal E. Bryant', '计算机科学', 'A区1层06架', 4, 2, NULL, '2025-07-29 03:13:27', '2017-05-10 00:00:00', '清华大学出版社', 4.95, '从程序员角度看计算机系统');
INSERT INTO `book_info` VALUES (7, '9787115449900', 'JavaScript高级程序设计', 'Nicholas C. Zakas', '计算机科学', 'A区1层07架', 8, 6, NULL, '2025-09-17 03:13:27', '2019-02-15 00:00:00', '人民邮电出版社', 4.75, '前端开发必读经典');
INSERT INTO `book_info` VALUES (8, '9787121378560', 'Docker从入门到实践', '杨保华', '计算机科学', 'A区1层08架', 6, 4, NULL, '2025-11-16 03:13:27', '2020-08-01 00:00:00', '电子工业出版社', 4.40, '容器技术入门指南');
INSERT INTO `book_info` VALUES (9, '9787115523457', 'Vue.js实战', '梁灏', '计算机科学', 'A区1层09架', 7, 5, NULL, '2025-11-26 03:13:27', '2020-10-20 00:00:00', '人民邮电出版社', 4.55, '全面讲解Vue框架');
INSERT INTO `book_info` VALUES (10, '9787121396785', 'Redis设计与实现', '黄健宏', '计算机科学', 'A区1层10架', 5, 3, NULL, '2025-10-17 03:13:27', '2020-03-15 00:00:00', '机械工业出版社', 4.85, '深度剖析Redis内部实现');
INSERT INTO `book_info` VALUES (11, '9787115486999', '机器学习实战', 'Peter Harrington', '计算机科学', 'A区1层11架', 6, 4, NULL, '2025-10-27 03:13:27', '2020-05-01 00:00:00', '人民邮电出版社', 4.45, '机器学习入门经典');
INSERT INTO `book_info` VALUES (12, '9787302534891', '数据结构与算法分析', 'Mark Allen Weiss', '计算机科学', 'A区1层12架', 8, 6, NULL, '2025-09-07 03:13:27', '2018-12-10 00:00:00', '清华大学出版社', 4.70, '数据结构教材');
INSERT INTO `book_info` VALUES (13, '9787121378799', '微服务架构设计模式', 'Chris Richardson', '计算机科学', 'A区1层13架', 4, 2, NULL, '2025-12-06 03:13:27', '2021-01-15 00:00:00', '电子工业出版社', 4.65, '微服务设计指南');
INSERT INTO `book_info` VALUES (14, '9787115542489', '深度学习入门', '斋藤康毅', '计算机科学', 'A区1层14架', 7, 5, NULL, '2025-12-16 03:13:27', '2021-03-20 00:00:00', '人民邮电出版社', 4.60, '零基础学深度学习');
INSERT INTO `book_info` VALUES (15, '9787111679974', 'Kubernetes权威指南', '龚正', '计算机科学', 'A区1层15架', 5, 3, NULL, '2026-01-05 03:13:27', '2021-06-01 00:00:00', '机械工业出版社', 4.50, 'K8s全面解析');
INSERT INTO `book_info` VALUES (16, '9787020125357', '红楼梦', '曹雪芹', '文学', 'B区2层01架', 10, 8, NULL, '2025-06-19 03:13:27', '2015-03-01 00:00:00', '人民文学出版社', 4.95, '中国古典文学四大名著之一');
INSERT INTO `book_info` VALUES (17, '9787020008735', '活着', '余华', '文学', 'B区2层02架', 12, 9, NULL, '2025-06-29 03:13:27', '2012-08-15 00:00:00', '作家出版社', 4.85, '感人至深的现代文学作品');
INSERT INTO `book_info` VALUES (18, '9787544270878', '平凡的世界', '路遥', '文学', 'B区2层03架', 8, 6, NULL, '2025-07-09 03:13:27', '2013-05-20 00:00:00', '南海出版社', 4.90, '茅盾文学奖获奖作品');
INSERT INTO `book_info` VALUES (19, '9787020002207', '围城', '钱钟书', '文学', 'B区2层04架', 9, 7, NULL, '2025-07-19 03:13:27', '2009-11-01 00:00:00', '人民文学出版社', 4.75, '知识分子的心理写照');
INSERT INTO `book_info` VALUES (20, '9787544291170', '追风筝的人', '[美]卡勒德·胡赛尼', '文学', 'B区2层05架', 11, 8, NULL, '2025-08-08 03:13:27', '2016-09-10 00:00:00', '上海人民出版社', 4.80, '关于救赎与成长的故事');
INSERT INTO `book_info` VALUES (21, '9787020147663', '百年孤独', '[哥伦比亚]加西亚·马尔克斯', '文学', 'B区2层06架', 7, 5, NULL, '2025-08-18 03:13:27', '2017-08-01 00:00:00', '人民文学出版社', 4.88, '魔幻现实主义文学代表作');
INSERT INTO `book_info` VALUES (22, '9787544266451', '解忧杂货店', '[日]东野圭吾', '文学', 'B区2层07架', 10, 7, NULL, '2025-10-07 03:13:27', '2019-04-15 00:00:00', '南海出版社', 4.65, '温暖治愈的奇幻故事');
INSERT INTO `book_info` VALUES (23, '9787020140596', '三体', '刘慈欣', '文学', 'B区2层08架', 15, 10, NULL, '2025-10-27 03:13:27', '2019-12-01 00:00:00', '重庆出版社', 4.92, '雨果奖科幻小说');
INSERT INTO `book_info` VALUES (24, '9787544270618', '白夜行', '[日]东野圭吾', '文学', 'B区2层09架', 8, 5, NULL, '2025-11-06 03:13:27', '2020-06-15 00:00:00', '南海出版社', 4.78, '推理小说经典之作');
INSERT INTO `book_info` VALUES (25, '9787020161331', '人间失格', '[日]太宰治', '文学', 'B区2层10架', 6, 4, NULL, '2025-11-16 03:13:27', '2020-08-20 00:00:00', '人民文学出版社', 4.55, '探讨人性的深刻作品');
INSERT INTO `book_info` VALUES (26, '9787101132816', '史记', '司马迁', '历史', 'C区3层01架', 6, 5, NULL, '2025-06-09 03:13:27', '2014-09-01 00:00:00', '中华书局', 4.93, '中国古代历史巨著');
INSERT INTO `book_info` VALUES (27, '9787108055354', '万历十五年', '黄仁宇', '历史', 'C区3层02架', 8, 6, NULL, '2025-06-29 03:13:27', '2017-03-15 00:00:00', '生活·读书·新知三联书店', 4.82, '大历史观的经典著作');
INSERT INTO `book_info` VALUES (28, '9787508643441', '人类简史', '[以色列]尤瓦尔·赫拉利', '历史', 'C区3层03架', 10, 7, NULL, '2025-08-28 03:13:27', '2018-07-20 00:00:00', '中信出版社', 4.87, '从认知革命到科技革命');
INSERT INTO `book_info` VALUES (29, '9787108059864', '枪炮、病菌与钢铁', '[美]贾雷德·戴蒙德', '历史', 'C区3层04架', 5, 4, NULL, '2025-09-07 03:13:27', '2018-10-10 00:00:00', '生活·读书·新知三联书店', 4.75, '人类社会发展史的探索');
INSERT INTO `book_info` VALUES (30, '9787508660752', '中国历代政治得失', '钱穆', '历史', 'C区3层05架', 7, 5, NULL, '2025-09-27 03:13:27', '2019-06-01 00:00:00', '中信出版社', 4.68, '中国政治制度史纲');
INSERT INTO `book_info` VALUES (31, '9787508685649', '原则', '[美]瑞·达利欧', '经济管理', 'D区1层01架', 9, 6, NULL, '2025-10-17 03:13:27', '2019-09-15 00:00:00', '中信出版社', 4.70, '桥水基金创始人的人生经验');
INSERT INTO `book_info` VALUES (32, '9787508634012', '从0到1', '[美]彼得·蒂尔', '经济管理', 'D区1层02架', 7, 5, NULL, '2025-09-17 03:13:27', '2019-01-10 00:00:00', '中信出版社', 4.60, '如何打造创新企业');
INSERT INTO `book_info` VALUES (33, '9787559614346', '贫穷的本质', '[美]阿比吉特·班纳吉', '经济管理', 'D区1层03架', 6, 4, NULL, '2025-11-06 03:13:27', '2020-05-20 00:00:00', '中信出版社', 4.55, '诺贝尔经济学奖得主力作');
INSERT INTO `book_info` VALUES (34, '9787111654540', '金字塔原理', '[美]芭芭拉·明托', '经济管理', 'D区1层04架', 8, 6, NULL, '2025-10-07 03:13:27', '2019-08-01 00:00:00', '机械工业出版社', 4.65, '思维与表达的逻辑');
INSERT INTO `book_info` VALUES (35, '9787510474095', '思考，快与慢', '[美]丹尼尔·卡尼曼', '经济管理', 'D区1层05架', 7, 5, NULL, '2025-10-27 03:13:27', '2020-02-15 00:00:00', '中信出版社', 4.72, '诺奖得主的认知心理学');

-- ----------------------------
-- Table structure for book_renew_request
-- ----------------------------
DROP TABLE IF EXISTS `book_renew_request`;
CREATE TABLE `book_renew_request`  (
  `renew_id` bigint NOT NULL AUTO_INCREMENT COMMENT '续借申请ID',
  `record_id` bigint NOT NULL COMMENT '借阅记录ID',
  `user_id` bigint NOT NULL COMMENT '申请人用户ID',
  `request_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请日期',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态：0=待审批，1=已批准，2=已拒绝',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '审批管理员ID',
  `review_date` datetime NULL DEFAULT NULL COMMENT '审批日期',
  `review_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审批备注',
  `new_due_date` datetime NULL DEFAULT NULL COMMENT '新的应还日期',
  PRIMARY KEY (`renew_id`) USING BTREE,
  UNIQUE INDEX `record_id`(`record_id` ASC) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_user_status_request`(`user_id` ASC, `status` ASC, `request_date` ASC) USING BTREE,
  CONSTRAINT `book_renew_request_ibfk_1` FOREIGN KEY (`record_id`) REFERENCES `borrow_record` (`record_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `book_renew_request_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `book_renew_request_ibfk_3` FOREIGN KEY (`admin_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of book_renew_request
-- ----------------------------
INSERT INTO `book_renew_request` VALUES (1, 8, 6, '2026-02-22 03:13:27', 0, NULL, NULL, NULL, NULL);
INSERT INTO `book_renew_request` VALUES (2, 10, 7, '2026-02-23 03:13:27', 0, NULL, NULL, NULL, NULL);
INSERT INTO `book_renew_request` VALUES (3, 12, 8, '2026-02-19 03:13:27', 1, 1, '2026-02-20 03:13:27', '批准续借30天', '2026-04-13 03:13:27');
INSERT INTO `book_renew_request` VALUES (4, 20, 15, '2026-02-17 03:13:27', 2, 1, '2026-02-18 03:13:27', '已逾期，请先归还', NULL);

-- ----------------------------
-- Table structure for borrow_record
-- ----------------------------
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '借阅记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '图书ID',
  `borrow_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借阅日期',
  `due_date` datetime NOT NULL COMMENT '应还日期（通常是借阅日期后30天）',
  `return_date` datetime NULL DEFAULT NULL COMMENT '实际归还日期',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态：0=借阅中，1=已归还，2=逾期',
  `is_renew` int NOT NULL DEFAULT 0 COMMENT '是否续借过：0=否，1=是',
  `fine` decimal(8, 2) NULL DEFAULT NULL COMMENT '罚款金额（0.5元/天，最多10元）',
  `renew_request_id` bigint NULL DEFAULT NULL COMMENT '续借申请ID',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_user_book_status`(`user_id` ASC, `book_id` ASC, `status` ASC, `borrow_date` ASC, `due_date` ASC) USING BTREE,
  CONSTRAINT `borrow_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `borrow_record_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `book_info` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrow_record
-- ----------------------------
INSERT INTO `borrow_record` VALUES (1, 6, 1, '2025-12-26 03:13:27', '2026-01-25 03:13:27', '2026-01-27 03:13:27', 1, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (2, 6, 5, '2025-12-31 03:13:27', '2026-01-30 03:13:27', '2026-01-31 03:13:27', 1, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (3, 7, 2, '2026-01-05 03:13:27', '2026-02-04 03:13:27', '2026-02-05 03:13:27', 1, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (4, 7, 11, '2026-01-07 03:13:27', '2026-02-06 03:13:27', '2026-02-07 03:13:27', 1, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (5, 8, 3, '2026-01-10 03:13:27', '2026-02-09 03:13:27', '2026-02-10 03:13:27', 1, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (6, 9, 16, '2026-01-15 03:13:27', '2026-02-14 03:13:27', '2026-02-15 03:13:27', 1, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (7, 10, 17, '2026-01-17 03:13:27', '2026-02-16 03:13:27', '2026-02-17 03:13:27', 1, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (8, 6, 4, '2026-02-04 03:13:27', '2026-03-06 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (9, 6, 7, '2026-02-06 03:13:27', '2026-03-08 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (10, 7, 8, '2026-02-09 03:13:27', '2026-03-11 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (11, 7, 18, '2026-02-10 03:13:27', '2026-03-12 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (12, 8, 9, '2026-02-12 03:13:27', '2026-03-14 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (13, 8, 19, '2026-02-14 03:13:27', '2026-03-16 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (14, 9, 10, '2026-02-16 03:13:27', '2026-03-18 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (15, 10, 20, '2026-02-18 03:13:27', '2026-03-20 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (16, 11, 21, '2026-02-19 03:13:27', '2026-03-21 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (17, 12, 22, '2026-01-28 03:13:27', '2026-02-27 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (18, 13, 23, '2026-01-27 03:13:27', '2026-02-26 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (19, 14, 24, '2026-01-26 03:13:27', '2026-02-25 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (20, 15, 25, '2026-01-15 03:13:27', '2026-02-14 03:13:27', NULL, 2, 0, 50.00, NULL);
INSERT INTO `borrow_record` VALUES (21, 6, 26, '2026-01-17 03:13:27', '2026-02-16 03:13:27', NULL, 2, 0, 40.00, NULL);
INSERT INTO `borrow_record` VALUES (22, 2, 1, '2026-01-30 03:13:27', '2026-03-31 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (23, 2, 3, '2026-02-01 03:13:27', '2026-04-02 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (24, 2, 5, '2026-02-03 03:13:27', '2026-04-04 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (25, 3, 6, '2026-02-04 03:13:27', '2026-04-05 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (26, 3, 12, '2026-02-06 03:13:27', '2026-04-07 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (27, 4, 13, '2026-02-08 03:13:27', '2026-04-09 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (28, 4, 14, '2026-02-09 03:13:27', '2026-04-10 03:13:27', NULL, 0, 0, NULL, NULL);
INSERT INTO `borrow_record` VALUES (29, 5, 15, '2026-02-12 03:13:27', '2026-04-13 03:13:27', NULL, 0, 0, NULL, NULL);

-- ----------------------------
-- Table structure for borrow_reminder
-- ----------------------------
DROP TABLE IF EXISTS `borrow_reminder`;
CREATE TABLE `borrow_reminder`  (
  `reminder_id` bigint NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `record_id` bigint NOT NULL COMMENT '借阅记录ID',
  `book_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图书标题',
  `reminder_type` int NOT NULL COMMENT '提醒类型：0=即将逾期，1=已逾期，2=罚款提醒',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提醒消息',
  `due_date` datetime NOT NULL COMMENT '应还日期',
  `overday_count` int NULL DEFAULT NULL COMMENT '逾期天数',
  `fine_amount` decimal(8, 2) NULL DEFAULT NULL COMMENT '罚款金额',
  `is_read` int NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `notification_channels` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IN_APP,EMAIL' COMMENT '通知渠道',
  `in_app_notified` int NOT NULL DEFAULT 1 COMMENT '站内信通知状态：0=未发送，1=已发送',
  `email_notified` int NOT NULL DEFAULT 1 COMMENT '邮件通知状态：0=未发送，1=已发送',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`reminder_id`) USING BTREE,
  UNIQUE INDEX `uk_user_record_type`(`user_id` ASC, `record_id` ASC, `reminder_type` ASC) USING BTREE,
  INDEX `fk_borrow_reminder_record`(`record_id` ASC) USING BTREE,
  INDEX `idx_user_read_time`(`user_id` ASC, `is_read` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_borrow_reminder_record` FOREIGN KEY (`record_id`) REFERENCES `borrow_record` (`record_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_borrow_reminder_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrow_reminder
-- ----------------------------
INSERT INTO `borrow_reminder` VALUES (1, 12, 17, '史记', 0, '您借阅的《史记》将在3天后到期，请及时归还', '2026-02-27 03:13:27', NULL, NULL, 0, 'IN_APP,EMAIL', 1, 1, '2026-02-24 03:13:27', '2026-02-24 03:13:27');
INSERT INTO `borrow_reminder` VALUES (2, 13, 18, '万历十五年', 0, '您借阅的《万历十五年》将在2天后到期，请及时归还', '2026-02-26 03:13:27', NULL, NULL, 0, 'IN_APP,EMAIL', 1, 1, '2026-02-24 03:13:27', '2026-02-24 03:13:27');
INSERT INTO `borrow_reminder` VALUES (3, 14, 19, '人类简史', 0, '您借阅的《人类简史》将在1天后到期，请及时归还', '2026-02-25 03:13:27', NULL, NULL, 0, 'IN_APP,EMAIL', 1, 0, '2026-02-24 03:13:27', '2026-02-24 03:13:27');
INSERT INTO `borrow_reminder` VALUES (4, 15, 20, '枪炮、病菌与钢铁', 1, '您借阅的《枪炮、病菌与钢铁》已逾期10天，请尽快归还', '2026-02-14 03:13:27', 10, 50.00, 0, 'IN_APP,EMAIL', 1, 1, '2026-02-15 03:13:27', '2026-02-24 03:13:27');
INSERT INTO `borrow_reminder` VALUES (5, 6, 21, '中国历代政治得失', 1, '您借阅的《中国历代政治得失》已逾期8天，请尽快归还', '2026-02-16 03:13:27', 8, 40.00, 1, 'IN_APP,EMAIL', 1, 1, '2026-02-17 03:13:27', '2026-02-24 03:13:27');
INSERT INTO `borrow_reminder` VALUES (6, 15, 20, '枪炮、病菌与钢铁', 2, '您有未支付的逾期罚款50.00元，请及时处理', '2026-02-14 03:13:27', 10, 50.00, 0, 'IN_APP,EMAIL', 1, 1, '2026-02-19 03:13:27', '2026-02-24 03:13:27');

-- ----------------------------
-- Table structure for fine_log
-- ----------------------------
DROP TABLE IF EXISTS `fine_log`;
CREATE TABLE `fine_log`  (
  `fine_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `borrow_id` bigint NOT NULL COMMENT '关联借阅记录',
  `amount` decimal(10, 2) NOT NULL COMMENT '罚款金额',
  `status` tinyint NOT NULL COMMENT '0:未缴,1:已缴',
  PRIMARY KEY (`fine_id`) USING BTREE,
  INDEX `idx_fine_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_fine_borrow`(`borrow_id` ASC) USING BTREE,
  CONSTRAINT `fk_fine_borrow` FOREIGN KEY (`borrow_id`) REFERENCES `borrow_record` (`record_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_fine_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '逾期罚款表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fine_log
-- ----------------------------
INSERT INTO `fine_log` VALUES (1, 5, 3, 6.00, 0);
INSERT INTO `fine_log` VALUES (2, 4, 1, 0.00, 1);
INSERT INTO `fine_log` VALUES (3, 2, 2, 0.00, 1);
INSERT INTO `fine_log` VALUES (4, 3, 4, 0.00, 1);
INSERT INTO `fine_log` VALUES (5, 4, 5, 1.50, 1);

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '操作人用户ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型：ADD_BOOK, DELETE_BOOK, FREEZE_USER, BORROW, RETURN等',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标对象ID（如图书ID、用户ID等）',
  `old_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '旧值',
  `new_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '新值',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_operation_time`(`user_id` ASC, `operation_type` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `operation_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (1, 1, '创建用户', 6, NULL, '{\"username\":\"stu2021001\",\"role\":2}', '192.168.1.100', '2025-11-16 03:13:27');
INSERT INTO `operation_log` VALUES (2, 1, '添加图书', 1, NULL, '{\"isbn\":\"9787111544661\",\"title\":\"Java核心技术卷I\"}', '192.168.1.100', '2025-08-08 03:13:27');
INSERT INTO `operation_log` VALUES (3, 1, '批准续借', 12, '{\"status\":0}', '{\"status\":1}', '192.168.1.100', '2026-02-20 03:13:27');
INSERT INTO `operation_log` VALUES (4, 1, '拒绝续借', 20, '{\"status\":0}', '{\"status\":2}', '192.168.1.100', '2026-02-18 03:13:27');
INSERT INTO `operation_log` VALUES (5, 6, '借阅图书', 1, NULL, '{\"bookId\":1,\"userId\":6}', '192.168.1.105', '2025-12-26 03:13:27');
INSERT INTO `operation_log` VALUES (6, 6, '归还图书', 1, NULL, '{\"bookId\":1,\"userId\":6}', '192.168.1.105', '2026-01-27 03:13:27');
INSERT INTO `operation_log` VALUES (7, 1, 'UPDATE_RECOMMENDATION_CONFIG', 1, 'old', 'new', NULL, '2026-02-24 04:12:32');
INSERT INTO `operation_log` VALUES (8, 9, 'RECOMMENDATION_GENERATE', 9, 'source=HYBRID', 'count=24', NULL, '2026-02-24 04:17:13');
INSERT INTO `operation_log` VALUES (9, 9, 'RECOMMENDATION_GENERATE', 9, 'source=HYBRID', 'count=24', NULL, '2026-02-24 04:17:23');
INSERT INTO `operation_log` VALUES (10, 9, 'RECOMMENDATION_GENERATE', 9, 'source=HYBRID', 'count=24', NULL, '2026-02-24 04:17:27');
INSERT INTO `operation_log` VALUES (11, 9, 'RECOMMENDATION_GENERATE', 9, 'source=HYBRID', 'count=24', NULL, '2026-02-24 04:18:27');
INSERT INTO `operation_log` VALUES (12, 7, 'RECOMMENDATION_GENERATE', 7, 'source=HYBRID', 'count=22', NULL, '2026-02-24 04:26:20');

-- ----------------------------
-- Table structure for rec_list
-- ----------------------------
DROP TABLE IF EXISTS `rec_list`;
CREATE TABLE `rec_list`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '目标用户',
  `book_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '推荐图书ID集合(逗号分隔)',
  `rec_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '推荐类型',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rec_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_rec_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推荐结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of rec_list
-- ----------------------------
INSERT INTO `rec_list` VALUES (1, 2, '1,3,4', 'CF', '2026-02-08 02:00:00');
INSERT INTO `rec_list` VALUES (2, 3, '2,4,5', 'Content', '2026-02-08 02:00:00');
INSERT INTO `rec_list` VALUES (3, 4, '1,2,3', 'CF', '2026-02-08 02:00:00');
INSERT INTO `rec_list` VALUES (4, 5, '2,5', 'Content', '2026-02-08 02:00:00');
INSERT INTO `rec_list` VALUES (5, 1, '1,2,3,4,5', 'CF', '2026-02-08 02:00:00');

-- ----------------------------
-- Table structure for recommendation_config
-- ----------------------------
DROP TABLE IF EXISTS `recommendation_config`;
CREATE TABLE `recommendation_config`  (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `collaborative_weight` decimal(3, 2) NULL DEFAULT 0.50 COMMENT '协同过滤权重（默认0.5）',
  `personalized_weight` decimal(3, 2) NULL DEFAULT 0.30 COMMENT '个性化权重（默认0.3）',
  `trending_weight` decimal(3, 2) NULL DEFAULT 0.20 COMMENT '热门度权重（默认0.2）',
  `min_similarity` decimal(3, 2) NULL DEFAULT 0.10 COMMENT '最小相似度（默认0.1）',
  `cold_start_strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'TRENDING' COMMENT '冷启动策略：TRENDING/CATEGORY/RANDOM',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`config_id`) USING BTREE,
  INDEX `updated_by`(`updated_by` ASC) USING BTREE,
  CONSTRAINT `recommendation_config_ibfk_1` FOREIGN KEY (`updated_by`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recommendation_config
-- ----------------------------
INSERT INTO `recommendation_config` VALUES (1, 0.50, 0.30, 0.20, 0.10, 'CATEGORY', '2026-02-24 04:12:32', 1);

-- ----------------------------
-- Table structure for recommendation_list
-- ----------------------------
DROP TABLE IF EXISTS `recommendation_list`;
CREATE TABLE `recommendation_list`  (
  `recommend_id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '推荐图书ID',
  `recommend_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐原因',
  `score` decimal(5, 4) NULL DEFAULT NULL COMMENT '推荐分数（0-1）',
  `recommend_type` int NOT NULL DEFAULT 0 COMMENT '推荐类型：0=个性化，1=热门，2=相关',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`recommend_id`) USING BTREE,
  INDEX `book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_user_type_score`(`user_id` ASC, `recommend_type` ASC, `score` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `recommendation_list_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `recommendation_list_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `book_info` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 130 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recommendation_list
-- ----------------------------
INSERT INTO `recommendation_list` VALUES (1, 6, 1, '基于您的借阅历史推荐', 0.9500, 0, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (2, 6, 3, '相关图书推荐', 0.8800, 2, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (3, 6, 6, '热门图书推荐', 0.9200, 1, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (5, 7, 11, '相关图书推荐', 0.8500, 2, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (6, 8, 4, '基于您的兴趣推荐', 0.8900, 0, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (7, 9, 16, '热门图书推荐', 0.9000, 1, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (8, 10, 20, '新书推荐', 0.8700, 0, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (9, 2, 13, '专业领域推荐', 0.9300, 0, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (10, 2, 14, '相关领域推荐', 0.8900, 2, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (11, 3, 15, '最新研究推荐', 0.9100, 1, '2026-02-24 03:13:27');
INSERT INTO `recommendation_list` VALUES (84, 9, 1, '热门借阅回退推荐', 2.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (85, 9, 3, '热门借阅回退推荐', 2.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (86, 9, 5, '热门借阅回退推荐', 2.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (87, 9, 2, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (88, 9, 4, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (89, 9, 6, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (90, 9, 7, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (91, 9, 8, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (92, 9, 9, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (93, 9, 11, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (94, 9, 12, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (95, 9, 13, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (96, 9, 14, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (97, 9, 15, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (98, 9, 17, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (99, 9, 18, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (100, 9, 19, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (101, 9, 20, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (102, 9, 21, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (103, 9, 22, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (104, 9, 23, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (105, 9, 24, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (106, 9, 25, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (107, 9, 26, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:18:27');
INSERT INTO `recommendation_list` VALUES (108, 7, 1, '热门借阅回退推荐', 2.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (109, 7, 3, '热门借阅回退推荐', 2.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (110, 7, 5, '热门借阅回退推荐', 2.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (111, 7, 4, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (112, 7, 6, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (113, 7, 7, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (114, 7, 9, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (115, 7, 10, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (116, 7, 12, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (117, 7, 13, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (118, 7, 14, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (119, 7, 15, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (120, 7, 16, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (121, 7, 17, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (122, 7, 19, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (123, 7, 20, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (124, 7, 21, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (125, 7, 22, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (126, 7, 23, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (127, 7, 24, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (128, 7, 25, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');
INSERT INTO `recommendation_list` VALUES (129, 7, 26, '热门借阅回退推荐', 1.0000, 0, '2026-02-24 04:26:20');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `role` int NOT NULL DEFAULT 2 COMMENT '角色：0=管理员，1=教师，2=学生',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门/班级',
  `max_limit` int NOT NULL DEFAULT 10 COMMENT '最大借阅数：管理员100，教师20，学生10',
  `status` int NOT NULL DEFAULT 1 COMMENT '用户状态：0=冻结，1=正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_role_status_time`(`role` ASC, `status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$ddYqfaRaHLXOmlC7PT9hbeovglJRCcVnmf1DTTSWzYMf6ry3EbHWa', '系统管理员', 0, '图书馆管理部', 100, 1, '2025-08-28 03:13:27', NULL, '13800000000');
INSERT INTO `sys_user` VALUES (2, 'teacher001', '$2a$10$jXl6pNcc.tXqUnmreHVMRunXN0DWO4lpf5kFA79jGcflytVXMbH5.', '张明', 1, '计算机学院', 20, 1, '2025-09-27 03:13:27', NULL, '13800000101');
INSERT INTO `sys_user` VALUES (3, 'teacher002', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '李芳', 1, '计算机学院', 20, 1, '2025-10-07 03:13:27', NULL, '13800000102');
INSERT INTO `sys_user` VALUES (4, 'teacher003', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '王强', 1, '软件学院', 20, 1, '2025-10-17 03:13:27', NULL, '13800000103');
INSERT INTO `sys_user` VALUES (5, 'teacher004', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '刘娜', 1, '人工智能学院', 20, 1, '2025-10-27 03:13:27', NULL, '13800000104');
INSERT INTO `sys_user` VALUES (6, 'stu2021001', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '陈浩', 2, '计算机21-01班', 10, 1, '2025-11-16 03:13:27', NULL, '13900000201');
INSERT INTO `sys_user` VALUES (7, 'stu2021002', '$2a$10$cJtPpOyPI4X96uohf8vyEul0muOp1Lz.ODhKlPLginyj9xLdeB4v6', '赵雪', 2, '计算机21-01班', 10, 1, '2025-11-21 03:13:27', NULL, '13900000202');
INSERT INTO `sys_user` VALUES (8, 'stu2021003', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '孙杰', 2, '计算机21-01班', 10, 1, '2025-11-26 03:13:27', NULL, '13900000203');
INSERT INTO `sys_user` VALUES (9, 'stu2021004', '$2a$10$OjOEm8q8g/YM1oqbxVsGpevXDHKrRw7jiTGX3sqyD4vM54iobBLm.', '周敏', 2, '计算机21-02班', 10, 1, '2025-12-01 03:13:27', NULL, '13900000204');
INSERT INTO `sys_user` VALUES (10, 'stu2021005', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '吴磊', 2, '计算机21-02班', 10, 1, '2025-12-06 03:13:27', NULL, '13900000205');
INSERT INTO `sys_user` VALUES (11, 'stu2022001', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '郑颖', 2, '计算机22-01班', 10, 1, '2025-12-16 03:13:27', NULL, '13900000301');
INSERT INTO `sys_user` VALUES (12, 'stu2022002', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '冯涛', 2, '计算机22-01班', 10, 1, '2025-12-21 03:13:27', NULL, '13900000302');
INSERT INTO `sys_user` VALUES (13, 'stu2022003', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '何欣', 2, '计算机22-02班', 10, 1, '2025-12-26 03:13:27', NULL, '13900000303');
INSERT INTO `sys_user` VALUES (14, 'stu2023001', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '许鹏', 2, '计算机23-01班', 10, 1, '2026-01-05 03:13:27', NULL, '13900000401');
INSERT INTO `sys_user` VALUES (15, 'stu2023002', '$2a$10$4NHR.W3YLfBLGEGK.pxUbOmgc6WMlHaypPATWEq.JXMVDmj6LGLPO', '余婷', 2, '计算机23-01班', 10, 1, '2026-01-10 03:13:27', NULL, '13900000402');

SET FOREIGN_KEY_CHECKS = 1;
