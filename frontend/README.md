# ZAGENT 前端

ZAGENT 智能体管理系统的前端，基于 **Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router 4** 构建。

## 功能页面

| 模块 | 说明 |
|------|------|
| 问答中心 | ZCode 风格流式问答：思考折叠、工具调用状态帧、耗时统计、历史回放 |
| AI 助手 | 模型配置（服务商/模型两层）、智能体配置、技能管理（zip 导入） |
| 系统管理 | 用户、角色、菜单、部门、岗位、字典、参数、日志 |
| 系统工具 | 系统接口（Springdoc Swagger UI） |

## 开发

```bash
npm install
npm run dev        # http://localhost，代理到后端 http://localhost:8080
```

## 构建

```bash
npm run build:prod   # 输出到 dist/，gzip 压缩
```

## 目录约定

- `src/api` 接口封装，按后端模块分目录
- `src/views` 页面，路由组件路径与后端菜单表 `component` 字段对应
- `src/layout` 主框架（侧边栏/顶栏/标签页）
- `src/store` Pinia 状态模块
- `src/utils/zagent.ts` 通用工具函数
