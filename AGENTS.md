# ZAGENT 工作区说明

智能体管理系统：RuoYi-Vue 骨架（已瘦身并全面改名 `com.zagent`、品牌 ZAGENT）+ Spring AI 2.0。后端 Java 17 / Spring Boot 4.1.2 / MyBatis / H2（默认，无 Redis）；前端 Vue 3.5 TypeScript / Vite 6 / Element Plus / Pinia。功能：服务商/模型两层配置、智能体配置、技能库（zip 导入）、仿 ZCode 风格的流式问答中心（思考折叠 + 工具状态帧 + 历史回放）。

## 目录结构

- `backend/` — Maven 多模块，依赖链：admin → framework → system → common，另 admin → ai → common
  - `zagent-admin`：入口 `ZAgentApplication`（端口 8080），`application.yml` / `application-h2.yml` / `application-druid.yml`
  - `zagent-framework`：安全 / JWT / AOP / Druid / H2 兼容别名
  - `zagent-system`：系统主干业务（用户/角色/菜单/字典等）
  - `zagent-common`：工具 / 基类 / 内存缓存
  - `zagent-ai`：AI 模块（provider/model/agent/skill 四套 CRUD、适配层、工具沙箱、SSE 问答）
- `frontend/` — 页面在 `src/views/ai/`（agent/chat/model/skill），API 封装在 `src/api/ai/`
- `sql/h2-init.sql` — H2/MySQL 双兼容幂等初始化脚本；`sql/ai-mysql.sql` 为 AI 表 MySQL 版
- `ai-workspace/` — 工具沙箱根目录（gitignored，运行时生成）
- `data/`、`logs/` — H2 数据库文件与日志（gitignored）
- `启动H2数据库.bat` / `停止H2数据库.bat` — 独立 H2 服务（TCP 9101，Web 控制台 http://localhost:8082）

## 常用命令

```bash
# 后端构建（仓库无测试代码，-DskipTests 即常规构建）
cd backend && mvn clean package -DskipTests

# 后端运行（需先启动 H2，或 IDE 运行 ZAgentApplication）
java -Dlog.path=./logs -jar backend/zagent-admin/target/zagent-admin.jar

# 前端
cd frontend && npm install && npm run dev   # 端口 80，/dev-api 代理到 localhost:8080
npm run build:prod                          # 生产构建
```

- 登录账号：`admin / admin123`（验证码默认关闭）
- 仓库没有任何测试，改动靠启动冒烟 + Swagger UI（`/swagger-ui.html`）验证

## 架构边界

- **AI 适配层**（`zagent-ai/adapter/`）：`ProviderAdapter` SPI 把厂商流式 API 归一为 `UnifiedEvent` Flux 流。新增厂商 = 新增一个实现类并注册为 Spring Bean；Chat 服务只消费 `UnifiedEvent`，不碰厂商 SDK 细节。
- **SSE 帧协议**：`UnifiedEvent.type` 与前端帧一一对应（thinking / delta / tool_start / tool_end / warning / error / done）。改协议需前后端同步。
- **工具沙箱**（`zagent-ai/tools/`）：read/write/edit 限制在 `./ai-workspace`（路径规范化防逃逸）；bash 工作目录为沙箱目录，60s 超时 + 输出截断。工作目录、轮数上限、超时等配置在 `zagent.ai.*`（application.yml）。
- **模块归属**：系统业务放 zagent-system，AI 相关一律进 zagent-ai；跨模块复用的放 zagent-common。
- **无 Redis**：默认内存缓存（`zagent.cache.type=memory`），JWT token 存内存 → 重启后端需重新登录；切 redis 只改该配置。
- **数据库**：默认 profile `h2`（MODE=MySQL）；MySQL 用 profile `druid`。建表/改表同时维护 `sql/h2-init.sql`（幂等 MERGE INTO 写法），Mapper XML 中的 SQL 保持 H2/MySQL 双兼容。

## 约定

- 注释、Javadoc、commit message 均用中文；Java 类带 `@author zagent` 标签。
- 品牌：全仓 ZAGENT，禁止再引入 RuoYi/若依字样（清理进行中，见残留随手清）。
- 后端沿用 RuoYi 风格：Controller 继承 `BaseController`、返回 `AjaxResult`、分页 `startPage()`、权限注解 `@PreAuthorize("@ss.hasPermi(...)")`。
- 前端：路径别名 `@` → `src/`；Vue API 与 Element Plus 组件已 auto-import（`auto-imports.d.ts`），无需手动 import。
- 技能 zip 导入规则：`{技能名}/SKILL.md` 是技能定义，其余文档作为附属资源落盘、不单独成技能；整个 zip 仅一个 md 时按散装单文件技能兼容导入。管理页编辑技能正文会同步写回沙箱 SKILL.md。

## 注意事项

- 先启动 H2（bat 脚本）再启动后端，否则连接失败；重置数据库 = 停止后删 `data/` 目录再跑脚本（脚本幂等，正常重启不丢数据）。
- 前端 dev 端口是 80，Windows 下可能需要管理员权限（或改 `vite.config.ts`）。
- XSS 过滤排除了 `/ai/chat/send` 和 `/ai/skill/import`；新增 JSON/文件流接口需同步 `application.yml` 的 `xss.excludes`。
- api_key 明文存库（demo 定位）；bash 工具在服务端真实执行命令，改动沙箱逻辑需谨慎。
- Spring Boot 4.1.2 + Spring AI 2.0.2 为较新组合，Maven 走 aliyun 镜像（见 backend/pom.xml）。
- OpenAI 官方模型经 Chat Completions 不回传推理文本（API 限制），思考面板为空属预期。
