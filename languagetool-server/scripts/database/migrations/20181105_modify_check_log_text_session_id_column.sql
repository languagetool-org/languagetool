alter table `check_log` modify column `text_session_id` bigint unsigned DEFAULT NULL COMMENT 'randomly generated number consistent across editing & check actions';
