<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="技能名">
          <el-input v-model="queryParams.skillName" placeholder="请输入技能名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.enabled" placeholder="状态" clearable style="width: 120px">
            <el-option label="已启用" value="1" />
            <el-option label="已停用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-hasPermi="['ai:skill:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-hasPermi="['ai:skill:import']" type="info" plain icon="Upload" @click="importRef?.click()">zip 导入</el-button>
          <input ref="importRef" type="file" accept=".zip" style="display: none" @change="handleImport" />
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="skillList">
        <el-table-column label="技能名" width="160">
          <template #default="scope">/{{ scope.row.skillName }}</template>
        </el-table-column>
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="内容" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.content">{{ scope.row.content.slice(0, 80) }}</span>
            <span v-else class="pkg-tip">技能包 · 附属资源见详情</span>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="90" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" active-value="1" inactive-value="0" @change="handleEnable(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button v-hasPermi="['ai:skill:edit']" link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
            <el-button v-hasPermi="['ai:skill:remove']" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog" :title="title" width="720px" append-to-body>
      <el-form ref="skillFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="技能名" prop="skillName">
          <el-input v-model="form.skillName" placeholder="聊天中以 /技能名 调用（字母数字中文连字符）">
            <template #prepend>/</template>
          </el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="一句话说明何时使用该技能" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="14" placeholder="markdown 格式的技能指令内容" class="skill-content" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" active-value="1" inactive-value="0" />
          <span class="form-tip">启用后才能被智能体勾选与 / 调用</span>
        </el-form-item>
        <el-form-item v-if="form.resources && form.resources.length" label="附属资源">
          <div class="resource-box">
            <div class="resource-head">技能包文件（沙箱 skills/{{ form.skillName }}/，聊天中模型可用 read / bash 等工具直接访问执行）</div>
            <el-table :data="form.resources" size="small" max-height="260">
              <el-table-column label="文件" prop="relPath" min-width="260" show-overflow-tooltip />
              <el-table-column label="大小" width="110" align="right">
                <template #default="scope">{{ formatSize(scope.row.fileSize) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialog = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="AiSkill">
import { listSkill, getSkill, addSkill, updateSkill, delSkill, importSkill, type AiSkill, type SkillImportRow } from '@/api/ai/skill'
import type { FormInstance, FormRules } from 'element-plus'

const { proxy } = getCurrentInstance() as any

const skillList = ref<AiSkill[]>([])
const loading = ref(false)
const total = ref(0)
const dialog = ref(false)
const title = ref('')
const skillFormRef = ref<FormInstance>()
const importRef = ref<HTMLInputElement>()
const form = ref<AiSkill>({})
const queryParams = ref({ pageNum: 1, pageSize: 10, skillName: '', enabled: '' })

const rules = ref<FormRules>({
  skillName: [{ required: true, message: '技能名不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }]
})

function getList() {
  loading.value = true
  listSkill(queryParams.value).then((res) => {
    skillList.value = res.rows
    total.value = res.total
  }).finally(() => (loading.value = false))
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 10, skillName: '', enabled: '' }
  getList()
}

function reset() {
  form.value = { enabled: '0' }
  skillFormRef.value?.resetFields()
}

function handleAdd() {
  reset()
  title.value = '新增技能'
  dialog.value = true
}

function handleView(row: AiSkill) {
  getSkill(row.skillId!).then((res) => {
    form.value = res.data
    title.value = '编辑技能 /' + res.data.skillName
    dialog.value = true
  })
}

function submitForm() {
  skillFormRef.value?.validate((valid) => {
    if (!valid) return
    if (form.value.skillId) {
      updateSkill(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        dialog.value = false
        getList()
      })
    } else {
      addSkill(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        dialog.value = false
        getList()
      })
    }
  })
}

function handleEnable(row: AiSkill) {
  updateSkill(row).then(() => {
    proxy.$modal.msgSuccess(row.enabled === '1' ? '已启用 /' + row.skillName : '已停用 /' + row.skillName)
  })
}

function handleDelete(row: AiSkill) {
  proxy.$modal.confirm('确认删除技能 /' + row.skillName + '？').then(() => {
    return delSkill(row.skillId!)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleImport(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  importSkill(file).then((res) => {
    const rows: SkillImportRow[] = res.data || []
    proxy.$modal.msgSuccess('导入完成：' + rows.map((r) => r.action + ' /' + r.name + (r.resourceCount ? `（${r.resourceCount} 个附属资源）` : '')).join('、'))
    getList()
  }).finally(() => {
    input.value = ''
  })
}

function formatSize(size?: number): string {
  if (size == null) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

getList()
</script>

<style scoped>
.pkg-tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.resource-box {
  width: 100%;
}
.resource-head {
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.mb8 {
  margin-bottom: 8px;
}
:deep(.skill-content textarea) {
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
}
</style>
