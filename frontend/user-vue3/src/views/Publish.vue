<template>
  <div class="publish-page">
    <header class="publish-head">
      <button type="button" class="publish-back" @click="router.push('/mine')">
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" /></svg>
        返回
      </button>
      <div>
        <p class="publish-kicker">{{ isEdit ? '编辑书籍' : '发布闲置' }}</p>
        <h1 class="publish-title">{{ isEdit ? '修改书籍信息' : '卖一本二手书' }}</h1>
        <p class="publish-sub">首图将作为封面 · 可先保存草稿，资料齐全后再提交审核</p>
      </div>
    </header>

    <div class="publish-card">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" class="publish-form">
        <!-- 图片 -->
        <section class="publish-section">
          <div class="section-head">
            <h2 class="section-title">宝贝图片</h2>
            <span class="section-hint">{{ form.imageUrls.length ? `已选 ${form.imageUrls.length} 张` : '最多上传 9 张' }}</span>
          </div>
          <input
            ref="fileInputRef"
            class="file-input-hidden"
            type="file"
            accept="image/*"
            multiple
            @change="onFiles"
          />
          <div class="photo-grid">
            <div
              v-for="(u, idx) in form.imageUrls"
              :key="u + '-' + idx"
              class="photo-item"
              :class="{ cover: idx === 0 }"
            >
              <img :src="fileUrl(u)" alt="" />
              <span v-if="idx === 0" class="photo-cover-tag">封面</span>
              <button type="button" class="photo-remove" title="删除" @click="removeImage(idx)">×</button>
            </div>
            <button
              v-if="form.imageUrls.length < 9"
              type="button"
              class="photo-add"
              :disabled="uploading"
              @click="fileInputRef?.click()"
            >
              <span class="photo-add-icon">+</span>
              <span>{{ uploading ? '上传中…' : '添加图片' }}</span>
            </button>
          </div>
        </section>

        <!-- 标题 -->
        <section class="publish-section">
          <a-form-item field="title" hide-label class="publish-field-plain">
            <input
              v-model="form.title"
              class="title-input"
              type="text"
              maxlength="80"
              placeholder="标题：书名 / 版本 / 亮点，吸引同学点击"
            />
          </a-form-item>
        </section>

        <!-- 分类 -->
        <section class="publish-section">
          <h2 class="section-title">分类</h2>
          <div class="chip-row">
            <button
              v-for="c in categories"
              :key="c.id"
              type="button"
              class="chip"
              :class="{ active: form.categoryId === c.id }"
              @click="form.categoryId = form.categoryId === c.id ? undefined : c.id"
            >
              {{ c.name }}
            </button>
          </div>
        </section>

        <!-- 价格 -->
        <section class="publish-section">
          <h2 class="section-title">价格</h2>
          <div class="price-row">
            <label class="price-field">
              <span class="price-label">售价</span>
              <div class="price-input-wrap">
                <span class="price-unit">¥</span>
                <a-input-number
                  v-model="form.price"
                  :min="0"
                  :precision="2"
                  placeholder="0.00"
                  hide-button
                  class="price-input"
                />
              </div>
            </label>
            <label class="price-field secondary">
              <span class="price-label">原价</span>
              <div class="price-input-wrap">
                <span class="price-unit muted">¥</span>
                <a-input-number
                  v-model="form.originPrice"
                  :min="0"
                  :precision="2"
                  placeholder="选填"
                  hide-button
                  class="price-input"
                />
              </div>
            </label>
          </div>
        </section>

        <!-- 成色 -->
        <section class="publish-section">
          <h2 class="section-title">成色</h2>
          <div class="chip-row">
            <button
              v-for="d in conditions"
              :key="d.value"
              type="button"
              class="chip"
              :class="{ active: form.conditionCode === d.value }"
              @click="form.conditionCode = d.value"
            >
              {{ d.label }}
            </button>
          </div>
        </section>

        <!-- 交易 -->
        <section class="publish-section">
          <h2 class="section-title">面交信息</h2>
          <div class="chip-row campus-row">
            <button
              v-for="d in campuses"
              :key="d.value"
              type="button"
              class="chip"
              :class="{ active: form.campus === d.value }"
              @click="form.campus = d.value"
            >
              {{ d.label }}
            </button>
          </div>
          <a-form-item field="meetupPlace" hide-label class="publish-field-plain meetup-field">
            <input
              v-model="form.meetupPlace"
              class="plain-input"
              type="text"
              placeholder="面交地点，如 一食堂门口 / 图书馆大厅"
            />
          </a-form-item>
        </section>

        <!-- 描述 -->
        <section class="publish-section">
          <div class="section-head">
            <h2 class="section-title">书籍描述</h2>
            <span class="section-hint">{{ (form.description || '').length }}/500</span>
          </div>
          <a-form-item field="description" hide-label class="publish-field-plain">
            <textarea
              v-model="form.description"
              class="desc-input"
              maxlength="500"
              rows="5"
              placeholder="描述成色、笔记、缺页、是否含习题册等，让买家更放心"
            />
          </a-form-item>
        </section>

        <!-- 补充信息 -->
        <section class="publish-section publish-section-last">
          <button type="button" class="extra-toggle" @click="showExtra = !showExtra">
            <span>补充图书信息（选填）</span>
            <svg viewBox="0 0 24 24" aria-hidden="true" :class="{ open: showExtra }">
              <path d="M6 9l6 6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
          </button>
          <div v-show="showExtra" class="extra-grid">
            <a-form-item label="作者" field="author" class="publish-field-compact">
              <a-input v-model="form.author" placeholder="作者" allow-clear />
            </a-form-item>
            <a-form-item label="出版社" field="publisher" class="publish-field-compact">
              <a-input v-model="form.publisher" placeholder="出版社" allow-clear />
            </a-form-item>
            <a-form-item label="ISBN" field="isbn" class="publish-field-compact">
              <a-input v-model="form.isbn" placeholder="ISBN" allow-clear />
            </a-form-item>
            <a-form-item label="课程名" field="courseName" class="publish-field-compact">
              <a-input v-model="form.courseName" placeholder="对应课程" allow-clear />
            </a-form-item>
            <a-form-item label="专业" field="majorName" class="publish-field-compact">
              <a-input v-model="form.majorName" placeholder="适用专业" allow-clear />
            </a-form-item>
          </div>
        </section>
      </a-form>
    </div>

    <footer class="publish-bar">
      <div class="publish-bar-inner">
        <p class="publish-bar-tip">保存仅存为草稿；提交需完整信息并进入审核</p>
        <div class="publish-bar-actions">
          <a-button size="large" @click="router.push('/mine')">取消</a-button>
          <a-button v-if="form.status !== 1" size="large" :loading="saving" @click="persist(false)">存草稿</a-button>
          <a-button type="primary" size="large" :loading="submitting" @click="persist(true)">
            {{ isEdit ? '保存并提交' : '发布' }}
          </a-button>
        </div>
      </div>
    </footer>
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
const showExtra = ref(false)
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
  const remain = 9 - form.imageUrls.length
  if (remain <= 0) {
    Message.warning('最多上传 9 张图片')
    input.value = ''
    return
  }
  uploading.value = true
  try {
    for (const file of files.slice(0, remain)) {
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
    restoreDraft()
    if (form.author || form.publisher || form.isbn || form.courseName || form.majorName) {
      showExtra.value = true
    }
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
.publish-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 20px 120px;
  animation: page-in 0.45s ease both;
}

.publish-head {
  margin-bottom: 20px;
}

.publish-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 14px;
  padding: 6px 10px 6px 6px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--muted);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease;
}

.publish-back svg {
  width: 18px;
  height: 18px;
}

.publish-back:hover {
  color: var(--teal-deep);
  background: var(--teal-soft);
}

.publish-kicker {
  margin: 0 0 6px;
  color: var(--teal);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.1em;
}

.publish-title {
  margin: 0 0 6px;
  font-family: var(--font-body);
  font-size: clamp(1.5rem, 3vw, 1.85rem);
  font-weight: 750;
  letter-spacing: -0.02em;
}

.publish-sub {
  margin: 0;
  color: var(--muted);
  font-size: 0.92rem;
}

.publish-card {
  background: var(--surface-raised);
  border: 1px solid var(--line);
  border-radius: calc(var(--radius) + 2px);
  box-shadow: var(--shadow-soft);
  overflow: hidden;
}

.publish-section {
  padding: 20px 22px;
  border-bottom: 1px solid rgba(213, 224, 217, 0.65);
}

.publish-section-last {
  border-bottom: none;
}

.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-title {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 650;
  color: var(--ink);
}

.section-head .section-title {
  margin-bottom: 0;
}

.section-hint {
  color: var(--muted);
  font-size: 12px;
}

.file-input-hidden {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.photo-item,
.photo-add {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
}

.photo-item {
  border: 1px solid var(--line);
  background: #f2f5f3;
}

.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.photo-item.cover {
  box-shadow: inset 0 0 0 2px var(--teal);
}

.photo-cover-tag {
  position: absolute;
  left: 6px;
  bottom: 6px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(13, 107, 88, 0.88);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.photo-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(20, 35, 28, 0.62);
  color: #fff;
  font-size: 16px;
  line-height: 22px;
  cursor: pointer;
}

.photo-remove:hover {
  background: var(--danger);
}

.photo-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1.5px dashed #b9cfc5;
  background: #f8fbf9;
  color: var(--muted);
  font-size: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.photo-add:hover:not(:disabled) {
  border-color: var(--teal);
  color: var(--teal-deep);
  background: var(--teal-soft);
}

.photo-add:disabled {
  opacity: 0.65;
  cursor: wait;
}

.photo-add-icon {
  font-size: 28px;
  line-height: 1;
  color: var(--teal);
  font-weight: 300;
}

.publish-field-plain :deep(.arco-form-item-message) {
  margin-top: 6px;
}

.title-input {
  width: 100%;
  padding: 4px 0;
  border: none;
  background: transparent;
  color: var(--ink);
  font-family: var(--font-body);
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
  outline: none;
}

.title-input::placeholder {
  color: #9aaba2;
  font-weight: 500;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  padding: 8px 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--ink-soft);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.chip:hover {
  border-color: #b9cfc5;
  color: var(--teal-deep);
}

.chip.active {
  border-color: var(--teal);
  background: var(--teal-soft);
  color: var(--teal-deep);
  font-weight: 600;
}

.price-row {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 14px;
}

.price-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-label {
  font-size: 13px;
  color: var(--muted);
}

.price-input-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fafcfb;
}

.price-field:not(.secondary) .price-input-wrap {
  border-color: rgba(13, 107, 88, 0.28);
  background: linear-gradient(180deg, #fff 0%, #f4faf7 100%);
}

.price-unit {
  color: var(--price);
  font-size: 20px;
  font-weight: 700;
}

.price-unit.muted {
  color: var(--muted);
  font-size: 16px;
}

.price-input {
  flex: 1;
}

.price-input :deep(.arco-input-number) {
  width: 100%;
  background: transparent;
  border: none;
  box-shadow: none;
}

.price-input :deep(.arco-input-number-input) {
  padding: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--price);
}

.price-field.secondary .price-input :deep(.arco-input-number-input) {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
}

.meetup-field {
  margin-top: 14px;
}

.plain-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fafcfb;
  color: var(--ink);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.plain-input:focus {
  border-color: var(--teal);
  box-shadow: 0 0 0 3px rgba(13, 107, 88, 0.12);
}

.desc-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fafcfb;
  color: var(--ink);
  font-size: 14px;
  line-height: 1.65;
  resize: vertical;
  min-height: 120px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.desc-input:focus {
  border-color: var(--teal);
  box-shadow: 0 0 0 3px rgba(13, 107, 88, 0.12);
}

.extra-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--ink-soft);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.extra-toggle svg {
  width: 18px;
  height: 18px;
  transition: transform 0.2s ease;
}

.extra-toggle svg.open {
  transform: rotate(180deg);
}

.extra-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 14px;
  margin-top: 16px;
}

.publish-field-compact :deep(.arco-form-item-label) {
  font-size: 13px;
  color: var(--muted);
}

.publish-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 30;
  border-top: 1px solid rgba(213, 224, 217, 0.95);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
}

.publish-bar-inner {
  max-width: 720px;
  margin: 0 auto;
  padding: 12px 20px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.publish-bar-tip {
  margin: 0;
  flex: 1;
  min-width: 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.4;
}

.publish-bar-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.publish-bar-actions :deep(.arco-btn-primary) {
  min-width: 108px;
  font-weight: 650;
}

@media (max-width: 640px) {
  .publish-page {
    padding-left: 14px;
    padding-right: 14px;
    padding-bottom: 140px;
  }

  .publish-section {
    padding: 16px;
  }

  .photo-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .price-row,
  .extra-grid {
    grid-template-columns: 1fr;
  }

  .publish-bar-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .publish-bar-tip {
    text-align: center;
  }

  .publish-bar-actions {
    width: 100%;
  }

  .publish-bar-actions :deep(.arco-btn) {
    flex: 1;
  }
}
</style>
