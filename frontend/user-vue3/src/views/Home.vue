<template>
  <div class="home">
    <section class="search-hero">
      <div class="search-hero-inner">
        <div class="search-wrap" @focusin="onSearchFocus" @focusout="onSearchBlur">
          <form class="search-bar" @submit.prevent="doSearch">
            <input
              v-model="keywordInput"
              class="search-input"
              type="search"
              enterkeyhint="search"
              placeholder="搜书名 / 作者 / 卖家，支持拼音如 xiyou"
              autocomplete="off"
              @input="onKeywordInput"
              @keydown="onSearchKeydown"
            />
            <button class="search-btn" type="submit">
              <svg class="search-ico" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2.2" />
                <path d="M16.5 16.5L21 21" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
              </svg>
              搜索
            </button>
          </form>
          <ul v-if="showSuggest && suggestions.length" class="search-suggest" role="listbox">
            <li
              v-for="(item, idx) in suggestions"
              :key="item.id + '-' + item.text"
              class="search-suggest-item"
              :class="{ active: idx === activeSuggest }"
              role="option"
              @mousedown.prevent="pickSuggest(item)"
            >
              <span class="suggest-text">{{ item.text }}</span>
              <span v-if="item.hint" class="suggest-hint">{{ item.hint }}</span>
            </li>
          </ul>
        </div>
        <div class="search-cats" role="list">
          <button
            type="button"
            class="cat-link"
            :class="{ active: query.categoryId == null }"
            @click="pickCategory(undefined)"
          >
            全部
          </button>
          <button
            v-for="c in categories"
            :key="c.id"
            type="button"
            class="cat-link"
            :class="{ active: query.categoryId === c.id }"
            @click="pickCategory(c.id)"
          >
            {{ c.name }}
          </button>
        </div>
      </div>
    </section>

    <div class="page home-body">
      <a-alert v-if="notices[0]" type="info" class="home-notice" show-icon>
        {{ notices[0].title }}
      </a-alert>

      <div class="home-toolbar">
        <div class="home-toolbar-left">
          <h2 class="home-section-title">
            {{ listTitle }}
          </h2>
          <span class="home-count">共 {{ total }} 本</span>
        </div>
        <div class="home-filters">
          <a-select
            v-model="query.campus"
            placeholder="校区"
            allow-clear
            size="small"
            style="width: 120px"
            @change="onFilterChange"
          >
            <a-option v-for="d in campuses" :key="d.value" :value="d.value">{{ d.label }}</a-option>
          </a-select>
          <a-select
            v-model="query.conditionCode"
            placeholder="成色"
            allow-clear
            size="small"
            style="width: 120px"
            @change="onFilterChange"
          >
            <a-option v-for="d in conditions" :key="d.value" :value="d.value">{{ d.label }}</a-option>
          </a-select>
          <router-link class="home-publish" to="/publish">去发布</router-link>
        </div>
      </div>

      <div class="grid">
        <BookGridCard
          v-for="b in list"
          :key="b.id"
          :book="b"
          :condition-label="conditionLabel"
        />
      </div>

      <div v-if="!list.length" class="empty-state">暂无在售书籍，换个关键词或分类试试</div>

      <div class="home-pager">
        <a-pagination
          :total="total"
          v-model:current="query.pageNo"
          :page-size="query.pageSize"
          @change="load"
        />
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import PinyinMatch from 'pinyin-match'
import BookGridCard from '@/components/BookGridCard.vue'
import { getBookPage, getBookSuggestIndex, getCategories, getDict, getNotices } from '@/api'

type SuggestIndexItem = {
  id: number
  title: string
  author?: string
  sellerNickname?: string
}

type SuggestItem = {
  id: number
  text: string
  hint?: string
}

/** 输入框草稿：仅点搜索后才写入 appliedKeyword 并刷新列表 */
const keywordInput = ref('')
/** 已生效关键词：标题与列表都按它展示，避免边输入边改标题 */
const appliedKeyword = ref('')

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  categoryId: undefined as number | undefined,
  campus: undefined as string | undefined,
  conditionCode: undefined as string | undefined
})
const list = ref<any[]>([])
const total = ref(0)
const categories = ref<any[]>([])
const campuses = ref<any[]>([])
const conditions = ref<any[]>([])
const notices = ref<any[]>([])
/** 忽略过期请求，避免首屏/筛选项初始化的旧结果覆盖搜索结果 */
let loadSeq = 0
const filtersReady = ref(false)

const suggestIndex = ref<SuggestIndexItem[]>([])
const suggestions = ref<SuggestItem[]>([])
const showSuggest = ref(false)
const activeSuggest = ref(-1)
let suggestTimer: ReturnType<typeof setTimeout> | undefined
let blurTimer: ReturnType<typeof setTimeout> | undefined

const conditionLabel = (code?: string) => {
  if (!code) return ''
  return conditions.value.find((d) => d.value === code)?.label || ''
}

const activeCategoryName = computed(() => {
  if (query.categoryId == null) return ''
  return categories.value.find((c) => c.id === query.categoryId)?.name || ''
})

const listTitle = computed(() => {
  const parts: string[] = []
  if (appliedKeyword.value) parts.push(`“${appliedKeyword.value}”`)
  if (activeCategoryName.value) parts.push(activeCategoryName.value)
  if (!parts.length) return '今日在售'
  return parts.join(' · ') + ' 相关'
})

const buildParams = () => {
  const params: Record<string, string | number> = {
    pageNo: query.pageNo,
    pageSize: query.pageSize
  }
  if (appliedKeyword.value) params.keyword = appliedKeyword.value
  if (query.categoryId != null) params.categoryId = query.categoryId
  if (query.campus) params.campus = query.campus
  if (query.conditionCode) params.conditionCode = query.conditionCode
  return params
}

const load = async () => {
  const seq = ++loadSeq
  const data = await getBookPage(buildParams())
  if (seq !== loadSeq) return
  list.value = data.list || []
  total.value = data.total || 0
}

const matchText = (text: string | undefined, kw: string) => {
  if (!text) return false
  const raw = String(text)
  if (raw.toLowerCase().includes(kw.toLowerCase())) return true
  try {
    return !!PinyinMatch.match(raw, kw)
  } catch {
    return false
  }
}

const collectSuggestions = (kw: string): SuggestItem[] => {
  if (!kw) return []
  const seen = new Set<string>()
  const result: SuggestItem[] = []
  for (const item of suggestIndex.value) {
    if (matchText(item.title, kw)) {
      const key = 't:' + item.title
      if (!seen.has(key)) {
        seen.add(key)
        result.push({
          id: item.id,
          text: item.title,
          hint: item.author || item.sellerNickname || undefined
        })
      }
    } else if (matchText(item.author, kw)) {
      const key = 'a:' + item.author
      if (!seen.has(key) && item.author) {
        seen.add(key)
        result.push({
          id: item.id,
          text: item.author,
          hint: item.title
        })
      }
    } else if (matchText(item.sellerNickname, kw)) {
      const key = 's:' + item.sellerNickname
      if (!seen.has(key) && item.sellerNickname) {
        seen.add(key)
        result.push({
          id: item.id,
          text: item.sellerNickname,
          hint: item.title
        })
      }
    }
    if (result.length >= 8) break
  }
  return result
}

const refreshSuggestions = () => {
  const kw = keywordInput.value.trim()
  suggestions.value = collectSuggestions(kw)
  activeSuggest.value = suggestions.value.length ? 0 : -1
  showSuggest.value = suggestions.value.length > 0
}

const onKeywordInput = () => {
  if (suggestTimer) clearTimeout(suggestTimer)
  suggestTimer = setTimeout(refreshSuggestions, 100)
}

const onSearchFocus = () => {
  if (blurTimer) clearTimeout(blurTimer)
  refreshSuggestions()
}

const onSearchBlur = () => {
  blurTimer = setTimeout(() => {
    showSuggest.value = false
    activeSuggest.value = -1
  }, 120)
}

const pickSuggest = async (item: SuggestItem) => {
  keywordInput.value = item.text
  showSuggest.value = false
  activeSuggest.value = -1
  await doSearch()
}

const resolveKeywordForSearch = () => {
  const kw = keywordInput.value.trim()
  if (!kw) return ''
  // 纯英文/数字输入时，优先用拼音命中的中文词，避免直接搜字母得到 0 结果
  if (/^[a-zA-Z0-9\s'.-]+$/.test(kw)) {
    const hits = suggestions.value.length ? suggestions.value : collectSuggestions(kw)
    if (hits.length) {
      keywordInput.value = hits[0].text
      return hits[0].text
    }
  }
  return kw
}

const doSearch = async () => {
  showSuggest.value = false
  appliedKeyword.value = resolveKeywordForSearch()
  query.pageNo = 1
  await load()
}

const onSearchKeydown = async (e: KeyboardEvent) => {
  if (e.key === 'ArrowDown') {
    if (!suggestions.value.length) return
    e.preventDefault()
    showSuggest.value = true
    activeSuggest.value = (activeSuggest.value + 1) % suggestions.value.length
    return
  }
  if (e.key === 'ArrowUp') {
    if (!suggestions.value.length) return
    e.preventDefault()
    showSuggest.value = true
    activeSuggest.value =
      activeSuggest.value <= 0 ? suggestions.value.length - 1 : activeSuggest.value - 1
    return
  }
  if (e.key === 'Escape') {
    showSuggest.value = false
    activeSuggest.value = -1
    return
  }
  if (e.key === 'Enter') {
    e.preventDefault()
    if (showSuggest.value && activeSuggest.value >= 0 && suggestions.value[activeSuggest.value]) {
      await pickSuggest(suggestions.value[activeSuggest.value])
      return
    }
    await doSearch()
  }
}

const pickCategory = async (id?: number) => {
  query.categoryId = id
  query.pageNo = 1
  await load()
}

const onFilterChange = async () => {
  if (!filtersReady.value) return
  query.pageNo = 1
  await load()
}

onMounted(async () => {
  const [cats, campusDict, conditionDict, noticeList, index] = await Promise.all([
    getCategories(),
    getDict('campus'),
    getDict('book_condition'),
    getNotices(),
    getBookSuggestIndex().catch(() => [])
  ])
  categories.value = cats || []
  campuses.value = campusDict || []
  conditions.value = conditionDict || []
  notices.value = noticeList || []
  suggestIndex.value = Array.isArray(index) ? index : []
  await load()
  filtersReady.value = true
})

onUnmounted(() => {
  if (suggestTimer) clearTimeout(suggestTimer)
  if (blurTimer) clearTimeout(blurTimer)
})
</script>
<style scoped>
.home {
  padding-bottom: 24px;
}

.search-hero {
  background: var(--teal);
  padding: 18px 20px 14px;
  border-bottom: 1px solid rgba(20, 35, 28, 0.08);
}

.search-hero-inner {
  max-width: var(--page-max);
  margin: 0 auto;
}

.search-wrap {
  position: relative;
  z-index: 5;
}

.search-bar {
  display: flex;
  align-items: stretch;
  width: 100%;
  min-height: 44px;
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  border-radius: 999px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 4px 14px rgba(10, 82, 68, 0.18);
}

.search-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  padding: 0 18px;
  font-size: 15px;
  color: var(--ink);
}

.search-input::placeholder {
  color: #a8a8a8;
}

.search-input::-webkit-search-cancel-button {
  -webkit-appearance: none;
}

.search-btn {
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 96px;
  padding: 0 18px;
  border: none;
  border-left: 1.5px solid rgba(13, 107, 88, 0.2);
  background: var(--teal-soft);
  color: var(--teal-deep);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}

.search-btn:hover {
  background: #c5e4da;
  color: var(--teal-deep);
}

.search-btn:active {
  background: #b5dbcf;
}

.search-ico {
  width: 16px;
  height: 16px;
}

.search-suggest {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 6px);
  margin: 0;
  padding: 6px 0;
  list-style: none;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(20, 35, 28, 0.16);
  border: 1px solid rgba(213, 224, 217, 0.95);
  max-height: 280px;
  overflow-y: auto;
}

.search-suggest-item {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  color: var(--ink);
  font-size: 14px;
}

.search-suggest-item:hover,
.search-suggest-item.active {
  background: rgba(13, 107, 88, 0.08);
}

.suggest-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}

.suggest-hint {
  flex: none;
  max-width: 42%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--muted);
  font-size: 12px;
}

.search-cats {
  display: flex;
  flex-wrap: nowrap;
  gap: 4px 18px;
  margin-top: 12px;
  padding: 0 4px 2px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.search-cats::-webkit-scrollbar {
  display: none;
}

.cat-link {
  flex: none;
  border: none;
  background: transparent;
  padding: 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 13px;
  line-height: 1.4;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.15s ease, font-weight 0.15s ease;
}

.cat-link:hover {
  color: #fff;
}

.cat-link.active {
  color: #fff;
  font-weight: 700;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.home-body {
  padding-top: 18px;
}

.home-notice {
  margin-bottom: 14px;
  border-radius: 10px;
}

.home-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.home-toolbar-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.home-section-title {
  margin: 0;
  font-family: var(--font-brand);
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.home-count {
  color: var(--muted);
  font-size: 13px;
}

.home-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.home-publish {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 8px;
  background: var(--teal);
  color: #fff;
  font-size: 13px;
  font-weight: 650;
  transition: background 0.2s ease;
}

.home-publish:hover {
  background: var(--teal-deep);
}

.home-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
}

@media (max-width: 640px) {
  .search-hero {
    padding: 14px 14px 12px;
  }

  .search-btn {
    min-width: 84px;
    padding: 0 14px;
  }

  .home-toolbar {
    align-items: flex-start;
  }

  .home-filters {
    width: 100%;
  }
}
</style>
