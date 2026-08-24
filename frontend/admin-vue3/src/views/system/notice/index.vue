<template>
  <div class="infra-page">
    <!-- 页头 -->
    <PageHeader title="通知公告">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:notice:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
    </PageHeader>

    <!-- 列表卡 -->
    <div class="list-card">
      <!-- 工具栏 -->
      <div class="list-toolbar">
        <div class="toolbar-left">
          <a-input-search
            v-model="queryParams.title"
            placeholder="公告标题"
            allow-clear
            style="width: 220px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-select
            v-model="queryParams.type"
            placeholder="公告类型"
            allow-clear
            style="width: 160px"
            @change="handleQuery"
          >
            <a-option
              v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_NOTICE_TYPE)"
              :key="dict.value"
              :value="dict.value"
              :label="dict.label"
            />
          </a-select>
          <a-select
            v-model="queryParams.status"
            placeholder="公告状态"
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
        :scroll="{ x: 800 }"
      >
        <template #columns>
          <a-table-column title="编号" data-index="id" :width="80" align="center" />
          <!-- 标题是唯一的正文列：不写宽度也不写 :min-width，靠 ellipsis tooltip 兜底，
               让它吃掉容器里所有定宽列之外的余量，窄窗口一起收缩而不是撑出滚动条。 -->
          <a-table-column title="公告标题" data-index="title" ellipsis tooltip />
          <a-table-column title="公告类型" :width="110" align="center">
            <template #cell="{ record }">
              <DictTag :type="DICT_TYPE.SYSTEM_NOTICE_TYPE" :value="record.type" />
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="90" align="center">
            <template #cell="{ record }">
              <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="record.status" />
            </template>
          </a-table-column>
          <a-table-column title="创建时间" :width="100" align="center">
            <template #cell="{ record }">
              <TimeCell :value="record.createTime" />
            </template>
          </a-table-column>
          <!-- 两个 small 文字按钮：132 才放得下，写窄了单元格不裁剪按钮，
               .arco-table-content 会恒比容器宽一点，浏览器全程画一条横向滚动条。 -->
          <a-table-column title="操作" :width="132" align="center">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['system:notice:update']"
                >
                  编辑
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="handleDelete(record.id)"
                  v-hasPermi="['system:notice:delete']"
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

    <!-- 表单弹窗：添加/修改 -->
    <NoticeForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as NoticeApi from '@/api/system/notice'
import NoticeForm from './NoticeForm.vue'
import { IconPlus, IconRefresh } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemNotice' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: undefined as string | undefined,
  type: undefined as number | undefined,
  status: undefined as number | undefined
})

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await NoticeApi.getNoticePage(queryParams)
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
  queryParams.title = undefined
  queryParams.type = undefined
  queryParams.status = undefined
  handleQuery()
}

/** 添加/修改 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await NoticeApi.deleteNotice(id)
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
