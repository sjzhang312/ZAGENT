<template>
  <div class="welcome">
    <div class="welcome-card">
      <div class="logo">ZAGENT</div>
      <div class="sub">智能体管理系统</div>
      <div class="desc">
        基于 RuoYi 骨干 + Spring AI 构建的智能体平台：双厂商模型接入（Anthropic / OpenAI）、
        智能体配置、技能库（/ 调用）、四工具沙箱（read / write / edit / bash）、流式问答与思考轨迹回放。
      </div>
      <div class="entry">
        <router-link to="/ai/chat">
          <el-button type="primary" size="large">进入问答中心</el-button>
        </router-link>
        <router-link to="/ai/model">
          <el-button size="large" plain>配置模型</el-button>
        </router-link>
      </div>
      <div class="meta">默认账号 admin / admin123 · H2 数据库 · 版本 v1.0.0</div>
    </div>
  </div>
</template>

<script setup lang="ts" name="Index">
</script>

<style scoped lang="scss">
.welcome {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 160px);

  .welcome-card {
    text-align: center;
    max-width: 560px;
    padding: 48px 40px;

    .logo {
      font-size: 44px;
      font-weight: 800;
      letter-spacing: 10px;
      background: linear-gradient(120deg, var(--el-color-primary), #67c23a);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }

    .sub {
      margin-top: 10px;
      font-size: 18px;
      color: var(--el-text-color-regular);
      letter-spacing: 4px;
    }

    .desc {
      margin-top: 22px;
      font-size: 14px;
      line-height: 1.9;
      color: var(--el-text-color-secondary);
    }

    .entry {
      margin-top: 30px;
      display: flex;
      gap: 14px;
      justify-content: center;
    }

    .meta {
      margin-top: 34px;
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }
}
</style>
