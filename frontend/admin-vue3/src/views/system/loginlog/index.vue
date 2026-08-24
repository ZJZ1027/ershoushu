<template>
  <div class="login-log-page">
    <!-- 页头 -->
    <PageHeader title="登录日志">
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['system:login-log:export']"
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
            v-model="queryParams.username"
            placeholder="用户账号"
            allow-clear
            style="width: 180px"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-input
            v-model="queryParams.userIp"
            placeholder="登录 IP"
            allow-clear
            style="width: 180px"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-select
            v-model="queryParams.status"
            placeholder="登录结果"
            allow-clear
            style="width: 140px"
            @change="handleQuery"
          >
            <a-option :value="true">成功</a-option>
            <a-option :value="false">失败</a-option>
          </a-select>
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
        :scroll="{ x: 1180 }"
      >
        <template #columns>
          <a-table-column title="日志编号" data-index="id" :width="90" align="center" />
          <a-table-column title="日志类型" :width="130" align="center">
            <template #cell="{ record }">
              {{ getDictLabel(DICT_TYPE.SYSTEM_LOGIN_TYPE, record.logType) }}
            </template>
          </a-table-column>
          <a-table-column title="用户账号" data-index="username" :width="160" align="center" ellipsis tooltip />
          <!-- 成功是常态：整列刷绿标签等于没信息。成功=安静的绿点+文字，失败才给红标签，
               这样「密码不正确」「验证码错误」在一屏里能被一眼扫到 -->
          <a-table-column title="登录结果" :width="160" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.result !== 0" color="red" size="small">
                {{ getDictLabel(DICT_TYPE.SYSTEM_LOGIN_RESULT, record.result) }}
              </a-tag>
              <span v-else class="enable-chip on">
                <i class="enable-dot"></i>
                {{ getDictLabel(DICT_TYPE.SYSTEM_LOGIN_RESULT, record.result) }}
              </span>
            </template>
          </a-table-column>
          <a-table-column title="登录 IP" :width="150" align="center">
            <template #cell="{ record }">
              <span class="bm-cell-num">{{ fmtIp(record.userIp) }}</span>
            </template>
          </a-table-column>
          <!-- 原样铺 UA 串时整列都是「Mozilla/5.0 (Windows NT 10.0; Win64; x64) Apple…」，
               占三成宽度还看不出是什么浏览器；这里解析成「Edge 148 · Windows」，原串进 tooltip -->
          <a-table-column title="登录客户端" :min-width="200" align="left">
            <template #cell="{ record }">
              <a-tooltip :content="record.userAgent || '—'">
                <span :class="record.userAgent ? 'bm-cell-main' : 'bm-cell-empty'">
                  {{ fmtUserAgent(record.userAgent) }}
                </span>
              </a-tooltip>
            </template>
          </a-table-column>
          <a-table-column title="登录时间" :width="180" align="center">
            <template #cell="{ record }">
              <span class="cell-time">{{ fmtDateTime(record.createTime) }}</span>
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
</template>
<script lang="ts" setup>
import dayjs from 'dayjs'
import { DICT_TYPE, getDictLabel } from '@/utils/dict'
import download from '@/utils/download'
import * as LoginLogApi from '@/api/system/loginlog'
import { formatIp, formatUserAgent } from '@/utils/clientInfo'
import { IconDownload, IconSearch, IconRefresh } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemLoginLog' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const exportLoading = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  username: undefined,
  userIp: undefined,
  status: undefined as boolean | undefined,
  createTime: [] as string[]
})

const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')
const fmtIp = formatIp
const fmtUserAgent = formatUserAgent

const getList = async () => {
  loading.value = true
  try {
    const data = await LoginLogApi.getLoginLogPage(queryParams)
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
  queryParams.username = undefined
  queryParams.userIp = undefined
  queryParams.status = undefined
  queryParams.createTime = []
  handleQuery()
}

/** 导出 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await LoginLogApi.exportLoginLog(queryParams)
    download.excel(data, '登录日志.xlsx')
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
.login-log-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}




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

.log-table {
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
