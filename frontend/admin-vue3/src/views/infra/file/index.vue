<template>
  <div class="infra-page">
    <!-- 页头 -->
    <PageHeader title="文件列表">
      <a-button type="primary" @click="openForm">
        <template #icon><icon-upload /></template>
        上传文件
      </a-button>
      <a-button
        status="danger"
        :disabled="!checkedIds.length"
        @click="handleDeleteBatch"
        v-hasPermi="['infra:file:delete']"
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
            placeholder="文件名"
            allow-clear
            style="width: 200px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-input-search
            v-model="queryParams.type"
            placeholder="文件类型"
            allow-clear
            style="width: 180px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-range-picker
            v-model="queryParams.createTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            show-time
            style="width: 320px"
            @change="handleQuery"
          />
          <a-button @click="resetQuery">
            <template #icon><icon-refresh /></template>
            重置
          </a-button>
        </div>
      </div>

      <a-table
        class="infra-table"
        :data="list"
        :loading="loading"
        row-key="id"
        size="large"
        :pagination="false"
        :row-selection="{ type: 'checkbox', showCheckedAll: true }"
        v-model:selected-keys="checkedIds"
      >
        <template #columns>
          <!-- 文件名 + 存储路径合成一格：原先「文件名 / 路径 / URL」三列各占一份宽度，
               结果三列全被截成半截字符串，URL 那列尤其没意义 -->
          <a-table-column title="文件" :min-width="300">
            <template #cell="{ record }">
              <div class="bm-cell-main" :title="record.name">{{ record.name || '—' }}</div>
              <div v-if="record.path" class="bm-cell-sub" :title="record.path">
                {{ record.path }}
              </div>
            </template>
          </a-table-column>
          <a-table-column title="类型" :width="120" align="center">
            <template #cell="{ record }">
              <a-tooltip :content="record.type || '未知'">
                <a-tag size="small" :color="fileKind(record).color">
                  {{ fileKind(record).label }}
                </a-tag>
              </a-tooltip>
            </template>
          </a-table-column>
          <a-table-column title="大小" :width="100" align="right">
            <template #cell="{ record }">
              <span class="bm-cell-num">{{ formatFileSize(record.size) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="预览" :width="90" align="center">
            <template #cell="{ record }">
              <a-image
                v-if="fileKind(record).label === '图片'"
                :src="record.url"
                width="40"
                height="40"
                fit="cover"
              />
              <a-link v-else :href="record.url" target="_blank">打开</a-link>
            </template>
          </a-table-column>
          <a-table-column title="上传时间" :width="180" align="center">
            <template #cell="{ record }">
              <span class="cell-time">{{ fmtDateTime(record.createTime) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="160" align="center" fixed="right">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button type="text" size="small" @click="copyToClipboard(record.url)">
                  复制链接
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="handleDelete(record.id)"
                  v-hasPermi="['infra:file:delete']"
                >
                  删除
                </a-button>
              </div>
            </template>
          </a-table-column>
        </template>
        <template #empty>
          <EmptyState
            v-if="!loading"
            :icon="IconFolder"
            title="暂无文件"
            description="这里汇总所有经平台上传的文件，可按文件名、类型和时间检索"
          />
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

    <!-- 上传弹窗 -->
    <FileForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import { useClipboard } from '@vueuse/core'
import { formatFileSize } from '@/utils/file'
import * as FileApi from '@/api/infra/file'
import FileForm from './FileForm.vue'
import { IconUpload, IconDelete, IconRefresh, IconFolder } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'InfraFile' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  type: undefined as string | undefined,
  createTime: [] as string[]
})
const checkedIds = ref<number[]>([])

const fmtDateTime = (v: any) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '—')

/**
 * MIME → 人能读的类型标签。
 * 原样展示 MIME 时列宽只够显示「application/vnd...」，看不出是 Word 还是 Excel；
 * 完整 MIME 移到 tooltip 里，需要时仍能查。
 */
const KINDS: Array<{ mime: RegExp; ext: RegExp; label: string; color: string }> = [
  { mime: /wordprocessingml|msword/, ext: /^(docx?|rtf)$/, label: 'Word', color: 'arcoblue' },
  { mime: /spreadsheetml|ms-excel/, ext: /^(xlsx?|xlsm|csv)$/, label: 'Excel', color: 'green' },
  { mime: /presentationml|ms-powerpoint/, ext: /^pptx?$/, label: 'PPT', color: 'orange' },
  { mime: /pdf/, ext: /^pdf$/, label: 'PDF', color: 'red' },
  { mime: /^image\//, ext: /^(png|jpe?g|gif|bmp|webp|svg)$/, label: '图片', color: 'purple' },
  { mime: /zip|rar|7z|compressed/, ext: /^(zip|rar|7z|gz|tar)$/, label: '压缩包', color: 'gold' },
  { mime: /^text\/|json|xml/, ext: /^(txt|md|json|xml|ya?ml)$/, label: '文本', color: 'cyan' }
]
const fileKind = (record: any) => {
  // 后缀优先：同一种文件的 MIME 在不同上传入口可能是 application/octet-stream，
  // 只看 MIME 会让同为 .docx 的行一半标 Word、一半标 DOCX。
  const ext = String(record?.name || '')
    .split('.')
    .pop()!
    .toLowerCase()
  const mime = String(record?.type || '')
  const hit = KINDS.find((k) => k.ext.test(ext)) || KINDS.find((k) => k.mime.test(mime))
  if (hit) return hit
  return { label: ext && ext.length <= 5 ? ext.toUpperCase() : '其它', color: 'gray' }
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FileApi.getFilePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryParams.name = undefined
  queryParams.type = undefined
  queryParams.createTime = []
  checkedIds.value = []
  handleQuery()
}

/** 上传 */
const formRef = ref()
const openForm = () => {
  formRef.value.open()
}

/** 复制链接 */
const copyToClipboard = async (text: string) => {
  const { copy, copied, isSupported } = useClipboard({ legacy: true, source: text })
  if (!isSupported) {
    message.error(t('common.copyError'))
    return
  }
  await copy()
  if (unref(copied)) {
    message.success(t('common.copySuccess'))
  }
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await FileApi.deleteFile(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 批量删除 */
const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await FileApi.deleteFileList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.infra-page {
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

.op-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.infra-table {
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
}

.list-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
