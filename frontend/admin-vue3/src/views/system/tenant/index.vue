<template>
  <div class="tenant-page">
    <!-- 页头 -->
    <PageHeader title="租户管理">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:tenant:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['system:tenant:export']"
      >
        <template #icon><icon-download /></template>
        导出
      </a-button>
      <a-button
        status="danger"
        :disabled="!checkedIds.length"
        @click="handleDeleteBatch"
        v-hasPermi="['system:tenant:delete']"
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
            placeholder="搜索租户名"
            allow-clear
            style="width: 240px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-select
            v-model="queryParams.status"
            placeholder="租户状态"
            allow-clear
            style="width: 150px"
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
        <div class="toolbar-right">
          <a-radio-group v-model="viewMode" type="button" class="view-toggle">
            <a-radio value="list"><icon-list class="vt-icon" />列表</a-radio>
            <a-radio value="card"><icon-apps class="vt-icon" />面板</a-radio>
          </a-radio-group>
          <a-button @click="filterVisible = true">
            <template #icon><icon-filter /></template>
            筛选
          </a-button>
        </div>
      </div>

      <!-- 列表视图：紧凑、自适应铺满，无横向滚动 -->
      <a-table
        v-if="viewMode === 'list'"
        class="tenant-table"
        :data="list"
        :loading="loading"
        row-key="id"
        size="large"
        :pagination="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        v-model:selected-keys="checkedIds"
      >
        <template #columns>
          <a-table-column title="租户" :min-width="200" ellipsis tooltip>
            <template #cell="{ record }">
              <div class="cell-tenant">
                <span class="cell-avatar">{{ (record.name || 'T').slice(0, 1).toUpperCase() }}</span>
                <div class="cell-tenant-main">
                  <span class="cell-tenant-name">{{ record.name }}</span>
                  <a-tag v-if="record.id === 1" color="red" size="small">系统租户</a-tag>
                </div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="绑定域名" data-index="domain" :min-width="160" ellipsis tooltip />
          <a-table-column title="联系人" :width="160" ellipsis>
            <template #cell="{ record }">
              <div class="cell-contact">
                <span class="cell-contact-name">{{ record.contactName || '—' }}</span>
                <span v-if="record.contactMobile" class="cell-sub">{{ record.contactMobile }}</span>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="账号额度" :width="100" align="right">
            <template #cell="{ record }">
              <span class="bm-cell-num">{{ record.accountCount ?? '—' }}</span>
            </template>
          </a-table-column>
          <a-table-column title="过期时间" :width="130" align="center">
            <template #cell="{ record }">
              <span class="cell-time">{{ fmtExpire(record.expireTime) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="90" align="center">
            <template #cell="{ record }">
              <span class="enable-chip" :class="{ on: record.status === 0 }">
                <i class="enable-dot"></i>
                {{ getDictLabel(DICT_TYPE.COMMON_STATUS, record.status) }}
              </span>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="110" align="center">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['system:tenant:update']"
                >
                  编辑
                </a-button>
                <a-dropdown
                  trigger="click"
                  :popup-max-height="false"
                  class="row-op-dropdown"
                  @select="(v) => onRowCommand(v as string, record)"
                >
                  <a-button type="text" size="small" class="more-btn">
                    <template #icon><icon-more /></template>
                  </a-button>
                  <template #content>
                    <a-doption value="delete" class="danger-option" v-hasPermi="['system:tenant:delete']">
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

      <!-- 面板视图：卡片承载完整字段 -->
      <a-spin v-else :loading="loading" style="width: 100%">
        <div v-if="list.length" class="tenant-cards">
          <div
            v-for="row in list"
            :key="row.id"
            class="tenant-card"
            :class="{ checked: checkedIds.includes(row.id) }"
          >
            <div class="tc-head">
              <a-checkbox
                class="tc-check"
                :model-value="checkedIds.includes(row.id)"
                @change="(v) => toggleCheck(row.id, v as boolean)"
              />
              <span class="tc-avatar">{{ (row.name || 'T').slice(0, 1).toUpperCase() }}</span>
              <div class="tc-title">
                <div class="tc-name" :title="row.name">{{ row.name }}</div>
                <div class="tc-tags">
                  <a-tag v-if="row.id === 1" color="red" size="small">系统租户</a-tag>
                  <span class="enable-chip" :class="{ on: row.status === 0 }">
                    <i class="enable-dot"></i>
                    {{ getDictLabel(DICT_TYPE.COMMON_STATUS, row.status) }}
                  </span>
                </div>
              </div>
              <a-dropdown
                trigger="click"
                :popup-max-height="false"
                class="row-op-dropdown"
                @select="(v) => onRowCommand(v as string, row)"
              >
                <a-button type="text" size="small" class="more-btn">
                  <template #icon><icon-more /></template>
                </a-button>
                <template #content>
                  <a-doption value="delete" class="danger-option" v-hasPermi="['system:tenant:delete']">
                    <template #icon><icon-delete /></template>
                    删除
                  </a-doption>
                </template>
              </a-dropdown>
            </div>
            <div class="tc-body">
              <div class="tc-row">
                <span class="tc-label">联系人</span>
                <span class="tc-val">
                  {{ row.contactName || '—' }}
                  <span v-if="row.contactMobile" class="tc-sub">{{ row.contactMobile }}</span>
                </span>
              </div>
              <div class="tc-row">
                <span class="tc-label">账号额度</span>
                <span class="tc-val"><a-tag color="arcoblue" size="small">{{ row.accountCount }}</a-tag></span>
              </div>
              <div class="tc-row">
                <span class="tc-label">过期时间</span>
                <span class="tc-val">{{ fmtDateTime(row.expireTime) }}</span>
              </div>
              <div class="tc-row">
                <span class="tc-label">创建时间</span>
                <span class="tc-val">{{ fmtDateTime(row.createTime) }}</span>
              </div>
              <div class="tc-row tc-row-domains">
                <span class="tc-label">绑定域名</span>
                <span class="tc-val tc-domains">{{ row.domain || '—' }}</span>
              </div>
            </div>
            <div class="tc-foot">
              <a-button
                type="outline"
                size="small"
                long
                @click="openForm('update', row.id)"
                v-hasPermi="['system:tenant:update']"
              >
                <template #icon><icon-edit /></template>
                编辑
              </a-button>
            </div>
          </div>
        </div>
        <!-- 原先是一个不带任何说明的裸 a-empty：只显示「暂无数据」，既不说明是筛掉了
             还是真没有，也没有下一步可点。后台页保持用图标（不上插画） -->
        <EmptyState
          v-else-if="!loading"
          :icon="hasQueryFilter ? IconFilter : IconApps"
          :title="hasQueryFilter ? '没有符合条件的租户' : '还没有租户'"
          :description="
            hasQueryFilter
              ? '换个关键词或清空筛选，看看全部租户。'
              : '新建租户时会同时开通该租户的第一个管理员账号。'
          "
          :action-text="hasQueryFilter ? '清空筛选' : canCreate ? '新增租户' : ''"
          :action-icon="hasQueryFilter ? undefined : IconPlus"
          @action="hasQueryFilter ? resetQuery() : openForm('create')"
        />
      </a-spin>

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
        <a-form-item label="租户名">
          <a-input v-model="queryParams.name" placeholder="请输入租户名" allow-clear />
        </a-form-item>
        <a-form-item label="联系人">
          <a-input v-model="queryParams.contactName" placeholder="请输入联系人" allow-clear />
        </a-form-item>
        <a-form-item label="联系手机">
          <a-input v-model="queryParams.contactMobile" placeholder="请输入联系手机" allow-clear />
        </a-form-item>
        <a-form-item label="租户状态">
          <a-select v-model="queryParams.status" placeholder="请选择租户状态" allow-clear>
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
    <TenantForm ref="formRef" @success="getList" />
  </div>
</template>
<script lang="ts" setup>
import dayjs from 'dayjs'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import download from '@/utils/download'
import * as TenantApi from '@/api/system/tenant'
import TenantForm from './TenantForm.vue'
import { checkPermi } from '@/utils/permission'
import {
  IconPlus,
  IconDownload,
  IconDelete,
  IconFilter,
  IconMore,
  IconList,
  IconApps,
  IconEdit
} from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemTenant' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref<TenantApi.TenantVO[]>([]) // 列表的数据
const filterVisible = ref(false) // 筛选抽屉
const viewMode = ref<'list' | 'card'>('list') // 视图模式：列表 / 面板
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  contactName: undefined,
  contactMobile: undefined,
  status: undefined,
  createTime: []
})
// 空态里的按钮同样要过权限，否则没有新增权的账号点了只会吃一个 403
const canCreate = computed(() => checkPermi(['system:tenant:create']))
// 空态要分清「筛掉了」和「真没有」：两者该给的下一步完全不同（清空筛选 vs 新建租户）
const hasQueryFilter = computed(
  () =>
    !!queryParams.name ||
    !!queryParams.contactName ||
    !!queryParams.contactMobile ||
    queryParams.status != null ||
    (queryParams.createTime?.length ?? 0) > 0
)
const exportLoading = ref(false) // 导出的加载中

// 时间格式化
const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')
// 过期时间基本都是「某天 00:00:00」，整列挂一串零毫无信息量，只有非零时分秒才补上
const fmtExpire = (v: any) => {
  if (!v) return '—'
  const d = dayjs(v)
  return d.format(d.hour() || d.minute() || d.second() ? 'YYYY-MM-DD HH:mm' : 'YYYY-MM-DD')
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TenantApi.getTenantPage(queryParams)
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
  queryParams.name = undefined
  queryParams.contactName = undefined
  queryParams.contactMobile = undefined
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

/** 行操作下拉 */
const onRowCommand = (cmd: string, row: any) => {
  if (cmd === 'delete') handleDelete(row.id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await TenantApi.deleteTenant(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 面板视图中切换勾选 */
const checkedIds = ref<number[]>([])
const toggleCheck = (id: number, checked: boolean) => {
  if (checked) {
    if (!checkedIds.value.includes(id)) checkedIds.value.push(id)
  } else {
    checkedIds.value = checkedIds.value.filter((x) => x !== id)
  }
}

/** 批量删除按钮操作 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起批量删除
    await TenantApi.deleteTenantList(checkedIds.value)
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
    const data = await TenantApi.exportTenant(queryParams)
    download.excel(data, '租户列表.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(getList)
</script>

<style lang="scss" scoped>
.tenant-page {
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

  .toolbar-right {
    display: flex;
    gap: 10px;
  }
}

/* 干净通透的表格：白底表头 + 灰字、仅保留行间细分隔线、行高加大、柔和悬停 */
.tenant-table {
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

.website-tag {
  margin: 2px 4px 2px 0;
}

/* ===== 视图切换 ===== */
.view-toggle .vt-icon {
  margin-right: 4px;
}

/* ===== 列表视图：租户单元格 ===== */
.cell-tenant {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.cell-avatar {
  display: inline-flex;
  width: 30px;
  height: 30px;
  font-size: 14px;
  font-weight: 700;

  /* -6 档在淡底上只有 3.6~3.9:1（换成靛蓝主题更低），压到 -7 后四套主题都过 AA */
  color: rgb(var(--primary-7));
  background: rgb(var(--primary-1));
  border-radius: 8px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
}

.cell-tenant-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;

  .cell-tenant-name {
    overflow: hidden;
    font-weight: 500;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.cell-contact {
  display: flex;
  flex-direction: column;
  line-height: 1.3;

  .cell-contact-name {
    color: var(--color-text-1, #1d2129);
  }

  .cell-sub {
    font-size: 12px;
    color: var(--bm-text-3);
  }
}

/* ===== 面板视图：卡片 ===== */
.tenant-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.tenant-card {
  display: flex;
  background: var(--color-bg-2, #fff);
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: 10px;
  transition: box-shadow var(--bm-dur) var(--bm-ease-out), border-color var(--bm-dur) var(--bm-ease-out);
  flex-direction: column;

  &:hover {
    border-color: rgb(var(--primary-5));
    box-shadow: 0 6px 18px rgb(0 0 0 / 8%);
  }

  &.checked {
    border-color: rgb(var(--primary-6));
    box-shadow: 0 0 0 1px rgb(var(--primary-6)) inset;
  }
}

.tc-head {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 14px 12px;
  border-bottom: 1px solid var(--color-fill-2, #f2f3f5);

  .tc-check {
    margin-top: 4px;
  }

  .tc-avatar {
    display: inline-flex;
    width: 38px;
    height: 38px;
    font-size: 16px;
    font-weight: 700;
    color: rgb(var(--primary-7));
    background: rgb(var(--primary-1));
    border-radius: 10px;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
  }

  .tc-title {
    flex: 1;
    min-width: 0;

    .tc-name {
      overflow: hidden;
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text-1, #1d2129);
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .tc-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 6px;
    }
  }
}

.tc-body {
  display: flex;
  padding: 12px 14px;
  flex: 1;
  flex-direction: column;
  gap: 10px;

  .tc-row {
    display: flex;
    align-items: baseline;
    gap: 10px;
    font-size: 13px;

    .tc-label {
      flex: 0 0 56px;
      color: var(--bm-text-3);
    }

    .tc-val {
      flex: 1;
      min-width: 0;
      color: var(--color-text-1, #1d2129);
      word-break: break-all;

      .tc-sub {
        margin-left: 6px;
        color: var(--bm-text-3);
      }
    }

    &.tc-row-domains .tc-domains {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }
  }
}

.tc-foot {
  padding: 0 14px 14px;
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
    border-radius: 8px;
    box-shadow: 0 4px 16px rgb(0 0 0 / 8%);
  }

  .arco-dropdown-option {
    height: 34px;
    padding: 0 10px;
    font-size: 13px;
    color: var(--color-text-1, #1d2129);
    border-radius: 6px;

    .arco-dropdown-option-icon {
      margin-right: 8px;
      font-size: 16px;
      color: var(--bm-text-3);
    }

    &:hover {
      color: rgb(var(--primary-6));
      background-color: rgb(var(--primary-1));

      .arco-dropdown-option-icon {
        color: rgb(var(--primary-6));
      }
    }

    &.danger-option {
      color: rgb(var(--red-6));

      .arco-dropdown-option-icon {
        color: rgb(var(--red-6));
      }

      &:hover {
        color: rgb(var(--red-6));
        background-color: rgb(var(--red-1));

        .arco-dropdown-option-icon {
          color: rgb(var(--red-6));
        }
      }
    }
  }
}
</style>
