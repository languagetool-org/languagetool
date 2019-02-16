use lt;

CREATE TABLE if not exists `rule_matches` (
  `match_id` int(10) unsigned auto_increment NOT NULL comment "primary key, one for each matching rule in a check",
  `check_id` int(10) unsigned NOT NULL comment "foreign key, referencing corresponding row in check_log",
  `rule_id` varchar(128) NOT NULL comment "name of the rule as defined in Java/XML code",
  `match_count` int(10) unsigned NOT NULL comment "number of matches in the text",
  PRIMARY KEY (`match_id`),
  KEY `check_index` (`check_id`),
  KEY `rule_index` (`rule_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

#ALTER table `rule_matches` modify column `match_id` int(10) unsigned not null auto_increment;

alter table `check_log` modify column `user_id` int(10) unsigned;
alter table `check_log` add column `server` int(10) unsigned comment "host name of server responsible for this request";
alter table `check_log` add column `client` int(10) unsigned comment "client responsible for this request, e.g. browser addon/website/word";
alter table `check_log` add column `language_detected` varchar(8) comment "language code as detected by server";
alter table `check_log` add column `computation_time` int(10) comment "request handling time in milliseconds";
alter table `check_log` add column `text_session_id` int(10) unsigned comment "randomly generated number consistent across editing & check actions";
--alter table `check_log` change column `language` `language_set` varchar(8) not null comment "language code provided by client, e.g. de-DE";


--#alter table `check_log` drop column `day`;
#alter table `check_log` add index `date_index` (`date`);
#alter table `check_log` drop index `check_log_day_user_id_index`;
#alter table `check_log` add index `date_user_index` (`date`, `user_id`);

create table if not exists `servers` (
	`id` int(10) unsigned not null auto_increment,
    `hostname` varchar(64) not null,
    primary key (`id`)
) engine=MyISAM default charset=utf8;
    
create table if not exists `clients` (
	`id` int(10) unsigned not null auto_increment,
    `name` varchar(64) not null,
    primary key (`id`)
) engine=MyISAM default charset=utf8;
    

create table if not exists  `cache_stats` (
	`id` int(10) unsigned not null auto_increment,
    `date` datetime not null,
    `server` int(10) unsigned not null,
    `cache_hits` float not null,
    primary key (`id`),
    key `date_index` (`date`)
    ) engine=MyISAM DEFAULT CHARSET=utf8;
    
create table if not exists  `misc_log` (
	`id` int(10) unsigned not null auto_increment,
    `date` datetime not null,
    `server` int(10) unsigned,
    `client` int(10) unsigned,
    `user` int(10) unsigned,
    `message` varchar(1024) not null,
    primary key (`id`),
    key `date_index` (`date`)
    ) engine=MyISAM default charset=utf8;

create table if not exists  `check_error` (
	`id` int(10) unsigned not null auto_increment,
    `type` varchar(64) not null,
    `date` datetime not null,
    `server` int(10) unsigned,
    `client` int(10) unsigned,
    `user` int(10) unsigned,
    `language_set` varchar(8),
    `language_detected` varchar(8),
    `text_length` int(10) unsigned,
    `extra` varchar(256),
    primary key (`id`),
    key `date_index` (`date`)
    ) engine=MyISAM default charset=utf8;
    
create table if not exists  `access_limits` (
	`id` int(10) unsigned not null auto_increment,
    `type` varchar(64) not null,
    `date` datetime not null,
    `server` int(10) unsigned,
    `client` int(10) unsigned,
    `user` int(10) unsigned,
    `referrer` varchar(128),
    `user_agent` varchar(512),
    `reason` varchar(128),
    primary key (`id`),
    key `date_index` (`date`)
    ) engine=MyISAM default charset=utf8;

    
