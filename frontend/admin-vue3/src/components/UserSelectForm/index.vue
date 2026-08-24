<template>
  <Dialog v-model="dialogVisible" title="人员选择" width="800">
    <div class="flex gap-2" v-loading="formLoading">
      <div class="w-1/4">
        <ContentWrap class="h-1/1">
          <a-tree
            v-model:expanded-keys="expandedKeys"
            :data="deptTree"
            :field-names="fieldNames"
            block-node
            @select="handleNodeSelect"
          />
        </ContentWrap>
      </div>
      <div class="flex-1">
        <a-transfer
          v-model="selectedUserIdList"
          :data="transferData"
          :title="['未选', '已选']"
          show-search
        />
      </div>
    </div>
    <template #footer>
      <a-button
        :disabled="formLoading || !selectedUserIdList?.length"
        type="primary"
        @click="submitForm"
      >
        确 定
      </a-button>
      <a-button @click="dialogVisible = false">取 消</a-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { handleTree } from '@/utils/tree'
import * as DeptApi from '@/api/system/dept'
import * as UserApi from '@/api/system/user'

defineOptions({ name: 'UserSelectForm' })
const emit = defineEmits<{
  confirm: [id: any, userList: any[]]
}>()
const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const deptTree = ref<Tree[]>([]) // 部门树形结构
const deptList = ref<any[]>([]) // 保存扁平化的部门列表数据
const userList = ref<UserApi.UserVO[]>([]) // 所有用户列表
const filteredUserList = ref<UserApi.UserVO[]>([]) // 当前部门过滤后的用户列表
const selectedUserIdList: any = ref([]) // 选中的用户列表
const expandedKeys = ref<number[]>([]) // 展开的部门节点
const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中
const activityId = ref()
// a-tree 字段映射：后端节点用 id/name，转成 a-tree 认的 key/title
const fieldNames = { key: 'id', title: 'name', children: 'children' }

/** 计算属性：合并已选择的用户和当前部门过滤后的用户 */
const transferUserList = computed(() => {
  // 1.1 获取所有已选择的用户
  const selectedUsers = userList.value.filter((user: any) =>
    selectedUserIdList.value.includes(user.id)
  )

  // 1.2 获取当前部门过滤后的未选择用户
  const filteredUnselectedUsers = filteredUserList.value.filter(
    (user: any) => !selectedUserIdList.value.includes(user.id)
  )

  // 2. 合并并去重
  return [...selectedUsers, ...filteredUnselectedUsers]
})

/** Arco a-transfer 需要的 { value, label } 结构数据 */
const transferData = computed(() =>
  transferUserList.value.map((user: any) => ({
    value: user.id,
    label: user.nickname
  }))
)

/** 收集所有节点 key（用于默认展开全部） */
const collectKeys = (nodes: any[]): number[] => {
  const keys: number[] = []
  const walk = (list: any[]) => {
    for (const n of list) {
      keys.push(n.id)
      if (n.children?.length) walk(n.children)
    }
  }
  walk(nodes)
  return keys
}

/** 打开弹窗 */
const open = async (id: number, selectedList?: any[]) => {
  activityId.value = id
  resetForm()

  // 加载部门、用户列表
  const deptData = await DeptApi.getSimpleDeptList()
  deptList.value = deptData // 保存扁平结构的部门数据
  deptTree.value = handleTree(deptData) // 转换成树形结构
  expandedKeys.value = collectKeys(deptTree.value) // 默认展开全部
  userList.value = await UserApi.getSimpleUserList()

  // 初始状态下，过滤列表等于所有用户列表
  filteredUserList.value = [...userList.value]
  selectedUserIdList.value = selectedList?.map((item: any) => item.id) || []
  dialogVisible.value = true
}

/** 获取指定部门及其所有子部门的ID列表 */
const getChildDeptIds = (deptId: number, deptList: any[]): number[] => {
  const ids = [deptId]
  const children = deptList.filter((dept) => dept.parentId === deptId)
  children.forEach((child) => {
    ids.push(...getChildDeptIds(child.id, deptList))
  })
  return ids
}

/** 获取部门过滤后的用户列表 */
const filterUserList = async (deptId?: number) => {
  formLoading.value = true
  try {
    if (!deptId) {
      // 如果没有选择部门，显示所有用户
      filteredUserList.value = [...userList.value]
      return
    }

    // 直接使用已保存的部门列表数据进行过滤
    const deptIds = getChildDeptIds(deptId, deptList.value)

    // 过滤出这些部门下的用户
    filteredUserList.value = userList.value.filter((user) => deptIds.includes(user.deptId))
  } finally {
    formLoading.value = false
  }
}

/** 提交选择 */
const submitForm = async () => {
  message.success(t('common.updateSuccess'))
  dialogVisible.value = false
  // 从所有用户列表中筛选出已选择的用户
  const emitUserList = userList.value.filter((user: any) =>
    selectedUserIdList.value.includes(user.id)
  )
  // 发送操作成功的事件
  emit('confirm', activityId.value, emitUserList)
}

/** 重置表单 */
const resetForm = () => {
  deptTree.value = []
  deptList.value = []
  userList.value = []
  filteredUserList.value = []
  selectedUserIdList.value = []
  expandedKeys.value = []
}

/** 处理部门被点击 */
const handleNodeSelect = (selectedKeys: (string | number)[]) => {
  filterUserList(selectedKeys[0] as number)
}

defineExpose({ open }) // 提供 open 方法，用于打开弹窗
</script>
