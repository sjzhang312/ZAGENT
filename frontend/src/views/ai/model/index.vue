<template>
  <div class="app-container">
    <!-- 服务商层 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="box-card">
          <template #header>
            <div class="card-header">
              <span>服务商（API 配置）</span>
              <el-button v-hasPermi="['ai:provider:add']" type="primary" plain icon="Plus" size="small" @click="handleProviderAdd()">新增</el-button>
            </div>
          </template>
          <el-table v-loading="providerLoading" :data="providerList" highlight-current-row @current-change="handleProviderSelect">
            <el-table-column label="名称" prop="providerName" min-width="120" show-overflow-tooltip />
            <el-table-column label="厂商" prop="providerCode" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.providerCode === 'anthropic' ? 'warning' : 'success'" effect="plain">{{ scope.row.providerCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="API 地址" prop="baseUrl" min-width="180" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="scope">
                <el-button link type="primary" icon="Connection" @click.stop="handleTest(scope.row)">测试</el-button>
                <el-button v-hasPermi="['ai:provider:edit']" link type="primary" icon="Edit" @click.stop="handleProviderUpdate(scope.row)">修改</el-button>
                <el-button v-hasPermi="['ai:provider:remove']" link type="danger" icon="Delete" @click.stop="handleProviderDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <!-- 模型层 -->
      <el-col :span="12">
        <el-card shadow="never" class="box-card">
          <template #header>
            <div class="card-header">
              <span>模型{{ currentProvider ? ' - ' + currentProvider.providerName : '' }}</span>
              <el-button v-hasPermi="['ai:model:add']" type="primary" plain icon="Plus" size="small" :disabled="!currentProvider" @click="handleModelAdd()">新增</el-button>
            </div>
          </template>
          <el-table v-loading="modelLoading" :data="modelList">
            <el-table-column label="模型标识" prop="modelCode" min-width="170" show-overflow-tooltip />
            <el-table-column label="显示名" prop="displayName" min-width="120" show-overflow-tooltip />
            <el-table-column label="思考" prop="supportReasoning" width="70" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.supportReasoning === '1'" type="warning" effect="plain" size="small">支持</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="70" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="scope">
                <el-button v-hasPermi="['ai:model:edit']" link type="primary" icon="Edit" @click="handleModelUpdate(scope.row)">修改</el-button>
                <el-button v-hasPermi="['ai:model:remove']" link type="danger" icon="Delete" @click="handleModelDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!currentProvider" description="选择左侧服务商后管理其模型" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 服务商弹窗 -->
    <el-dialog v-model="providerDialog" :title="providerTitle" width="520px" append-to-body>
      <el-form ref="providerFormRef" :model="providerForm" :rules="providerRules" label-width="90px">
        <el-form-item label="名称" prop="providerName">
          <el-input v-model="providerForm.providerName" placeholder="如 Anthropic 官方" />
        </el-form-item>
        <el-form-item label="厂商" prop="providerCode">
          <el-radio-group v-model="providerForm.providerCode" @change="handleCodeChange">
            <el-radio value="anthropic">Anthropic</el-radio>
            <el-radio value="openai">OpenAI</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="providerForm.apiKey" placeholder="sk-..." show-password />
        </el-form-item>
        <el-form-item label="API 地址" prop="baseUrl">
          <el-input v-model="providerForm.baseUrl" :placeholder="baseUrlPlaceholder" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="providerForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="providerForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitProvider">确 定</el-button>
        <el-button @click="providerDialog = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 模型弹窗 -->
    <el-dialog v-model="modelDialog" :title="modelTitle" width="520px" append-to-body>
      <el-form ref="modelFormRef" :model="modelForm" :rules="modelRules" label-width="90px">
        <el-form-item label="模型标识" prop="modelCode">
          <el-input v-model="modelForm.modelCode" placeholder="如 claude-sonnet-4-5 / gpt-4o" />
        </el-form-item>
        <el-form-item label="显示名" prop="displayName">
          <el-input v-model="modelForm.displayName" placeholder="如 Claude Sonnet 4.5" />
        </el-form-item>
        <el-form-item label="支持思考">
          <el-switch v-model="modelForm.supportReasoning" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="modelForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="modelForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitModel">确 定</el-button>
        <el-button @click="modelDialog = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 测试连接弹窗 -->
    <el-dialog v-model="testDialog" :title="'连通性测试 - ' + (testingProvider?.providerName || '')" width="520px" append-to-body>
      <div v-loading="testing">
        <el-alert v-if="testError" :title="testError" type="error" :closable="false" />
        <template v-else-if="remoteModels.length">
          <el-alert title="连接成功，可勾选以下模型快速添加：" type="success" :closable="false" class="mb8" />
          <el-checkbox-group v-model="selectedRemoteModels">
            <el-checkbox v-for="m in remoteModels" :key="m" :value="m" class="remote-model-item">{{ m }}</el-checkbox>
          </el-checkbox-group>
        </template>
        <el-empty v-else-if="!testing" description="无返回模型" :image-size="60" />
      </div>
      <template #footer>
        <el-button v-if="remoteModels.length" type="primary" @click="batchAddModels">添加所选模型</el-button>
        <el-button @click="testDialog = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="AiModel">
import { listProvider, getProvider, addProvider, updateProvider, delProvider, testProvider, type AiProvider } from '@/api/ai/provider'
import { listModel, getModel, addModel, updateModel, delModel, type AiModel } from '@/api/ai/model'
import type { FormInstance, FormRules } from 'element-plus'

const { proxy } = getCurrentInstance() as any

const providerList = ref<AiProvider[]>([])
const modelList = ref<AiModel[]>([])
const providerLoading = ref(false)
const modelLoading = ref(false)
const currentProvider = ref<AiProvider | null>(null)

const providerDialog = ref(false)
const providerTitle = ref('')
const providerFormRef = ref<FormInstance>()
const providerForm = ref<AiProvider>({})
const providerRules = ref<FormRules>({
  providerName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  providerCode: [{ required: true, message: '请选择厂商', trigger: 'change' }],
  apiKey: [{ required: true, message: 'API Key 不能为空', trigger: 'blur' }]
})

const modelDialog = ref(false)
const modelTitle = ref('')
const modelFormRef = ref<FormInstance>()
const modelForm = ref<AiModel>({})
const modelRules = ref<FormRules>({
  modelCode: [{ required: true, message: '模型标识不能为空', trigger: 'blur' }]
})

const testDialog = ref(false)
const testing = ref(false)
const testError = ref('')
const testingProvider = ref<AiProvider | null>(null)
const remoteModels = ref<string[]>([])
const selectedRemoteModels = ref<string[]>([])

const baseUrlPlaceholder = computed(() =>
  providerForm.value.providerCode === 'anthropic' ? 'https://api.anthropic.com（留空默认）' : 'https://api.openai.com（留空默认，兼容服务填对应地址）'
)

/** 查询服务商列表 */
function getProviders() {
  providerLoading.value = true
  listProvider().then((res) => {
    providerList.value = res.rows
    if (!currentProvider.value && res.rows.length) {
      currentProvider.value = res.rows[0]
      getModels()
    }
  }).finally(() => (providerLoading.value = false))
}

/** 查询当前服务商的模型 */
function getModels() {
  if (!currentProvider.value) {
    modelList.value = []
    return
  }
  modelLoading.value = true
  listModel({ providerId: currentProvider.value.providerId }).then((res) => {
    modelList.value = res.rows
  }).finally(() => (modelLoading.value = false))
}

function handleProviderSelect(row: AiProvider | null) {
  currentProvider.value = row
  getModels()
}

/** ---------- 服务商 CRUD ---------- */
function resetProvider() {
  providerForm.value = { providerCode: 'anthropic', status: '0' }
  providerFormRef.value?.resetFields()
}

function handleProviderAdd() {
  resetProvider()
  providerTitle.value = '新增服务商'
  providerDialog.value = true
}

function handleProviderUpdate(row: AiProvider) {
  getProvider(row.providerId!).then((res) => {
    providerForm.value = res.data
    providerTitle.value = '修改服务商'
    providerDialog.value = true
  })
}

function submitProvider() {
  providerFormRef.value?.validate((valid) => {
    if (!valid) return
    if (providerForm.value.providerId) {
      updateProvider(providerForm.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        providerDialog.value = false
        getProviders()
      })
    } else {
      addProvider(providerForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        providerDialog.value = false
        getProviders()
      })
    }
  })
}

function handleProviderDelete(row: AiProvider) {
  proxy.$modal.confirm('确认删除服务商「' + row.providerName + '」？其下模型将无法使用').then(() => {
    return delProvider(row.providerId!)
  }).then(() => {
    getProviders()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleCodeChange() {
  providerForm.value.baseUrl = ''
}

/** ---------- 测试连接 ---------- */
function handleTest(row: AiProvider) {
  testingProvider.value = row
  testDialog.value = true
  testing.value = true
  testError.value = ''
  remoteModels.value = []
  selectedRemoteModels.value = []
  testProvider(row.providerId!).then((res) => {
    remoteModels.value = res.data || []
  }).catch((e) => {
    testError.value = e || '连接失败'
  }).finally(() => (testing.value = false))
}

function batchAddModels() {
  const providerId = currentProvider.value?.providerId || testingProvider.value?.providerId
  const tasks = selectedRemoteModels.value.map((code) => addModel({ providerId, modelCode: code, displayName: code, supportReasoning: '0', status: '0' }))
  Promise.all(tasks).then(() => {
    proxy.$modal.msgSuccess('已添加 ' + tasks.length + ' 个模型')
    testDialog.value = false
    if (!currentProvider.value && testingProvider.value) {
      currentProvider.value = testingProvider.value
    }
    getModels()
  })
}

/** ---------- 模型 CRUD ---------- */
function resetModel() {
  modelForm.value = { supportReasoning: '0', status: '0' }
  modelFormRef.value?.resetFields()
}

function handleModelAdd() {
  resetModel()
  modelTitle.value = '新增模型'
  modelDialog.value = true
}

function handleModelUpdate(row: AiModel) {
  getModel(row.modelId!).then((res) => {
    modelForm.value = res.data
    modelTitle.value = '修改模型'
    modelDialog.value = true
  })
}

function submitModel() {
  modelFormRef.value?.validate((valid) => {
    if (!valid) return
    if (modelForm.value.modelId) {
      updateModel(modelForm.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        modelDialog.value = false
        getModels()
      })
    } else {
      modelForm.value.providerId = currentProvider.value?.providerId
      addModel(modelForm.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        modelDialog.value = false
        getModels()
      })
    }
  })
}

function handleModelDelete(row: AiModel) {
  proxy.$modal.confirm('确认删除模型「' + row.modelCode + '」？').then(() => {
    return delModel(row.modelId!)
  }).then(() => {
    getModels()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getProviders()
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb8 {
  margin-bottom: 8px;
}
.remote-model-item {
  display: block;
}
</style>
