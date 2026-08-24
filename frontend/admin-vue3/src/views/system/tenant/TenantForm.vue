<template>
  <a-drawer
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    :width="560"
    :mask-closable="false"
    :ok-text="formType === 'create' ? '确定创建' : '保存'"
    cancel-text="取消"
    :ok-loading="formLoading"
    :on-before-ok="handleBeforeOk"
    unmount-on-close
    @cancel="dialogVisible = false"
  >
    <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
      <a-form-item field="name" label="租户名" required>
        <a-input v-model="formData.name" placeholder="请输入租户名" allow-clear />
      </a-form-item>
      <a-form-item field="contactName" label="联系人">
        <a-input v-model="formData.contactName" placeholder="请输入联系人" allow-clear />
      </a-form-item>
      <a-form-item field="contactMobile" label="联系手机">
        <a-input v-model="formData.contactMobile" placeholder="请输入联系手机" allow-clear />
      </a-form-item>
      <!-- 新建租户会同时开通该租户的第一个管理员账号，因此账号密码只在新增时出现 -->
      <a-form-item v-if="formType === 'create'" field="username" label="管理员账号" required>
        <a-input v-model="formData.username" placeholder="请输入管理员账号" allow-clear />
      </a-form-item>
      <a-form-item v-if="formType === 'create'" field="password" label="管理员密码" required>
        <a-input-password v-model="formData.password" placeholder="请输入管理员密码" allow-clear />
      </a-form-item>
      <a-form-item field="accountCount" label="账号额度" required>
        <a-input-number
          v-model="formData.accountCount"
          :min="0"
          placeholder="0 表示不限制"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item field="expireTime" label="过期时间">
        <a-date-picker
          v-model="formData.expireTime"
          placeholder="留空表示长期有效"
          value-format="timestamp"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item field="domain" label="绑定域名">
        <a-input v-model="formData.domain" placeholder="如 admin.example.com" allow-clear />
      </a-form-item>
      <a-form-item field="status" label="租户状态" required>
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
    </a-form>
  </a-drawer>
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as TenantApi from '@/api/system/tenant'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'SystemTenantForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: undefined,
  contactName: undefined,
  contactMobile: undefined,
  accountCount: 0,
  expireTime: undefined,
  domain: undefined,
  status: CommonStatusEnum.ENABLE,
  // 新增专属
  username: undefined,
  password: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '租户名不能为空' }],
  status: [{ required: true, message: '租户状态不能为空' }],
  accountCount: [{ required: true, message: '账号额度不能为空' }],
  username: [{ required: true, message: '管理员账号不能为空' }],
  password: [{ required: true, message: '管理员密码不能为空' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增租户' : '编辑租户'
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await TenantApi.getTenant(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const handleBeforeOk = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return false
  formLoading.value = true
  try {
    const data = formData.value as unknown as TenantApi.TenantVO
    if (formType.value === 'create') {
      await TenantApi.createTenant(data)
      message.success(t('common.createSuccess'))
    } else {
      await TenantApi.updateTenant(data)
      message.success(t('common.updateSuccess'))
    }
    // 发送操作成功的事件
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
    name: undefined,
    contactName: undefined,
    contactMobile: undefined,
    accountCount: 0,
    expireTime: undefined,
    domain: undefined,
    status: CommonStatusEnum.ENABLE,
    username: undefined,
    password: undefined
  }
  formRef.value?.resetFields()
}
</script>
