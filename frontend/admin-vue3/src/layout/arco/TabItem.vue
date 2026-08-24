<template>
  <a-dropdown trigger="contextMenu" :popup-max-height="false" @select="onAction">
    <span
      class="arco-tag arco-tag-size-medium arco-tag-checked tab-chip"
      :class="{ 'is-active': isActive }"
      @click="goto"
    >
      <span class="tab-link">{{ title }}</span>
      <span
        v-if="!isAffix"
        class="arco-icon-hover arco-tag-icon-hover arco-icon-hover-size-medium arco-tag-close-btn"
        @click.stop="closeSelf"
      >
        <icon-close />
      </span>
    </span>
    <template #content>
      <a-doption :disabled="!isActive" value="reload">
        <template #icon><icon-refresh /></template>
        <span>重新加载</span>
      </a-doption>
      <a-doption :disabled="isAffix" value="current">
        <template #icon><icon-close /></template>
        <span>关闭当前</span>
      </a-doption>
      <a-doption :disabled="disabledLeft" value="left">
        <template #icon><icon-to-left /></template>
        <span>关闭左侧</span>
      </a-doption>
      <a-doption :disabled="disabledRight" value="right">
        <template #icon><icon-to-right /></template>
        <span>关闭右侧</span>
      </a-doption>
      <a-doption value="others">
        <template #icon><icon-swap /></template>
        <span>关闭其它</span>
      </a-doption>
      <a-doption value="all">
        <template #icon><icon-folder-delete /></template>
        <span>关闭全部</span>
      </a-doption>
    </template>
  </a-dropdown>
</template>

<script lang="ts" setup>
import { computed, nextTick, inject } from 'vue'
import { useRouter, useRoute, type RouteLocationNormalizedLoaded } from 'vue-router'
import { Dropdown as ADropdown, Doption as ADoption } from '@arco-design/web-vue'
import {
  IconClose,
  IconRefresh,
  IconToLeft,
  IconToRight,
  IconSwap,
  IconFolderDelete
} from '@arco-design/web-vue/es/icon'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { useI18n } from '@/hooks/web/useI18n'

defineOptions({ name: 'ArcoTabItem' })

const props = defineProps<{
  itemData: RouteLocationNormalizedLoaded
  index: number
}>()

const router = useRouter()
const route = useRoute()
const tagsViewStore = useTagsViewStore()
const { t } = useI18n()

const reload = inject<() => void>('reload', () => {})

const title = computed(() => {
  const raw = (props.itemData?.meta?.title as string) || (props.itemData?.name as string) || ''
  return raw ? (t(raw) as string) || raw : ''
})
const isActive = computed(() => props.itemData.fullPath === route.fullPath)
const isAffix = computed(() => !!props.itemData?.meta?.affix)

const list = computed(() => tagsViewStore.getVisitedViews)
const disabledLeft = computed(() => props.index === 0)
const disabledRight = computed(() => props.index === list.value.length - 1)

const goto = () => {
  if (isActive.value) return
  router.push({ path: props.itemData.path, query: props.itemData.query })
}

const goLatest = () => {
  const latest =
    tagsViewStore.visitedViews[tagsViewStore.visitedViews.length - 1] ||
    tagsViewStore.visitedViews[0]
  if (latest) router.push({ path: latest.path, query: latest.query })
  else router.push('/')
}

const closeSelf = () => {
  tagsViewStore.delView(props.itemData)
  if (isActive.value) goLatest()
}

const onAction = async (val: any) => {
  switch (val) {
    case 'reload':
      tagsViewStore.delCachedView()
      await nextTick()
      reload()
      break
    case 'current':
      closeSelf()
      break
    case 'left':
      tagsViewStore.delLeftViews(props.itemData)
      if (!list.value.some((v) => v.fullPath === route.fullPath)) {
        router.push({ path: props.itemData.path, query: props.itemData.query })
      }
      break
    case 'right':
      tagsViewStore.delRightViews(props.itemData)
      if (!list.value.some((v) => v.fullPath === route.fullPath)) {
        router.push({ path: props.itemData.path, query: props.itemData.query })
      }
      break
    case 'others':
      tagsViewStore.delOthersViews(props.itemData)
      router.push({ path: props.itemData.path, query: props.itemData.query })
      break
    case 'all':
      tagsViewStore.delAllViews()
      goLatest()
      break
  }
}
</script>

<style lang="scss" scoped>
.tab-chip {
  display: inline-flex;
  height: 26px;
  padding: 0 8px 0 12px;
  margin-right: 6px;
  font-size: 13px;
  line-height: 24px;
  color: var(--color-text-2, #4e5969);
  cursor: pointer;
  background-color: var(--color-fill-2, #f7f8fa);
  border: 1px solid transparent;
  border-radius: 4px;
  transition: background-color var(--bm-dur-fast) var(--bm-ease-out), color var(--bm-dur-fast) var(--bm-ease-out), border-color var(--bm-dur-fast) var(--bm-ease-out);
  align-items: center;

  .tab-link {
    color: inherit;
  }

  &:hover {
    background-color: var(--color-fill-3, #f2f3f5);
  }

  .arco-tag-close-btn {
    margin-left: 6px;
    font-size: 12px;
  }

  &.is-active {
    color: rgb(var(--primary-6, 22 93 255));
    background-color: rgb(var(--primary-1, 232 243 255));
    border-color: rgb(var(--primary-3, 148 191 255));

    .tab-link {
      color: rgb(var(--primary-6, 22 93 255));
    }

    .arco-tag-close-btn {
      color: rgb(var(--primary-6, 22 93 255));
    }
  }
}
</style>
