<template>
  <div class="operate-log-page">
    <!-- 页头 -->
    <PageHeader title="操作日志">
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['system:operate-log:export']"
      >
        <template #icon><icon-download /></template>
        导出
      </a-button>
    </PageHeader>

    <!-- 列表卡 -->
    <div class="list-card">
      <!-- 工具栏 -->
      <div class="list-toolbar">
        <div class="toolbar-left">
          <a-input
            v-model="queryParams.module"
            placeholder="操作模块"
            allow-clear
            style="width: 160px"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-input
            v-model="queryParams.name"
            placeholder="操作名"
            allow-clear
            style="width: 160px"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-input
            v-model="queryParams.username"
            placeholder="操作人"
            allow-clear
            style="width: 160px"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-range-picker
            v-model="queryParams.createTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            show-time
            style="width: 320px"
          />
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
        class="log-table"
        :data="list"
        :loading="loading"
        row-key="id"
        size="large"
        :pagination="false"
        :scroll="{ x: 1170 }"
      >
        <template #columns>
          <a-table-column title="编号" data-index="id" :width="80" align="center" />
          <a-table-column title="操作人" :width="130" align="left" ellipsis>
            <template #cell="{ record }">
              <span :class="record.username ? 'bm-cell-main' : 'bm-cell-empty'">
                {{ record.username || '—' }}
              </span>
            </template>
          </a-table-column>
          <!-- 模块 + 操作名合成一格：两者同源（如「岗位」+「新增」），分列只是把一句话劈成两栏 -->
          <a-table-column title="业务动作" :width="190" ellipsis>
            <template #cell="{ record }">
              <div class="bm-cell-main">{{ record.name || '—' }}</div>
              <div v-if="record.module" class="bm-cell-sub">{{ record.module }}</div>
            </template>
          </a-table-column>
          <a-table-column title="请求" :min-width="260" align="left" ellipsis tooltip>
            <template #cell="{ record }">
              <div class="bm-cell-main">{{ record.requestUrl }}</div>
              <div class="bm-cell-sub">{{ record.requestMethod }}</div>
            </template>
          </a-table-column>
          <a-table-column title="结果" :width="90" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.resultCode === 200 ? 'green' : 'red'" size="small">
                {{ record.resultCode === 200 ? '成功' : '失败' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="耗时" :width="90" align="right">
            <template #cell="{ record }">
              <span class="bm-cell-num">{{ record.duration }} ms</span>
            </template>
          </a-table-column>
          <a-table-column title="用户 IP" :width="140" align="center">
            <template #cell="{ record }">
              <span class="bm-cell-num">{{ formatIp(record.userIp) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="操作时间" :width="180" align="center">
            <template #cell="{ record }">
              <span class="cell-time">{{ fmtDateTime(record.createTime) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="90" align="center" fixed="right">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="openDetail(record)">详情</a-button>
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

    <!-- 详情抽屉 -->
    <a-drawer v-model:visible="detailVisible" title="操作日志详情" :width="560" :footer="false">
      <a-descriptions v-if="detail" :column="1" bordered size="medium" :label-style="{ width: '110px' }">
        <a-descriptions-item label="日志编号">{{ detail.id }}</a-descriptions-item>
        <a-descriptions-item label="操作人">{{ detail.username || '—' }}</a-descriptions-item>
        <a-descriptions-item label="操作模块">{{ detail.module }}</a-descriptions-item>
        <a-descriptions-item label="操作名">{{ detail.name }}</a-descriptions-item>
        <a-descriptions-item label="请求方法">{{ detail.requestMethod }}</a-descriptions-item>
        <a-descriptions-item label="请求地址">{{ detail.requestUrl }}</a-descriptions-item>
        <a-descriptions-item label="请求参数">
          <div class="params-raw">{{ detail.requestParams || '—' }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="目标方法">{{ detail.javaMethod || '—' }}</a-descriptions-item>
        <a-descriptions-item label="执行结果">
          {{ detail.resultCode === 200 ? '成功' : `失败（${detail.resultCode}）` }}
          <div v-if="detail.resultMsg" class="ua-raw">{{ detail.resultMsg }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="执行时长">{{ detail.duration }} ms</a-descriptions-item>
        <a-descriptions-item label="用户 IP">{{ formatIp(detail.userIp) }}</a-descriptions-item>
        <a-descriptions-item label="登录客户端">
          {{ formatUserAgent(detail.userAgent) }}
          <div v-if="detail.userAgent" class="ua-raw">{{ detail.userAgent }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="操作时间">{{ fmtDateTime(detail.createTime) }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>
<script lang="ts" setup>
import dayjs from 'dayjs'
import download from '@/utils/download'
import * as OperateLogApi from '@/api/system/operatelog'
import { formatIp, formatUserAgent } from '@/utils/clientInfo'
import { IconDownload, IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemOperateLog' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const exportLoading = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  module: undefined,
  name: undefined,
  username: undefined,
  createTime: [] as string[]
})

const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')

const getList = async () => {
  loading.value = true
  try {
    const data = await OperateLogApi.getOperateLogPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.module = undefined
  queryParams.name = undefined
  queryParams.username = undefined
  queryParams.createTime = []
  handleQuery()
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = (record: any) => {
  detail.value = record
  detailVisible.value = true
}

/** 导出 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await OperateLogApi.exportOperateLog(queryParams)
    download.excel(data, '操作日志.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.operate-log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 详情里既给结论（Edge 148 · Windows）也留原始 UA，方便排查 */
.ua-raw {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--bm-text-3);
  word-break: break-all;
}

/* 请求参数是整段 JSON，给等宽字体并限高，避免一条日志把抽屉撑成长卷 */
.params-raw {
  max-height: 220px;
  overflow: auto;
  font-family: var(--bm-font-mono, monospace);
  font-size: 12px;
  line-height: 1.6;
  color: var(--bm-text-2);
  word-break: break-all;
  white-space: pre-wrap;
}

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

.log-table {
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
}

.list-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
