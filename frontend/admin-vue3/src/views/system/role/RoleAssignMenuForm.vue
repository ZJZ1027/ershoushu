<template>
  <a-modal
    v-model:visible="dialogVisible"
    title="菜单权限"
    :width="600"
    :mask-closable="false"
    ok-text="确定"
    cancel-text="取消"
    :ok-loading="formLoading"
    :on-before-ok="handleBeforeOk"
    @cancel="dialogVisible = false"
  >
    <!-- 角色名/标识是只读上下文，占两行表单纯属浪费高度，压成一行副标题 -->
    <div class="perm-head">
      <span class="perm-role">{{ formData.name || '—' }}</span>
      <span class="perm-code">{{ formData.code }}</span>
    </div>
    <div class="perm-bar">
      <a-input v-model="keyword" allow-clear placeholder="搜索菜单名称" class="perm-search">
        <template #prefix><icon-search /></template>
      </a-input>
      <a-checkbox v-model="treeNodeAll" @change="handleCheckedTreeNodeAll">全选</a-checkbox>
      <a-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand">展开全部</a-checkbox>
      <span class="perm-count">已选 {{ checkedKeys.length }} 项</span>
    </div>
    <a-spin :loading="formLoading" style="width: 100%">
      <div class="tree-box">
        <a-tree
          v-model:checked-keys="checkedKeys"
          v-model:expanded-keys="expandedKeys"
          :data="menuOptions"
          :field-names="fieldNames"
          :filter-tree-node="filterNode"
          checkable
          block-node
        />
      </div>
    </a-spin>
  </a-modal>
</template>
<script lang="ts" setup>
import { handleTree } from '@/utils/tree'
import * as RoleApi from '@/api/system/role'
import * as MenuApi from '@/api/system/menu'
import * as PermissionApi from '@/api/system/permission'

defineOptions({ name: 'SystemRoleAssignMenuForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  menuIds: [] as number[]
})
const formRef = ref() // 表单 Ref
const menuOptions = ref<any[]>([]) // 菜单树形结构
const menuExpand = ref(false) // 展开/折叠
const treeNodeAll = ref(false) // 全选/全不选
const keyword = ref('') // 菜单搜索关键字：菜单上百个，靠肉眼翻树不现实
const checkedKeys = ref<number[]>([]) // 选中的菜单节点 key
const expandedKeys = ref<number[]>([]) // 展开的节点 key
// a-tree 字段映射：后端节点用 id/name，转成 a-tree 认的 key/title
const fieldNames = { key: 'id', title: 'name', children: 'children' }

// 树元信息：全部 key、叶子 key 集合、子->父 映射（用于联动推导与半选祖先计算）
const treeMeta = computed(() => {
  const allKeys: number[] = []
  const leafIds = new Set<number>()
  const parentMap: Record<number, number | undefined> = {}
  const walk = (nodes: any[], parent?: number) => {
    for (const n of nodes) {
      allKeys.push(n.id)
      parentMap[n.id] = parent
      if (n.children?.length) {
        walk(n.children, n.id)
      } else {
        leafIds.add(n.id)
      }
    }
  }
  walk(menuOptions.value)
  return { allKeys, leafIds, parentMap }
})

/** 打开弹窗 */
const open = async (row: RoleApi.RoleVO) => {
  dialogVisible.value = true
  resetForm()
  // 加载 Menu 列表
  menuOptions.value = handleTree(await MenuApi.getSimpleMenusList())
  // 设置数据
  formData.id = row.id
  formData.name = row.name
  formData.code = row.code
  formLoading.value = true
  try {
    const menuIds = await PermissionApi.getRoleMenuList(row.id)
    // 仅用叶子节点驱动选中，父节点由 Arco 联动自动推导（全选/半选）
    const { leafIds } = treeMeta.value
    checkedKeys.value = (menuIds as number[]).filter((id) => leafIds.has(id))
  } finally {
    formLoading.value = false
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const handleBeforeOk = async () => {
  // 提交请求
  formLoading.value = true
  try {
    // 选中 + 半选祖先：checkedKeys 已含全选父节点，再补齐其所有祖先（半选父节点）
    const { parentMap } = treeMeta.value
    const result = new Set<number>(checkedKeys.value)
    for (const k of checkedKeys.value) {
      let p = parentMap[k]
      while (p !== undefined && p !== null) {
        result.add(p)
        p = parentMap[p]
      }
    }
    const data = {
      // 弹窗只能由 open(row) 打开，走到这里 id 必定已赋值
      roleId: formData.id!,
      menuIds: Array.from(result)
    }
    await PermissionApi.assignRoleMenu(data)
    message.success(t('common.updateSuccess'))
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
  // 重置选项
  treeNodeAll.value = false
  menuExpand.value = false
  // 重置表单
  formData.id = undefined
  formData.name = ''
  formData.code = ''
  formData.menuIds = []
  checkedKeys.value = []
  expandedKeys.value = []
  formRef.value?.resetFields()
}

/** 关键字过滤：命中的节点保留，父节点由 Arco 自行保留链路 */
const filterNode = (node: any) => {
  const kw = keyword.value.trim()
  if (!kw) return true
  return String(node.name || '').toLowerCase().includes(kw.toLowerCase())
}
// 搜索时自动展开，否则命中的深层节点还是折叠着看不见
watch(keyword, (kw) => {
  if (kw.trim()) expandedKeys.value = [...treeMeta.value.allKeys]
})

/** 全选/全不选 */
const handleCheckedTreeNodeAll = () => {
  checkedKeys.value = treeNodeAll.value ? [...treeMeta.value.allKeys] : []
}

/** 展开/折叠全部 */
const handleCheckedTreeExpand = () => {
  expandedKeys.value = menuExpand.value ? [...treeMeta.value.allKeys] : []
}
</script>
<style lang="scss" scoped>
.perm-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.perm-role {
  font-size: var(--bm-fs-md);
  font-weight: 600;
  color: var(--bm-text-1);
}

.perm-code {
  font-family: var(--bm-font-mono, ui-monospace, monospace);
  font-size: var(--bm-fs-xs);
  color: var(--bm-text-3);
}

.perm-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.perm-search {
  width: 200px;
}

.perm-count {
  margin-left: auto;
  font-size: var(--bm-fs-xs);
  color: var(--bm-text-3);
  white-space: nowrap;
}

.tree-box {
  max-height: 46vh;
  min-height: 280px;
  padding: 8px 12px;
  overflow-y: auto;
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-sm);
}
</style>
