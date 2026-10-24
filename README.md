# ZAGENT — 智能体管理系统

以 RuoYi-Vue 为骨干（瘦身 + 全面改名 `com.zagent`）+ **Spring AI 2.0** 构建的前后端分离智能体平台。

## 功能总览

| 模块 | 说明 |
|------|------|
| 主干骨架 | 登录/JWT、用户、角色、菜单、部门、岗位、字典、参数、操作/登录日志、在线用户、swagger、druid 监控 |
| 模型配置 | 两层结构：服务商（Anthropic / OpenAI，API Key + Base URL 支持中转）→ 模型（modelId、显示名、是否支持思考）；一键测试连通并拉取可选模型列表 |
| 智能体配置 | 绑定模型、思考等级（关/低/中/高）、温度、maxTokens、启用工具、系统提示词 |
| 技能管理 | 全局技能库，手动录入 + **zip 导入**（`{name}/SKILL.md` frontmatter 格式，兼容散装 md）；**启用后才能被勾选**；聊天中 `/技能名` 调用，可多个 |
| 问答中心 | 仿 ZCode 桌面版：流式输出、"已工作 x 分 x 秒"状态条、可折叠思考（模型 reasoning）、工具调用行（`● read x` / `● bash x` / `● edit x` 状态帧 + 耗时 + 展开参数/输出）、markdown 渲染 |
| 工具沙箱 | read / write / edit / bash，限制在 `./ai-workspace` 目录（路径规范化防逃逸）；bash 60s 超时、输出截断；自动执行 |
| 会话留痕 | 会话/消息/工具轨迹三表落库，历史完整回放（含思考与工具过程） |
| 无 Redis | 默认内存缓存（`zagent.cache.type=memory`），零外部依赖；需要分布式时切 `redis` 即可 |

## 目录结构

```
ZAGENT/
├── backend/                  # 后端（Maven 多模块）
│   ├── zagent-admin          # 入口 ZAgentApplication（默认端口 8080）
│   ├── zagent-framework      # 安全/JWT/AOP/Druid/H2 兼容别名
│   ├── zagent-system         # 系统主干业务
│   ├── zagent-common         # 工具/基类/内存缓存
│   └── zagent-ai             # AI 模块（CRUD + 适配层 + 工具 + SSE 问答）
├── frontend/                 # 前端（RuoYi-Vue3 TypeScript 分支改造，Vite + Element Plus）
├── sql/
│   └── h2-init.sql           # H2 初始化脚本（幂等，可重复执行）
├── 启动H2数据库.bat           # 一键启动独立 H2 服务（TCP 9101 + Web 控制台 8082）
└── 停止H2数据库.bat
```

## 快速启动（默认 H2，零外部依赖）

```bat
1. 启动H2数据库.bat        :: 启动 H2 服务并自动初始化数据库（首次自动下载 h2.jar）
2. 后端：IDE 运行 ZAgentApplication，或
   java -Dlog.path=./logs -jar backend/zagent-admin/target/zagent-admin.jar
   （首次需先构建：cd backend && mvn clean package -DskipTests）
3. 前端：cd frontend && npm install && npm run dev   :: http://localhost（代理到 8080）
4. 登录：admin / admin123
```

- H2 控制台：http://localhost:8082 （JDBC URL `jdbc:h2:tcp://localhost:9101/./data/zagent;MODE=MySQL;DATABASE_TO_LOWER=TRUE`，用户 `sa`，密码为空）
- 重置数据库：停止后删除 `data/` 目录再运行启动脚本（脚本幂等，正常重启不丢数据）
- 验证码默认关闭（`sys.account.captchaEnabled=false`），可在 参数设置 中开启

## 使用 MySQL（可选）

1. 用 `sql/h2-init.sql` 中的表结构在 MySQL 建库导入（该脚本为 H2/MySQL 双兼容写法；`sql/ai-mysql.sql` 含 AI 相关表的 MySQL 版本）
2. 启动后端时指定：`--spring.profiles.active=druid`，并修改 `application-druid.yml` 连接信息

## AI 配置说明

- **Anthropic**：Base URL 默认 `https://api.anthropic.com`；思考等级映射 extended thinking 预算（低 2048 / 中 8196 / 高 16384，开启时 temperature 自动置 1.0）；流式思考增量独立下发
- **OpenAI（及兼容服务）**：Base URL 填到根地址即可（自动补 `/v1`），DeepSeek 等中转直接填对应地址；GPT-5 / o 系自动使用 `maxCompletionTokens` 并禁用 temperature；思考等级映射 `reasoning_effort`；兼容服务的 `reasoning_content` 思考内容已做差分处理
- **限制**：官方 OpenAI 模型经 Chat Completions 不回传推理文本（API 限制），思考面板会显示为空

## 技术栈

- 后端：Java 17 · Spring Boot 4.1.2 · Spring AI 2.0.2（spring-ai-anthropic / spring-ai-openai + 官方 SDK OkHttp 客户端）· MyBatis · Druid · H2 2.3.232 · 无 Redis（内存缓存可切换）
- 前端：Vue 3.5 (TypeScript) · Element Plus · Vite 6 · Pinia · marked + highlight.js
- 认证：JWT（token 存内存缓存，重启后端后需重新登录）

## 安全提示

- api_key 明文存于数据库（demo 定位），生产环境请自行加密
- bash 工具在服务端真实执行命令，已做沙箱目录 + 超时 + 输出截断 + 独立权限，但仍属高危能力，仅建议在可信环境使用
