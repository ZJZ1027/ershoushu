<template>
  <div class="page" style="max-width:640px">
    <h1 class="page-title">{{ isEdit ? '编辑书籍' : '发布二手书' }}</h1>
    <p class="page-sub">可先保存草稿，资料齐全后再提交审核。</p>
    <div class="panel">
    <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
      <a-form-item label="书名" field="title" required>
        <a-input v-model="form.title" placeholder="请输入书名" allow-clear />
      </a-form-item>
      <a-form-item label="分类" field="categoryId">
        <a-select v-model="form.categoryId" placeholder="请选择分类" allow-clear>
          <a-option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="作者" field="author">
        <a-input v-model="form.author" placeholder="请输入作者" allow-clear />
      </a-form-item>
      <a-form-item label="出版社" field="publisher">
        <a-input v-model="form.publisher" placeholder="请输入出版社" allow-clear />
      </a-form-item>
      <a-form-item label="ISBN" field="isbn">
        <a-input v-model="form.isbn" placeholder="请输入 ISBN" allow-clear />
      </a-form-item>
      <a-form-item label="售价" field="price">
        <a-input-number v-model="form.price" :min="0" :precision="2" placeholder="提交前必填" style="width:100%" />
      </a-form-item>
      <a-form-item label="原价" field="originPrice">
        <a-input-number v-model="form.originPrice" :min="0" :precision="2" placeholder="选填" style="width:100%" />
      </a-form-item>
      <a-form-item label="成色" field="conditionCode">
        <a-select v-model="form.conditionCode" placeholder="请选择成色">
          <a-option v-for="d in conditions" :key="d.value" :value="d.value">{{ d.label }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="校区" field="campus">
        <a-select v-model="form.campus" placeholder="请选择校区" allow-clear>
          <a-option v-for="d in campuses" :key="d.value" :value="d.value">{{ d.label }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item label="面交地点" field="meetupPlace">
        <a-input v-model="form.meetupPlace" placeholder="如 一食堂门口" allow-clear />
      </a-form-item>
      <a-form-item label="课程名" field="courseName">
        <a-input v-model="form.courseName" placeholder="教材对应课程，选填" allow-clear />
      </a-form-item>
      <a-form-item label="专业" field="majorName">
        <a-input v-model="form.majorName" placeholder="选填" allow-clear />
      </a-form-item>
      <a-form-item label="描述" field="description">
        <a-textarea v-model="form.description" placeholder="成色、笔记、缺页等说明" :auto-size="{ minRows: 3 }" />
      </a-form-item>
      <a-form-item label="封面 / 图片">
        <div class="upload-row">
          <input
            ref="fileInputRef"
            class="file-input-hidden"
            type="file"
            accept="image/*"
            multiple
            @change="onFiles"
          />
          <a-button :loading="uploading" @click="fileInputRef?.click()">选择图片</a-button>
          <span class="muted">{{ form.imageUrls.length ? `已选择 ${form.imageUrls.length} 张` : '未选择图片' }}</span>
        </div>
        <div class="img-list">
          <div v-for="(u, idx) in form.imageUrls" :key="u + '-' + idx" class="img-item">
            <img :src="fileUrl(u)" alt="" />
            <button type="button" class="img-remove" title="删除" @click="removeImage(idx)">×</button>
          </div>
        </div>
      </a-form-item>
    </a-form>
    </div>
    <div class="publish-footer">
      <a-button @click="router.push('/mine')">取消</a-button>
      <a-button v-if="form.status !== 1" :loading="saving" @click="persist(false)">保存</a-button>
      <a-button type="primary" :loading="submitting" @click="persist(true)">提交</a-button>
    </div>
    <p class="muted publish-tip">保存仅存为草稿；提交需完整信息并进入审核</p>
  </div>
</template>
<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { getBook, getCategories, getDict, publishBook, updateBook, uploadFile } from '@/api'
import { fileUrl } from '@/api/http'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const isEdit = !!route.query.id
const bookId = ref<number | undefined>(isEdit ? Number(route.query.id) : undefined)
const saving = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const formRef = ref()
const fileInputRef = ref<HTMLInputElement>()
const categories = ref<any[]>([])
const campuses = ref<any[]>([])
const conditions = ref<any[]>([])
const form = reactive<any>({
  title: '',
  categoryId: undefined,
  author: '',
  publisher: '',
  isbn: '',
  price: undefined,
  originPrice: undefined,
  conditionCode: 'used',
  campus: '本部',
  meetupPlace: '',
  courseName: '',
  majorName: '',
  description: '',
  coverUrl: '',
  imageUrls: [] as string[],
  status: undefined as number | undefined
})
const rules = {
  title: [{ required: true, message: '请输入书名' }]
}

/** 新建发布的本地草稿（按用户区分）；已提交/保存成功后清除 */
const draftKey = () => {
  const uid = user.profile?.id || 'anon'
  return bookId.value ? `publishDraft:edit:${uid}:${bookId.value}` : `publishDraft:new:${uid}`
}

const DRAFT_FIELDS = [
  'title',
  'categoryId',
  'author',
  'publisher',
  'isbn',
  'price',
  'originPrice',
  'conditionCode',
  'campus',
  'meetupPlace',
  'courseName',
  'majorName',
  'description',
  'coverUrl',
  'imageUrls',
  'status'
] as const

let draftReady = false
let draftTimer: ReturnType<typeof setTimeout> | undefined

const snapshotForm = () => {
  const data: Record<string, unknown> = {}
  for (const key of DRAFT_FIELDS) {
    data[key] = form[key]
  }
  return data
}

const saveDraft = () => {
  if (!draftReady) return
  try {
    sessionStorage.setItem(draftKey(), JSON.stringify(snapshotForm()))
  } catch {
    /* 配额满等忽略 */
  }
}

const scheduleSaveDraft = () => {
  if (!draftReady) return
  if (draftTimer) clearTimeout(draftTimer)
  draftTimer = setTimeout(saveDraft, 200)
}

const clearDraft = () => {
  try {
    sessionStorage.removeItem(draftKey())
    // 新建发布成功后也清掉「new」键，防止保存草稿生成 id 前后键不一致残留
    const uid = user.profile?.id || 'anon'
    sessionStorage.removeItem(`publishDraft:new:${uid}`)
  } catch {
    /* ignore */
  }
}

const restoreDraft = () => {
  try {
    const raw = sessionStorage.getItem(draftKey())
    if (!raw) return false
    const data = JSON.parse(raw)
    if (!data || typeof data !== 'object') return false
    for (const key of DRAFT_FIELDS) {
      if (key in data) form[key] = data[key]
    }
    if (!Array.isArray(form.imageUrls)) form.imageUrls = []
    syncCover()
    return true
  } catch {
    return false
  }
}

const syncCover = () => {
  form.coverUrl = form.imageUrls[0] || ''
}

const onFiles = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const files = Array.from(input.files || [])
  if (!files.length) return
  uploading.value = true
  try {
    for (const file of files) {
      const url = await uploadFile(file)
      form.imageUrls.push(url)
    }
    syncCover()
  } finally {
    uploading.value = false
    input.value = ''
  }
}

const removeImage = (idx: number) => {
  form.imageUrls.splice(idx, 1)
  syncCover()
}

const buildPayload = (submit: boolean) => ({
  title: form.title,
  categoryId: form.categoryId,
  author: form.author,
  publisher: form.publisher,
  isbn: form.isbn,
  price: form.price,
  originPrice: form.originPrice,
  conditionCode: form.conditionCode,
  campus: form.campus,
  meetupPlace: form.meetupPlace,
  courseName: form.courseName,
  majorName: form.majorName,
  description: form.description,
  coverUrl: form.coverUrl,
  imageUrls: form.imageUrls,
  submit
})

const validateBefore = async (submit: boolean) => {
  const errors = await formRef.value?.validate()
  if (errors) {
    Message.warning('请先填写书名')
    return false
  }
  if (!submit) return true
  if (form.price == null || Number(form.price) < 0) {
    Message.warning('请填写有效售价后再提交')
    return false
  }
  if (!form.imageUrls?.length && !form.coverUrl) {
    Message.warning('请上传封面或图片后再提交')
    return false
  }
  if (!form.campus) {
    Message.warning('请选择校区后再提交')
    return false
  }
  if (!String(form.meetupPlace || '').trim()) {
    Message.warning('请填写面交地点后再提交')
    return false
  }
  return true
}

const persist = async (submit: boolean) => {
  if (!(await validateBefore(submit))) return
  const loading = submit ? submitting : saving
  loading.value = true
  try {
    const payload = buildPayload(submit)
    if (bookId.value) {
      await updateBook(bookId.value, payload)
    } else {
      const id = await publishBook(payload)
      bookId.value = Number(id)
    }
    clearDraft()
    Message.success(submit ? '已提交审核' : '已保存草稿')
    router.push('/mine')
  } catch {
    /* 错误已由 axios 拦截器提示 */
  } finally {
    loading.value = false
  }
}

watch(form, scheduleSaveDraft, { deep: true })

onMounted(async () => {
  if (!user.profile) {
    try {
      await user.loadProfile()
    } catch {
      /* ignore */
    }
  }
  categories.value = await getCategories()
  campuses.value = await getDict('campus')
  conditions.value = await getDict('book_condition')
  if (bookId.value) {
    const data = await getBook(bookId.value)
    Object.assign(form, data)
    if (!Array.isArray(form.imageUrls)) form.imageUrls = []
    // 编辑页：若有未提交的本地改动，覆盖服务端快照
    restoreDraft()
  } else {
    restoreDraft()
  }
  draftReady = true
})

onUnmounted(() => {
  if (draftTimer) clearTimeout(draftTimer)
  saveDraft()
})
</script>
<style scoped>
.publish-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}
.publish-tip {
  margin-top: 8px;
  text-align: right;
}
.upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.file-input-hidden {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}
.img-list {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.img-item {
  position: relative;
  width: 72px;
  height: 72px;
}
.img-item img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
  background: #f2f3f5;
}
.img-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(29, 33, 41, 0.72);
  color: #fff;
  font-size: 14px;
  line-height: 18px;
  cursor: pointer;
}
.img-remove:hover {
  background: #f53f3f;
}
</style>
