<template>
  <div class="app-container chat-container">
    <!-- 左侧：智能体 + 会话列表 -->
    <div class="sidebar">
      <el-select v-model="agentId" placeholder="选择智能体" size="default" class="agent-select">
        <el-option v-for="a in agents" :key="a.agentId" :label="a.agentName" :value="a.agentId">
          <span>{{ a.agentName }}</span>
          <span class="agent-model">{{ a.providerCode }}/{{ a.modelCode }}</span>
        </el-option>
      </el-select>
      <el-button type="primary" plain class="new-chat" icon="Plus" @click="newSession">新会话</el-button>
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: s.sessionId === currentSessionId }"
          @click="openSession(s.sessionId)"
        >
          <span class="session-name">{{ s.sessionName }}</span>
          <el-icon class="session-del" @click.stop="removeSession(s)"><Delete /></el-icon>
        </div>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="50" />
      </div>
    </div>

    <!-- 主区：消息流 + 输入 -->
    <div class="main">
      <div ref="messageBox" class="message-box">
        <div v-if="!messages.length" class="empty-hint">
          <div class="empty-title">ZAGENT 问答</div>
          <div class="empty-sub">输入 / 可调用已启用技能（可多个），如：/code-review 帮我看看这段代码</div>
        </div>

        <div v-for="(msg, idx) in messages" :key="idx" class="message">
          <!-- 用户消息 -->
          <div v-if="msg.role === 'user'" class="user-msg">{{ msg.content }}</div>

          <!-- AI 消息（ZCode 风格） -->
          <div v-else class="ai-msg">
            <!-- 状态条：已工作 x 分 x 秒，点击折叠/展开细节 -->
            <div class="status-bar" @click="msg.detailOpen = !msg.detailOpen">
              <el-icon class="arrow" :class="{ collapsed: !msg.detailOpen }"><ArrowRight /></el-icon>
              <span class="status-text">
                {{ msg.status === 'running' ? '工作中 · ' + formatElapsed(msg.elapsedMs) : '已工作 ' + formatElapsed(msg.elapsedMs) }}
              </span>
              <span v-if="msg.modelDesc" class="model-desc">{{ msg.modelDesc }}</span>
              <el-icon v-if="msg.status === 'running'" class="loading-icon is-loading"><Loading /></el-icon>
            </div>

            <div v-show="msg.detailOpen" class="detail">
              <!-- 思考折叠面板 -->
              <div v-if="msg.reasoning" class="thinking">
                <div class="thinking-header" @click="msg.thinkingOpen = !msg.thinkingOpen">
                  <el-icon class="arrow" :class="{ collapsed: !msg.thinkingOpen }"><ArrowRight /></el-icon>
                  <span>{{ msg.status === 'running' && !msg.content ? '思考中…' : '思考过程' }}</span>
                </div>
                <div v-show="msg.thinkingOpen" class="thinking-body">{{ msg.reasoning }}</div>
              </div>

              <!-- 工具调用行 -->
              <div v-for="(t, ti) in msg.tools" :key="ti" class="tool-item">
                <div class="tool-line" @click="t.open = !t.open">
                  <span class="tool-dot" :class="t.status" />
                  <span class="tool-name">{{ t.tool }}</span>
                  <span class="tool-args">{{ argsSummary(t.args) }}</span>
                  <span class="tool-duration">{{ t.status === 'running' ? '执行中…' : formatElapsed(t.durationMs || 0) }}</span>
                  <el-icon class="arrow small" :class="{ collapsed: !t.open }"><ArrowRight /></el-icon>
                </div>
                <div v-if="t.open" class="tool-detail">
                  <div class="tool-detail-block"><div class="label">参数</div><pre>{{ pretty(t.args) }}</pre></div>
                  <div class="tool-detail-block"><div class="label">输出{{ t.status === 'error' ? '（失败）' : '' }}</div><pre>{{ t.result }}</pre></div>
                </div>
              </div>
            </div>

            <!-- 错误信息 -->
            <div v-if="msg.error" class="error-line">⚠ {{ msg.error }}</div>

            <!-- 正文 markdown -->
            <div v-if="msg.content" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
            <div v-else-if="msg.status === 'running' && !msg.reasoning && !msg.tools.length" class="cursor-line">▍</div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <div v-if="skillPopup.show" class="skill-popup">
          <div
            v-for="s in skillPopup.items"
            :key="s.skillId"
            class="skill-option"
            :class="{ active: skillPopup.index === skillPopup.items.indexOf(s) }"
            @click="pickSkill(s)"
          >
            <span class="skill-name">/{{ s.skillName }}</span>
            <span class="skill-desc">{{ s.description }}</span>
          </div>
          <div v-if="!skillPopup.items.length" class="skill-empty">无匹配技能（仅显示已启用）</div>
        </div>
        <div class="enabled-skills" v-if="enabledSkills.length">
          <el-tag v-for="s in enabledSkills" :key="s.skillId" size="small" effect="plain" class="skill-chip" @click="appendSkill(s)">/{{ s.skillName }}</el-tag>
        </div>
        <div class="input-row">
          <textarea
            ref="inputRef"
            v-model="input"
            class="chat-input"
            :rows="3"
            placeholder="输入问题，回车发送（Shift+回车换行）；开头输入 / 调用技能"
            :disabled="streaming"
            @input="onInputChange"
            @keydown="onKeydown"
          ></textarea>
          <el-button type="primary" class="send-btn" :loading="streaming" @click="send">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" name="AiChat">
import { nextTick } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import { getToken } from '@/utils/auth'
import { listChatAgents, listChatSessions, listSessionMessages, delChatSession, type ChatSessionVo } from '@/api/ai/chat'
import type { ChatAgentVo } from '@/api/ai/agent'
import { listEnabledSkill, type AiSkill } from '@/api/ai/skill'
import { ElMessage, ElMessageBox } from 'element-plus'

marked.setOptions({
  breaks: true,
  highlight(code: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  }
} as any)

interface ToolRow {
  tool: string
  args: string
  result: string
  status: 'running' | 'ok' | 'error'
  durationMs: number
  open: boolean
}

interface ChatMsg {
  role: 'user' | 'assistant'
  content: string
  reasoning: string
  status: string
  elapsedMs: number
  modelDesc: string
  error?: string
  tools: ToolRow[]
  detailOpen: boolean
  thinkingOpen: boolean
}

const { proxy } = getCurrentInstance() as any

const agents = ref<ChatAgentVo[]>([])
const agentId = ref<number>()
const sessions = ref<ChatSessionVo[]>([])
const currentSessionId = ref<number | null>(null)
const messages = ref<ChatMsg[]>([])
const input = ref('')
const streaming = ref(false)
const enabledSkills = ref<AiSkill[]>([])
const messageBox = ref<HTMLElement>()
const inputRef = ref<HTMLTextAreaElement>()
const skillPopup = reactive({ show: false, items: [] as AiSkill[], index: 0, keyword: '' })
let timerHandle: ReturnType<typeof setInterval> | null = null

/** ---------- 初始化 ---------- */
function loadAgents() {
  listChatAgents().then((res) => {
    agents.value = res.data || []
    if (agents.value.length && !agentId.value) {
      agentId.value = agents.value[0].agentId
    } else if (!agents.value.length) {
      ElMessage.warning('暂无可用智能体，请先在「AI 助手 → 智能体配置」创建')
    }
  })
}

function loadSessions() {
  listChatSessions().then((res) => {
    sessions.value = res.data || []
  })
}

function loadSkills() {
  listEnabledSkill().then((res) => {
    enabledSkills.value = res.data || []
  })
}

/** ---------- 会话 ---------- */
function newSession() {
  currentSessionId.value = null
  messages.value = []
}

function openSession(sessionId: number) {
  currentSessionId.value = sessionId
  messages.value = []
  listSessionMessages(sessionId).then((res) => {
    const list = res.data?.messages || []
    messages.value = list.map((m) => ({
      role: m.role,
      content: m.content || '',
      reasoning: m.reasoning || '',
      status: m.status,
      elapsedMs: m.elapsedMs || 0,
      modelDesc: m.modelDesc || '',
      error: m.status === 'error' && !m.content ? '生成失败' : '',
      tools: (m.traces || []).map((t) => ({
        tool: t.toolName,
        args: t.args || '',
        result: t.result || '',
        status: t.status,
        durationMs: t.durationMs || 0,
        open: false
      })),
      detailOpen: false,
      thinkingOpen: false
    }))
    scrollToBottom()
  })
}

function removeSession(s: ChatSessionVo) {
  ElMessageBox.confirm('确认删除会话「' + s.sessionName + '」？', '提示', { type: 'warning' })
    .then(() => delChatSession(s.sessionId))
    .then(() => {
      if (currentSessionId.value === s.sessionId) {
        newSession()
      }
      loadSessions()
    })
    .catch(() => {})
}

/** ---------- 发送与 SSE ---------- */
function send() {
  const content = input.value.trim()
  if (!content || streaming.value) return
  if (!agentId.value) {
    ElMessage.warning('请先选择智能体')
    return
  }
  input.value = ''
  skillPopup.show = false
  messages.value.push({ role: 'user', content, reasoning: '', status: 'done', elapsedMs: 0, modelDesc: '', tools: [], detailOpen: true, thinkingOpen: true })
  // reactive 包装：push 后继续修改需保持响应式代理引用
  const ai = reactive<ChatMsg>({
    role: 'assistant',
    content: '',
    reasoning: '',
    status: 'running',
    elapsedMs: 0,
    modelDesc: '',
    tools: [],
    detailOpen: true,
    thinkingOpen: true
  })
  messages.value.push(ai)
  streaming.value = true
  const startTs = Date.now()
  ai.elapsedMs = 0
  timerHandle = setInterval(() => {
    ai.elapsedMs = Date.now() - startTs
  }, 500)
  scrollToBottom()

  const controller = new AbortController()
  fetch(import.meta.env.VITE_APP_BASE_API + '/ai/chat/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: 'Bearer ' + getToken()
    },
    body: JSON.stringify({ sessionId: currentSessionId.value, agentId: agentId.value, content }),
    signal: controller.signal
  })
    .then((resp) => {
      if (!resp.ok || !resp.body) {
        return resp.text().then((t) => Promise.reject(t || ('HTTP ' + resp.status)))
      }
      const reader = resp.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      const pump = (): Promise<void> =>
        reader.read().then(({ done, value }) => {
          if (done) return Promise.resolve()
          buffer += decoder.decode(value, { stream: true })
          let sep: number
          while ((sep = buffer.indexOf('\n\n')) >= 0) {
            const block = buffer.slice(0, sep)
            buffer = buffer.slice(sep + 2)
            handleFrame(block, ai)
          }
          return pump()
        })
      return pump()
    })
    .catch((e) => {
      if (String(e).indexOf('aborted') < 0 && String(e).indexOf('AbortError') < 0) {
        ai.error = typeof e === 'string' ? e : e?.message || '网络错误'
        ai.status = 'error'
      }
    })
    .finally(() => {
      streaming.value = false
      if (timerHandle) {
        clearInterval(timerHandle)
        timerHandle = null
      }
      ai.tools.forEach((t) => {
        if (t.status === 'running') t.status = 'error'
      })
      if (ai.status === 'running') ai.status = 'done'
      if (ai.detailOpen) {
        ai.detailOpen = !!ai.reasoning || ai.tools.length > 0
      }
      loadSessions()
      scrollToBottom()
    })
}

/** 解析一帧 SSE：event: xxx \n data: {...} */
function handleFrame(block: string, ai: ChatMsg) {
  let eventName = 'message'
  let data = ''
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) eventName = line.slice(6).trim()
    else if (line.startsWith('data:')) data += line.slice(5).trim()
    else if (line.startsWith(':')) continue
  }
  if (!data) return
  let payload: any = {}
  try {
    payload = JSON.parse(data)
  } catch {
    return
  }
  switch (eventName) {
    case 'start':
      if (payload.sessionId) currentSessionId.value = Number(payload.sessionId)
      if (payload.messageId) {
        // 保留
      }
      break
    case 'thinking':
      ai.reasoning += payload.text || ''
      ai.thinkingOpen = true
      break
    case 'delta':
      ai.content += payload.text || ''
      break
    case 'tool_start':
      ai.tools.push({ tool: payload.tool, args: payload.args || '', result: '', status: 'running', durationMs: 0, open: false })
      break
    case 'tool_end': {
      for (let i = ai.tools.length - 1; i >= 0; i--) {
        if (ai.tools[i].tool === payload.tool && ai.tools[i].status === 'running') {
          ai.tools[i].status = payload.status === 'ok' ? 'ok' : 'error'
          ai.tools[i].result = payload.result || ''
          ai.tools[i].durationMs = payload.durationMs || 0
          break
        }
      }
      break
    }
    case 'warning':
      ElMessage.warning(payload.message || '')
      break
    case 'error':
      ai.error = payload.message || '生成失败'
      ai.status = 'error'
      break
    case 'done':
      if (payload.elapsedMs != null) ai.elapsedMs = payload.elapsedMs
      break
    default:
      break
  }
  scrollToBottom()
}

/** ---------- / 技能弹层 ---------- */
function onInputChange() {
  const v = input.value
  const m = /(?:^|\s)\/([\w\u4e00-\u9fa5-]*)$/.exec(v)
  if (m) {
    skillPopup.keyword = m[1]
    skillPopup.items = enabledSkills.value.filter((s) => (s.skillName || '').includes(skillPopup.keyword))
    skillPopup.index = 0
    skillPopup.show = true
  } else {
    skillPopup.show = false
  }
}

function onKeydown(e: KeyboardEvent) {
  if (skillPopup.show) {
    if (e.key === 'ArrowDown') {
      skillPopup.index = Math.min(skillPopup.index + 1, skillPopup.items.length - 1)
      e.preventDefault()
      return
    }
    if (e.key === 'ArrowUp') {
      skillPopup.index = Math.max(skillPopup.index - 1, 0)
      e.preventDefault()
      return
    }
    if (e.key === 'Enter' || e.key === 'Tab') {
      if (skillPopup.items[skillPopup.index]) {
        pickSkill(skillPopup.items[skillPopup.index])
        e.preventDefault()
        return
      }
    }
    if (e.key === 'Escape') {
      skillPopup.show = false
      return
    }
  }
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

function pickSkill(s: AiSkill) {
  const v = input.value
  input.value = v.replace(/\/([\w\u4e00-\u9fa5-]*)$/, '/' + s.skillName + ' ')
  skillPopup.show = false
  inputRef.value?.focus()
}

function appendSkill(s: AiSkill) {
  input.value = (input.value ? input.value + ' ' : '') + '/' + s.skillName + ' '
  inputRef.value?.focus()
}

/** ---------- 渲染辅助 ---------- */
function renderMarkdown(md: string): string {
  try {
    return marked.parse(md) as string
  } catch {
    return md
  }
}

function pretty(s: string): string {
  try {
    return JSON.stringify(JSON.parse(s), null, 2)
  } catch {
    return s || ''
  }
}

function argsSummary(args: string): string {
  const p = pretty(args).replace(/\n/g, ' ').replace(/\s+/g, ' ')
  return p.length > 60 ? p.slice(0, 60) + '…' : p
}

function formatElapsed(ms: number): string {
  if (!ms || ms < 0) ms = 0
  const s = Math.floor(ms / 1000)
  if (s < 60) return s + ' 秒'
  const m = Math.floor(s / 60)
  const rs = s % 60
  if (m < 60) return rs ? m + ' 分 ' + rs + ' 秒' : m + ' 分'
  return Math.floor(m / 60) + ' 时 ' + (m % 60) + ' 分'
}

function scrollToBottom() {
  nextTick(() => {
    if (messageBox.value) {
      messageBox.value.scrollTop = messageBox.value.scrollHeight
    }
  })
}

onMounted(() => {
  loadAgents()
  loadSessions()
  loadSkills()
})

onBeforeUnmount(() => {
  if (timerHandle) clearInterval(timerHandle)
})
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  height: calc(100vh - 84px);
  padding: 0;
  background: var(--el-bg-color);
}

/* ---------- 左侧 ---------- */
.sidebar {
  width: 240px;
  flex-shrink: 0;
  border-right: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;
  padding: 12px 10px;
  gap: 10px;

  .agent-select {
    width: 100%;
  }
  .new-chat {
    width: 100%;
  }
  .session-list {
    flex: 1;
    overflow-y: auto;
  }
  .session-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 10px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    margin-bottom: 4px;
    .session-name {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .session-del {
      visibility: hidden;
      color: var(--el-text-color-secondary);
      flex-shrink: 0;
    }
    &:hover {
      background: var(--el-fill-color-light);
      .session-del {
        visibility: visible;
      }
    }
    &.active {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
  }
  .agent-model {
    float: right;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

/* ---------- 主区 ---------- */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-box {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;

  .empty-hint {
    text-align: center;
    margin-top: 120px;
    .empty-title {
      font-size: 22px;
      font-weight: 600;
      letter-spacing: 2px;
    }
    .empty-sub {
      margin-top: 10px;
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }
  }
}

.user-msg {
  margin: 18px 0;
  padding: 10px 14px;
  background: var(--el-fill-color);
  border-radius: 8px;
  display: inline-block;
  max-width: 80%;
  white-space: pre-wrap;
  word-break: break-word;
}

/* ---------- AI 消息（ZCode 风格） ---------- */
.ai-msg {
  margin: 18px 0 26px;

  .status-bar {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    user-select: none;
    &:hover {
      color: var(--el-color-primary);
    }
    .arrow {
      transition: transform 0.15s;
      &.collapsed {
        transform: rotate(0deg);
      }
      &:not(.collapsed) {
        transform: rotate(90deg);
      }
      &.small {
        font-size: 12px;
      }
    }
    .status-text {
      font-variant-numeric: tabular-nums;
    }
    .model-desc {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }

  .detail {
    margin-top: 6px;
    padding-left: 4px;
  }

  /* 思考区 */
  .thinking {
    margin-bottom: 10px;
    .thinking-header {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      font-size: 13px;
      color: var(--el-color-warning);
      user-select: none;
      .arrow {
        transition: transform 0.15s;
        &.collapsed {
          transform: rotate(0deg);
        }
        &:not(.collapsed) {
          transform: rotate(90deg);
        }
      }
    }
    .thinking-body {
      margin: 6px 0 6px 16px;
      padding: 8px 12px;
      border-left: 2px solid var(--el-color-warning-light-5);
      color: var(--el-text-color-secondary);
      font-size: 13px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
      max-height: 280px;
      overflow-y: auto;
    }
  }

  /* 工具行 */
  .tool-item {
    margin: 6px 0;
    font-size: 13px;

    .tool-line {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 8px;
      border-radius: 6px;
      cursor: pointer;
      &:hover {
        background: var(--el-fill-color-light);
      }
      .tool-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex-shrink: 0;
        &.running {
          background: var(--el-color-warning);
          animation: blink 1s infinite;
        }
        &.ok {
          background: var(--el-color-success);
        }
        &.error {
          background: var(--el-color-danger);
        }
      }
      .tool-name {
        font-family: Consolas, Monaco, monospace;
        font-weight: 600;
        flex-shrink: 0;
      }
      .tool-args {
        color: var(--el-text-color-secondary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-family: Consolas, Monaco, monospace;
        font-size: 12px;
      }
      .tool-duration {
        margin-left: auto;
        color: var(--el-text-color-placeholder);
        font-size: 12px;
        flex-shrink: 0;
        font-variant-numeric: tabular-nums;
      }
      .arrow {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
        flex-shrink: 0;
        transition: transform 0.15s;
        &.collapsed {
          transform: rotate(0deg);
        }
        &:not(.collapsed) {
          transform: rotate(90deg);
        }
      }
    }

    .tool-detail {
      margin: 4px 0 4px 24px;
      .tool-detail-block {
        margin-bottom: 6px;
        .label {
          font-size: 12px;
          color: var(--el-text-color-placeholder);
          margin-bottom: 2px;
        }
        pre {
          margin: 0;
          padding: 8px 10px;
          background: var(--el-fill-color-darker);
          border-radius: 6px;
          font-size: 12px;
          font-family: Consolas, Monaco, monospace;
          max-height: 220px;
          overflow: auto;
          white-space: pre-wrap;
          word-break: break-word;
        }
      }
    }
  }

  .error-line {
    margin: 8px 0;
    color: var(--el-color-danger);
    font-size: 13px;
  }

  .cursor-line {
    color: var(--el-color-primary);
    animation: blink 1s infinite;
  }
}

/* ---------- 输入区 ---------- */
.input-area {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 10px 16px 14px;
  position: relative;

  .enabled-skills {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 8px;
    .skill-chip {
      cursor: pointer;
    }
  }

  .input-row {
    display: flex;
    gap: 10px;
    align-items: flex-end;
  }

  .chat-input {
    flex: 1;
    resize: none;
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    padding: 10px 12px;
    font-size: 14px;
    font-family: inherit;
    line-height: 1.6;
    outline: none;
    background: var(--el-bg-color);
    color: var(--el-text-color-primary);
    &:focus {
      border-color: var(--el-color-primary);
    }
    &:disabled {
      background: var(--el-fill-color-light);
    }
  }

  .send-btn {
    height: 42px;
    width: 84px;
  }
}

/* / 技能弹层 */
.skill-popup {
  position: absolute;
  bottom: 100%;
  left: 16px;
  width: 420px;
  max-height: 260px;
  overflow-y: auto;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
  padding: 6px;
  margin-bottom: 6px;
  z-index: 10;

  .skill-option {
    padding: 8px 10px;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: baseline;
    gap: 8px;
    &.active,
    &:hover {
      background: var(--el-fill-color-light);
    }
    .skill-name {
      font-family: Consolas, Monaco, monospace;
      font-weight: 600;
      flex-shrink: 0;
    }
    .skill-desc {
      color: var(--el-text-color-secondary);
      font-size: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  .skill-empty {
    padding: 10px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    text-align: center;
  }
}

/* markdown 正文 */
.markdown-body {
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;

  :deep(pre) {
    background: #0d1117;
    color: #e6edf3;
    padding: 12px 14px;
    border-radius: 8px;
    overflow: auto;
    font-size: 13px;
  }
  :deep(code) {
    font-family: Consolas, Monaco, monospace;
  }
  :deep(:not(pre) > code) {
    background: var(--el-fill-color);
    padding: 1px 5px;
    border-radius: 4px;
    font-size: 13px;
  }
  :deep(table) {
    border-collapse: collapse;
    margin: 8px 0;
    th,
    td {
      border: 1px solid var(--el-border-color-lighter);
      padding: 6px 10px;
      font-size: 13px;
    }
  }
  :deep(blockquote) {
    margin: 8px 0;
    padding: 4px 12px;
    border-left: 3px solid var(--el-border-color);
    color: var(--el-text-color-secondary);
  }
  :deep(p) {
    margin: 8px 0;
  }
  :deep(ul),
  :deep(ol) {
    padding-left: 22px;
    margin: 8px 0;
  }
}

@keyframes blink {
  50% {
    opacity: 0.3;
  }
}
</style>
