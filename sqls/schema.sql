CREATE TABLE `ratelimit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(255) DEFAULT NULL,
  `request_count` int DEFAULT NULL,
  `request_date` datetime DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
