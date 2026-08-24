<template>
  <div class="infra-page">
    <!-- 页头 -->
    <PageHeader title="岗位管理">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['system:post:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['system:post:export']"
      >
        <template #icon><icon-download /></template>
        导出
      </a-button>
      <a-button
        status="danger"
        :disabled="!checkedIds.length"
        @click="handleDeleteBatch"
        v-hasPermi="['system:post:delete']"
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
            placeholder="岗位名称"
            allow-clear
            style="width: 200px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-input-search
            v-model="queryParams.code"
            placeholder="岗位编码"
            allow-clear
            style="width: 200px"
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
        :scroll="{ x: 800 }"
      >
        <template #columns>
          <a-table-column title="编号" data-index="id" :width="80" align="center" />
          <!-- 名称与编码都是「有多少给多少」的截断列：不写宽度，均分容器余量，窄窗口一起收缩。
               每列都写死宽度才是横向滚动条的来源，也别给它们补 :min-width（scroll.x 是数字时
               min-width 会生效，等于把刚性下限又垒回来）。 -->
          <a-table-column title="岗位名称" data-index="name" ellipsis tooltip />
          <a-table-column title="岗位编码" data-index="code" ellipsis tooltip />
          <a-table-column title="排序" data-index="sort" :width="80" align="center" />
          <!-- 备注反而给固定宽：三列都弹性会被均分，岗位编码（真正要复制的串）就从「刚好放得下」
               掉到被截断。备注是「悬浮才读」的散文列，把它钉住、余量让给名称与编码更划算。 -->
          <a-table-column title="备注" data-index="remark" :width="160" ellipsis tooltip>
            <template #cell="{ record }">
              <span :class="record.remark ? '' : 'bm-cell-empty'">{{ record.remark || '—' }}</span>
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
          <!-- 132 才放得下「编辑 + 删除」：两个 small 文字按钮各 60px + 2px 间隙 = 122px，
               写 120 就会右溢 1px，浏览器全程画一条横向滚动条。也不加 fixed="right"：
               Arco 给固定列无条件挂一条内阴影，表格不溢出时那就是「右边还有内容」的假信号。 -->
          <a-table-column title="操作" :width="132" align="center">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['system:post:update']"
                >
                  编辑
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="handleDelete(record.id)"
                  v-hasPermi="['system:post:delete']"
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
    <PostForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import download from '@/utils/download'
import * as PostApi from '@/api/system/post'
import PostForm from './PostForm.vue'
import { IconPlus, IconDownload, IconDelete, IconRefresh } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'SystemPost' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  code: undefined as string | undefined,
  status: undefined as number | undefined
})
const exportLoading = ref(false)
const checkedIds = ref<number[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PostApi.getPostPage(queryParams)
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
  queryParams.code = undefined
  queryParams.status = undefined
  checkedIds.value = []
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
    await PostApi.deletePost(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 批量删除 */
const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await PostApi.deletePostList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 导出 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await PostApi.exportPost(queryParams)
    download.excel(data, '岗位列表.xlsx')
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
