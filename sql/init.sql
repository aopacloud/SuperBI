/*!40101 SET NAMES utf8 */;

create database bdp_super_bi;

use bdp_super_bi;

CREATE TABLE `bi_auth_role`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workspace_id` int(11) DEFAULT NULL COMMENT '空间ID',
    `name`         varchar(255) DEFAULT NULL COMMENT '成员组',
    `description`  varchar(255) DEFAULT NULL COMMENT '描述',
    `creator`      varchar(255) DEFAULT NULL COMMENT '创建人',
    `remark`       varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workspace_name` (`workspace_id`,`name`),
    KEY            `idx_workspace_id` (`workspace_id`),
    KEY            `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限组';



CREATE TABLE `bi_auth_role_user`
(
    `id`          int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`     int(11) DEFAULT NULL COMMENT '成员组ID',
    `username`    varchar(255) DEFAULT NULL COMMENT '成员',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_id_username` (`role_id`,`username`),
    KEY           `idx_group` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限组成员';



CREATE TABLE `bi_dashboard`
(
    `id`                int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workspace_id`      int(11) DEFAULT NULL COMMENT '空间ID',
    `name`              varchar(255) DEFAULT NULL COMMENT '名称',
    `description`       varchar(255) DEFAULT NULL COMMENT '描述',
    `status`            varchar(30)  DEFAULT NULL COMMENT '状态',
    `version`           int(11) DEFAULT '0' COMMENT '当前发布版本号',
    `last_edit_version` int(11) DEFAULT '0' COMMENT '最新编辑版本号',
    `creator`           varchar(255) DEFAULT NULL COMMENT '创建人',
    `operator`          varchar(255) DEFAULT NULL COMMENT '更新人',
    `deleted`           int(4) DEFAULT '0' COMMENT '是否删除',
    `create_time`       timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_workspace_name_deleted` (`workspace_id`,`name`,`deleted`),
    KEY                 `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板表';


CREATE TABLE `bi_dashboard_component`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dashboard_id` int(11) NOT NULL COMMENT 'Dashboard ID ',
    `version`      int(11) DEFAULT '0' COMMENT '版本号',
    `type`         varchar(255) DEFAULT NULL COMMENT '组件类型, REMARK, FILTER, REPORT',
    `report_id`    int(20) DEFAULT NULL,
    `layout`       longtext COMMENT '图表配置参数，样式',
    `content`      longtext COMMENT '图表内容',
    `create_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_dashbaord_version` (`dashboard_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板下组件表';


CREATE TABLE `bi_dashboard_filter`
(
    `id`              int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `component_id`    int(11) DEFAULT NULL COMMENT '组件ID',
    `name`            varchar(255) DEFAULT NULL COMMENT '过滤条件名称',
    `filter_type`     varchar(11)  DEFAULT NULL COMMENT '选项类型',
    `condition_type`  int(4) DEFAULT NULL COMMENT '条件形式  0：单条件，1：或条件 ，2：且条件',
    `relationship`    text COMMENT '图表关联关系',
    `params`          text COMMENT '参数，用于存放条件形式，查询方式 ',
    `sort`            int(11) DEFAULT NULL COMMENT '排序',
    `single`          int(4) DEFAULT NULL COMMENT '是否单选，1:必选',
    `required`        int(4) DEFAULT NULL COMMENT '是否必选，1:必选',
    `default_value`   text COMMENT '默认值',
    `selected_values` text COMMENT '配置的选项值',
    `create_time`     timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY               `idx_chart_id` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板全局过滤器';


CREATE TABLE `bi_dashboard_push_setting`
(
    `id`                  int(11) unsigned NOT NULL AUTO_INCREMENT,
    `dashboard_id`        int(11) NOT NULL COMMENT '看板ID',
    `receive_type`        varchar(50)           DEFAULT '' COMMENT '接收方式',
    `push_content_type`   varchar(100)          DEFAULT NULL COMMENT '推送形式，URL链接， IMAGE 截图',
    `receive_users`       text COMMENT '接收人',
    `channel_description` varchar(255)          DEFAULT NULL COMMENT '频道描述',
    `channel`             varchar(255)          DEFAULT NULL COMMENT '频道ID',
    `cron`                varchar(255) NOT NULL DEFAULT '' COMMENT '推送 cron',
    `cron_params`         text COMMENT '前端参数',
    `timezone`            varchar(255)          DEFAULT NULL COMMENT '时区',
    `actived`             int(11) NOT NULL DEFAULT '1' COMMENT '是否开启，1: 开启， 2：关闭',
    `creator`             varchar(255)          DEFAULT NULL COMMENT '创建人',
    `schedule_job_id`     int(11) DEFAULT NULL COMMENT '调度系统中 job_id',
    `create_time`         timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `bi_dashboard_share`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`     varchar(255) DEFAULT NULL COMMENT '用户',
    `permission`   varchar(128) DEFAULT NULL COMMENT '权限许可；READ 只读，WRITE 协作',
    `operator`     varchar(255) DEFAULT NULL COMMENT '操作人',
    `dashboard_id` int(11) DEFAULT NULL COMMENT '目标资源ID',
    `type`         varchar(20)  DEFAULT NULL COMMENT '共享对象类型，ROLE,USER',
    `role_id`      int(11) DEFAULT NULL COMMENT '成员组id',
    `create_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_dashboard_id` (`dashboard_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板共享列表';


CREATE TABLE `bi_dataset`
(
    `id`                int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workspace_id`      int(11) DEFAULT NULL COMMENT '空间ID',
    `status`            varchar(30)  DEFAULT NULL COMMENT '状态',
    `name`              varchar(255) DEFAULT NULL COMMENT '数据集名称',
    `description`       varchar(255) DEFAULT NULL COMMENT '数据集描述',
    `version`           int(11) DEFAULT '0' COMMENT '当前发布版本号',
    `last_edit_version` int(11) DEFAULT '0' COMMENT '最新编辑版本号',
    `enable_apply`      int(4) DEFAULT '1' COMMENT '全员是否可申请',
    `creator`           varchar(30)  DEFAULT NULL COMMENT '创建人',
    `operator`          varchar(30)  DEFAULT NULL COMMENT '更新人',
    `doc_url`           text COMMENT '数据集说明文档',
    `deleted`           int(4) DEFAULT '0' COMMENT '是否删除',
    `create_time`       timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_workspace_name_deleted` (`workspace_id`,`name`,`deleted`),
    KEY                 `idx_workspace_id` (`workspace_id`),
    KEY                 `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集表';



CREATE TABLE `bi_dataset_apply`
(
    `id`               int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`         varchar(512)  DEFAULT NULL COMMENT '申请人',
    `alias_name`       varchar(512)  DEFAULT NULL COMMENT '申请人别名',
    `dataset_id`       int(11) DEFAULT NULL COMMENT '申请的数据集',
    `workspace_id`     int(11) DEFAULT NULL COMMENT '空间ID',
    `dataset_name`     varchar(512)  DEFAULT NULL COMMENT '数据集名称',
    `dataset_creator`  varchar(512)  DEFAULT NULL COMMENT '数据集所有者',
    `datasource`       varchar(1024) DEFAULT NULL COMMENT '源表名',
    `reason`           varchar(1024) DEFAULT NULL COMMENT '申请理由',
    `approve_status`   varchar(255)  DEFAULT NULL COMMENT '审批状态',
    `current_reviewer` varchar(255)  DEFAULT NULL COMMENT '当前审批人',
    `expire_duration`  int(11) DEFAULT NULL COMMENT '权限过期时间周期',
    `extra`            varchar(512)  DEFAULT NULL COMMENT '备注',
    `authorize_status` varchar(20)   DEFAULT NULL COMMENT '授权状态',
    `authorize_remark` varchar(512)  DEFAULT NULL COMMENT '授权备注',
    `create_time`      timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY                `idx_username` (`username`),
    KEY                `idx_dataset_owner` (`dataset_creator`),
    KEY                `idx_approve_status` (`approve_status`),
    KEY                `idx_authorize_status` (`authorize_status`),
    KEY                `idx_current_revirewer` (`current_reviewer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集权限申请';


CREATE TABLE `bi_dataset_authorize`
(
    `id`               int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `scope`            varchar(20)  DEFAULT NULL COMMENT '共享对象类型，ROLE,USER',
    `permission`       varchar(128) DEFAULT NULL COMMENT '权限许可；READ 只读，WRITE 协作',
    `username`         varchar(255) DEFAULT NULL COMMENT '用户名',
    `role_id`          int(11) DEFAULT NULL COMMENT '成员组ID',
    `dataset_id`       int(11) DEFAULT NULL COMMENT '数据集ID',
    `privilege_type`   varchar(255) DEFAULT NULL COMMENT '权限类型',
    `operator`         varchar(255) DEFAULT NULL COMMENT '授权操作者',
    `start_time`       timestamp NULL DEFAULT NULL COMMENT '权限开始时间',
    `expire_duration`  int(11) DEFAULT '0' COMMENT '过期时间周期，秒，0为永不过期',
    `column_privilege` text COMMENT '列权限参数',
    `row_privilege`    text COMMENT '行权限参数',
    `row_param`        text COMMENT '行权限前端参数',
    `remark`           varchar(255) DEFAULT NULL COMMENT '备注',
    `expired`          int(11) DEFAULT '0' COMMENT '权限是否过期',
    `deleted`          int(2) DEFAULT '0' COMMENT '是否取消',
    `create_time`      timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY                `idx_dataset_id` (`dataset_id`),
    KEY                `idx_username` (`username`),
    KEY                `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集授权';


CREATE TABLE `bi_dataset_field`
(
    `id`                   int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dataset_id`           int(11) DEFAULT NULL COMMENT '数据源id',
    `version`              int(11) DEFAULT '0' COMMENT '版本号',
    `name`                 varchar(255)  DEFAULT NULL COMMENT '字段名, 同数据集不允许重复',
    `display_name`         varchar(255)  DEFAULT NULL COMMENT '展示名称',
    `category`             varchar(20)   DEFAULT NULL COMMENT '数据类型，指标还是维度',
    `type`                 varchar(20)   DEFAULT NULL COMMENT '字段来源, ADD 新增，ORIGIN 原生',
    `expression`           text COMMENT '新增字段表达式',
    `source_field_name`    varchar(1024) DEFAULT NULL COMMENT '引用的字段名',
    `aggregator`           varchar(255)  DEFAULT NULL COMMENT '默认聚合方式',
    `data_type`            varchar(255)  DEFAULT NULL COMMENT '数据类型',
    `origin_data_type`     varchar(255)  DEFAULT NULL COMMENT '原始数据类型',
    `database_data_type`   varchar(255)  DEFAULT NULL COMMENT '在数据库中的数据类型',
    `data_format`          varchar(30)   DEFAULT NULL COMMENT '数据格式',
    `custom_format_config` text COMMENT '自定义数据格式配置',
    `compute_expression`   varchar(255)  DEFAULT NULL COMMENT '字段计算',
    `description`          varchar(1024) DEFAULT NULL COMMENT '字段描述',
    `status`               varchar(30)   DEFAULT 'VIEW' COMMENT '字段状态，VIEW 显示，HIDE 隐藏',
    `sort`                 int(11) DEFAULT NULL COMMENT '字段排序',
    `partition`            tinyint(4) DEFAULT '0' COMMENT '是否为分区字段',
    `create_time`          timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unq_name` (`dataset_id`,`version`,`name`),
    KEY                    `idx_dataset_version` (`dataset_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集字段表';



CREATE TABLE `bi_dataset_meta_config`
(
    `id`            int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dataset_id`    int(11) DEFAULT NULL COMMENT '数据集ID',
    `version`       int(11) DEFAULT '0' COMMENT '版本号',
    `datasource_name` varchar(255) DEFAULT NULL COMMENT '数据源名称',
    `engine`        varchar(255) DEFAULT NULL COMMENT '引擎',
    `db_name`       varchar(255) DEFAULT NULL COMMENT '数据库名',
    `table_name`    varchar(255) DEFAULT NULL COMMENT '表名',
    `content`       text COMMENT '多表join内容',
    `sql`           text COMMENT 'sql建表语句',
    `create_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dataset_id_version` (`dataset_id`,`version`),
    KEY             `dataset_id` (`dataset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集源配置表';



CREATE TABLE `bi_dataset_query_log`
(
    `id`          int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dataset_id`  int(11) DEFAULT NULL COMMENT '数据集ID',
    `username`    varchar(255) DEFAULT NULL COMMENT '用户名',
    `alias_name`  varchar(255) DEFAULT NULL COMMENT '中文名',
    `sql`         text COMMENT '查询SQL',
    `type`        varchar(20)  DEFAULT NULL COMMENT '操作类型，查询or下载',
    `status`      varchar(20)  DEFAULT NULL COMMENT '查询状态',
    `from_source` varchar(20)  DEFAULT NULL COMMENT '查询来源',
    `error_log`   text COMMENT '错误日志',
    `remark`      varchar(255) DEFAULT NULL COMMENT '备注',
    `data_num`    int(11) DEFAULT NULL COMMENT '数据条数',
    `elapsed`     int(20) DEFAULT NULL COMMENT '查询耗时，毫秒',
    `report_id`   int(11) DEFAULT NULL COMMENT '报表ID',
    `query_param` text COMMENT '数据集参数',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_type_dataset_username` (`dataset_id`,`type`,`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `bi_datasource` (
     `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
     `workspace_id` int(11) DEFAULT NULL COMMENT '空间ID',
     `name` varchar(255) DEFAULT NULL COMMENT '库名',
     `creator` varchar(255) DEFAULT NULL COMMENT '所有者',
     `engine` varchar(255) DEFAULT NULL COMMENT '库引擎',
     `version` varchar(255) DEFAULT NULL COMMENT '版本',
     `url` varchar(255) DEFAULT NULL COMMENT '链接url',
     `host` varchar(255) DEFAULT NULL COMMENT '地址',
     `port` int(10) DEFAULT NULL COMMENT '端口',
     `user` varchar(255) DEFAULT NULL COMMENT '用户名',
     `password` varchar(255) DEFAULT NULL COMMENT '密码',
     `database` varchar(255) DEFAULT NULL COMMENT '数据库',
     `init_sql` varchar(1024) DEFAULT NULL COMMENT '初始化SQL',
     `ssl_enable` int(1) DEFAULT '0' COMMENT '是否启用SSL',
     `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `unq_workspace_name` (`workspace_id`,`name`),
     KEY `idx_workspace_id` (`workspace_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='数据库表';


CREATE TABLE `bi_favorite`
(
    `id`          int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `position`    varchar(255) DEFAULT NULL COMMENT '资源类型',
    `target_id`   int(11) DEFAULT NULL COMMENT '资源ID',
    `username`    varchar(255) DEFAULT NULL COMMENT '收藏用户',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_username_position` (`username`,`position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏列表';


CREATE TABLE `bi_folder`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workspace_id` int(11) DEFAULT NULL COMMENT '空间ID',
    `name`         varchar(512) DEFAULT NULL COMMENT '名称',
    `parent_id`    int(11) DEFAULT NULL COMMENT '上级组 id',
    `creator`      varchar(255) DEFAULT NULL COMMENT '所有者',
    `type`         varchar(255) DEFAULT NULL COMMENT '组类别，ALL 全部 , personal 个人',
    `position`     varchar(255) DEFAULT NULL COMMENT '组位置  dataset, dashboard',
    `deleted`      int(11) DEFAULT '0' COMMENT '是否删除',
    `create_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_parent` (`parent_id`),
    KEY            `idx_workspace_type_position` (`workspace_id`,`position`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件夹';



CREATE TABLE `bi_folder_resource_rel`
(
    `id`            int(11) unsigned NOT NULL AUTO_INCREMENT,
    `folder_id`     int(11) DEFAULT NULL COMMENT '组ID',
    `target_id`     int(11) DEFAULT NULL COMMENT '资源ID',
    `resource_type` varchar(255) DEFAULT NULL COMMENT '资源类型',
    `create_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



CREATE TABLE `bi_notification`
(
    `id`            int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`      varchar(255) DEFAULT NULL COMMENT '用户',
    `readed`        int(4) NOT NULL DEFAULT '0' COMMENT '是否已读',
    `type`          varchar(255) DEFAULT NULL COMMENT '通知消息类型',
    `resource_type` varchar(255) DEFAULT NULL COMMENT '资源类型',
    `resource_id`   int(11) DEFAULT NULL COMMENT '资源ID',
    `resource_name` varchar(255) DEFAULT NULL,
    `content`       text COMMENT '消息内容',
    `create_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';



CREATE TABLE `bi_report`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workspace_id` int(11) DEFAULT NULL COMMENT '空间ID',
    `name`         varchar(255) DEFAULT NULL COMMENT '图表名称',
    `description`  varchar(255) DEFAULT NULL COMMENT '图表描述',
    `report_type`  varchar(255) DEFAULT NULL COMMENT '图表类型',
    `dataset_id`   int(11) DEFAULT NULL COMMENT '数据集ID',
    `creator`      varchar(255) DEFAULT NULL COMMENT '创建人',
    `operator`     varchar(255) DEFAULT NULL COMMENT '更新人',
    `layout`       longtext COMMENT '图表前端配置',
    `query_param`  longtext COMMENT '查询参数',
    `deleted`      int(4) DEFAULT '0' COMMENT '是否删除',
    `create_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_workspace_name_deleted` (`workspace_id`,`name`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图表';



CREATE TABLE `bi_report_field`
(
    `id`                 int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `expression`         text COMMENT '字段表达式',
    `aggregation`        varchar(255) DEFAULT NULL,
    `display_name`       varchar(255) DEFAULT NULL COMMENT '显示名称',
    `dataset_field_id`   int(11) DEFAULT NULL,
    `dataset_field_name` varchar(255) DEFAULT NULL COMMENT '源数据集字段名',
    `category`           varchar(20)  DEFAULT NULL COMMENT '类别，维度，指标，过滤',
    `params`             text COMMENT '过滤字段参数，前端传过来的 JSON',
    `report_id`          int(11) DEFAULT NULL COMMENT '图表ID',
    `create_time`        timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY                  `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图表字段';



CREATE TABLE `bi_sys_admin`
(
    `id`          int(11) unsigned NOT NULL AUTO_INCREMENT,
    `username`    varchar(255) DEFAULT NULL COMMENT '用户名',
    `deleted`     int(1) DEFAULT '0' COMMENT '0-未删除,1-删除',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='超级管理员';



CREATE TABLE `bi_sys_analyse_scope`
(
    `id`         int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `module`     varchar(255) NOT NULL COMMENT '模块',
    `permission` varchar(255) NOT NULL COMMENT '权限',
    `scope`      varchar(255) NOT NULL COMMENT '分析范围',
    `remark`     text COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析权限';



CREATE TABLE `bi_sys_login_log`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`      bigint(20) NOT NULL COMMENT '用户id',
    `username`     varchar(255) COLLATE utf8_bin NOT NULL COMMENT '用户名',
    `alias_name`   varchar(255) COLLATE utf8_bin NOT NULL COMMENT '显示名',
    `ip`           varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'ip地址',
    `browser`      varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '浏览器类型',
    `os`           varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '操作系统',
    `login_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '登录状态，0-成功；1-失败',
    `error_msg`    text COLLATE utf8_bin         NOT NULL COMMENT '错误信息',
    `create_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='登录日志';



CREATE TABLE `bi_sys_menu`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`        varchar(255) COLLATE utf8_bin NOT NULL COMMENT '菜单名',
    `name_en`     varchar(255) COLLATE utf8_bin NOT NULL COMMENT '菜单英文名',
    `parent_id`   bigint(20) NOT NULL COMMENT '父菜单id',
    `app_path`    varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '应用路径',
    `sort`        int(10) NOT NULL DEFAULT '0' COMMENT '显示顺序，越小越靠前',
    `icon`        varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '菜单图标',
    `url`         varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '菜单url',
    `hidden`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否隐藏，1-是；0-否',
    `description` varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '描述',
    `creator`     varchar(255) COLLATE utf8_bin NOT NULL COMMENT '创建人',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `operator`    varchar(255) COLLATE utf8_bin NOT NULL COMMENT '更新人',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='菜单信息';



CREATE TABLE `bi_sys_operator_log`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id`     bigint(20) NOT NULL COMMENT '用户id',
    `username`    varchar(255) COLLATE utf8_bin NOT NULL COMMENT '用户名',
    `alias_name`  varchar(255) COLLATE utf8_bin NOT NULL COMMENT '显示名',
    `ip`          varchar(255) COLLATE utf8_bin NOT NULL COMMENT 'ip地址',
    `url`         varchar(255) COLLATE utf8_bin NOT NULL COMMENT '请求url',
    `method`      varchar(255) COLLATE utf8_bin NOT NULL COMMENT '请求方法',
    `params`      text COLLATE utf8_bin         NOT NULL COMMENT '请求参数',
    `result`      text COLLATE utf8_bin         NOT NULL COMMENT '返回结果',
    `status`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态，0-成功；1-失败',
    `error_msg`   text COLLATE utf8_bin         NOT NULL COMMENT '错误信息',
    `cost_time`   bigint(20) NOT NULL DEFAULT '0' COMMENT '消耗时间',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='操作日志';



CREATE TABLE `bi_sys_resource`
(
    `id`              int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `resource_type`   varchar(255) NOT NULL COMMENT '资源类型，MENU:菜单；FUNCTION:功能；button:按钮',
    `module`          varchar(255) DEFAULT NULL COMMENT '模块',
    `module_name`     varchar(255) DEFAULT NULL COMMENT '模块名称',
    `permission`      varchar(255) DEFAULT NULL COMMENT '权限',
    `permission_name` varchar(255) DEFAULT NULL COMMENT '权限显示名称',
    `code`            varchar(255) DEFAULT NULL COMMENT '资源code',
    `name`            varchar(255) DEFAULT NULL COMMENT '资源名称',
    `comment`         varchar(255) DEFAULT NULL COMMENT '提示',
    `remark`          text COMMENT '备注',
    `create_time`     timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `creator`         varchar(255) DEFAULT NULL COMMENT '创建人',
    `operator`        varchar(255) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统资源';


CREATE TABLE `bi_sys_role`
(
    `id`          int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(255) DEFAULT NULL COMMENT '角色名称',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `role_type`   varchar(50)  DEFAULT '0' COMMENT '角色类型，SYSTEM 系统内置，CUSTOM 普通角色',
    `creator`     varchar(255) DEFAULT NULL COMMENT '创建人',
    `extra`       varchar(255) DEFAULT NULL COMMENT '额外信息',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `operator`    varchar(255) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unq_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';



CREATE TABLE `bi_sys_role_menu_rel`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `role_id`     bigint(20) NOT NULL COMMENT '角色id',
    `menu_id`     bigint(20) NOT NULL COMMENT '菜单id',
    `creator`     varchar(255) COLLATE utf8_bin NOT NULL COMMENT '创建人',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `operator`    varchar(255) COLLATE utf8_bin NOT NULL COMMENT '更新人',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_id` (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='角色菜单关联信息';



CREATE TABLE `bi_sys_role_resource_rel`
(
    `id`            int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id`       int(11) DEFAULT NULL,
    `resource_code` text NOT NULL COMMENT '资源Code , 逗号分割',
    `creator`       varchar(255) DEFAULT NULL COMMENT '创建人',
    `create_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `operator`      varchar(255) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';



CREATE TABLE `bi_sys_user`
(
    `id`                  bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `username`            varchar(255) COLLATE utf8_bin NOT NULL COMMENT '账号名',
    `alias_name`          varchar(255) COLLATE utf8_bin NOT NULL COMMENT '显示名',
    `account_expire_time` timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '账号有效期',
    `avatar`              text COLLATE utf8_bin COMMENT '头像',
    `email`               varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '邮箱',
    `mobile`              varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '手机号',
    `password`            varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '密码，md5加密',
    `deleted`             int(10) NOT NULL DEFAULT '0' COMMENT '是否删除，0：未删除，其他：删除',
    `login_ip`            varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '最后登录ip',
    `last_online_time`    timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次使用时间',
    `creator`             varchar(255) COLLATE utf8_bin NOT NULL COMMENT '创建人',
    `create_time`         timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `operator`            varchar(255) COLLATE utf8_bin NOT NULL COMMENT '更新人',
    `update_time`         timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `description`         varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户信息';



CREATE TABLE `bi_workspace`
(
    `id`          int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(100) DEFAULT NULL COMMENT '空间名称',
    `description` varchar(255) DEFAULT '' COMMENT '空间描述',
    `sort`        int(8) DEFAULT NULL COMMENT '排序',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作空间表';



CREATE TABLE `bi_workspace_config`
(
    `id`                int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workspace_id`      int(11) unsigned NOT NULL COMMENT '空间ID',
    `shared`            int(1) DEFAULT '0' COMMENT '是否私有',
    `active_timezone`   varchar(20)  DEFAULT NULL COMMENT '默认时区',
    `selected_timezone` varchar(255) DEFAULT NULL COMMENT '可选时区',
    `create_time`       timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workspace_id` (`workspace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作空间配置';



CREATE TABLE `bi_workspace_member`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workspace_id` int(11) DEFAULT NULL COMMENT '空间ID',
    `username`     varchar(255) DEFAULT NULL COMMENT '用户名称',
    `level`        varchar(20)  DEFAULT NULL COMMENT '成员等级： ADMIN 管理员，ANALYZER 业务分析，WORKER 数据生产者',
    `sys_role_id`  int(11) DEFAULT NULL COMMENT '角色ID',
    `join_time`    datetime     DEFAULT NULL COMMENT '加入时间',
    `create_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workspace_id_username` (`workspace_id`,`username`),
    KEY            `idx_workspace_id` (`workspace_id`),
    KEY            `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空间成员';


INSERT INTO `bi_sys_resource` (`id`, `resource_type`, `module`, `module_name`, `permission`, `permission_name`, `code`, `name`, `comment`, `remark`, `create_time`, `update_time`, `creator`, `operator`)
VALUES
    (2,'FUNCTION','DASHBOARD','看板','VIEW','显示','DASHBOARD:VIEW:CREATE','新建看板',NULL,NULL,'2023-11-14 17:01:07','2023-11-20 14:35:39',NULL,NULL),
    (3,'FUNCTION','DASHBOARD','看板','VIEW','显示','DASHBOARD:VIEW:PUBLIC:FOLDER','公共文件夹','可创建/编辑/删除看板列表的公共文件夹',NULL,'2023-11-14 17:01:40','2023-11-20 14:35:39',NULL,NULL),
    (4,'FUNCTION','DASHBOARD','看板','VIEW','显示','DASHBOARD:VIEW:PRIVATE:FOLDER','个人文件夹','可创建/编辑/删除看板列表的个人文件夹',NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (5,'FUNCTION','DATASET','数据集','VIEW','显示','DATASET:VIEW:CREATE','新建数据集',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (6,'FUNCTION','DATASET','数据集','VIEW','显示','DATASET:VIEW:PUBLIC:FOLDER','公共文件夹','可创建/编辑/删除数据集列表的公共文件夹',NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (7,'FUNCTION','DATASET','数据集','VIEW','显示','DATASET:VIEW:PRIVATE:FOLDER','个人文件夹','可创建/编辑/删除数据集列表的个人文件夹',NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (8,'FUNCTION','REPORT','图表','VIEW','显示','REPORT:VIEW:CREATE','新建图表',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (9,'FUNCTION','WORKSPACE','空间管理','VIEW','显示','WORKSPACE:VIEW:MANAGE:USER','管理成员',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (10,'FUNCTION','WORKSPACE','空间管理','VIEW','显示','WORKSPACE:VIEW:MANAGE:ROLE','管理成员组','可添加/编辑/删除空间内的成员组和管理成员组成员',NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (11,'SCOPE','DASHBOARD','看板','READ','查看','DASHBOARD:READ:HAS:PRIVILEGE','有权限的',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (12,'SCOPE','DASHBOARD','看板','READ','查看','DASHBOARD:READ:ALL:WORKSPACE','空间内所有',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (13,'SCOPE','DASHBOARD','看板','WRITE','编辑','DASHBOARD:WRITE:CREATE','创建的',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (14,'SCOPE','DASHBOARD','看板','WRITE','编辑','DASHBOARD:WRITE:HAS:PRIVILEGE','有协作权限的','看板创建人和协作者均有看板协作权限',NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (15,'SCOPE','DASHBOARD','看板','WRITE','编辑','DASHBOARD:WRITE:ALL:WORKSPACE','空间内所有',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (16,'SCOPE','DASHBOARD','看板','MANAGE','管理','DASHBOARD:MANAGE:CREATE','创建的',NULL,'看板移动、授权、下线/发布、删除操作','2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (17,'SCOPE','DASHBOARD','看板','MANAGE','管理','DASHBOARD:MANAGE:HAS:PRIVILEGE','有协作权限的',NULL,'看板移动、授权、下线/发布、删除操作','2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (18,'SCOPE','DASHBOARD','看板','MANAGE','管理','DASHBOARD:MANAGE:ALL:WORKSPACE','空间内所有',NULL,'看板移动、授权、下线/发布、删除操作','2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (19,'SCOPE','DATASET','数据集','ANALYSIS','分析','DATASET:ANALYSIS:HAS:PRIVILEGE','有权限的',NULL,NULL,'2023-11-14 17:03:28','2023-11-30 15:05:56',NULL,NULL),
    (20,'SCOPE','DATASET','数据集','ANALYSIS','分析','DATASET:ANALYSIS:ALL:WORKSPACE','空间内所有',NULL,NULL,'2023-11-14 17:03:28','2023-11-30 15:06:00',NULL,NULL),
    (21,'SCOPE','DATASET','数据集','WRITE','编辑','DATASET:WRITE:CREATE','创建的',NULL,NULL,'2023-11-14 17:03:28','2023-11-30 15:06:05',NULL,NULL),
    (22,'SCOPE','DATASET','数据集','WRITE','编辑','DATASET:WRITE:HAS:PRIVILEGE','有协作权限的',NULL,NULL,'2023-11-14 17:03:28','2023-11-30 15:06:09',NULL,NULL),
    (23,'SCOPE','DATASET','数据集','WRITE','编辑','DATASET:WRITE:ALL:WORKSPACE','空间内所有',NULL,NULL,'2023-11-14 17:03:28','2023-11-30 15:06:12',NULL,NULL),
    (24,'SCOPE','DATASET','数据集','MANAGE','管理','DATASET:MANAGE:CREATE','创建的',NULL,'数据集移动、授权、导出、设置、下线/发布、删除操作','2023-11-14 17:03:28','2023-11-30 15:06:18',NULL,NULL),
    (25,'SCOPE','DATASET','数据集','MANAGE','管理','DATASET:MANAGE:HAS:PRIVILEGE','有协作权限的',NULL,'数据集移动、授权、导出、设置、下线/发布、删除操作','2023-11-14 17:03:28','2023-11-30 15:06:22',NULL,NULL),
    (26,'SCOPE','DATASET','数据集','MANAGE','管理','DATASET:MANAGE:ALL:WORKSPACE','空间内所有',NULL,'数据集移动、授权、导出、设置、下线/发布、删除操作','2023-11-14 17:03:28','2023-11-30 15:06:25',NULL,NULL),
    (33,'SCOPE','REPORT','图表','READ','分析','REPORT:READ:HAS:PRIVILEGE','有权限的',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (34,'SCOPE','REPORT','图表','READ','分析','REPORT:READ:ALL:WORKSPACE','空间内所有',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (35,'SCOPE','REPORT','图表','WRITE','编辑','REPORT:WRITE:CREATE','创建的',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (36,'SCOPE','REPORT','图表','WRITE','编辑','REPORT:WRITE:HAS:PRIVILEGE','有协作权限的',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (37,'SCOPE','REPORT','图表','WRITE','编辑','REPORT:WRITE:ALL:WORKSPACE','空间内所有',NULL,NULL,'2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (38,'SCOPE','REPORT','图表','MANAGE','管理','REPORT:MANAGE:CREATE','创建的',NULL,'图表删除操作','2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (39,'SCOPE','REPORT','图表','MANAGE','管理','REPORT:MANAGE:HAS:PRIVILEGE','有协作权限的',NULL,'图表删除操作','2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL),
    (40,'SCOPE','REPORT','图表','MANAGE','管理','REPORT:MANAGE:ALL:WORKSPACE','空间内所有',NULL,'图表删除操作','2023-11-14 17:03:28','2023-11-20 14:35:39',NULL,NULL);

INSERT INTO `bi_sys_role` (`id`, `name`, `description`, `role_type`, `creator`, `extra`, `create_time`, `update_time`, `operator`)
VALUES
    (1, '空间管理员', '系统预置角色，有空间内最大权限', 'SYSTEM', 'system', '46', '2023-10-16 14:34:24', '2023-11-29 18:32:39', NULL);
INSERT INTO `bi_sys_role_resource_rel` (`id`, `role_id`, `resource_code`, `creator`, `create_time`, `update_time`, `operator`)
VALUES
    (1, 1, 'DASHBOARD:VIEW:CREATE,DASHBOARD:VIEW:PUBLIC:FOLDER,DASHBOARD:VIEW:PRIVATE:FOLDER,DATASET:VIEW:CREATE,DATASET:VIEW:PUBLIC:FOLDER,DATASET:VIEW:PRIVATE:FOLDER,REPORT:VIEW:CREATE,WORKSPACE:VIEW:MANAGE:USER,WORKSPACE:VIEW:MANAGE:ROLE,DASHBOARD:READ:ALL:WORKSPACE,DASHBOARD:WRITE:ALL:WORKSPACE,DASHBOARD:MANAGE:ALL:WORKSPACE,DATASET:MANAGE:ALL:WORKSPACE,DATASET:ANALYSIS:ALL:WORKSPACE,DATASET:WRITE:ALL:WORKSPACE,REPORT:READ:ALL:WORKSPACE,REPORT:WRITE:ALL:WORKSPACE,REPORT:MANAGE:ALL:WORKSPACE,WORKSPACE:ACCESS:ALL,WORKSPACE:MANAGE:ALL', NULL, '2023-11-17 10:20:52', '2023-11-20 14:37:28', NULL);

INSERT INTO bi_sys_role_menu_rel (role_id,menu_id,creator, operator)
VALUES (1, 1, 'Admin','Admin'),
       (1, 2, 'Admin','Admin'),
       (1, 3, 'Admin','Admin'),
       (1, 4, 'Admin','Admin'),
       (1, 5, 'Admin','Admin'),
       (1, 6, 'Admin','Admin'),
       (1, 7, 'Admin','Admin'),
       (1, 8, 'Admin','Admin'),
       (1, 9, 'Admin','Admin');

INSERT INTO bdp_super_bi.bi_sys_menu (name,name_en,parent_id,app_path,sort,icon,url,hidden,description,creator,create_time,operator,update_time) VALUES
     ('看板','dashboard',0,'/super-bi',1,NULL,'/dashboard',0,NULL,'','2023-12-05 16:00:50','','2023-12-05 16:23:25'),
     ('数据集','dataset',0,'/super-bi',2,'','/dataset',0,NULL,'','2023-12-05 16:15:26','','2023-12-05 16:23:27'),
     ('图表','report',0,'/super-bi',3,NULL,'/report',0,NULL,'','2023-12-05 16:15:46','','2023-12-05 16:23:28'),
     ('数据源','Datasource',0,'/super-bi',4,NULL,'/datasource',0,NULL,'','2023-12-05 16:23:05','','2023-12-20 17:15:32'),
     ('空间管理','workspace',0,'/super-bi',5,NULL,'/system/workspace',1,NULL,'','2023-12-05 16:16:16','','2023-12-20 17:15:26'),
     ('授权中心','Authorize Center',0,'/super-bi',6,NULL,'/authority',1,NULL,'','2023-12-05 16:17:11','','2023-12-20 17:15:30'),
     ('我的申请','Apply',6,'/super-bi',1,NULL,'/authority/apply',1,NULL,'','2023-12-05 16:21:34','','2023-12-20 17:15:31'),
     ('我的审批','Approve',6,'/super-bi',2,NULL,'/authority/approve',1,NULL,'','2023-12-05 16:22:30','','2023-12-20 17:15:32'),
     ('申请管理','Manage',6,'/super-bi',3,NULL,'/authority/manage',1,NULL,'','2023-12-05 16:23:05','','2023-12-20 17:15:32');

INSERT INTO bi_sys_admin(username) VALUES ('Admin');

INSERT INTO `bi_sys_user` (`id`, `username`, `alias_name`, `account_expire_time`, `avatar`, `email`, `mobile`, `password`, `creator`, `operator`)
VALUES (1,'Admin','管理员','2023-12-31 23:59:59', null, null,null,'$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'Admin', 'Admin');

INSERT INTO `bi_workspace`(`name`,`description`,`sort`) VALUES ('默认空间','系统自带默认空间',1);