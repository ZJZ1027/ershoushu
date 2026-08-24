<template>
  <div class="user-page">
    <!-- 页头 -->
    <PageHeader title="用户管理">
      <a-button @click="filterVisible = true">
        <template #icon><icon-filter /></template>
        筛选
      </a-button>
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:user:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button status="warning" @click="handleImport" v-hasPermi="['system:user:import']">
        <template #icon><icon-upload /></template>
        导入
      </a-button>
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['system:user:export']"
      >
        <template #icon><icon-download /></template>
        导出
      </a-button>
      <a-button
        status="danger"
        :disabled="!checkedIds.length"
        @click="handleDeleteBatch"
        v-hasPermi="['system:user:delete']"
      >
        <template #icon><icon-delete /></template>
        批量删除
      </a-button>
    </PageHeader>

    <!-- 主体：左侧部门树 + 右侧用户列表 -->
    <div class="user-body">
      <!-- 左侧部门树（沿用原组件） -->
      <div class="dept-card">
        <DeptTree @node-click="handleDeptNodeClick" />
      </div>

      <!-- 右侧用户列表卡 -->
      <div class="list-card user-main">
        <!-- 工具栏 -->
        <div class="list-toolbar">
          <div class="toolbar-left">
            <a-input-search
              v-model="queryParams.username"
              placeholder="搜索用户名称"
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
          class="user-table"
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
            <a-table-column title="用户编号" data-index="id" :width="90" align="center" />
            <a-table-column title="用户名称" data-index="username" :width="140" ellipsis tooltip />
            <a-table-column title="用户昵称" data-index="nickname" :width="140" ellipsis tooltip />
            <a-table-column title="部门" data-index="deptName" :width="140" ellipsis tooltip>
              <template #cell="{ record }">{{ record.deptName || '—' }}</template>
            </a-table-column>
            <a-table-column title="手机号码" data-index="mobile" :width="130" align="center">
              <template #cell="{ record }">{{ record.mobile || '—' }}</template>
            </a-table-column>
            <a-table-column title="状态" :width="90" align="center">
              <template #cell="{ record }">
                <a-switch
                  v-model="record.status"
                  size="small"
                  :checked-value="0"
                  :unchecked-value="1"
                  :disabled="!checkPermi(['system:user:update'])"
                  @change="handleStatusChange(record)"
                />
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
                    v-hasPermi="['system:user:update']"
                  >
                    修改
                  </a-button>
                  <!-- v-if 而非 v-hasPermi：a-dropdown 根节点是 Trigger 组件，DOM 移除指令无法作用 -->
                  <a-dropdown
                    v-if="
                      checkPermi([
                        'system:user:delete',
                        'system:user:update-password',
                        'system:permission:assign-user-role'
                      ])
                    "
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
                        value="handleResetPwd"
                        v-if="checkPermi(['system:user:update-password'])"
                      >
                        <template #icon><icon-lock /></template>
                        重置密码
                      </a-doption>
                      <a-doption
                        value="handleRole"
                        v-if="checkPermi(['system:permission:assign-user-role'])"
                      >
                        <template #icon><icon-user-group /></template>
                        分配角色
                      </a-doption>
                      <a-doption
                        value="handleDelete"
                        v-if="checkPermi(['system:user:delete'])"
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
        <a-form-item label="用户名称">
          <a-input v-model="queryParams.username" placeholder="请输入用户名称" allow-clear />
        </a-form-item>
        <a-form-item label="手机号码">
          <a-input v-model="queryParams.mobile" placeholder="请输入手机号码" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model="queryParams.status" placeholder="请选择用户状态" allow-clear>
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

    <!-- 添加或修改用户对话框 -->
    <UserForm ref="formRef" @success="getList" />
    <!-- 用户导入对话框 -->
    <UserImportForm ref="importFormRef" @success="getList" />
    <!-- 分配角色 -->
    <UserAssignRoleForm ref="assignRoleFormRef" @success="getList" />
  </div>
</template>
<script lang="ts" setup>
import dayjs from 'dayjs'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { checkPermi } from '@/utils/permission'
import download from '@/utils/download'
import { CommonStatusEnum } from '@/utils/constants'
import * as UserApi from '@/api/system/user'
import UserForm from './UserForm.vue'
import UserImportForm from './UserImportForm.vue'
import UserAssignRoleForm from './UserAssignRoleForm.vue'
import DeptTree from './DeptTree.vue'
import {
  IconFilter,
  IconPlus,
  IconUpload,
  IconDownload,
  IconDelete,
  IconMore,
  IconLock,
  IconUserGroup
} from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemUser' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数
const filterVisible = ref(false) // 筛选抽屉
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  username: undefined,
  mobile: undefined,
  status: undefined,
  deptId: undefined,
  createTime: []
})

// 时间格式化
const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await UserApi.getUserPage(queryParams)
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
  queryParams.username = undefined
  queryParams.mobile = undefined
  queryParams.status = undefined
  queryParams.createTime = []
  handleQuery()
}

/** 抽屉「查询」 */
const applyFilter = () => {
  filterVisible.value = false
  handleQuery()
}

/** 处理部门被点击 */
const handleDeptNodeClick = async (row: any) => {
  if (row === undefined) {
    queryParams.deptId = undefined
    await getList()
  } else {
    queryParams.deptId = row.id
    await getList()
  }
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 用户导入 */
const importFormRef = ref()
const handleImport = () => {
  importFormRef.value.open()
}

/** 修改用户状态 */
const handleStatusChange = async (row: UserApi.UserVO) => {
  try {
    // 修改状态的二次确认
    const text = row.status === CommonStatusEnum.ENABLE ? '启用' : '停用'
    await message.confirm('确认要"' + text + '""' + row.username + '"用户吗?')
    // 发起修改状态
    await UserApi.updateUserStatus(row.id, row.status)
    // 刷新列表
    await getList()
  } catch {
    // 取消后，进行恢复按钮
    row.status =
      row.status === CommonStatusEnum.ENABLE ? CommonStatusEnum.DISABLE : CommonStatusEnum.ENABLE
  }
}

/** 导出按钮操作 */
const exportLoading = ref(false)
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await UserApi.exportUser(queryParams)
    download.excel(data, '用户数据.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 操作分发 */
const handleCommand = (command: string, row: UserApi.UserVO) => {
  switch (command) {
    case 'handleDelete':
      handleDelete(row.id)
      break
    case 'handleResetPwd':
      handleResetPwd(row)
      break
    case 'handleRole':
      handleRole(row)
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
    await UserApi.deleteUser(id)
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
    await UserApi.deleteUserList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 重置密码 */
const handleResetPwd = async (row: UserApi.UserVO) => {
  try {
    // 重置的二次确认
    const result = await message.prompt(
      '请输入"' + row.username + '"的新密码',
      t('common.reminder')
    )
    const password = result.value
    // 发起重置
    await UserApi.resetUserPassword(row.id, password)
    message.success('修改成功，新密码是：' + password)
  } catch {}
}

/** 分配角色 */
const assignRoleFormRef = ref()
const handleRole = (row: UserApi.UserVO) => {
  assignRoleFormRef.value.open(row)
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.user-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 页头 ===== */



/* ===== 主体两栏 ===== */
.user-body {
  display: flex;
  align-items: stretch;
  gap: 16px;
}

/* 左侧部门树卡 */
.dept-card {
  width: 248px;
  padding: 16px;
  overflow: auto;
  background: var(--bm-bg-card);
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius);
  box-shadow: var(--bm-shadow-card);
  flex: 0 0 248px;
}

/* ===== 列表卡 ===== */
.list-card {
  padding: 16px 20px 20px;
  background: var(--bm-bg-card);
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius);
  box-shadow: var(--bm-shadow-card);
}

.user-main {
  flex: 1;
  min-width: 0;
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
.user-table {
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

/* 让左侧部门树融入卡片风格（DeptTree 内为 Arco a-input） */
.dept-card :deep(.head-container) {
  margin-bottom: 0;
}

.dept-card :deep(.arco-input-wrapper) {
  margin-bottom: 12px;
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
