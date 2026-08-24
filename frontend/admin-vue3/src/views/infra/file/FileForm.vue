<template>
  <a-modal
    v-model:visible="dialogVisible"
    title="上传文件"
    :width="520"
    :mask-closable="false"
    ok-text="确定上传"
    cancel-text="取消"
    :ok-loading="formLoading"
    :on-before-ok="handleBeforeOk"
    @cancel="onCancel"
  >
    <a-upload
      v-model:file-list="fileList"
      :auto-upload="false"
      :limit="1"
      draggable
      :show-remove-button="true"
    />
    <div class="upload-tip">提示：单次仅上传一个文件；上传成功后可在列表中复制其访问链接。</div>
  </a-modal>
</template>

<script lang="ts" setup>
import * as FileApi from '@/api/infra/file'

defineOptions({ name: 'InfraFileForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const fileList = ref<any[]>([])

/** 打开弹窗 */
const open = async () => {
  dialogVisible.value = true
  fileList.value = []
}
defineExpose({ open })

/** 提交上传 */
const emit = defineEmits(['success'])
const handleBeforeOk = async () => {
  const item = fileList.value[0]
  const raw = item?.file as File | undefined
  if (!raw) {
    message.error('请选择要上传的文件')
    return false
  }
  formLoading.value = true
  try {
    await FileApi.uploadFile({ file: raw })
    message.success(t('common.createSuccess'))
    emit('success')
    fileList.value = []
    return true
  } catch {
    message.error('上传失败，请重新上传')
    return false
  } finally {
    formLoading.value = false
  }
}

const onCancel = () => {
  fileList.value = []
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--bm-text-3);
}
</style>
