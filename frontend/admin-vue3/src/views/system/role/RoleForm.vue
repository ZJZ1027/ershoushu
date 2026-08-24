<template>
  <a-modal
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    :width="600"
    :mask-closable="false"
    :footer="false"
    @cancel="dialogVisible = false"
  >
    <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
      <a-form-item field="name" label="角色名称" required>
        <a-input v-model="formData.name" placeholder="请输入角色名称" allow-clear />
      </a-form-item>
      <a-form-item field="code" label="角色标识" required>
        <a-input v-model="formData.code" placeholder="如 app_user、operator" allow-clear />
      </a-form-item>
      <a-form-item field="sort" label="显示顺序" required>
        <a-input-number v-model="formData.sort" :min="0" placeholder="数字越小越靠前" style="width: 100%" />
      </a-form-item>
      <a-form-item field="status" label="状态" required>
        <a-select v-model="formData.status" placeholder="请选择状态">
          <a-option :value="0">正常</a-option>
          <a-option :value="1">停用</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="remark" label="备注">
        <a-textarea v-model="formData.remark" placeholder="请输入备注" :auto-size="{ minRows: 3, maxRows: 5 }" />
      </a-form-item>
    </a-form>
    <div class="form-footer">
      <a-button @click="dialogVisible = false">取消</a-button>
      <a-button type="primary" :loading="formLoading" @click="submit">保存</a-button>
    </div>
  </a-modal>
</template>
<script lang="ts" setup>
import { CommonStatusEnum } from '@/utils/constants'
import * as RoleApi from '@/api/system/role'

defineOptions({ name: 'SystemRoleForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref(emptyForm())
const formRules = reactive({
  name: [{ required: true, message: '角色名称不能为空' }],
  code: [{ required: true, message: '角色标识不能为空' }],
  sort: [{ required: true, message: '显示顺序不能为空' }],
  status: [{ required: true, message: '状态不能为空' }]
})
const formRef = ref()

function emptyForm() {
  return {
    id: undefined as number | undefined,
    name: '',
    code: '',
    sort: 0,
    status: CommonStatusEnum.ENABLE,
    remark: ''
  }
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增角色' : '编辑角色'
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await RoleApi.getRole(id)
    } finally {
      formLoading.value = false
    }
  }
}

const resetForm = () => {
  formData.value = emptyForm()
  formRef.value?.resetFields()
}
defineExpose({ open })

const emit = defineEmits(['success'])

const submit = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return
  formLoading.value = true
  try {
    const data = formData.value as unknown as RoleApi.RoleVO
    if (formType.value === 'create') {
      await RoleApi.createRole(data)
      message.success('保存成功')
    } else {
      await RoleApi.updateRole(data)
      message.success('保存成功')
    }
    dialogVisible.value = false
    emit('success')
  } catch {
    /* 错误已由 axios 拦截器提示 */
  } finally {
    formLoading.value = false
  }
}
</script>
<style scoped>
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-2, #e5e6eb);
}
</style>
