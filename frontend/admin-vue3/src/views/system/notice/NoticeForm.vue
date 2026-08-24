<template>
  <a-modal
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    :width="720"
    :mask-closable="false"
    :ok-text="formType === 'create' ? '确定创建' : '保存'"
    cancel-text="取消"
    :ok-loading="formLoading"
    :on-before-ok="handleBeforeOk"
    @cancel="dialogVisible = false"
  >
    <a-spin :loading="contentLoading" tip="加载中…" style="display: block">
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item field="title" label="公告标题" required>
          <a-input v-model="formData.title" placeholder="请输入公告标题" allow-clear />
        </a-form-item>
        <!-- 上游用富文本编辑器承载公告正文，本项目没有 Editor 组件，改为多行文本域：
             按内容自动伸高（6~14 行），超出后自身滚动，不把弹窗顶长。 -->
        <a-form-item field="content" label="公告内容" required>
          <a-textarea
            v-model="formData.content"
            placeholder="请输入公告内容"
            :auto-size="{ minRows: 6, maxRows: 14 }"
          />
          <template #extra>
            <span class="form-tip">支持纯文本，如需富文本请自行接入编辑器</span>
          </template>
        </a-form-item>
        <a-form-item field="type" label="公告类型" required>
          <a-select v-model="formData.type" placeholder="请选择公告类型" allow-clear>
            <a-option
              v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_NOTICE_TYPE)"
              :key="dict.value"
              :value="dict.value"
              :label="dict.label"
            />
          </a-select>
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
import * as NoticeApi from '@/api/system/notice'

defineOptions({ name: 'SystemNoticeForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const contentLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined,
  title: '',
  type: undefined as number | undefined,
  content: '',
  status: CommonStatusEnum.ENABLE,
  remark: ''
})
const formRules = {
  title: [{ required: true, message: '公告标题不能为空' }],
  content: [{ required: true, message: '公告内容不能为空' }],
  type: [{ required: true, message: '公告类型不能为空' }],
  status: [{ required: true, message: '状态不能为空' }]
}
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增公告' : '编辑公告'
  formType.value = type
  resetForm()
  if (id) {
    contentLoading.value = true
    try {
      formData.value = await NoticeApi.getNotice(id)
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
    const data = formData.value as unknown as NoticeApi.NoticeVO
    if (formType.value === 'create') {
      await NoticeApi.createNotice(data)
      message.success(t('common.createSuccess'))
    } else {
      await NoticeApi.updateNotice(data)
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
    title: '',
    type: undefined,
    content: '',
    status: CommonStatusEnum.ENABLE,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>

<style lang="scss" scoped>
.form-tip {
  font-size: 12px;
  color: var(--color-text-3, #86909c);
}
</style>
