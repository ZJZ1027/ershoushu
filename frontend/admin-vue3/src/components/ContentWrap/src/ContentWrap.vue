<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { useDesign } from '@/hooks/web/useDesign'

defineOptions({ name: 'ContentWrap' })

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('content-wrap')

defineProps({
  title: propTypes.string.def(''),
  message: propTypes.string.def(''),
  // 默认内距对齐原 ElCard + patch 的 16px 18px（迁移 Arco 后由本组件自带，不再依赖 el-card 皮肤）
  bodyStyle: propTypes.object.def({ padding: '16px 18px' })
})
</script>

<template>
  <div :class="[prefixCls, 'mb-15px']">
    <div v-if="title" class="v-content-wrap__header">
      <span class="text-16px font-700">{{ title }}</span>
      <a-tooltip v-if="message" position="right">
        <template #content>
          <div class="max-w-200px">{{ message }}</div>
        </template>
        <Icon :size="14" class="ml-5px" icon="ep:question-filled" />
      </a-tooltip>
      <div class="flex flex-grow pl-20px">
        <slot name="header"></slot>
      </div>
    </div>
    <div class="v-content-wrap__body" :style="bodyStyle">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.v-content-wrap {
  margin-bottom: 16px;
  overflow: visible;
  background: #fff;
  border: 1px solid var(--arco-color-border);
  border-radius: 6px;
  box-shadow: none;

  &__header {
    display: flex;
    align-items: center;
    min-height: auto;
    padding: 14px 18px;
    border-bottom: 1px solid var(--arco-color-border-light);
  }
}
</style>
