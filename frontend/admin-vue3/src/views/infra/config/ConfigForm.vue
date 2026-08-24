<template>
  <a-modal
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    :width="600"
    :mask-closable="false"
    :ok-text="formType === 'create' ? '确定创建' : '保存'"
    cancel-text="取消"
    :ok-loading="formLoading"
    :on-before-ok="handleBeforeOk"
    @cancel="dialogVisible = false"
  >
    <a-spin :loading="contentLoading" tip="加载中…" style="display: block">
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item field="category" label="参数分类" required>
          <a-input v-model="formData.category" placeholder="请输入参数分类" allow-clear />
        </a-form-item>
        <a-form-item field="name" label="参数名称" required>
          <a-input v-model="formData.name" placeholder="请输入参数名称" allow-clear />
        </a-form-item>
        <a-form-item field="configKey" label="参数键名" required>
          <a-input v-model="formData.configKey" placeholder="请输入参数键名" allow-clear />
        </a-form-item>
        <a-form-item field="configValue" label="参数键值" required>
          <a-input v-model="formData.configValue" placeholder="请输入参数键值" allow-clear />
        </a-form-item>
        <a-form-item field="visible" label="是否可见" required>
          <a-radio-group v-model="formData.visible">
            <a-radio
              v-for="dict in getBoolDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING)"
              :key="String(dict.value)"
              :value="dict.value"
            >
              {{ dict.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item field="remark" label="备注">
          <a-textarea
            v-model="formData.remark"
            placeholder="请输入内容"
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getBoolDictOptions } from '@/utils/dict'
import * as ConfigApi from '@/api/infra/config'

defineOptions({ name: 'InfraConfigForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const contentLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  category: '',
  name: '',
  configKey: '',
  configValue: '',
  visible: true,
  remark: ''
})
const formRules = {
  category: [{ required: true, message: '参数分类不能为空' }],
  name: [{ required: true, message: '参数名称不能为空' }],
  configKey: [{ required: true, message: '参数键名不能为空' }],
  configValue: [{ required: true, message: '参数键值不能为空' }],
  visible: [{ required: true, message: '是否可见不能为空' }]
}
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增参数' : '编辑参数'
  formType.value = type
  resetForm()
  if (id) {
    contentLoading.value = true
    try {
      formData.value = await ConfigApi.getConfig(id)
    } finally {
      contentLoading.value = false
    }
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const handleBeforeOk = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return false
  formLoading.value = true
  try {
    const data = formData.value as unknown as ConfigApi.ConfigVO
    if (formType.value === 'create') {
      await ConfigApi.createConfig(data)
      message.success(t('common.createSuccess'))
    } else {
      await ConfigApi.updateConfig(data)
      message.success(t('common.updateSuccess'))
    }
    emit('success')
    return true
  } catch {
    return false
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    category: '',
    name: '',
    configKey: '',
    configValue: '',
    visible: true,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
