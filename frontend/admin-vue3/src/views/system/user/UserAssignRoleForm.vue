<template>
  <a-modal
    v-model:visible="dialogVisible"
    title="分配角色"
    :width="600"
    :mask-closable="false"
    ok-text="确定"
    cancel-text="取消"
    :ok-loading="formLoading"
    :on-before-ok="handleBeforeOk"
    @cancel="dialogVisible = false"
  >
    <a-form ref="formRef" :model="formData" layout="vertical">
      <a-form-item field="username" label="用户名称">
        <a-input v-model="formData.username" :disabled="true" />
      </a-form-item>
      <a-form-item field="nickname" label="用户昵称">
        <a-input v-model="formData.nickname" :disabled="true" />
      </a-form-item>
      <a-form-item field="roleIds" label="角色">
        <a-select v-model="formData.roleIds" multiple placeholder="请选择角色" allow-clear>
          <a-option v-for="item in roleList" :key="item.id" :value="item.id" :label="item.name" />
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import * as PermissionApi from '@/api/system/permission'
import * as UserApi from '@/api/system/user'
import * as RoleApi from '@/api/system/role'

defineOptions({ name: 'SystemUserAssignRoleForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({
  id: -1,
  nickname: '',
  username: '',
  roleIds: []
})
const formRef = ref() // 表单 Ref
const roleList = ref([] as RoleApi.RoleVO[]) // 角色的列表

/** 打开弹窗 */
const open = async (row: UserApi.UserVO) => {
  dialogVisible.value = true
  resetForm()
  // 设置数据
  formData.value.id = row.id
  formData.value.username = row.username
  formData.value.nickname = row.nickname
  // 获得角色拥有的菜单集合
  formLoading.value = true
  try {
    formData.value.roleIds = await PermissionApi.getUserRoleList(row.id)
  } finally {
    formLoading.value = false
  }
  // 获得角色列表
  roleList.value = await RoleApi.getSimpleRoleList()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const handleBeforeOk = async () => {
  // 校验表单
  const errors = await formRef.value?.validate()
  if (errors) return false
  // 提交请求
  formLoading.value = true
  try {
    await PermissionApi.assignUserRole({
      userId: formData.value.id,
      roleIds: formData.value.roleIds
    })
    message.success(t('common.updateSuccess'))
    // 发送操作成功的事件
    emit('success', true)
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
    id: -1,
    nickname: '',
    username: '',
    roleIds: []
  }
  formRef.value?.resetFields()
}
</script>
