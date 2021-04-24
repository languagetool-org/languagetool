alter table check_error modify column language_detected varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'language code as detected by server';
alter table check_error modify column language varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'language code as provided by client';
