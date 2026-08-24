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
      <a-form-item field="parentId" label="上级菜单">
        <a-tree-select
          v-model="formData.parentId"
          :data="menuTree"
          :field-names="{ key: 'id', title: 'name', children: 'children' }"
          allow-clear
          placeholder="选择上级菜单"
          style="width: 100%"
        />
      </a-form-item>
      <a-form-item field="name" label="菜单名称">
        <a-input v-model="formData.name" allow-clear placeholder="请输入菜单名称" />
      </a-form-item>
      <a-form-item field="type" label="菜单类型">
        <a-radio-group v-model="formData.type" type="button">
          <a-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_MENU_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item v-if="formData.type !== 3" label="菜单图标">
        <IconSelect v-model="formData.icon" clearable />
      </a-form-item>
      <a-form-item v-if="formData.type !== 3" field="path" label="路由地址">
        <template #label>
          <Tooltip
            message="访问的路由地址，如：`user`。如需外网地址时，则以 `http(s)://` 开头"
            title="路由地址"
          />
        </template>
        <a-input v-model="formData.path" allow-clear placeholder="请输入路由地址" />
      </a-form-item>
      <a-form-item v-if="formData.type === 2" field="component" label="组件地址">
        <a-input v-model="formData.component" allow-clear placeholder="例如说：system/user/index" />
      </a-form-item>
      <a-form-item v-if="formData.type === 2" field="componentName" label="组件名字">
        <a-input v-model="formData.componentName" allow-clear placeholder="例如说：SystemUser" />
      </a-form-item>
      <a-form-item v-if="formData.type !== 1" field="permission" label="权限标识">
        <template #label>
          <Tooltip
            message="Controller 方法上的权限字符，如：@PreAuthorize(`@ss.hasPermission('system:user:list')`)"
            title="权限标识"
          />
        </template>
        <a-input v-model="formData.permission" allow-clear placeholder="请输入权限标识" />
      </a-form-item>
      <a-form-item field="sort" label="显示排序">
        <a-input-number v-model="formData.sort" :min="0" style="width: 100%" />
      </a-form-item>
      <a-form-item field="status" label="菜单状态">
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
      <a-form-item v-if="formData.type !== 3" field="visible" label="显示状态">
        <template #label>
          <Tooltip message="选择隐藏时，路由将不会出现在侧边栏，但仍然可以访问" title="显示状态" />
        </template>
        <a-radio-group v-model="formData.visible">
          <a-radio key="true" :value="true">显示</a-radio>
          <a-radio key="false" :value="false">隐藏</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item v-if="formData.type !== 3" field="alwaysShow" label="总是显示">
        <template #label>
          <Tooltip
            message="选择不是时，当该菜单只有一个子菜单时，不展示自己，直接展示子菜单"
            title="总是显示"
          />
        </template>
        <a-radio-group v-model="formData.alwaysShow">
          <a-radio key="true" :value="true">总是</a-radio>
          <a-radio key="false" :value="false">不是</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item v-if="formData.type === 2" field="keepAlive" label="缓存状态">
        <template #label>
          <Tooltip
            message="选择缓存时，则会被 `keep-alive` 缓存，必须填写「组件名称」字段"
            title="缓存状态"
          />
        </template>
        <a-radio-group v-model="formData.keepAlive">
          <a-radio key="true" :value="true">缓存</a-radio>
          <a-radio key="false" :value="false">不缓存</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as MenuApi from '@/api/system/menu'
import { CACHE_KEY, useAuthCache } from '@/hooks/web/useCache'
import { CommonStatusEnum, SystemMenuTypeEnum } from '@/utils/constants'
import { handleTree } from '@/utils/tree'

defineOptions({ name: 'SystemMenuForm' })

const { wsCache } = useAuthCache()
const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: '',
  permission: '',
  type: SystemMenuTypeEnum.DIR,
  sort: Number(undefined),
  parentId: 0,
  path: '',
  icon: '',
  component: '',
  componentName: '',
  status: CommonStatusEnum.ENABLE,
  visible: true,
  keepAlive: true,
  alwaysShow: true
})
const formRules = {
  name: [{ required: true, message: '菜单名称不能为空' }],
  type: [{ required: true, message: '菜单类型不能为空' }],
  sort: [{ required: true, message: '菜单顺序不能为空' }],
  path: [{ required: true, message: '路由地址不能为空' }],
  status: [{ required: true, message: '状态不能为空' }]
}
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number, parentId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增菜单' : '编辑菜单'
  formType.value = type
  resetForm()
  if (parentId) {
    formData.value.parentId = parentId
  }
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await MenuApi.getMenu(id)
    } finally {
      formLoading.value = false
    }
  }
  // 获得菜单列表
  await getTree()
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
    if (
      formData.value.type === SystemMenuTypeEnum.DIR ||
      formData.value.type === SystemMenuTypeEnum.MENU
    ) {
      if (!isExternal(formData.value.path)) {
        if (formData.value.parentId === 0 && formData.value.path.charAt(0) !== '/') {
          message.error('路径必须以 / 开头')
          return false
        } else if (formData.value.parentId !== 0 && formData.value.path.charAt(0) === '/') {
          message.error('路径不能以 / 开头')
          return false
        }
      }
    }
    const data = formData.value as unknown as MenuApi.MenuVO
    if (formType.value === 'create') {
      await MenuApi.createMenu(data)
      message.success(t('common.createSuccess'))
    } else {
      await MenuApi.updateMenu(data)
      message.success(t('common.updateSuccess'))
    }
    // 发送操作成功的事件
    emit('success')
    return true
  } catch {
    return false
  } finally {
    formLoading.value = false
    // 清空，从而触发刷新
    wsCache.delete(CACHE_KEY.ROLE_ROUTERS)
  }
}

/** 获取下拉框[上级菜单]的数据  */
const menuTree = ref<Tree[]>([]) // 树形结构
const getTree = async () => {
  menuTree.value = []
  const res = await MenuApi.getSimpleMenusList()
  let menu: Tree = { id: 0, name: '主类目', children: [] }
  menu.children = handleTree(res)
  menuTree.value.push(menu)
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    name: '',
    permission: '',
    type: SystemMenuTypeEnum.DIR,
    sort: Number(undefined),
    parentId: 0,
    path: '',
    icon: '',
    component: '',
    componentName: '',
    status: CommonStatusEnum.ENABLE,
    visible: true,
    keepAlive: true,
    alwaysShow: true
  }
  formRef.value?.resetFields()
}

/** 判断 path 是不是外部的 HTTP 等链接 */
const isExternal = (path: string) => {
  return /^(https?:|mailto:|tel:)/.test(path)
}
</script>
