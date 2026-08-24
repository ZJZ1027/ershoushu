<template>
  <div class="page home">
    <section class="home-intro">
      <div>
        <p class="home-kicker">今日在售</p>
        <h1 class="page-title">把教材交给下一位同学</h1>
        <p class="page-sub">按书名、课程、校区筛选，校内面交更省心。</p>
      </div>
      <router-link class="home-cta" to="/publish">去发布</router-link>
    </section>

    <a-alert v-if="notices[0]" type="info" class="home-notice" show-icon>
      {{ notices[0].title }}
    </a-alert>

    <div class="filters home-filters">
      <a-input-search
        v-model="query.keyword"
        placeholder="搜书名 / 作者 / ISBN / 课程"
        class="home-search"
        allow-clear
        @search="load"
      />
      <a-select v-model="query.categoryId" placeholder="分类" allow-clear style="width: 140px" @change="load">
        <a-option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</a-option>
      </a-select>
      <a-select v-model="query.campus" placeholder="校区" allow-clear style="width: 140px" @change="load">
        <a-option v-for="d in campuses" :key="d.value" :value="d.value">{{ d.label }}</a-option>
      </a-select>
      <a-select v-model="query.conditionCode" placeholder="成色" allow-clear style="width: 140px" @change="load">
        <a-option v-for="d in conditions" :key="d.value" :value="d.value">{{ d.label }}</a-option>
      </a-select>
    </div>

    <div class="grid">
      <router-link v-for="b in list" :key="b.id" class="card-book" :to="'/book/' + b.id">
        <div class="card-cover">
          <img :src="cover(b)" alt="" />
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ b.title }}</h3>
          <div class="price">¥{{ b.price }}</div>
          <div class="muted">{{ b.campus || '校内面交' }} · {{ b.sellerNickname }}</div>
        </div>
      </router-link>
    </div>

    <div v-if="!list.length" class="empty-state">暂无在售书籍，换个筛选条件试试</div>

    <div class="home-pager">
      <a-pagination
        :total="total"
        v-model:current="query.pageNo"
        :page-size="query.pageSize"
        @change="load"
      />
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getBookPage, getCategories, getDict, getNotices } from '@/api'
import { fileUrl } from '@/api/http'

const query = reactive({
  pageNo: 1,
  pageSize: 12,
  keyword: '',
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

const cover = (b: any) => fileUrl(b.coverUrl) || 'https://placehold.co/400x300?text=Book'

const load = async () => {
  const data = await getBookPage(query)
  list.value = data.list
  total.value = data.total
}

onMounted(async () => {
  categories.value = await getCategories()
  campuses.value = await getDict('campus')
  conditions.value = await getDict('book_condition')
  notices.value = await getNotices()
  await load()
})
</script>
<style scoped>
.home-intro {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 8px;
  padding: 8px 0 4px;
}

.home-kicker {
  margin: 0 0 8px;
  color: var(--teal);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.home-cta {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 18px;
  border-radius: 10px;
  background: var(--teal);
  color: #fff;
  font-weight: 650;
  transition: background 0.2s ease, transform 0.2s ease;
}

.home-cta:hover {
  background: var(--teal-deep);
  transform: translateY(-1px);
}

.home-notice {
  margin-bottom: 16px;
  border-radius: 10px;
}

.home-filters {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.72);
}

.home-search {
  width: min(320px, 100%);
}

.card-cover {
  overflow: hidden;
}

.home-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
}

@media (max-width: 640px) {
  .home-intro {
    flex-direction: column;
    align-items: flex-start;
  }

  .home-search {
    width: 100%;
  }
}
</style>
