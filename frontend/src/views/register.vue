<template>
  <div class="register">
    <div class="register-bg">
      <div class="orb orb-a"></div>
      <div class="orb orb-b"></div>
      <div class="grid-overlay"></div>
    </div>

    <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form">
      <div class="form-header">
        <img class="form-logo" src="@/assets/logo/logo.svg" alt="ZAGENT" />
        <div class="form-title">{{ title }}</div>
        <div class="form-subtitle">创建新账号</div>
      </div>
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password" :rules="registerPwdValidator">
        <el-input
          v-model="registerForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="确认密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          size="large"
          v-model="registerForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleRegister"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="register-code">
          <img :src="codeUrl" @click="getCode" class="register-code-img"/>
        </div>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          class="register-btn"
          @click.prevent="handleRegister"
        >
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div class="to-login">
          <router-link class="link-type" :to="'/login'">使用已有账户登录</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from "element-plus"
import { getCodeImg, register } from "@/api/login"
import defaultSettings from '@/settings'
import { usePasswordRule } from "@/utils/passwordRule"
import type { RegisterForm } from '@/types/api/login'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const router = useRouter()
const { proxy } = getCurrentInstance()
const { registerPwdValidator } = usePasswordRule()

const registerForm = ref<RegisterForm>({
  username: "",
  password: "",
  confirmPassword: "",
  code: "",
  uuid: ""
})

const equalToPassword = (rule: any, value: string, callback: (error?: Error) => void): void => {
  if (registerForm.value.password !== value) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" },
    { min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请再次输入您的密码" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref<string>("")
const loading = ref<boolean>(false)
const captchaEnabled = ref<boolean>(true)

function handleRegister(): void {
  proxy.$refs.registerRef.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(() => {
        const username = registerForm.value.username
        ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", "系统提示", {
          dangerouslyUseHTMLString: true,
          type: "success",
        }).then(() => {
          router.push("/login")
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled) {
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
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang='scss' scoped>
.register {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(1200px 800px at 78% 18%, #232158 0%, transparent 55%),
              radial-gradient(900px 700px at 12% 85%, #12224d 0%, transparent 55%),
              linear-gradient(160deg, #0b1023 0%, #0e1330 48%, #131a3f 100%);
}

.register-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(90px);
    opacity: .5;
    animation: regFloat 14s ease-in-out infinite;
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
@keyframes regFloat {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(24px, -20px, 0) scale(1.06); }
}

.register-form {
  position: relative;
  z-index: 1;
  width: min(420px, calc(100vw - 32px));
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

  .register-btn {
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

  .to-login {
    float: right;
    margin-top: 4px;

    .link-type {
      color: #a5b4fc;
      font-size: 13px;

      &:hover {
        color: #c7d2fe;
      }
    }
  }
}

.register-code {
  width: 33%;
  height: 44px;
  float: right;

  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.register-code-img {
  height: 44px;
  padding-left: 12px;
  border-radius: 8px;
}

.el-register-footer {
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
</style>
