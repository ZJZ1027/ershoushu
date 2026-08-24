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
        <a-form-item field="name" label="岗位名称" required>
          <a-input v-model="formData.name" placeholder="请输入岗位名称" allow-clear />
        </a-form-item>
        <a-form-item field="code" label="岗位编码" required>
          <a-input v-model="formData.code" placeholder="请输入岗位编码" allow-clear />
        </a-form-item>
        <a-form-item field="sort" label="岗位顺序" required>
          <a-input-number
            v-model="formData.sort"
            :min="0"
            :precision="0"
            placeholder="请输入岗位顺序"
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item field="status" label="状态" required>
          <a-radio-group v-model="formData.status">
            <a-radio
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :value="dict.value"
            >
              {{ dict.label }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item field="remark" label="备注">
          <a-textarea
            v-model="formData.remark"
            placeholder="请输入备注"
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import * as PostApi from '@/api/system/post'

defineOptions({ name: 'SystemPostForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const contentLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  name: '',
  code: '',
  sort: 0,
  status: CommonStatusEnum.ENABLE,
  remark: ''
})
const formRules = {
  name: [{ required: true, message: '岗位名称不能为空' }],
  code: [{ required: true, message: '岗位编码不能为空' }],
  sort: [{ required: true, message: '岗位顺序不能为空' }],
  status: [{ required: true, message: '岗位状态不能为空' }]
}
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增岗位' : '编辑岗位'
  formType.value = type
  resetForm()
  if (id) {
    contentLoading.value = true
    try {
      formData.value = await PostApi.getPost(id)
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
    const data = formData.value as unknown as PostApi.PostVO
    if (formType.value === 'create') {
      await PostApi.createPost(data)
      message.success(t('common.createSuccess'))
    } else {
      await PostApi.updatePost(data)
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
    name: '',
    code: '',
    sort: 0,
    status: CommonStatusEnum.ENABLE,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
