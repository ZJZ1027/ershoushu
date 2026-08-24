<template>
  <div class="role-page">
    <!-- 页头 -->
    <PageHeader title="角色管理">
      <a-button @click="filterVisible = true">
        <template #icon><icon-filter /></template>
        筛选
      </a-button>
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:role:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['system:role:export']"
      >
        <template #icon><icon-download /></template>
        导出
      </a-button>
      <a-button
        status="danger"
        :disabled="!checkedIds.length"
        @click="handleDeleteBatch"
        v-hasPermi="['system:role:delete']"
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
          <a-input-search
            v-model="queryParams.name"
            placeholder="搜索角色名称"
            allow-clear
            style="width: 240px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-select
            v-model="queryParams.status"
            placeholder="状态"
            allow-clear
            style="width: 160px"
            @change="handleQuery"
          >
            <a-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :value="dict.value"
              :label="dict.label"
            />
          </a-select>
        </div>
      </div>

      <a-table
        class="role-table"
        :data="list"
        :loading="loading"
        row-key="id"
        size="large"
        :pagination="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        v-model:selected-keys="checkedIds"
        :scroll="{ x: '100%' }"
      >
        <template #columns>
          <a-table-column title="角色编号" data-index="id" :width="90" align="center" />
          <a-table-column title="角色名称" data-index="name" :width="160" ellipsis tooltip />
          <a-table-column title="角色类型" :width="110" align="center">
            <template #cell="{ record }">
              <a-tag :color="roleTypeColor(record.type)" size="small">
                {{ getDictLabel(DICT_TYPE.SYSTEM_ROLE_TYPE, record.type) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="角色标识" data-index="code" :width="160" ellipsis tooltip />
          <a-table-column title="显示顺序" data-index="sort" :width="90" align="center" />
          <a-table-column title="备注" data-index="remark" ellipsis tooltip>
            <template #cell="{ record }">{{ record.remark || '—' }}</template>
          </a-table-column>
          <a-table-column title="状态" :width="90" align="center">
            <template #cell="{ record }">
              <span class="enable-chip" :class="{ on: record.status === 0 }">
                <i class="enable-dot"></i>
                {{ getDictLabel(DICT_TYPE.COMMON_STATUS, record.status) }}
              </span>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" :width="180" align="center">
            <template #cell="{ record }">{{ fmtDateTime(record.createTime) }}</template>
          </a-table-column>
          <a-table-column title="操作" :width="150" align="center" fixed="right">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['system:role:update']"
                >
                  编辑
                </a-button>
                <!-- v-if 而非 v-hasPermi：a-dropdown 根节点是 Trigger 组件，DOM 移除指令无法作用 -->
                <a-dropdown
                  v-if="checkPermi(['system:permission:assign-role-menu', 'system:role:delete'])"
                  trigger="click"
                  :popup-max-height="false"
                  class="row-op-dropdown"
                  @select="(v) => handleCommand(v as string, record)"
                >
                  <a-button type="text" size="small" class="more-btn">
                    <template #icon><icon-more /></template>
                  </a-button>
                  <template #content>
                    <a-doption
                      value="assignMenu"
                      v-if="checkPermi(['system:permission:assign-role-menu'])"
                    >
                      <template #icon><icon-menu /></template>
                      菜单权限
                    </a-doption>
                    <a-doption
                      value="delete"
                      v-if="checkPermi(['system:role:delete'])"
                      class="danger-option"
                    >
                      <template #icon><icon-delete /></template>
                      删除
                    </a-doption>
                  </template>
                </a-dropdown>
              </div>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <!-- 分页 -->
      <div class="list-pager">
        <a-pagination
          :total="total"
          v-model:current="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          show-total
          show-jumper
          show-page-size
          @change="getList"
          @page-size-change="handleQuery"
        />
      </div>
    </div>

    <!-- 筛选抽屉 -->
    <a-drawer
      v-model:visible="filterVisible"
      title="筛选"
      :width="360"
      :footer="true"
      unmount-on-close
    >
      <a-form :model="queryParams" layout="vertical">
        <a-form-item label="角色名称">
          <a-input v-model="queryParams.name" placeholder="请输入角色名称" allow-clear />
        </a-form-item>
        <a-form-item label="角色标识">
          <a-input v-model="queryParams.code" placeholder="请输入角色标识" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="queryParams.status" placeholder="请选择状态" allow-clear>
            <a-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :value="dict.value"
              :label="dict.label"
            />
          </a-select>
        </a-form-item>
        <a-form-item label="创建时间">
          <a-range-picker
            v-model="queryParams.createTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button @click="resetQuery">重置</a-button>
        <a-button type="primary" @click="applyFilter">查询</a-button>
      </template>
    </a-drawer>

    <!-- 表单弹窗：添加/修改 -->
    <RoleForm ref="formRef" @success="getList" />
    <!-- 表单弹窗：菜单权限 -->
    <RoleAssignMenuForm ref="assignMenuFormRef" @success="getList" />
  </div>
</template>
<script lang="ts" setup>
import dayjs from 'dayjs'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import { checkPermi } from '@/utils/permission'
import download from '@/utils/download'
import * as RoleApi from '@/api/system/role'
import RoleForm from './RoleForm.vue'
import RoleAssignMenuForm from './RoleAssignMenuForm.vue'
import {
  IconFilter,
  IconPlus,
  IconDownload,
  IconDelete,
  IconMore,
  IconMenu
} from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemRole' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const filterVisible = ref(false) // 筛选抽屉
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  code: '',
  name: '',
  status: undefined,
  createTime: []
})
const exportLoading = ref(false) // 导出的加载中

// 时间格式化
const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')

// 角色类型标签配色
const roleTypeColor = (v: any) => (Number(v) === 1 ? 'arcoblue' : 'green')

/** 查询角色列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await RoleApi.getRolePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.name = ''
  queryParams.code = ''
  queryParams.status = undefined
  queryParams.createTime = []
  handleQuery()
}

/** 抽屉「查询」 */
const applyFilter = () => {
  filterVisible.value = false
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 菜单权限操作 */
const assignMenuFormRef = ref()
const openAssignMenuForm = async (row: RoleApi.RoleVO) => {
  assignMenuFormRef.value.open(row)
}

/** 行操作下拉分发 */
const handleCommand = (command: string, row: RoleApi.RoleVO) => {
  switch (command) {
    case 'assignMenu':
      openAssignMenuForm(row)
      break
    case 'delete':
      handleDelete(row.id)
      break
    default:
      break
  }
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await RoleApi.deleteRole(id)
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
    await RoleApi.deleteRoleList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await RoleApi.exportRole(queryParams)
    download.excel(data, '角色数据.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.role-page {
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
.role-table {
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

  .more-btn {
    color: var(--bm-text-3);
  }
}

.list-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

<!-- 行操作下拉：弹层 teleport 到 body，class 落在 .arco-trigger-popup 上，需非 scoped 才能命中 -->
<style lang="scss">
.row-op-dropdown {
  .arco-dropdown {
    min-width: 148px;
    padding: 4px;
    border-radius: var(--bm-radius);
    box-shadow: var(--bm-shadow-hover);
  }

  .arco-dropdown-option {
    height: 34px;
    padding: 0 10px;
    font-size: 13px;
    color: var(--bm-text-1);
    border-radius: var(--bm-radius-sm);

    .arco-dropdown-option-icon {
      margin-right: 8px;
      font-size: 16px;
      color: var(--bm-text-3);
    }

    &:hover {
      color: var(--bm-brand);
      background-color: var(--bm-brand-bg);

      .arco-dropdown-option-icon {
        color: var(--bm-brand);
      }
    }

    &.danger-option {
      color: var(--bm-danger-text);

      .arco-dropdown-option-icon {
        color: var(--bm-danger-text);
      }

      &:hover {
        color: var(--bm-danger-text);
        background-color: var(--bm-danger-bg);

        .arco-dropdown-option-icon {
          color: var(--bm-danger-text);
        }
      }
    }
  }
}
</style>
