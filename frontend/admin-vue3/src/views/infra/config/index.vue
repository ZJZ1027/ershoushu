<template>
  <div class="infra-page">
    <!-- 页头 -->
    <PageHeader title="参数配置">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['infra:config:create']">
        <template #icon><icon-plus /></template>
        新增
      </a-button>
      <a-button
        status="success"
        :loading="exportLoading"
        @click="handleExport"
        v-hasPermi="['infra:config:export']"
      >
        <template #icon><icon-download /></template>
        导出
      </a-button>
      <a-button
        status="danger"
        :disabled="!checkedIds.length"
        @click="handleDeleteBatch"
        v-hasPermi="['infra:config:delete']"
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
            placeholder="参数名称"
            allow-clear
            style="width: 200px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-input-search
            v-model="queryParams.configKey"
            placeholder="参数键名"
            allow-clear
            style="width: 200px"
            @search="handleQuery"
            @press-enter="handleQuery"
            @clear="handleQuery"
          />
          <a-select
            v-model="queryParams.type"
            placeholder="系统内置"
            allow-clear
            style="width: 140px"
            @change="handleQuery"
          >
            <a-option
              v-for="dict in getIntDictOptions(DICT_TYPE.INFRA_CONFIG_TYPE)"
              :key="dict.value"
              :value="dict.value"
              :label="dict.label"
            />
          </a-select>
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
        :scroll="{ x: 800 }"
      >
        <!-- scroll.x 给一个「够小的数字」而不是 '100%'：Arco 会把它当表格最小宽度，
             容器更宽时靠自带的 min-width:100% 撑满，容器窄到 800 以下才真的横向滚
             （对应约 920px 视口，已在支持范围外）。这样列不会无限压缩成一堆省略号。 -->
        <template #columns>
          <!-- 名称 + 分类·键名合成一格：键名才是运维真正要复制的东西，分类只是分组线索，
               并进副行省掉一整列，名称也才拿得到最多空间 -->
          <a-table-column title="参数">
            <template #cell="{ record }">
              <div class="bm-cell-main" :title="record.name">{{ record.name || '—' }}</div>
              <div class="bm-cell-sub">
                <template v-if="record.category">
                  <span class="cfg-cat">{{ record.category }}</span>
                  <em>·</em>
                </template>
                <span class="cfg-key" :title="record.configKey">{{ record.configKey }}</span>
              </div>
            </template>
          </a-table-column>
          <!-- 参数与键值不写宽度：它们是「有多少给多少」的截断列，均分容器余量，窄窗口一起收缩。
               每列都写死宽度才是横向滚动条的来源（本页原先九列八个定宽，加起来 1250px 的刚性下限，
               而 1280 视口的容器只有 986px）。
               ⚠ 别给它们加 :min-width：scroll.x 是数字时 min-width 会生效，等于把刚性下限又垒回来。
               （另：scroll.x='100%' 时 Arco 反而完全忽略 min-width，实测写 220/180/160 与全不写
               六档宽度一模一样，所以那种写法下它也只是障眼法。） -->
          <a-table-column title="参数键值" data-index="configValue" ellipsis tooltip>
            <template #cell="{ record }">
              <span :class="record.configValue ? 'bm-cell-main' : 'bm-cell-empty'">
                {{ record.configValue || '—' }}
              </span>
            </template>
          </a-table-column>
          <!-- 备注反而给固定宽：三列都弹性会被均分，键名（运维真正要复制的串）就从「刚好放得下」
               掉到被截断。备注是「悬浮才读」的散文列，把它钉住、余量让给参数与键值更划算。 -->
          <a-table-column title="备注" data-index="remark" :width="160" ellipsis tooltip>
            <template #cell="{ record }">
              <span :class="record.remark ? '' : 'bm-cell-empty'">{{ record.remark || '—' }}</span>
            </template>
          </a-table-column>
          <!-- 来源与可见都是「这行是什么性质」的旁注，各占一列不值得：合成一列两行。
               只有「系统内置」值得一个标签；自定义是常态，整列刷色毫无信息量。 -->
          <a-table-column title="属性" :width="96" align="center">
            <template #cell="{ record }">
              <a-tag v-if="Number(record.type) === 1" color="red" size="small">系统内置</a-tag>
              <span v-else class="bm-cell-empty">自定义</span>
              <div class="bm-cell-sub cfg-vis">{{ record.visible ? '可见' : '不可见' }}</div>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" :width="100" align="center">
            <template #cell="{ record }">
              <TimeCell :value="record.createTime" />
            </template>
          </a-table-column>
          <!-- 宽度必须放得下「编辑 + 删除」：两个 small 文字按钮各 60px + 2px 间隙 = 122px，
               原来写 120px，.op-cell 这个 flex 就右溢 1px —— 单元格不裁剪按钮，于是
               .arco-table-content 的 scrollWidth 恒比 clientWidth 多 1，浏览器全程画一条
               横向滚动条（滑块几乎占满轨道，看着像条灰带）。以前整表溢出几百 px，这 1px 被埋着看不出来。
               不再 fixed="right"：Arco 给固定列无条件挂一条 10px 内阴影（不判断是否真能横向滚动），
               表格已经不会溢出，那条阴影就成了「右边还有内容」的假信号。 -->
          <a-table-column title="操作" :width="132" align="center">
            <template #cell="{ record }">
              <div class="op-cell">
                <a-button
                  type="text"
                  size="small"
                  @click="openForm('update', record.id)"
                  v-hasPermi="['infra:config:update']"
                >
                  编辑
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  status="danger"
                  @click="handleDelete(record.id)"
                  v-hasPermi="['infra:config:delete']"
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
    <ConfigForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import download from '@/utils/download'
import * as ConfigApi from '@/api/infra/config'
import ConfigForm from './ConfigForm.vue'
import { IconPlus, IconDownload, IconDelete, IconRefresh } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'InfraConfig' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref<any[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  configKey: undefined as string | undefined,
  type: undefined as number | undefined,
  createTime: [] as string[]
})
const exportLoading = ref(false)
const checkedIds = ref<number[]>([])

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ConfigApi.getConfigPage(queryParams)
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
  queryParams.configKey = undefined
  queryParams.type = undefined
  queryParams.createTime = []
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
    await ConfigApi.deleteConfig(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 批量删除 */
const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await ConfigApi.deleteConfigList(checkedIds.value)
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
    const data = await ConfigApi.exportConfig(queryParams)
    download.excel(data, '参数配置.xlsx')
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

/* 参数副行：分类不参与压缩（它短且是分组线索），键名吃掉余量并在末尾截断 */
.cfg-cat {
  flex: none;
  max-width: 92px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cfg-key {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 属性列是居中列，而 .bm-cell-sub 是 flex，默认贴左，得显式居中 */
.cfg-vis {
  justify-content: center;
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
