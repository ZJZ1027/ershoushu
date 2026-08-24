<template>
  <div class="head-container">
    <a-input v-model="deptName" class="mb-20px" allow-clear placeholder="请输入部门名称">
      <template #prefix>
        <Icon icon="ep:search" />
      </template>
    </a-input>
  </div>
  <div class="head-container">
    <a-tree
      v-model:expanded-keys="expandedKeys"
      v-model:selected-keys="selectedKeys"
      :data="treeData"
      :field-names="fieldNames"
      block-node
      @select="handleSelect"
    />
  </div>
</template>

<script lang="ts" setup>
import * as DeptApi from '@/api/system/dept'
import { handleTree } from '@/utils/tree'

defineOptions({ name: 'SystemUserDeptTree' })

const deptName = ref('')
const deptList = ref<Tree[]>([]) // 树形结构
const selectedKeys = ref<(string | number)[]>([])
const expandedKeys = ref<(string | number)[]>([])
// a-tree 字段映射：后端节点用 id/name，转成 a-tree 认的 key/title
const fieldNames = { key: 'id', title: 'name', children: 'children' }

/** 获得部门树 */
const getTree = async () => {
  const res = await DeptApi.getSimpleDeptList()
  deptList.value = handleTree(res)
}

/** 收集所有节点 key */
const collectKeys = (nodes: any[]): (string | number)[] => {
  const keys: (string | number)[] = []
  const walk = (list: any[]) => {
    for (const n of list) {
      keys.push(n.id)
      if (n.children?.length) walk(n.children)
    }
  }
  walk(nodes)
  return keys
}

/** 按名称过滤：保留匹配节点及其祖先 */
const filterTree = (nodes: any[], keyword: string): any[] => {
  const result: any[] = []
  for (const node of nodes) {
    const children = node.children?.length ? filterTree(node.children, keyword) : []
    if ((node.name || '').includes(keyword) || children.length) {
      result.push({ ...node, children })
    }
  }
  return result
}

const treeData = computed(() => {
  if (!deptName.value) return deptList.value
  return filterTree(deptList.value, deptName.value)
})

// 始终展开全部（对齐原 default-expand-all）
watch(
  treeData,
  (val) => {
    expandedKeys.value = collectKeys(val)
  },
  { immediate: true }
)

const emits = defineEmits(['node-click'])

/** 处理部门被点击：选中=>派发节点；取消选中=>派发 undefined */
const handleSelect = (_keys: (string | number)[], data: { selected?: boolean; node?: any }) => {
  if (data.selected && data.node) {
    emits('node-click', data.node)
  } else {
    emits('node-click', undefined)
  }
}

/** 初始化 */
onMounted(async () => {
  await getTree()
})
</script>
