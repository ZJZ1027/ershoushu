<template>
  <img class="bl" :src="logoUrl" :alt="alt" :style="{ '--bl-size': size + 'px' }" />
</template>

<script lang="ts" setup>
import logoUrl from '@/assets/imgs/logo.png'

/**
 * 品牌标识。图形是纯图形块（不含文字），产品名一律由旁边的实文字排出来，
 * 这样才能跟着 VITE_APP_TITLE / 租户自定义标题变。
 *
 * 换品牌只需替换 `src/assets/imgs/logo.png`（正方形、无内边距、无文字），
 * 顶栏 32px 与登录页 40px 两处都不用改。
 */
defineOptions({ name: 'BrandLogo' })

withDefaults(defineProps<{ size?: number; alt?: string }>(), {
  size: 32,
  alt: 'logo'
})
</script>

<style lang="scss" scoped>
.bl {
  display: block;
  flex: 0 0 auto;
  width: var(--bl-size);
  height: var(--bl-size);
  object-fit: contain;
  border-radius: calc(var(--bl-size) * 0.25);
}
</style>
