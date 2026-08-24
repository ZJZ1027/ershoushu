<template>
  <a-modal
    v-model:visible="dialogVisible"
    title="用户导入"
    :width="400"
    :mask-closable="false"
    @cancel="dialogVisible = false"
  >
    <a-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="importUrl + '?updateSupport=' + updateSupport"
      :auto-upload="false"
      :disabled="formLoading"
      :headers="uploadHeaders"
      :limit="1"
      accept=".xlsx,.xls"
      draggable
      @error="submitFormError"
      @exceed-limit="handleExceed"
      @success="submitFormSuccess"
    >
      <template #upload-button>
        <div class="upload-trigger">
          <Icon icon="ep:upload" :size="40" />
          <div class="upload-trigger-text">将文件拖到此处，或<em>点击上传</em></div>
        </div>
      </template>
      <template #tip>
        <div class="upload-tip">
          <a-checkbox v-model="updateSupport">是否更新已经存在的用户数据</a-checkbox>
          <a-alert class="mt-8px">
            仅允许导入 xls、xlsx 格式文件。
            <a-link style="font-size: 12px; vertical-align: baseline" @click="importTemplate">
              下载模板
            </a-link>
          </a-alert>
        </div>
      </template>
    </a-upload>
    <template #footer>
      <a-button :disabled="formLoading" @click="dialogVisible = false">取 消</a-button>
      <a-button :loading="formLoading" type="primary" @click="submitForm">确 定</a-button>
    </template>
  </a-modal>
</template>
<script lang="ts" setup>
import * as UserApi from '@/api/system/user'
import { getAccessToken, getTenantId } from '@/utils/auth'
import download from '@/utils/download'

defineOptions({ name: 'SystemUserImportForm' })

const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中
const uploadRef = ref()
const importUrl =
  import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/system/user/import'
const uploadHeaders = ref() // 上传 Header 头
const fileList = ref([]) // 文件列表
const updateSupport = ref(0) // 是否更新已经存在的用户数据
/** 打开弹窗 */
const open = () => {
  dialogVisible.value = true
  updateSupport.value = 0
  fileList.value = []
  resetForm()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const submitForm = async () => {
  if (fileList.value.length == 0) {
    message.error('请上传文件')
    return
  }
  // 提交请求
  uploadHeaders.value = {
    Authorization: 'Bearer ' + getAccessToken(),
    'tenant-id': getTenantId()
  }
  formLoading.value = true
  uploadRef.value!.submit()
}

/** 文件上传成功（Arco @success 回调入参为 fileItem，原始响应在 fileItem.response） */
const emits = defineEmits(['success'])
const submitFormSuccess = (fileItem: any) => {
  const response = fileItem?.response
  if (!response) {
    message.error('上传失败，请您重新上传！')
    resetForm()
    return
  }
  if (response.code !== 200) {
    message.error(response.msg)
    resetForm()
    return
  }
  // 拼接提示语
  const data = response.data
  const lines = [
    `新增成功 ${data.createUsernames.length} 个：${data.createUsernames.join('、') || '无'}`,
    `更新成功 ${data.updateUsernames.length} 个：${data.updateUsernames.join('、') || '无'}`
  ]
  const failures = Object.entries(data.failureUsernames || {})
  lines.push(`导入失败 ${failures.length} 个${failures.length ? '：' : ''}`)
  failures.forEach(([username, reason]) => lines.push(`${username}：${reason}`))
  message.alert(lines.join('\n'))
  formLoading.value = false
  dialogVisible.value = false
  // 发送操作成功的事件
  emits('success')
}

/** 上传错误提示 */
const submitFormError = (): void => {
  message.error('上传失败，请您重新上传！')
  formLoading.value = false
}

/** 重置表单 */
const resetForm = async (): Promise<void> => {
  // 重置上传状态和文件
  formLoading.value = false
  await nextTick()
  fileList.value = []
}

/** 文件数超出提示 */
const handleExceed = (): void => {
  message.error('最多只能上传一个文件！')
}

/** 下载模板操作 */
const importTemplate = async () => {
  const res = await UserApi.importUserTemplate()
  download.excel(res, '用户导入模板.xlsx')
}
</script>
<style lang="scss" scoped>
.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px 0;
  color: var(--bm-text-3);

  .upload-trigger-text {
    font-size: 14px;

    em {
      font-style: normal;
      color: var(--color-primary-6);
    }
  }
}

.upload-tip {
  margin-top: 8px;
  text-align: center;
}
</style>
