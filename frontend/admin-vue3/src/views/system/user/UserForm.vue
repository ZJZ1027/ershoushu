<template>
  <a-modal
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    :width="640"
    :mask-closable="false"
    :footer="false"
    @cancel="dialogVisible = false"
  >
    <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item field="nickname" label="用户昵称" required>
            <a-input v-model="formData.nickname" placeholder="请输入用户昵称" allow-clear />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="deptId" label="归属部门">
            <a-tree-select
              v-model="formData.deptId"
              :data="deptList"
              :field-names="{ key: 'id', title: 'name', children: 'children' }"
              placeholder="请选择归属部门"
              allow-clear
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item v-if="formType === 'create'" field="username" label="用户账号" required>
            <a-input v-model="formData.username" placeholder="请输入登录账号" allow-clear />
          </a-form-item>
          <a-form-item v-else field="username" label="用户账号">
            <a-input v-model="formData.username" disabled />
          </a-form-item>
        </a-col>
        <a-col v-if="formType === 'create'" :span="12">
          <a-form-item field="password" label="用户密码" required>
            <a-input-password v-model="formData.password" placeholder="请输入用户密码" allow-clear />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item field="mobile" label="手机号码">
            <a-input v-model="formData.mobile" :max-length="11" placeholder="请输入手机号码" allow-clear />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="email" label="邮箱">
            <a-input v-model="formData.email" :max-length="50" placeholder="请输入邮箱" allow-clear />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item field="wechat" label="微信号">
            <a-input v-model="formData.wechat" placeholder="校园用户面交用" allow-clear />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="campus" label="校区">
            <a-input v-model="formData.campus" placeholder="如 本部 / 东校区" allow-clear />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item field="sex" label="用户性别">
            <a-select v-model="formData.sex" placeholder="请选择" allow-clear>
              <a-option
                v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_USER_SEX)"
                :key="dict.value"
                :value="dict.value"
                :label="dict.label"
              />
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="status" label="状态">
            <a-select v-model="formData.status" placeholder="请选择状态">
              <a-option :value="0">正常</a-option>
              <a-option :value="1">停用</a-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item field="roleIds" label="角色">
            <a-select v-model="formData.roleIds" multiple placeholder="请选择角色，校园用户请勾选「校园用户」" allow-clear>
              <a-option v-for="item in roleList" :key="item.id" :value="item.id!" :label="item.name" />
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item field="postIds" label="岗位">
            <a-select v-model="formData.postIds" multiple placeholder="请选择" allow-clear>
              <a-option v-for="item in postList" :key="item.id" :value="item.id!" :label="item.name" />
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
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
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { handleTree } from '@/utils/tree'
import * as PostApi from '@/api/system/post'
import * as DeptApi from '@/api/system/dept'
import * as UserApi from '@/api/system/user'
import * as RoleApi from '@/api/system/role'

defineOptions({ name: 'SystemUserForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref(emptyForm())
const formRules = reactive({
  username: [{ required: true, message: '用户账号不能为空' }],
  nickname: [{ required: true, message: '用户昵称不能为空' }],
  password: [{ required: true, message: '用户密码不能为空' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址' }],
  mobile: [{ match: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }]
})
const formRef = ref()
const deptList = ref<Tree[]>([])
const postList = ref([] as PostApi.PostVO[])
const roleList = ref([] as RoleApi.RoleVO[])

function emptyForm() {
  return {
    nickname: '',
    deptId: undefined as number | undefined,
    mobile: '',
    email: '',
    id: undefined as number | undefined,
    username: '',
    password: '',
    sex: undefined as number | undefined,
    postIds: [] as number[],
    remark: '',
    status: CommonStatusEnum.ENABLE,
    roleIds: [] as number[],
    wechat: '',
    campus: ''
  }
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增用户' : '编辑用户'
  formType.value = type
  resetForm()
  deptList.value = handleTree(await DeptApi.getSimpleDeptList())
  postList.value = await PostApi.getSimplePostList()
  roleList.value = await RoleApi.getSimpleRoleList()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await UserApi.getUser(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])

const submit = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return
  formLoading.value = true
  try {
    const data = { ...formData.value } as any
    if (data.deptId === '' || data.deptId == null) data.deptId = undefined
    if (!data.email) data.email = undefined
    if (!data.mobile) data.mobile = undefined
    if (formType.value === 'create') {
      await UserApi.createUser(data)
      message.success('保存成功')
    } else {
      await UserApi.updateUser(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } catch {
    /* 错误已由 axios 拦截器提示 */
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = emptyForm()
  formRef.value?.resetFields()
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
