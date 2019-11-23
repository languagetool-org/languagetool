alter table check_log modify column language_detected varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'language code as detected by server';
