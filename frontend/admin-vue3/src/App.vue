<script lang="ts" setup>
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'

defineOptions({ name: 'APP' })

const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('app')
const appStore = useAppStore()
const currentSize = computed(() => appStore.getCurrentSize)
const greyMode = computed(() => appStore.getGreyMode)
const { wsCache } = useCache()

/**
 * 明暗主题的初始档。
 *
 * 原先缓存为空时会去嗅探系统的 `prefers-color-scheme`：Windows 开了深色的用户，
 * 第一次打开登录页看到的就是黑底，并且这个「系统替他做的决定」还会被写进 localStorage
 * 固化下来。本产品的品牌基调是浅色（登录页左侧品牌蓝、全站白底卡片都按浅色调的），
 * 所以首访一律浅色，深色只作为用户显式切过之后的偏好（顶栏 / 登录页右上角开关）。
 */
const setDefaultTheme = () => {
  const cached = wsCache.get(CACHE_KEY.IS_DARK)
  appStore.setIsDark(cached === null ? false : cached)
}
setDefaultTheme()
</script>
<template>
  <!-- 这里原先还挂着 basepro 自带的 routerSearch：它同样监听 Ctrl+K，
       会和顶栏的命令面板（MenuSearch）同时弹出两个搜索框叠在一起。
       命令面板已完整覆盖它的能力（菜单 + 快捷动作 + 键盘导航），故移除。 -->
  <ConfigGlobal :size="currentSize">
    <RouterView :class="greyMode ? `${prefixCls}-grey-mode` : ''" />
  </ConfigGlobal>
</template>
<style lang="scss">
$prefix-cls: #{$namespace}-app;

.size {
  width: 100%;
  height: 100%;
}

html,
body {
  @extend .size;

  padding: 0 !important;
  margin: 0;
  overflow: hidden;

  #app {
    @extend .size;
  }
}

.#{$prefix-cls}-grey-mode {
  filter: grayscale(100%);
}
</style>
