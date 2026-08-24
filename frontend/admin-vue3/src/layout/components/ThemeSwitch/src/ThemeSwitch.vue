<script lang="ts" setup>
import { useAppStore } from '@/store/modules/app'

/**
 * 明暗切换按钮（登录页 / SSO 页用）。
 *
 * 原先是一枚黑色 a-switch 药丸，配 emoji 太阳/月亮图标：在登录页的白底上像贴了块黑胶布，
 * 而且开关语义反了（打开=夜间却显示太阳）。这里改成与顶栏一致的图标按钮：
 * 当前是浅色就显示月亮（点它去夜间），当前是深色就显示太阳。
 */
defineOptions({ name: 'ThemeSwitch' })

const appStore = useAppStore()
// 跟着 store 读，别在本地 ref 里存一份快照：快照在别处切主题后就对不上了（图标会反）
const isDark = computed(() => appStore.getIsDark)

const toggle = () => appStore.setIsDark(!isDark.value)
</script>

<template>
  <a-tooltip :content="isDark ? '切换浅色' : '切换深色'" position="bl">
    <button class="theme-switch" type="button" :aria-label="isDark ? '切换浅色' : '切换深色'" @click="toggle">
      <icon-sun-fill v-if="isDark" />
      <icon-moon-fill v-else />
    </button>
  </a-tooltip>
</template>

<style lang="scss" scoped>
.theme-switch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  font-size: 17px;
  color: var(--bm-text-2);
  cursor: pointer;
  background: transparent;
  border: 1px solid var(--bm-border);
  border-radius: var(--bm-radius-sm, 8px);
  transition: background-color var(--bm-dur) var(--bm-ease-out), color var(--bm-dur) var(--bm-ease-out), border-color var(--bm-dur) var(--bm-ease-out);

  &:hover {
    color: var(--bm-brand);
    background: var(--bm-brand-bg);
    border-color: transparent;
  }
}
</style>
