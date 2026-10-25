<template>
  <div class="login">
    <!-- 背景光晕 -->
    <div class="login-bg">
      <div class="orb orb-a"></div>
      <div class="orb orb-b"></div>
      <div class="orb orb-c"></div>
      <div class="grid-overlay"></div>
    </div>

    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-logo">
        <img src="@/assets/logo/logo.svg" alt="ZAGENT" />
        <span class="brand-name">ZAGENT</span>
      </div>
      <h1 class="brand-title">智能体管理平台</h1>
      <p class="brand-desc">
        模型接入 · 智能体编排 · 技能扩展 · 全流程问答
      </p>
      <ul class="brand-points">
        <li>
          <span class="dot"></span>
          <div class="point-body">
            <div class="point-title">双厂商模型接入</div>
            <div class="point-sub">Anthropic / OpenAI 一键配置与连通测试</div>
          </div>
        </li>
        <li>
          <span class="dot"></span>
          <div class="point-body">
            <div class="point-title">流式问答与思考轨迹</div>
            <div class="point-sub">工具调用状态帧全程可视、历史完整回放</div>
          </div>
        </li>
        <li>
          <span class="dot"></span>
          <div class="point-body">
            <div class="point-title">技能库与工具沙箱</div>
            <div class="point-sub">zip 技能导入，read / write / edit / bash 沙箱执行</div>
          </div>
        </li>
      </ul>
    </div>

    <!-- 右侧登录卡片 -->
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <div class="form-header">
        <img class="form-logo" src="@/assets/logo/logo.svg" alt="ZAGENT" />
        <div class="form-title">{{ title }}</div>
        <div class="form-subtitle">欢迎回来，请登录您的账号</div>
      </div>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <div class="form-options">
        <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        <el-link v-if="register" type="primary" :underline="false" @click="router.push('/register')">
          立即注册
        </el-link>
      </div>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          class="login-btn"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 底部版权 -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'
import type { CaptchaInfoResult } from '@/types/api/login'
import type { LoginForm } from '@/types/api/login'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref<LoginForm>({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref<string | undefined>(undefined)

watch(route, (newRoute: any) => {
    redirect.value = (newRoute.query && newRoute.query.redirect) as string | undefined
}, { immediate: true })

function handleLogin(): void {
  proxy.$refs.loginRef.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc: Record<string, any>, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode(): void {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie(): void {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 72px;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(1200px 800px at 78% 18%, #232158 0%, transparent 55%),
              radial-gradient(900px 700px at 12% 85%, #12224d 0%, transparent 55%),
              linear-gradient(160deg, #0b1023 0%, #0e1330 48%, #131a3f 100%);
}

/* ---------- 背景光晕与网格 ---------- */
.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(90px);
    opacity: .5;
    animation: float 14s ease-in-out infinite;
  }
  .orb-a {
    width: 420px;
    height: 420px;
    right: 6%;
    top: -120px;
    background: #6366f1;
  }
  .orb-b {
    width: 360px;
    height: 360px;
    left: 4%;
    bottom: -140px;
    background: #7c3aed;
    animation-delay: -5s;
  }
  .orb-c {
    width: 260px;
    height: 260px;
    left: 42%;
    top: 55%;
    background: #0891b2;
    opacity: .3;
    animation-delay: -9s;
  }
  .grid-overlay {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px),
      linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
    background-size: 44px 44px;
    mask-image: radial-gradient(ellipse 90% 80% at 50% 40%, #000 30%, transparent 100%);
  }
}
@keyframes float {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(24px, -20px, 0) scale(1.06); }
}

/* ---------- 左侧品牌区 ---------- */
.brand-panel {
  position: relative;
  z-index: 1;
  max-width: 460px;
  color: #e2e8f0;

  .brand-logo {
    display: flex;
    align-items: center;
    gap: 14px;

    img {
      width: 46px;
      height: 46px;
      border-radius: 12px;
      box-shadow: 0 8px 24px rgba(99, 102, 241, 0.45);
    }
    .brand-name {
      font-size: 26px;
      font-weight: 800;
      letter-spacing: 4px;
      background: linear-gradient(92deg, #a5b4fc 0%, #c4b5fd 45%, #67e8f9 100%);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }
  }
  .brand-title {
    margin: 26px 0 10px;
    font-size: 30px;
    font-weight: 700;
    color: #f1f5f9;
    letter-spacing: 2px;
  }
  .brand-desc {
    margin: 0 0 34px;
    font-size: 15px;
    color: #94a3b8;
    letter-spacing: 1px;
  }
  .brand-points {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 20px;

    li {
      display: flex;
      align-items: flex-start;
      gap: 14px;
    }
    .dot {
      flex-shrink: 0;
      width: 9px;
      height: 9px;
      margin-top: 7px;
      border-radius: 50%;
      background: linear-gradient(135deg, #818cf8, #67e8f9);
      box-shadow: 0 0 10px rgba(129, 140, 248, 0.9);
    }
    .point-title {
      font-size: 15px;
      font-weight: 600;
      color: #e2e8f0;
    }
    .point-sub {
      margin-top: 3px;
      font-size: 13px;
      color: #7c8aa5;
    }
  }
}

/* ---------- 登录卡片 ---------- */
.login-form {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 38px 36px 22px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: 0 24px 64px rgba(2, 6, 23, 0.55);

  .form-header {
    text-align: center;
    margin-bottom: 26px;

    .form-logo {
      width: 52px;
      height: 52px;
      border-radius: 14px;
      box-shadow: 0 10px 28px rgba(99, 102, 241, 0.5);
    }
    .form-title {
      margin-top: 14px;
      font-size: 22px;
      font-weight: 700;
      color: #f8fafc;
      letter-spacing: 2px;
    }
    .form-subtitle {
      margin-top: 6px;
      font-size: 13px;
      color: #8fa0bd;
    }
  }

  .el-input {
    height: 44px;

    :deep(.el-input__wrapper) {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.14);
      border-radius: 10px;
      box-shadow: none;
      transition: border-color .25s, background .25s;

      &:hover,
      &.is-focus {
        background: rgba(255, 255, 255, 0.12);
        border-color: rgba(129, 140, 248, 0.75);
        box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.18);
      }

      .el-input__inner {
        color: #eef2ff;

        &::placeholder {
          color: #7c8aa5;
        }
      }
    }
    .input-icon {
      height: 42px;
      width: 16px;
      color: #8fa0bd;
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 2px 0 20px;

    :deep(.el-checkbox__label) {
      color: #aab6d3;
      font-size: 13px;
    }
    :deep(.el-checkbox__inner) {
      background: transparent;
      border-color: rgba(255, 255, 255, 0.3);
    }
    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background: #6366f1;
      border-color: #6366f1;
    }
  }

  .login-btn {
    width: 100%;
    height: 46px;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 6px;
    border: none;
    border-radius: 10px;
    background: linear-gradient(94deg, #6366f1 0%, #8b5cf6 55%, #a855f7 100%);
    box-shadow: 0 10px 28px rgba(124, 58, 237, 0.45);
    transition: transform .2s, box-shadow .2s, filter .2s;

    &:hover {
      transform: translateY(-1px);
      filter: brightness(1.08);
      box-shadow: 0 14px 34px rgba(124, 58, 237, 0.6);
    }
    &:active {
      transform: translateY(0);
    }
  }
}

.login-code {
  width: 33%;
  height: 44px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.login-code-img {
  height: 44px;
  padding-left: 12px;
  border-radius: 8px;
}

/* ---------- 底部版权 ---------- */
.el-login-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 40px;
  line-height: 40px;
  text-align: center;
  color: rgba(226, 232, 240, 0.45);
  font-family: Arial, sans-serif;
  font-size: 12px;
  letter-spacing: 1px;
}

/* ---------- 响应式 ---------- */
@media (max-width: 992px) {
  .login {
    gap: 0;
  }
  .brand-panel {
    display: none;
  }
  .login-form {
    width: min(420px, calc(100vw - 32px));
  }
}
</style>
