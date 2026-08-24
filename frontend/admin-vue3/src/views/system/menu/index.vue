<template>
  <div class="menu-page">
    <!-- 页头 -->
    <PageHeader title="菜单管理">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:menu:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button @click="toggleExpandAll">
        <template #icon><icon-expand /></template>
        展开/折叠
      </a-button>
      <a-button @click="refreshMenu">
        <template #icon><icon-sync /></template>
        刷新菜单缓存
      </a-button>
    </PageHeader>

    <!-- 列表卡 -->
    <div class="list-card">
      <!-- 工具栏 -->
      <div class="list-toolbar">
        <div class="toolbar-left">
          <a-input
            v-model="queryParams.name"
            placeholder="请输入菜单名称"
            allow-clear
            style="width: 240px"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-select
            v-model="queryParams.status"
            placeholder="菜单状态"
            allow-clear
            style="width: 200px"
            @change="handleQuery"
          >
            <a-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :value="dict.value"
              :label="dict.label"
            />
          </a-select>
          <a-button type="primary" @click="handleQuery">
            <template #icon><icon-search /></template>
            搜索
          </a-button>
          <a-button @click="resetQuery">
            <template #icon><icon-refresh /></template>
            重置
          </a-button>
        </div>
      </div>

      <a-table
        class="menu-table"
        :data="list"
        :loading="loading"
        row-key="id"
        size="large"
        :pagination="false"
        v-model:expanded-keys="expandedKeys"
        :hide-expand-button-on-empty="true"
        :scroll="{ x: 1210 }"
      >
        <template #columns>
          <a-table-column title="菜单名称" data-index="name" :min-width="240" ellipsis tooltip />
          <a-table-column title="图标" :width="64" align="center">
            <template #cell="{ record }">
              <!-- 与侧栏同一个 resolveMenuIcon：这一列就是侧栏的真实预览，不能各渲染一套 -->
              <component :is="resolveMenuIcon(record.icon)" v-if="record.icon" class="menu-icon" />
              <span v-else class="bm-cell-empty">—</span>
            </template>
          </a-table-column>
          <a-table-column title="排序" data-index="sort" :width="64" align="center" />
          <a-table-column title="权限标识" data-index="permission" :width="240" ellipsis tooltip>
            <template #cell="{ record }">
              <span :class="record.permission ? 'bm-cell-main' : 'bm-cell-empty'">
                {{ record.permission || '—' }}
              </span>
            </template>
          </a-table-column>
          <!-- 组件路径 + 组件名称合成一格：两者一一对应，分两列会各自被截成半截路径 -->
          <!-- 只留「菜单名称」一列弹性：两列同时 min-width 时余量分摊各自向上取整，
               合计比容器宽 2px，白拉出一条横向滚动条 -->
          <a-table-column title="组件" :width="320">
            <template #cell="{ record }">
              <div
                :class="record.component ? 'bm-cell-main' : 'bm-cell-empty'"
                :title="record.component"
              >
                {{ record.component || '—' }}
              </div>
              <div v-if="record.componentName" class="bm-cell-sub">{{ record.componentName }}</div>
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="90" align="center">
            <template #cell="{ record }">
              <a-switch
                v-if="checkPermi(['system:menu:update'])"
                v-model="record.status"
                size="small"
                :checked-value="CommonStatusEnum.ENABLE"
                :unchecked-value="CommonStatusEnum.DISABLE"
                :loading="menuStatusUpdating[record.id]"
                @change="(val) => handleStatusChanged(record, val as number)"
              />
              <span
                v-else
                class="enable-chip"
                :class="{ on: record.status === CommonStatusEnum.ENABLE }"
              >
                <i class="enable-dot"></i>
                {{ getDictLabel(DICT_TYPE.COMMON_STATUS, record.status) }}
              </span>
            </template>
          </a-table-column>
          <!-- 宽度必须放得下「修改 + 新增 + 删除」：small 文字按钮各 60px（14px 双字 + 15px 内距
               ×2 + 1px 透明边 ×2）+ 2px 间隙 ×2 = 184px。原来写 180，.op-cell 这个 flex 撑不下
               就往单元格外溢 2px（(184−148)/2 − 16），.arco-table-content 的 scrollWidth 恒比
               clientWidth 多 2 —— 浏览器全程画一条横向滚动条（滑块占满轨道，看着像条灰带），
               同时 Arco 判定「右侧还有内容」给固定列挂上那条竖向内阴影。留 8px 余量抗缩放取整。 -->
          <a-table-column title="操作" :width="192" align="center" fixed="right">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['system:menu:update']"
                >
                  修改
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('create', undefined, record.id)"
                  v-hasPermi="['system:menu:create']"
                >
                  新增
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="handleDelete(record.id)"
                  v-hasPermi="['system:menu:delete']"
                >
                  删除
                </a-button>
              </div>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- 表单弹窗：添加/修改 -->
    <MenuForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import { handleTree } from '@/utils/tree'
import * as MenuApi from '@/api/system/menu'
import { MenuVO } from '@/api/system/menu'
import MenuForm from './MenuForm.vue'
import { resolveMenuIcon } from '@/components/Icon'
import { checkPermi } from '@/utils/permission'
import { CommonStatusEnum } from '@/utils/constants'
import { CACHE_KEY, useAuthCache } from '@/hooks/web/useCache'
import { IconPlus, IconExpand, IconSync, IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemMenu' })

const { wsCache } = useAuthCache()
const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const loading = ref(true) // 列表的加载中
const list = ref<any[]>([]) // 列表的数据
const queryParams = reactive({
  name: undefined,
  status: undefined
})
const isExpandAll = ref(false) // 是否展开，默认全部折叠
const expandedKeys = ref<number[]>([]) // 受控展开的行 key

/** 收集所有含子节点的行 key（用于展开全部） */
const collectExpandKeys = (nodes: any[]): number[] => {
  const keys: number[] = []
  const walk = (arr: any[]) => {
    arr.forEach((n) => {
      if (n.children && n.children.length) {
        keys.push(n.id)
        walk(n.children)
      }
    })
  }
  walk(nodes || [])
  return keys
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await MenuApi.getMenuList(queryParams)
    list.value = handleTree(data)
    expandedKeys.value = isExpandAll.value ? collectExpandKeys(list.value) : []
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.name = undefined
  queryParams.status = undefined
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number, parentId?: number) => {
  formRef.value.open(type, id, parentId)
}

/** 展开/折叠操作 */
const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  expandedKeys.value = isExpandAll.value ? collectExpandKeys(list.value) : []
}

/** 刷新菜单缓存按钮操作 */
const refreshMenu = async () => {
  try {
    await message.confirm('即将更新缓存刷新浏览器！', '刷新菜单缓存')
    // 清空，从而触发刷新
    wsCache.delete(CACHE_KEY.USER)
    wsCache.delete(CACHE_KEY.ROLE_ROUTERS)
    // 刷新浏览器
    location.reload()
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await MenuApi.deleteMenu(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 开启/关闭菜单的状态 */
const menuStatusUpdating = ref<Record<number, boolean>>({}) // 菜单状态更新中的 menu 映射。key：菜单编号，value：是否更新中
const handleStatusChanged = async (menu: MenuVO, val: number) => {
  // 1. 标记 menu.id 更新中
  menuStatusUpdating.value[menu.id] = true
  try {
    // 2. 发起更新状态
    menu.status = val
    await MenuApi.updateMenu(menu)
  } finally {
    // 3. 标记 menu.id 更新完成
    menuStatusUpdating.value[menu.id] = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.menu-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 页头 ===== */



/* ===== 列表卡 ===== */
.list-card {
  padding: 16px 20px 20px;
  background: var(--color-bg-2, #fff);
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: 8px;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }
}

/* 干净通透的表格：白底表头 + 灰字、仅保留行间细分隔线、行高加大、柔和悬停 */
.menu-table {
  :deep(.arco-table-th) {
    font-size: 13px;
    font-weight: 500;
    color: var(--bm-text-3);
    background-color: var(--color-bg-2, #fff);
    border-bottom: 1px solid var(--color-border-2, #e5e6eb);
  }

  :deep(.arco-table-th .arco-table-cell)::before {
    display: none;
  }

  :deep(.arco-table-td) {
    height: 56px;
    font-size: 14px;
    color: var(--color-text-1, #1d2129);
    border-bottom: 1px solid var(--color-fill-2, #f2f3f5);
  }

  :deep(.arco-table-tr:hover .arco-table-td) {
    background-color: var(--color-fill-1, #f7f8fa);
  }

  :deep(.arco-table-container),
  :deep(.arco-table-content) {
    border: none;
  }

  :deep(.arco-tag) {
    font-weight: 500;
    border: none;
    border-radius: 10px;
  }
}

.op-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

/* 图标列：与侧栏同尺寸档位，方便对着改配置 */
.menu-icon {
  font-size: 16px;
  color: var(--bm-text-2);
}
</style>
