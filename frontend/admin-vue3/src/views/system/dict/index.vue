<template>
  <div class="dict-type-page">
    <!-- 页头 -->
    <PageHeader title="字典类型">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:dict:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['system:dict:export']"
      >
        <template #icon><icon-download /></template>
        导出
      </a-button>
      <a-button
        status="danger"
        :disabled="!checkedIds.length"
        @click="handleDeleteBatch"
        v-hasPermi="['system:dict:delete']"
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
            placeholder="搜索字典名称"
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
            style="width: 140px"
            @change="handleQuery"
          >
            <a-option
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :value="dict.value"
              :label="dict.label"
            />
          </a-select>
          <a-button @click="filterVisible = true">
            <template #icon><icon-filter /></template>
            筛选
          </a-button>
        </div>
      </div>

      <a-table
        class="dict-type-table"
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
          <a-table-column title="字典编号" data-index="id" :width="100" align="center" />
          <a-table-column title="字典名称" data-index="name" :width="180" ellipsis tooltip />
          <a-table-column title="字典类型" :width="280" ellipsis tooltip>
            <template #cell="{ record }">
              <a class="dict-type-link" @click="goData(record)">{{ record.type }}</a>
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="100" align="center">
            <template #cell="{ record }">
              <span class="enable-chip" :class="{ on: Number(record.status) === 0 }">
                <i class="enable-dot"></i>
                {{ getDictLabel(DICT_TYPE.COMMON_STATUS, record.status) }}
              </span>
            </template>
          </a-table-column>
          <a-table-column title="备注" data-index="remark" ellipsis tooltip>
            <template #cell="{ record }">{{ record.remark || '—' }}</template>
          </a-table-column>
          <a-table-column title="创建时间" :width="180" align="center">
            <template #cell="{ record }">{{ fmtDateTime(record.createTime) }}</template>
          </a-table-column>
          <a-table-column title="操作" :width="190" align="center" fixed="right">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['system:dict:update']"
                >
                  修改
                </a-button>
                <a-button type="text" size="small" @click="goData(record)">数据</a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="handleDelete(record.id)"
                  v-hasPermi="['system:dict:delete']"
                >
                  删除
                </a-button>
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
        <a-form-item label="字典类型">
          <a-input v-model="queryParams.type" placeholder="请输入字典类型" allow-clear />
        </a-form-item>
        <a-form-item label="创建时间">
          <a-range-picker
            v-model="queryParams.createTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            show-time
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
    <DictTypeForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import * as DictTypeApi from '@/api/system/dict/dict.type'
import DictTypeForm from './DictTypeForm.vue'
import download from '@/utils/download'
import { useRouter } from 'vue-router'
import { IconPlus, IconDownload, IconDelete, IconFilter } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemDictType' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const router = useRouter()

const loading = ref(true) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 字典表格数据
const filterVisible = ref(false) // 筛选抽屉
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  type: '',
  status: undefined,
  createTime: []
})
const exportLoading = ref(false) // 导出的加载中

// 时间格式化
const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')
/** 查询字典类型列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await DictTypeApi.getDictTypePage(queryParams)
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
  queryParams.type = ''
  queryParams.status = undefined
  queryParams.createTime = []
  checkedIds.value = []
  handleQuery()
}

/** 抽屉「查询」 */
const applyFilter = () => {
  filterVisible.value = false
  handleQuery()
}

/** 进入字典数据（保留原 /dict/type/data/:type 跳转逻辑） */
const goData = (record: DictTypeApi.DictTypeVO) => {
  router.push('/dict/type/data/' + record.type)
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
    await DictTypeApi.deleteDictType(id)
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
    await DictTypeApi.deleteDictTypeList(checkedIds.value)
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
    const data = await DictTypeApi.exportDictType(queryParams)
    download.excel(data, '字典类型.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.dict-type-page {
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

.dict-type-link {
  font-family: var(--bm-font-mono, ui-monospace, monospace);

  /* 与全站链接同档：-6 在靛蓝主题白底只有 4.47，-7 才稳过 4.5 */
  color: rgb(var(--primary-7));
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.op-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

/* 干净通透的表格：白底表头 + 灰字、仅保留行间细分隔线、行高加大、柔和悬停 */
.dict-type-table {
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

.list-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
