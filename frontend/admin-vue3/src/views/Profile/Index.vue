<template>
  <div class="profile-page flex items-start">
    <a-card class="user w-1/3" hoverable>
      <template #title>
        <div class="card-header">
          <span>{{ t('profile.user.title') }}</span>
        </div>
      </template>
      <ProfileUser ref="profileUserRef" />
    </a-card>
    <a-card class="user ml-3 w-2/3" hoverable>
      <a-tabs v-model:active-key="activeName" class="profile-tabs" position="top">
        <a-tab-pane key="basicInfo" :title="t('profile.info.basicInfo')">
          <BasicInfo @success="handleBasicInfoSuccess" />
        </a-tab-pane>
        <a-tab-pane key="resetPwd" :title="t('profile.info.resetPwd')">
          <ResetPwd />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>
<script lang="ts" setup>
import { BasicInfo, ProfileUser, ResetPwd } from './components'

const { t } = useI18n()
defineOptions({ name: 'Profile' })
const activeName = ref('basicInfo')
const profileUserRef = ref()

// 处理基本信息更新成功
const handleBasicInfoSuccess = async () => {
  await profileUserRef.value?.refresh()
}
</script>
<style scoped>
.card-header {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 右侧设置区：留出内容最小高度，避免切到短内容时卡片塌陷；同时不再写死高度产生大片留白 */
.profile-tabs {
  min-height: 360px;
}

.profile-tabs :deep(.arco-tabs-content) {
  padding: 24px 8px;
}
</style>
