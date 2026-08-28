<template>
  <div class="page follow-page">
    <button type="button" class="follow-back" @click="router.back()">← 返回</button>
    <h1 class="page-title">{{ pageTitle }}</h1>
    <p class="page-sub">{{ pageSub }}</p>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="list.length" class="follow-list">
        <UserFollowRow
          v-for="item in list"
          :key="item.id"
          :user="item"
          :loading="togglingId === item.id"
          @toggle="onToggle"
        />
      </div>
      <div v-else-if="!loading" class="empty-state">{{ emptyText }}</div>

      <div v-if="total > query.pageSize" class="home-pager">
        <a-pagination
          :total="total"
          v-model:current="query.pageNo"
          :page-size="query.pageSize"
          @change="load"
        />
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import UserFollowRow from '@/components/UserFollowRow.vue'
import { getFollowers, getFollowing, getSeller, toggleFollow } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const loading = ref(true)
const list = ref<any[]>([])
const total = ref(0)
const ownerName = ref('')
const togglingId = ref<number>()
const query = reactive({
  pageNo: 1,
  pageSize: 20
})

const type = computed(() => String(route.params.type))
const userId = computed(() => Number(route.params.userId || user.profile?.id))
const isFollowers = computed(() => type.value === 'followers')

const pageTitle = computed(() => (isFollowers.value ? '粉丝' : '关注'))
const pageSub = computed(() => {
  const name = ownerName.value || 'TA'
  return isFollowers.value ? `${name} 的粉丝` : `${name} 关注的人`
})
const emptyText = computed(() => (isFollowers.value ? '还没有粉丝' : '还没有关注任何人'))

const load = async () => {
  if (!userId.value) {
    router.replace('/mine')
    return
  }
  loading.value = true
  try {
    const params = { userId: userId.value, pageNo: query.pageNo, pageSize: query.pageSize }
    const data = isFollowers.value ? await getFollowers(params) : await getFollowing(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadOwner = async () => {
  if (!userId.value) return
  if (userId.value === user.profile?.id) {
    ownerName.value = user.profile?.nickname || '我'
    return
  }
  try {
    const info = await getSeller(userId.value)
    ownerName.value = info.nickname || '同学'
  } catch {
    ownerName.value = '同学'
  }
}

const onToggle = async (targetId: number) => {
  togglingId.value = targetId
  try {
    const followed = await toggleFollow(targetId)
    Message.success(followed ? '已关注' : '已取消关注')
    const item = list.value.find((u) => u.id === targetId)
    if (item) item.isFollowing = followed
  } finally {
    togglingId.value = undefined
  }
}

watch(
  () => [route.params.type, route.params.userId],
  async () => {
    query.pageNo = 1
    await loadOwner()
    await load()
  }
)

onMounted(async () => {
  if (!user.profile) await user.loadProfile()
  await loadOwner()
  await load()
})
</script>

<style scoped>
.follow-page {
  padding-top: 8px;
}

.follow-back {
  border: none;
  background: transparent;
  color: var(--muted);
  font-size: 13px;
  padding: 0;
  margin-bottom: 12px;
  cursor: pointer;
}

.follow-back:hover {
  color: var(--teal-deep);
}

.follow-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.home-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 22px;
}
</style>
