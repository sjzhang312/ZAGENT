-- ZAGENT AI 模块 MySQL 版表结构与菜单（可选：使用 druid/MySQL profile 时导入）
-- 说明：表结构与 h2-init.sql 中 AI 段一致；种子使用 REPLACE INTO 幂等写入
-- ZAGENT AI 模块表
-- ----------------------------

-- 服务商配置（anthropic / openai）
CREATE TABLE IF NOT EXISTS ai_provider (
  provider_id       bigint       not null auto_increment,
  provider_name     varchar(50)  not null,
  provider_code     varchar(20)  not null,
  api_key           varchar(255) not null,
  base_url          varchar(255)     default null,
  status            char(1)          default '0',
  create_by         varchar(64)      default '',
  create_time       datetime,
  update_by         varchar(64)      default '',
  update_time       datetime,
  remark            varchar(500)     default null,
  primary key (provider_id)
);

-- 模型配置（挂在服务商下）
CREATE TABLE IF NOT EXISTS ai_model (
  model_id          bigint       not null auto_increment,
  provider_id       bigint       not null,
  model_code        varchar(100) not null,
  display_name      varchar(100)     default '',
  support_reasoning char(1)          default '0',
  status            char(1)          default '0',
  create_by         varchar(64)      default '',
  create_time       datetime,
  update_by         varchar(64)      default '',
  update_time       datetime,
  remark            varchar(500)     default null,
  primary key (model_id)
);

-- 智能体配置
CREATE TABLE IF NOT EXISTS ai_agent (
  agent_id          bigint       not null auto_increment,
  agent_name        varchar(50)  not null,
  description       varchar(255)     default '',
  model_id          bigint       not null,
  thinking_level    int              default 0,
  temperature       decimal(3,2)     default 0.7,
  max_tokens        int              default 4096,
  enabled_tools     varchar(100)     default 'read,write,edit,bash',
  system_prompt     text,
  status            char(1)          default '0',
  create_by         varchar(64)      default '',
  create_time       datetime,
  update_by         varchar(64)      default '',
  update_time       datetime,
  remark            varchar(500)     default null,
  primary key (agent_id)
);

-- 技能库（全局）
CREATE TABLE IF NOT EXISTS ai_skill (
  skill_id          bigint       not null auto_increment,
  skill_name        varchar(50)  not null,
  description       varchar(255)     default '',
  content           text,
  enabled           char(1)          default '0',
  create_by         varchar(64)      default '',
  create_time       datetime,
  update_by         varchar(64)      default '',
  update_time       datetime,
  remark            varchar(500)     default null,
  primary key (skill_id)
);

-- 问答会话
CREATE TABLE IF NOT EXISTS ai_chat_session (
  session_id        bigint       not null auto_increment,
  session_name      varchar(100)     default '',
  agent_id          bigint       not null,
  user_id           bigint       not null,
  create_by         varchar(64)      default '',
  create_time       datetime,
  update_by         varchar(64)      default '',
  update_time       datetime,
  remark            varchar(500)     default null,
  primary key (session_id)
);

-- 问答消息（含思考内容与耗时）
CREATE TABLE IF NOT EXISTS ai_chat_message (
  message_id        bigint       not null auto_increment,
  session_id        bigint       not null,
  role              varchar(20)      default 'user',
  content           text,
  reasoning         text,
  status            varchar(20)      default 'done',
  elapsed_ms        bigint           default 0,
  model_desc        varchar(200)     default '',
  create_time       datetime,
  primary key (message_id)
);

-- 工具调用轨迹（历史回放）
CREATE TABLE IF NOT EXISTS ai_chat_trace (
  trace_id          bigint       not null auto_increment,
  message_id        bigint       not null,
  seq               int              default 0,
  tool_name         varchar(50)      default '',
  args              text,
  result            text,
  status            char(1)          default '0',
  duration_ms       bigint           default 0,
  create_time       datetime,
  primary key (trace_id)
);

-- ----------------------------
-- AI 菜单（menu_id 从 2000 起）
-- ----------------------------
REPLACE INTO sys_menu VALUES(2000, 'AI 助手', 0, 4, 'ai', null, '', '', 1, 0, 'M', '0', '0', '', 'message', 'admin', CURRENT_TIMESTAMP, '', null, 'AI 助手目录');
REPLACE INTO sys_menu VALUES(2001, '模型配置', 2000, 1, 'model', 'ai/model/index', '', '', 1, 0, 'C', '0', '0', 'ai:provider:list', 'list', 'admin', CURRENT_TIMESTAMP, '', null, '服务商与模型配置菜单');
REPLACE INTO sys_menu VALUES(2002, '智能体配置', 2000, 2, 'agent', 'ai/agent/index', '', '', 1, 0, 'C', '0', '0', 'ai:agent:list', 'peoples', 'admin', CURRENT_TIMESTAMP, '', null, '智能体配置菜单');
REPLACE INTO sys_menu VALUES(2003, '技能管理', 2000, 3, 'skill', 'ai/skill/index', '', '', 1, 0, 'C', '0', '0', 'ai:skill:list', 'skill', 'admin', CURRENT_TIMESTAMP, '', null, '技能管理菜单');
REPLACE INTO sys_menu VALUES(2004, '问答中心', 2000, 4, 'chat', 'ai/chat/index', '', '', 1, 0, 'C', '0', '0', 'ai:chat:list', 'online', 'admin', CURRENT_TIMESTAMP, '', null, 'AI 问答菜单');

REPLACE INTO sys_menu VALUES(2005, '服务商查询', 2001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2006, '服务商新增', 2001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2007, '服务商修改', 2001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2008, '服务商删除', 2001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2009, '模型查询', 2001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2010, '模型新增', 2001, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2011, '模型修改', 2001, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2012, '模型删除', 2001, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2013, '智能体查询', 2002, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2014, '智能体新增', 2002, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2015, '智能体修改', 2002, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2016, '智能体删除', 2002, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2017, '技能查询', 2003, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2018, '技能新增', 2003, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2019, '技能修改', 2003, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2020, '技能删除', 2003, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2021, '技能导入', 2003, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:import', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
REPLACE INTO sys_menu VALUES(2022, '发起问答', 2004, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:chat:chat', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

-- ----------------------------
-- 示例技能（默认停用，启用后才能被勾选/调用）
-- ----------------------------
REPLACE INTO ai_skill VALUES(1, 'code-review', '代码评审专家：对指定代码给出问题清单、风险点与改进建议', '# 代码评审技能

你是一名资深代码评审专家。收到代码后请：

1. 先通读代码，理解意图
2. 按严重程度列出问题（阻断 / 一般 / 建议）
3. 每个问题给出：位置、原因、修复建议（附代码示例）
4. 最后给出总体评价与改进优先级

## 评审关注点
- 正确性：边界条件、空值、并发、资源释放
- 安全性：注入、越权、敏感信息
- 可维护性：命名、职责划分、重复代码
- 性能：复杂度、不必要的 IO/拷贝', '0', 'admin', CURRENT_TIMESTAMP, '', null, '示例技能，可删除');

REPLACE INTO ai_skill VALUES(2, 'sql-expert', 'SQL 优化专家：分析慢查询并给出索引与改写建议', '# SQL 优化技能

你是一名数据库 SQL 优化专家。收到 SQL 后请：

1. 解释该 SQL 的执行逻辑与可能的性能瓶颈
2. 给出建议的索引（含字段顺序与理由）
3. 给出改写后的 SQL（说明每处改动的收益）
4. 提示潜在的锁、事务与分页深翻页问题

## 常用思路
- EXPLAIN 关键指标：type、rows、Extra
- 覆盖索引避免回表
- 大分页用游标或延迟关联', '0', 'admin', CURRENT_TIMESTAMP, '', null, '示例技能，可删除');

