<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="名称">
          <el-input v-model="queryParams.agentName" placeholder="请输入名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-hasPermi="['ai:agent:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="agentList">
        <el-table-column label="名称" prop="agentName" min-width="120" show-overflow-tooltip />
        <el-table-column label="描述" prop="description" min-width="160" show-overflow-tooltip />
        <el-table-column label="模型" min-width="170" show-overflow-tooltip>
          <template #default="scope">{{ modelLabel(scope.row.modelId) }}</template>
        </el-table-column>
        <el-table-column label="思考" width="80" align="center">
          <template #default="scope">
            <el-tag :type="['info', 'primary', 'warning', 'danger'][scope.row.thinkingLevel || 0]" effect="plain" size="small">
              {{ ['关闭', '低', '中', '高'][scope.row.thinkingLevel || 0] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="工具" prop="enabledTools" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="70" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button v-hasPermi="['ai:agent:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button v-hasPermi="['ai:agent:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog" :title="title" width="640px" append-to-body>
      <el-form ref="agentFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="agentName">
          <el-input v-model="form.agentName" placeholder="如 通用助手" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="一句话说明用途" />
        </el-form-item>
        <el-form-item label="绑定模型" prop="modelId">
          <el-select v-model="form.modelId" placeholder="选择模型" style="width: 100%">
            <el-option v-for="g in modelGroups" :key="g.providerId" :label="g.providerName" :value="g.providerId" disabled>
              <span style="font-weight: 600">{{ g.providerName }}</span>
            </el-option>
            <el-option v-for="m in allModels" :key="m.modelId" :label="m.displayName + '（' + m.modelCode + '）'" :value="m.modelId!">
              <span style="padding-left: 12px">{{ m.displayName || m.modelCode }} <span style="color: var(--el-text-color-secondary)">{{ m.modelCode }}</span></span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="思考等级">
          <el-radio-group v-model="form.thinkingLevel">
            <el-radio-button :value="0">关闭</el-radio-button>
            <el-radio-button :value="1">低</el-radio-button>
            <el-radio-button :value="2">中</el-radio-button>
            <el-radio-button :value="3">高</el-radio-button>
          </el-radio-group>
          <div class="form-tip">模型须支持思考；Anthropic 映射 thinking 预算，OpenAI 映射 reasoning_effort</div>
        </el-form-item>
        <el-form-item label="温度" prop="temperature">
          <el-input-number v-model="form.temperature" :min="0" :max="1" :step="0.1" />
        </el-form-item>
        <el-form-item label="最大 Tokens" prop="maxTokens">
          <el-input-number v-model="form.maxTokens" :min="256" :max="65536" :step="256" />
        </el-form-item>
        <el-form-item label="启用工具">
          <el-checkbox-group v-model="formTools">
            <el-checkbox value="read">read</el-checkbox>
            <el-checkbox value="write">write</el-checkbox>
            <el-checkbox value="edit">edit</el-checkbox>
            <el-checkbox value="bash">bash</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="默认技能">
          <el-select v-model="formSkills" multiple placeholder="选择会话中可快捷使用的技能（仅已启用）" style="width: 100%">
            <el-option v-for="s in enabledSkills" :key="s.skillId" :label="s.skillName" :value="s.skillName!">
              <span>/{{ s.skillName }}</span>
              <span style="float: right; color: var(--el-text-color-secondary)">{{ s.description }}</span>
            </el-option>
          </el-select>
          <div class="form-tip">技能在聊天输入框以 / 调用，可多个；此处仅作预选参考</div>
        </el-form-item>
        <el-form-item label="系统提示词" prop="systemPrompt">
          <el-input v-model="form.systemPrompt" type="textarea" :rows="6" placeholder="定义智能体的角色与行为准则" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialog = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="AiAgent">
import { listAgent, getAgent, addAgent, updateAgent, delAgent, type AiAgent } from '@/api/ai/agent'
import { listModel, type AiModel } from '@/api/ai/model'
import { listProvider, type AiProvider } from '@/api/ai/provider'
import { listEnabledSkill, type AiSkill } from '@/api/ai/skill'
import type { FormInstance, FormRules } from 'element-plus'

const { proxy } = getCurrentInstance() as any

const agentList = ref<AiAgent[]>([])
const loading = ref(false)
const total = ref(0)
const dialog = ref(false)
const title = ref('')
const agentFormRef = ref<FormInstance>()
const form = ref<AiAgent>({})
const formTools = ref<string[]>(['read', 'write', 'edit', 'bash'])
const formSkills = ref<string[]>([])
const allModels = ref<AiModel[]>([])
const providers = ref<AiProvider[]>([])
const enabledSkills = ref<AiSkill[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 10, agentName: '', status: '' })

const rules = ref<FormRules>({
  agentName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  modelId: [{ required: true, message: '请选择模型', trigger: 'change' }]
})

const modelGroups = computed(() => {
  const ids = new Set(allModels.value.map((m) => m.providerId))
  return providers.value.filter((p) => ids.has(p.providerId))
})

function modelLabel(modelId?: number) {
  const m = allModels.value.find((x) => x.modelId === modelId)
  return m ? (m.displayName || m.modelCode || '') : String(modelId || '')
}

function getList() {
  loading.value = true
  listAgent(queryParams.value).then((res) => {
    agentList.value = res.rows
    total.value = res.total
  }).finally(() => (loading.value = false))
}

function loadRefs() {
  listModel({ pageNum: 1, pageSize: 200 }).then((res) => (allModels.value = res.rows))
  listProvider({ pageNum: 1, pageSize: 50 }).then((res) => (providers.value = res.rows))
  listEnabledSkill().then((res) => (enabledSkills.value = res.data || []))
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 10, agentName: '', status: '' }
  getList()
}

function reset() {
  form.value = { thinkingLevel: 0, temperature: 0.7, maxTokens: 4096, status: '0' }
  formTools.value = ['read', 'write', 'edit', 'bash']
  formSkills.value = []
  agentFormRef.value?.resetFields()
}

function handleAdd() {
  reset()
  title.value = '新增智能体'
  dialog.value = true
}

function handleUpdate(row: AiAgent) {
  getAgent(row.agentId!).then((res) => {
    form.value = res.data
    formTools.value = (res.data.enabledTools || '').split(',').map((s) => s.trim()).filter(Boolean)
    title.value = '修改智能体'
    dialog.value = true
  })
}

function submitForm() {
  agentFormRef.value?.validate((valid) => {
    if (!valid) return
    form.value.enabledTools = formTools.value.join(',')
    if (form.value.agentId) {
      updateAgent(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        dialog.value = false
        getList()
      })
    } else {
      addAgent(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        dialog.value = false
        getList()
      })
    }
  })
}

function handleDelete(row: AiAgent) {
  proxy.$modal.confirm('确认删除智能体「' + row.agentName + '」？').then(() => {
    return delAgent(row.agentId!)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
loadRefs()
</script>

<style scoped>
.form-tip {
  width: 100%;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
.mb8 {
  margin-bottom: 8px;
}
</style>
