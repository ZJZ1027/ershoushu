<template>
  <Dialog v-model="dialogVisible" title="部门选择" width="600">
    <div v-loading="formLoading">
      <ContentWrap class="h-1/1">
        <a-tree
          v-model:checked-keys="checkedKeys"
          v-model:expanded-keys="expandedKeys"
          :data="deptTree"
          :field-names="fieldNames"
          :check-strictly="checkStrictly"
          checkable
          block-node
          @check="handleCheck"
        />
      </ContentWrap>
    </div>
    <template #footer>
      <a-button
        :disabled="formLoading || !checkedKeys?.length"
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

defineOptions({ name: 'DeptSelectForm' })

const emit = defineEmits<{
  confirm: [deptList: any[]]
}>()

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const props = defineProps({
  // 是否严格的遵循父子不互相关联（与 Arco a-tree 的 check-strictly 语义一致）
  checkStrictly: {
    type: Boolean,
    default: false
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: true
  }
})

const deptTree = ref<Tree[]>([]) // 部门树形结构
const checkedKeys = ref<number[]>([]) // 选中的部门 ID 列表
const expandedKeys = ref<number[]>([]) // 展开的节点 ID 列表
const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中
// a-tree 字段映射：后端节点用 id/name，转成 a-tree 认的 key/title
const fieldNames = { key: 'id', title: 'name', children: 'children' }

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

/** 根据 id 查找节点（用于提交时回传完整节点数据） */
const findNodes = (ids: number[]): any[] => {
  const map = new Map<number, any>()
  const walk = (list: any[]) => {
    for (const n of list) {
      map.set(n.id, n)
      if (n.children?.length) walk(n.children)
    }
  }
  walk(deptTree.value)
  return ids.map((id) => map.get(id)).filter(Boolean)
}

/** 打开弹窗 */
const open = async (selectedList?: DeptApi.DeptVO[]) => {
  resetForm()
  formLoading.value = true
  try {
    // 加载部门列表
    const deptData = await DeptApi.getSimpleDeptList()
    deptTree.value = handleTree(deptData)
    // 默认展开全部
    expandedKeys.value = collectKeys(deptTree.value)
  } finally {
    formLoading.value = false
  }
  dialogVisible.value = true
  // 设置已选择的部门
  if (selectedList?.length) {
    checkedKeys.value = selectedList
      .map((dept) => dept.id)
      .filter((id): id is number => id !== undefined)
  }
}

/** 处理选中状态变化（单选模式下只保留最后选择的节点） */
const handleCheck = () => {
  if (!props.multiple && checkedKeys.value.length > 1) {
    checkedKeys.value = [checkedKeys.value[checkedKeys.value.length - 1]]
  }
}

/** 提交选择 */
const submitForm = async () => {
  const checkedNodes = findNodes(checkedKeys.value)
  message.success(t('common.updateSuccess'))
  dialogVisible.value = false
  emit('confirm', checkedNodes)
}

/** 重置表单 */
const resetForm = () => {
  deptTree.value = []
  checkedKeys.value = []
  expandedKeys.value = []
}

defineExpose({ open }) // 提供 open 方法，用于打开弹窗
</script>
