-- ----------------------------
-- 1、部门表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dept (
  dept_id           bigint      not null auto_increment,
  parent_id         bigint      default 0,
  ancestors         varchar(50)     default '',
  dept_name         varchar(30)     default '',
  order_num         int          default 0,
  leader            varchar(20)     default null,
  phone             varchar(11)     default null,
  email             varchar(50)     default null,
  status            char(1)         default '0',
  del_flag          char(1)         default '0',
  create_by         varchar(64)     default '',
  create_time 	    datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  primary key (dept_id)
);

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
MERGE INTO sys_dept KEY(dept_id) VALUES(100,  0,   '0',          'ZAGENT',   0, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(101,  100, '0,100',      '深圳总公司', 1, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(102,  100, '0,100',      '长沙分公司', 2, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(103,  101, '0,100,101',  '研发部门',   1, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(104,  101, '0,100,101',  '市场部门',   2, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(105,  101, '0,100,101',  '测试部门',   3, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(106,  101, '0,100,101',  '财务部门',   4, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(107,  101, '0,100,101',  '运维部门',   5, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(108,  102, '0,100,102',  '市场部门',   1, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);
MERGE INTO sys_dept KEY(dept_id) VALUES(109,  102, '0,100,102',  '财务部门',   2, 'ZAGENT', '15888888888', 'ry@qq.com', '0', '0', 'admin', CURRENT_TIMESTAMP, '', null);

-- ----------------------------
-- 2、用户信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user (
  user_id           bigint      not null auto_increment,
  dept_id           bigint      default null,
  user_name         varchar(30)     not null,
  nick_name         varchar(30)     not null,
  user_type         varchar(2)      default '00',
  email             varchar(50)     default '',
  phonenumber       varchar(11)     default '',
  sex               char(1)         default '0',
  avatar            varchar(100)    default '',
  password          varchar(100)    default '',
  status            char(1)         default '0',
  del_flag          char(1)         default '0',
  login_ip          varchar(128)    default '',
  login_date        datetime,
  pwd_update_date   datetime,
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(500)    default null,
  primary key (user_id)
);

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
MERGE INTO sys_user KEY(user_id) VALUES(1,  103, 'admin', 'ZAGENT', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '', null, '管理员');
MERGE INTO sys_user KEY(user_id) VALUES(2,  105, 'ry',    'ZAGENT', '00', 'ry@qq.com',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, '', null, '测试员');

-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_post (
  post_id       bigint      not null auto_increment,
  post_code     varchar(64)     not null,
  post_name     varchar(50)     not null,
  post_sort     int          not null,
  status        char(1)         not null,
  create_by     varchar(64)     default '',
  create_time   datetime,
  update_by     varchar(64)     default '',
  update_time   datetime,
  remark        varchar(500)    default null,
  primary key (post_id)
);

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------
MERGE INTO sys_post KEY(post_id) VALUES(1, 'ceo',  '董事长',    1, '0', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_post KEY(post_id) VALUES(2, 'se',   '项目经理',  2, '0', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_post KEY(post_id) VALUES(3, 'hr',   '人力资源',  3, '0', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_post KEY(post_id) VALUES(4, 'user', '普通员工',  4, '0', 'admin', CURRENT_TIMESTAMP, '', null, '');

-- ----------------------------
-- 4、角色信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role (
  role_id              bigint      not null auto_increment,
  role_name            varchar(30)     not null,
  role_key             varchar(100)    not null,
  role_sort            int          not null,
  data_scope           char(1)         default '1',
  menu_check_strictly  tinyint      default 1,
  dept_check_strictly  tinyint      default 1,
  status               char(1)         not null,
  del_flag             char(1)         default '0',
  create_by            varchar(64)     default '',
  create_time          datetime,
  update_by            varchar(64)     default '',
  update_time          datetime,
  remark               varchar(500)    default null,
  primary key (role_id)
);

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------
MERGE INTO sys_role KEY(role_id) VALUES('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', CURRENT_TIMESTAMP, '', null, '超级管理员');
MERGE INTO sys_role KEY(role_id) VALUES('2', '普通角色',    'common', 2, 2, 1, 1, '0', '0', 'admin', CURRENT_TIMESTAMP, '', null, '普通角色');

-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
  menu_id           bigint      not null auto_increment,
  menu_name         varchar(50)     not null,
  parent_id         bigint      default 0,
  order_num         int          default 0,
  path              varchar(200)    default '',
  component         varchar(255)    default null,
  query             varchar(255)    default null,
  route_name        varchar(50)     default '',
  is_frame          int          default 1,
  is_cache          int          default 0,
  menu_type         char(1)         default '',
  visible           char(1) default '0',
  status            char(1) default '0',
  perms             varchar(100)    default null,
  icon              varchar(100)    default '#',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(500)    default '',
  primary key (menu_id)
);

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
MERGE INTO sys_menu KEY(menu_id) VALUES('1', '系统管理', '0', '1', 'system',           null, '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', CURRENT_TIMESTAMP, '', null, '系统管理目录');
MERGE INTO sys_menu KEY(menu_id) VALUES('3', '系统工具', '0', '3', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', CURRENT_TIMESTAMP, '', null, '系统工具目录');
-- 二级菜单
MERGE INTO sys_menu KEY(menu_id) VALUES('100',  '用户管理', '1',   '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', CURRENT_TIMESTAMP, '', null, '用户管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('101',  '角色管理', '1',   '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', CURRENT_TIMESTAMP, '', null, '角色管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('102',  '菜单管理', '1',   '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', CURRENT_TIMESTAMP, '', null, '菜单管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', CURRENT_TIMESTAMP, '', null, '部门管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', CURRENT_TIMESTAMP, '', null, '岗位管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('105',  '字典管理', '1',   '6', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', CURRENT_TIMESTAMP, '', null, '字典管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('106',  '参数设置', '1',   '7', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', CURRENT_TIMESTAMP, '', null, '参数设置菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('108',  '日志管理', '1',   '9', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', CURRENT_TIMESTAMP, '', null, '日志管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('117',  '系统接口', '3',   '3', 'swagger',    'tool/swagger/index',       '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',       'swagger',       'admin', CURRENT_TIMESTAMP, '', null, '系统接口菜单');
-- 三级菜单
MERGE INTO sys_menu KEY(menu_id) VALUES('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          'admin', CURRENT_TIMESTAMP, '', null, '操作日志菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    'admin', CURRENT_TIMESTAMP, '', null, '登录日志菜单');
-- 用户管理按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1000', '用户查询', '100', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1001', '用户新增', '100', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1002', '用户修改', '100', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1003', '用户删除', '100', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1004', '用户导出', '100', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1005', '用户导入', '100', '6',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1006', '重置密码', '100', '7',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 角色管理按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1007', '角色查询', '101', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1008', '角色新增', '101', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1009', '角色修改', '101', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1010', '角色删除', '101', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1011', '角色导出', '101', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 菜单管理按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1012', '菜单查询', '102', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1013', '菜单新增', '102', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1014', '菜单修改', '102', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1015', '菜单删除', '102', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 部门管理按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1016', '部门查询', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1017', '部门新增', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1018', '部门修改', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1019', '部门删除', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 岗位管理按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1020', '岗位查询', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1021', '岗位新增', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1022', '岗位修改', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1023', '岗位删除', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1024', '岗位导出', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 字典管理按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1025', '字典查询', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1026', '字典新增', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1027', '字典修改', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1028', '字典删除', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1029', '字典导出', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 参数设置按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1030', '参数查询', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1031', '参数新增', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1032', '参数修改', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1033', '参数删除', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1034', '参数导出', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 通知公告按钮
-- 操作日志按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1039', '操作查询', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1040', '操作删除', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1041', '日志导出', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 登录日志按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1042', '登录查询', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1043', '登录删除', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1044', '日志导出', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1045', '账户解锁', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 在线用户按钮
MERGE INTO sys_menu KEY(menu_id) VALUES('1046', '在线查询', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1047', '批量强退', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES('1048', '单条强退', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
-- 定时任务按钮
-- 代码生成按钮

-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id   bigint not null,
  role_id   bigint not null,
  primary key(user_id, role_id)
);

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------
MERGE INTO sys_user_role KEY(user_id, role_id) VALUES ('1', '1');
MERGE INTO sys_user_role KEY(user_id, role_id) VALUES ('2', '2');

-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
  role_id   bigint not null,
  menu_id   bigint not null,
  primary key(role_id, menu_id)
);

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '3');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '100');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '101');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '102');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '103');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '104');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '105');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '106');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '108');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '117');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '500');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '501');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1000');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1001');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1002');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1003');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1004');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1005');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1006');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1007');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1008');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1009');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1010');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1011');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1012');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1013');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1014');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1015');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1016');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1017');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1018');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1019');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1020');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1021');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1022');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1023');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1024');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1025');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1026');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1027');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1028');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1029');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1030');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1031');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1032');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1033');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1034');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1039');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1040');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1041');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1042');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1043');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1044');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1045');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1046');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1047');
MERGE INTO sys_role_menu KEY(role_id, menu_id) VALUES ('2', '1048');

-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_role_dept (
  role_id   bigint not null,
  dept_id   bigint not null,
  primary key(role_id, dept_id)
);

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------
MERGE INTO sys_role_dept KEY(role_id, dept_id) VALUES ('2', '100');
MERGE INTO sys_role_dept KEY(role_id, dept_id) VALUES ('2', '101');
MERGE INTO sys_role_dept KEY(role_id, dept_id) VALUES ('2', '105');

-- ----------------------------
-- 9、用户与岗位关联表  用户1-N岗位
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_user_post (
  user_id   bigint not null,
  post_id   bigint not null,
  primary key (user_id, post_id)
);

-- ----------------------------
-- 初始化-用户与岗位关联表数据
-- ----------------------------
MERGE INTO sys_user_post KEY(user_id, post_id) VALUES ('1', '1');
MERGE INTO sys_user_post KEY(user_id, post_id) VALUES ('2', '2');

-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_oper_log (
  oper_id           bigint      not null auto_increment,
  title             varchar(50)     default '',
  business_type     int          default 0,
  method            varchar(200)    default '',
  request_method    varchar(10)     default '',
  operator_type     int          default 0,
  oper_name         varchar(50)     default '',
  dept_name         varchar(50)     default '',
  oper_url          varchar(255)    default '',
  oper_ip           varchar(128)    default '',
  oper_location     varchar(255)    default '',
  oper_param        varchar(2000)   default '',
  json_result       varchar(2000)   default '',
  status            int          default 0,
  error_msg         varchar(2000)   default '',
  oper_time         datetime,
  cost_time         bigint      default 0,
  primary key (oper_id)
);

-- ----------------------------
-- 11、字典类型表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dict_type (
  dict_id          bigint      not null auto_increment,
  dict_name        varchar(100)    default '',
  dict_type        varchar(100)    default '',
  status           char(1)         default '0',
  create_by        varchar(64)     default '',
  create_time      datetime,
  update_by        varchar(64)     default '',
  update_time      datetime,
  remark           varchar(500)    default null,
  primary key (dict_id),
  unique (dict_type)
);

MERGE INTO sys_dict_type KEY(dict_id) VALUES(1,  '用户性别', 'sys_user_sex',        '0', 'admin', CURRENT_TIMESTAMP, '', null, '用户性别列表');
MERGE INTO sys_dict_type KEY(dict_id) VALUES(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', CURRENT_TIMESTAMP, '', null, '菜单状态列表');
MERGE INTO sys_dict_type KEY(dict_id) VALUES(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', CURRENT_TIMESTAMP, '', null, '系统开关列表');
MERGE INTO sys_dict_type KEY(dict_id) VALUES(6,  '系统是否', 'sys_yes_no',          '0', 'admin', CURRENT_TIMESTAMP, '', null, '系统是否列表');
MERGE INTO sys_dict_type KEY(dict_id) VALUES(9,  '操作类型', 'sys_oper_type',       '0', 'admin', CURRENT_TIMESTAMP, '', null, '操作类型列表');
MERGE INTO sys_dict_type KEY(dict_id) VALUES(10, '系统状态', 'sys_common_status',   '0', 'admin', CURRENT_TIMESTAMP, '', null, '登录状态列表');

-- ----------------------------
-- 12、字典数据表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_dict_data (
  dict_code        bigint      not null auto_increment,
  dict_sort        int          default 0,
  dict_label       varchar(100)    default '',
  dict_value       varchar(100)    default '',
  dict_type        varchar(100)    default '',
  css_class        varchar(100)    default null,
  list_class       varchar(100)    default null,
  is_default       char(1)         default 'N',
  status           char(1)         default '0',
  create_by        varchar(64)     default '',
  create_time      datetime,
  update_by        varchar(64)     default '',
  update_time      datetime,
  remark           varchar(500)    default null,
  primary key (dict_code)
);

MERGE INTO sys_dict_data KEY(dict_code) VALUES(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', CURRENT_TIMESTAMP, '', null, '性别男');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '性别女');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '性别未知');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', CURRENT_TIMESTAMP, '', null, '显示菜单');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '隐藏菜单');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', CURRENT_TIMESTAMP, '', null, '正常状态');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '停用状态');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', CURRENT_TIMESTAMP, '', null, '系统默认是');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '系统默认否');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(18, 99, '其他',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '其他操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(19, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '新增操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(20, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '修改操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(21, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '删除操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(22, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '授权操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(23, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '导出操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(24, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '导入操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(25, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '强退操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(26, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '生成操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(27, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '清空操作');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(28, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '正常状态');
MERGE INTO sys_dict_data KEY(dict_code) VALUES(29, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', CURRENT_TIMESTAMP, '', null, '停用状态');

-- ----------------------------
-- 13、参数配置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_config (
  config_id         int          not null auto_increment,
  config_name       varchar(100)    default '',
  config_key        varchar(100)    default '',
  config_value      varchar(500)    default '',
  config_type       char(1)         default 'N',
  create_by         varchar(64)     default '',
  create_time       datetime,
  update_by         varchar(64)     default '',
  update_time       datetime,
  remark            varchar(500)    default null,
  primary key (config_id)
);

MERGE INTO sys_config KEY(config_id) VALUES(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', CURRENT_TIMESTAMP, '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
MERGE INTO sys_config KEY(config_id) VALUES(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',        'Y', 'admin', CURRENT_TIMESTAMP, '', null, '初始化密码 123456' );
MERGE INTO sys_config KEY(config_id) VALUES(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', CURRENT_TIMESTAMP, '', null, '深色主题theme-dark，浅色主题theme-light' );
MERGE INTO sys_config KEY(config_id) VALUES(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'false',          'Y', 'admin', CURRENT_TIMESTAMP, '', null, '是否开启验证码功能（true开启，false关闭）');
MERGE INTO sys_config KEY(config_id) VALUES(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', CURRENT_TIMESTAMP, '', null, '是否开启注册用户功能（true开启，false关闭）');
MERGE INTO sys_config KEY(config_id) VALUES(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',              'Y', 'admin', CURRENT_TIMESTAMP, '', null, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
MERGE INTO sys_config KEY(config_id) VALUES(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', CURRENT_TIMESTAMP, '', null, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
MERGE INTO sys_config KEY(config_id) VALUES(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', CURRENT_TIMESTAMP, '', null, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
MERGE INTO sys_config KEY(config_id) VALUES(9, '用户管理-密码字符范围',         'sys.account.chrtype',              '0',             'Y', 'admin', CURRENT_TIMESTAMP, '', null, '默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');

-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS sys_logininfor (
  info_id        bigint     not null auto_increment,
  user_name      varchar(50)    default '',
  ipaddr         varchar(128)   default '',
  login_location varchar(255)   default '',
  browser        varchar(50)    default '',
  os             varchar(50)    default '',
  status         char(1)        default '0',
  msg            varchar(255)   default '',
  login_time     datetime,
  primary key (info_id)
);

-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------

-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------

-- ----------------------------
-- 17、通知公告表
-- ----------------------------

-- ----------------------------
-- 初始化-公告信息表数据
-- ----------------------------

-- ----------------------------
-- 18、公告已读记录表
-- ----------------------------

-- ----------------------------
-- 19、代码生成业务表
-- ----------------------------

-- ----------------------------
-- 20、代码生成业务表字段
-- ----------------------------

-- ----------------------------
-- 二级索引（原 MySQL 内联 KEY 定义）
-- ----------------------------
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_bt ON sys_oper_log(business_type);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_s ON sys_oper_log(status);
CREATE INDEX IF NOT EXISTS idx_sys_oper_log_ot ON sys_oper_log(oper_time);
CREATE INDEX IF NOT EXISTS idx_sys_logininfor_s ON sys_logininfor(status);
CREATE INDEX IF NOT EXISTS idx_sys_logininfor_lt ON sys_logininfor(login_time);
-- ----------------------------
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
  description       varchar(2000)    default '',
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
  description       varchar(2000)    default '',
  content           text,
  enabled           char(1)          default '0',
  create_by         varchar(64)      default '',
  create_time       datetime,
  update_by         varchar(64)      default '',
  update_time       datetime,
  remark            varchar(500)     default null,
  primary key (skill_id)
);

-- 技能附属资源（现代技能包：SKILL.md 之外的 references/scripts/data 等，落盘于沙箱 skills/{技能名}/）
CREATE TABLE IF NOT EXISTS ai_skill_resource (
  resource_id       bigint       not null auto_increment,
  skill_id          bigint       not null,
  rel_path          varchar(500)     not null,
  file_size         bigint           default 0,
  create_time       datetime,
  primary key (resource_id)
);
CREATE INDEX IF NOT EXISTS idx_skill_resource ON ai_skill_resource (skill_id);

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
MERGE INTO sys_menu KEY(menu_id) VALUES(2000, 'AI 助手', 0, 4, 'ai', null, '', '', 1, 0, 'M', '0', '0', '', 'message', 'admin', CURRENT_TIMESTAMP, '', null, 'AI 助手目录');
MERGE INTO sys_menu KEY(menu_id) VALUES(2001, '模型配置', 2000, 1, 'model', 'ai/model/index', '', '', 1, 0, 'C', '0', '0', 'ai:provider:list', 'list', 'admin', CURRENT_TIMESTAMP, '', null, '服务商与模型配置菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES(2002, '智能体配置', 2000, 2, 'agent', 'ai/agent/index', '', '', 1, 0, 'C', '0', '0', 'ai:agent:list', 'peoples', 'admin', CURRENT_TIMESTAMP, '', null, '智能体配置菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES(2003, '技能管理', 2000, 3, 'skill', 'ai/skill/index', '', '', 1, 0, 'C', '0', '0', 'ai:skill:list', 'skill', 'admin', CURRENT_TIMESTAMP, '', null, '技能管理菜单');
MERGE INTO sys_menu KEY(menu_id) VALUES(2004, '问答中心', 2000, 4, 'chat', 'ai/chat/index', '', '', 1, 0, 'C', '0', '0', 'ai:chat:list', 'online', 'admin', CURRENT_TIMESTAMP, '', null, 'AI 问答菜单');

MERGE INTO sys_menu KEY(menu_id) VALUES(2005, '服务商查询', 2001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2006, '服务商新增', 2001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2007, '服务商修改', 2001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2008, '服务商删除', 2001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:provider:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2009, '模型查询', 2001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2010, '模型新增', 2001, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2011, '模型修改', 2001, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2012, '模型删除', 2001, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:model:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2013, '智能体查询', 2002, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2014, '智能体新增', 2002, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2015, '智能体修改', 2002, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2016, '智能体删除', 2002, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:agent:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2017, '技能查询', 2003, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2018, '技能新增', 2003, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2019, '技能修改', 2003, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2020, '技能删除', 2003, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2021, '技能导入', 2003, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:skill:import', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
MERGE INTO sys_menu KEY(menu_id) VALUES(2022, '发起问答', 2004, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'ai:chat:chat', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

-- ----------------------------
-- 示例技能（默认停用，启用后才能被勾选/调用）
-- ----------------------------
MERGE INTO ai_skill KEY(skill_id) VALUES(1, 'code-review', '代码评审专家：对指定代码给出问题清单、风险点与改进建议', '# 代码评审技能

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

MERGE INTO ai_skill KEY(skill_id) VALUES(2, 'sql-expert', 'SQL 优化专家：分析慢查询并给出索引与改写建议', '# SQL 优化技能

你是一名数据库 SQL 优化专家。收到 SQL 后请：

1. 解释该 SQL 的执行逻辑与可能的性能瓶颈
2. 给出建议的索引（含字段顺序与理由）
3. 给出改写后的 SQL（说明每处改动的收益）
4. 提示潜在的锁、事务与分页深翻页问题

## 常用思路
- EXPLAIN 关键指标：type、rows、Extra
- 覆盖索引避免回表
- 大分页用游标或延迟关联', '0', 'admin', CURRENT_TIMESTAMP, '', null, '示例技能，可删除');

