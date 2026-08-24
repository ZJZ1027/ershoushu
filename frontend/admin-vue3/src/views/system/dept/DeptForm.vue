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
    <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
      <a-form-item field="parentId" label="上级部门" required>
        <a-tree-select
          v-model="formData.parentId"
          :data="deptTree"
          :field-names="{ key: 'id', title: 'name', children: 'children' }"
          placeholder="请选择上级部门"
          allow-clear
        />
      </a-form-item>
      <a-form-item field="name" label="部门名称" required>
        <a-input v-model="formData.name" placeholder="请输入部门名称" allow-clear />
      </a-form-item>
      <a-form-item field="sort" label="显示排序" required>
        <a-input-number v-model="formData.sort" :min="0" style="width: 100%" />
      </a-form-item>
      <a-form-item field="leaderUserId" label="负责人">
        <a-select v-model="formData.leaderUserId" placeholder="请选择负责人" allow-clear>
          <a-option
            v-for="item in userList"
            :key="item.id"
            :value="item.id"
            :label="item.nickname"
          />
        </a-select>
      </a-form-item>
      <a-form-item field="phone" label="联系电话">
        <a-input v-model="formData.phone" :max-length="11" placeholder="请输入联系电话" allow-clear />
      </a-form-item>
      <a-form-item field="email" label="邮箱">
        <a-input v-model="formData.email" :max-length="50" placeholder="请输入邮箱" allow-clear />
      </a-form-item>
      <a-form-item field="status" label="状态" required>
        <a-select v-model="formData.status" placeholder="请选择状态" allow-clear>
          <a-option
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
            :label="dict.label"
          />
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { handleTree } from '@/utils/tree'
import * as DeptApi from '@/api/system/dept'
import * as UserApi from '@/api/system/user'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'SystemDeptForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  title: '',
  parentId: undefined,
  name: undefined,
  sort: undefined,
  leaderUserId: undefined,
  phone: undefined,
  email: undefined,
  status: CommonStatusEnum.ENABLE
})
const formRules = {
  parentId: [{ required: true, message: '上级部门不能为空' }],
  name: [{ required: true, message: '部门名称不能为空' }],
  sort: [{ required: true, message: '显示排序不能为空' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址' }],
  phone: [{ match: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }],
  status: [{ required: true, message: '状态不能为空' }]
}
const formRef = ref() // 表单 Ref
const deptTree = ref() // 树形结构
const userList = ref<UserApi.UserVO[]>([]) // 用户列表

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增部门' : '编辑部门'
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await DeptApi.getDept(id)
    } finally {
      formLoading.value = false
    }
  }
  // 获得用户列表
  userList.value = await UserApi.getSimpleUserList()
  // 获得部门树
  await getTree()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const handleBeforeOk = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return false
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as DeptApi.DeptVO
    if (formType.value === 'create') {
      await DeptApi.createDept(data)
      message.success(t('common.createSuccess'))
    } else {
      await DeptApi.updateDept(data)
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
    title: '',
    parentId: undefined,
    name: undefined,
    sort: undefined,
    leaderUserId: undefined,
    phone: undefined,
    email: undefined,
    status: CommonStatusEnum.ENABLE
  }
  formRef.value?.resetFields()
}

/** 获得部门树 */
const getTree = async () => {
  deptTree.value = []
  const data = await DeptApi.getSimpleDeptList()
  let dept: Tree = { id: 0, name: '顶级部门', children: [] }
  dept.children = handleTree(data)
  deptTree.value.push(dept)
}
</script>
