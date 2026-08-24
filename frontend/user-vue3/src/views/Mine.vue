<template>
  <div class="page mine">
    <section class="mine-hero">
      <div>
        <p class="mine-kicker">个人中心</p>
        <h1 class="page-title">{{ displayName }}</h1>
        <p class="page-sub">管理你发布的教材与收藏，完善联系方式方便面交。</p>
      </div>
      <a-button type="primary" @click="$router.push('/profile')">个人资料</a-button>
    </section>

    <a-tabs default-active-key="books" class="mine-tabs">
      <a-tab-pane key="books" title="我发布的">
        <div v-if="!books.length" class="empty-state">还没有发布记录</div>
        <div v-else class="mine-list">
          <article v-for="item in books" :key="item.id" class="mine-item">
            <div>
              <h3 class="mine-item-title">{{ item.title }}</h3>
              <p class="muted">{{ bookDesc(item) }}</p>
            </div>
            <div class="mine-actions">
              <a-button type="text" @click="$router.push('/book/' + item.id)">查看</a-button>
              <a-button
                v-if="[0, 1, 5, 6].includes(item.status)"
                type="text"
                @click="$router.push('/publish?id=' + item.id)"
              >编辑</a-button>
              <a-button
                v-if="item.status === 1 || item.status === 0"
                type="text"
                status="danger"
                @click="off(item.id)"
              >下架</a-button>
            </div>
          </article>
        </div>
      </a-tab-pane>
      <a-tab-pane key="fav" title="收藏">
        <div v-if="!favs.length" class="empty-state">暂无收藏</div>
        <div v-else class="mine-list">
          <article v-for="item in favs" :key="item.id" class="mine-item">
            <div>
              <h3 class="mine-item-title">{{ item.title }}</h3>
              <p class="price" style="font-size: 1rem; margin: 4px 0 0">¥{{ item.price }}</p>
            </div>
            <div class="mine-actions">
              <a-button type="text" @click="$router.push('/book/' + item.id)">查看</a-button>
            </div>
          </article>
        </div>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getFavorites, getMyBooks, offShelf } from '@/api'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const books = ref<any[]>([])
const favs = ref<any[]>([])
const displayName = computed(() => user.profile?.nickname || user.profile?.username || '我的书栈')
const statusText = (s: number) => ['待审', '在售', '预约中', '已成交', '已下架', '已驳回', '草稿'][s] || ''
const bookDesc = (item: any) => {
  const price = item.price == null ? '未定价' : '¥' + item.price
  return price + ' · ' + statusText(item.status)
}

const load = async () => {
  books.value = (await getMyBooks({ pageNo: 1, pageSize: 50 })).list
  favs.value = (await getFavorites({ pageNo: 1, pageSize: 50 })).list
}
const off = async (id: number) => {
  await offShelf(id)
  Message.success('已下架')
  await load()
}
onMounted(load)
</script>
<style scoped>
.mine-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.mine-kicker {
  margin: 0 0 8px;
  color: var(--teal);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.mine-tabs {
  margin-top: 8px;
}

.mine-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mine-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface-raised);
  box-shadow: var(--shadow-soft);
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.mine-item:hover {
  border-color: #b9cfc5;
  transform: translateY(-1px);
}

.mine-item-title {
  margin: 0 0 4px;
  font-size: 1rem;
  font-weight: 650;
}

.mine-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

@media (max-width: 640px) {
  .mine-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .mine-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
