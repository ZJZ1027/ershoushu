<template>
  <div class="infra-page">
    <PageHeader title="分类管理">
      <a-button type="primary" @click="openForm('create')" v-hasPermi="['business:category:create']">
        <template #icon><icon-plus /></template>新增
      </a-button>
    </PageHeader>
    <div class="list-card">
      <a-table :data="list" :loading="loading" row-key="id" :pagination="false">
        <template #columns>
          <a-table-column title="名称" data-index="name" />
          <a-table-column title="排序" data-index="sort" :width="80" />
          <a-table-column title="状态" :width="90">
            <template #cell="{ record }">
              <a-tag :color="record.status === 0 ? 'green' : 'red'">{{ record.status === 0 ? '正常' : '停用' }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="160">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="openForm('update', record.id)" v-hasPermi="['business:category:update']">编辑</a-button>
              <a-button type="text" size="small" status="danger" @click="handleDelete(record.id)" v-hasPermi="['business:category:delete']">删除</a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <div class="list-pager">
        <a-pagination :total="total" v-model:current="queryParams.pageNo" v-model:page-size="queryParams.pageSize"
                      show-total @change="getList" @page-size-change="handleQuery" />
      </div>
    </div>
    <a-modal v-model:visible="visible" :title="formType === 'create' ? '新增分类' : '编辑分类'" @ok="submit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="名称"><a-input v-model="form.name" /></a-form-item>
        <a-form-item label="排序"><a-input-number v-model="form.sort" :min="0" /></a-form-item>
        <a-form-item label="状态">
          <a-select v-model="form.status">
            <a-option :value="0">正常</a-option>
            <a-option :value="1">停用</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { IconPlus } from '@arco-design/web-vue/es/icon'
import { createCategory, deleteCategory, getCategory, getCategoryPage, updateCategory } from '@/api/business/book'
defineOptions({ name: 'BusinessCategory' })
const message = useMessage()
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 20, name: undefined as string | undefined })
const visible = ref(false)
const formType = ref('create')
const form = ref({ id: undefined as number | undefined, name: '', sort: 0, status: 0 })
const getList = async () => {
  loading.value = true
  try {
    const data = await getCategoryPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}
const handleQuery = () => { queryParams.pageNo = 1; getList() }
const openForm = async (type: string, id?: number) => {
  formType.value = type
  form.value = { id: undefined, name: '', sort: 0, status: 0 }
  if (id) form.value = await getCategory(id)
  visible.value = true
}
const submit = async () => {
  if (formType.value === 'create') await createCategory(form.value)
  else await updateCategory(form.value)
  message.success('保存成功')
  visible.value = false
  await getList()
}
const handleDelete = async (id: number) => {
  await message.delConfirm()
  await deleteCategory(id)
  message.success('删除成功')
  await getList()
}
onMounted(getList)
</script>
<style lang="scss" scoped>
.infra-page { display: flex; flex-direction: column; gap: 16px; }
.list-card { padding: 16px 20px 20px; background: var(--color-bg-2, #fff); border: 1px solid var(--color-border-2, #e5e6eb); border-radius: 8px; }
.list-pager { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
