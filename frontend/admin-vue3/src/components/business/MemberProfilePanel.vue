<template>
  <div v-if="member" class="member-profile">
    <div class="profile-head">
      <img v-if="memberAvatar" class="profile-avatar" :src="memberAvatar" alt="" />
      <div v-else class="profile-avatar fallback">{{ avatarLetter }}</div>
      <div>
        <h3 class="profile-name">{{ member.nickname || member.username }}</h3>
        <p class="muted">账号 {{ member.username || '—' }} · ID {{ member.id }}</p>
        <a-tag :color="member.status === 0 ? 'green' : 'red'">{{ member.status === 0 ? '正常' : '停用' }}</a-tag>
      </div>
    </div>

    <div class="profile-grid">
      <div class="profile-item"><span class="label">手机</span><span>{{ member.mobile || '—' }}</span></div>
      <div class="profile-item"><span class="label">微信</span><span>{{ member.wechat || '—' }}</span></div>
      <div class="profile-item"><span class="label">校区</span><span>{{ member.campus || '—' }}</span></div>
      <div class="profile-item profile-item-wide">
        <span class="label">个性签名</span><span>{{ member.signature || '—' }}</span>
      </div>
      <div class="profile-item"><span class="label">性别</span><span>{{ sexText(member.sex) }}</span></div>
      <div class="profile-item"><span class="label">邮箱</span><span>{{ member.email || '—' }}</span></div>
      <div class="profile-item"><span class="label">备注</span><span>{{ member.remark || '—' }}</span></div>
      <div class="profile-item"><span class="label">注册时间</span><span>{{ member.createTime || '—' }}</span></div>
      <div class="profile-item"><span class="label">最近登录</span><span>{{ member.loginDate || '—' }}</span></div>
      <div class="profile-item"><span class="label">登录 IP</span><span>{{ member.loginIp || '—' }}</span></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps<{
  member: Record<string, any> | undefined
}>()

const fileUrl = (url?: string) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_BASE_URL + url
}

const memberAvatar = computed(() => fileUrl(props.member?.avatar))
const avatarLetter = computed(() => {
  const name = props.member?.nickname || props.member?.username || '?'
  return String(name).trim().charAt(0).toUpperCase() || '?'
})

const sexText = (sex?: number) => {
  if (sex === 1) return '男'
  if (sex === 2) return '女'
  return '—'
}
</script>

<style lang="scss" scoped>
.member-profile {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  background: #e8f3ef;
  flex: none;
}

.profile-avatar.fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #0d6b58;
}

.profile-name {
  margin: 0 0 4px;
  font-size: 18px;
}

.muted {
  color: var(--color-text-3, #86909c);
  font-size: 13px;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}

.profile-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: 8px;
  background: var(--color-fill-1, #f7f8fa);
}

.profile-item .label {
  color: var(--color-text-3, #86909c);
  font-size: 12px;
}

.profile-item-wide {
  grid-column: 1 / -1;
}

@media (max-width: 600px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
