<template>
  <article class="follow-item">
    <router-link :to="'/seller/' + user.id" class="follow-main">
      <span class="follow-avatar">
        <img v-if="avatarSrc" :src="avatarSrc" alt="" />
        <span v-else>{{ avatarLetter }}</span>
      </span>
      <span class="follow-meta">
        <span class="follow-name">{{ user.nickname || '同学' }}</span>
        <span v-if="user.signature" class="follow-sig">{{ user.signature }}</span>
        <span v-else-if="user.campus" class="follow-sig muted">{{ user.campus }}</span>
      </span>
    </router-link>
    <a-button
      v-if="showFollowBtn"
      size="small"
      :type="user.isFollowing ? 'outline' : 'primary'"
      :loading="loading"
      @click="emit('toggle', user.id)"
    >
      {{ user.isFollowing ? '已关注' : '关注' }}
    </a-button>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { fileUrl } from '@/api/http'
import { useUserStore } from '@/stores/user'

const props = defineProps<{
  user: {
    id: number
    nickname?: string
    avatar?: string
    signature?: string
    campus?: string
    isFollowing?: boolean
  }
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'toggle', userId: number): void
}>()

const me = useUserStore()
const avatarSrc = computed(() => fileUrl(props.user.avatar))
const avatarLetter = computed(() => {
  const name = props.user.nickname || '同学'
  return String(name).trim().charAt(0).toUpperCase() || '同'
})
const showFollowBtn = computed(() => me.profile?.id && props.user.id !== me.profile.id)
</script>

<style scoped>
.follow-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface-raised);
  box-shadow: var(--shadow-soft);
}

.follow-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
  color: inherit;
}

.follow-main:hover .follow-name {
  color: var(--teal-deep);
}

.follow-avatar {
  flex: none;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--teal-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--teal-deep);
  font-size: 16px;
  font-weight: 700;
}

.follow-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.follow-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.follow-name {
  font-weight: 650;
  transition: color 0.15s ease;
}

.follow-sig {
  color: var(--muted);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follow-sig.muted {
  font-size: 12px;
}
</style>
