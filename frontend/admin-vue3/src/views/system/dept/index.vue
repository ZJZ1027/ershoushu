<template>
  <div class="dept-page">
    <!-- 页头 -->
    <PageHeader title="部门管理">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:dept:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button @click="toggleExpandAll">
        <template #icon><icon-expand /></template>
        展开/折叠
      </a-button>
      <a-button
        status="danger"
        :disabled="checkedIds.length === 0"
        @click="handleDeleteBatch"
        v-hasPermi="['system:dept:delete']"
      >
        <template #icon><icon-delete /></template>
        批量删除
      </a-button>
    </PageHeader>

    <!-- 列表卡 -->
    <div class="list-card">
      <!-- 工具栏 -->
      <div class="list-toolbar">
        <div class="toolbar-left">
          <a-input
            v-model="queryParams.name"
            placeholder="请输入部门名称"
            allow-clear
            style="width: 240px"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-select
            v-model="queryParams.status"
            placeholder="部门状态"
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
        class="dept-table"
        :data="list"
        :loading="loading"
        row-key="id"
        size="large"
        :pagination="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        v-model:selected-keys="checkedIds"
        v-model:expanded-keys="expandedKeys"
        :hide-expand-button-on-empty="true"
        :scroll="{ x: '100%' }"
      >
        <template #columns>
          <a-table-column title="部门名称" data-index="name" :width="240" ellipsis tooltip />
          <a-table-column title="负责人" :width="160" ellipsis tooltip>
            <template #cell="{ record }">
              {{ userList.find((user) => user.id === record.leaderUserId)?.nickname || '—' }}
            </template>
          </a-table-column>
          <a-table-column title="排序" data-index="sort" :width="90" align="center" />
          <a-table-column title="状态" :width="100" align="center">
            <template #cell="{ record }">
              <span class="enable-chip" :class="{ on: record.status === CommonStatusEnum.ENABLE }">
                <i class="enable-dot"></i>
                {{ getDictLabel(DICT_TYPE.COMMON_STATUS, record.status) }}
              </span>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" :width="180" align="center">
            <template #cell="{ record }">{{ fmtDateTime(record.createTime) }}</template>
          </a-table-column>
          <a-table-column title="操作" :width="140" align="center" fixed="right">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['system:dept:update']"
                >
                  修改
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="handleDelete(record.id)"
                  v-hasPermi="['system:dept:delete']"
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
    <DeptForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import { handleTree } from '@/utils/tree'
import { CommonStatusEnum } from '@/utils/constants'
import * as DeptApi from '@/api/system/dept'
import DeptForm from './DeptForm.vue'
import * as UserApi from '@/api/system/user'
import { IconPlus, IconExpand, IconDelete, IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemDept' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<any[]>([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 100,
  name: undefined,
  status: undefined
})
const isExpandAll = ref(true) // 是否展开，默认全部展开
const expandedKeys = ref<number[]>([]) // 受控展开的行 key
const userList = ref<UserApi.UserVO[]>([]) // 用户列表

// 时间格式化
const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')

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

/** 查询部门列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await DeptApi.getDeptList(queryParams)
    list.value = handleTree(data)
    expandedKeys.value = isExpandAll.value ? collectExpandKeys(list.value) : []
  } finally {
    loading.value = false
  }
}

/** 展开/折叠操作 */
const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  expandedKeys.value = isExpandAll.value ? collectExpandKeys(list.value) : []
}

/** 搜索按钮操作 */
const handleQuery = () => {
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.pageNo = 1
  queryParams.name = undefined
  queryParams.status = undefined
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await DeptApi.deleteDept(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除按钮操作 */
const checkedIds = ref<number[]>([])
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起批量删除
    await DeptApi.deleteDeptList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(async () => {
  await getList()
  // 获取用户列表
  userList.value = await UserApi.getSimpleUserList()
})
</script>

<style lang="scss" scoped>
.dept-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 页头 ===== */



/* ===== 列表卡 ===== */
.list-card {
  padding: 16px 20px 20px;
  background: var(--bm-bg-card);
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius);
  box-shadow: var(--bm-shadow-card);
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
.dept-table {
  :deep(.arco-table-th) {
    font-size: 13px;
    font-weight: 500;
    color: var(--bm-text-3);
    background-color: var(--bm-bg-card);
    border-bottom: 1px solid var(--bm-border);
  }

  :deep(.arco-table-th .arco-table-cell)::before {
    display: none;
  }

  :deep(.arco-table-td) {
    height: 56px;
    font-size: 14px;
    color: var(--bm-text-1);
    border-bottom: 1px solid var(--bm-fill);
  }

  :deep(.arco-table-tr:hover .arco-table-td) {
    background-color: var(--bm-fill-light);
  }

  :deep(.arco-table-container),
  :deep(.arco-table-content) {
    border: none;
  }

  :deep(.arco-tag) {
    font-weight: 500;
    border: none;
    border-radius: 999px;
  }
}

.op-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}
</style>
