<template>
  <div class="arco-tab-bar">
    <div class="tab-bar-scroll">
      <div class="tags-wrap">
        <TabItem
          v-for="(tag, index) in tagList"
          :key="tag.fullPath"
          :index="index"
          :item-data="tag as any"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useTagsViewStore } from '@/store/modules/tagsView'
import TabItem from './TabItem.vue'

defineOptions({ name: 'ArcoTabBar' })

const route = useRoute()
const tagsViewStore = useTagsViewStore()

const tagList = computed(() => tagsViewStore.getVisitedViews)

watch(
  () => route.fullPath,
  () => {
    if (!route?.meta?.noTagsView && route.name) {
      tagsViewStore.addView(route as any)
      tagsViewStore.setSelectedTag(route as any)
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.arco-tab-bar {
  position: relative;
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 16px;
  background-color: var(--color-bg-2, #fff);
  border-bottom: 1px solid var(--color-border, #e5e6eb);
}

.tab-bar-scroll {
  flex: 1;
  overflow: hidden;
}

.tags-wrap {
  display: flex;
  height: 28px;
  overflow: auto hidden;
  white-space: nowrap;
  align-items: center;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--color-neutral-3, #e5e6eb);
    border-radius: 2px;
  }
}
</style>
