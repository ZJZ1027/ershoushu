<template>
  <div class="app-shell">
    <header class="nav">
      <router-link class="brand" to="/">
        <span class="brand-mark" aria-hidden="true"></span>
        <span class="brand-text">校园二手书</span>
      </router-link>
      <nav class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/publish">发布</router-link>
        <router-link to="/messages" class="nav-badge-link">
          留言
          <span v-if="user.unreadMsg > 0" class="nav-badge">
            {{ user.unreadMsg > 99 ? '99+' : user.unreadMsg }}
          </span>
        </router-link>
        <router-link to="/orders" class="nav-badge-link">
          预约
          <span v-if="user.pendingWant > 0" class="nav-badge">
            {{ user.pendingWant > 99 ? '99+' : user.pendingWant }}
          </span>
        </router-link>
        <router-link to="/mine">我的</router-link>
        <template v-if="user.token && user.profile">
          <div class="nav-user">
            <span class="nav-user-name">{{ user.profile.nickname || user.profile.username }}</span>
            <button type="button" class="nav-logout" @click="onLogout">退出</button>
          </div>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </nav>
    </header>
    <router-view />
  </div>
</template>
<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const router = useRouter()
let timer: ReturnType<typeof setInterval> | undefined

onMounted(async () => {
  if (!user.token) return
  try {
    await user.loadProfile()
    await user.refreshBadges()
  } catch {
    user.clear()
    router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  timer = setInterval(() => user.refreshBadges(), 15000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const onLogout = async () => {
  await user.logout()
  router.push('/login')
}
</script>
