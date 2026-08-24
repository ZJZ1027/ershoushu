<template>
  <div class="arco-breadcrumb-bar">
    <a-breadcrumb class="container-breadcrumb">
      <template #separator>
        <icon-right class="bc-sep" />
      </template>
      <!-- 首级回首页：实心小房子（线框 IconHome 在 16px 易发虚，填充版更利落） -->
      <a-breadcrumb-item class="bc-item bc-home clickable" @click="goHome">
        <svg class="bc-home-fill" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <path
            fill="currentColor"
            d="M24 7.5 6.5 21.2a1.5 1.5 0 0 0-.5 1.1V41a2 2 0 0 0 2 2h12.5V29.5a1.5 1.5 0 0 1 1.5-1.5h4a1.5 1.5 0 0 1 1.5 1.5V43H40a2 2 0 0 0 2-2V22.3a1.5 1.5 0 0 0-.5-1.1L24 7.5Z"
          />
        </svg>
      </a-breadcrumb-item>
      <a-breadcrumb-item
        v-for="(item, i) in crumbItems"
        :key="i"
        class="bc-item"
        :class="{ clickable: isClickable(item, i) }"
        @click="onClick(item, i)"
      >
        <component :is="resolveMenuIcon(item.icon)" v-if="item.icon" class="bc-icon" />
        <span>{{ item.label }}</span>
      </a-breadcrumb-item>
    </a-breadcrumb>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRouter, type RouteRecordRaw } from 'vue-router'
import { Breadcrumb as ABreadcrumb, BreadcrumbItem as ABreadcrumbItem } from '@arco-design/web-vue'
import { IconRight } from '@arco-design/web-vue/es/icon'
import { useI18n } from '@/hooks/web/useI18n'
import { usePermissionStore } from '@/store/modules/permission'
import { pathResolve } from '@/utils/routerHelper'
import { isUrl } from '@/utils/is'
import { resolveMenuIcon } from '@/components/Icon'

defineOptions({ name: 'ArcoBreadcrumb' })

interface Crumb {
  label: string
  to?: string
  icon?: string
}

const router = useRouter()
const { t } = useI18n()
const permissionStore = usePermissionStore()

/** 按完整 path 在动态菜单树里找 meta.icon（子路由 path 多为相对路径，需 pathResolve） */
const findRouteIcon = (path?: string): string | undefined => {
  if (!path) return undefined
  const walk = (list: RouteRecordRaw[], parentPath: string): string | undefined => {
    for (const r of list) {
      const full = isUrl(r.path) ? r.path : pathResolve(parentPath, r.path || '')
      if (full === path) return r.meta?.icon as string | undefined
      if (r.children?.length) {
        const hit = walk(r.children, full)
        if (hit) return hit
      }
    }
    return undefined
  }
  // 动态菜单存的是 AppRouteRecordRaw（children 可选），结构上与 RouteRecordRaw 不兼容，
  // 这里只读 path/meta/children，做一次断言即可，不改动路由数据结构
  return walk((permissionStore.getRouters || []) as unknown as RouteRecordRaw[], '')
}

const localize = (raw?: string) => (raw ? (t(raw) as string) || raw : '')

const items = computed<Crumb[]>(() => {
  const matched = router.currentRoute.value.matched || []
  const crumbs: Crumb[] = []

  const leaf = matched[matched.length - 1]
  const parents = (leaf?.meta?.parents as { title: string; path: string; icon?: string }[] | undefined) || []
  parents.forEach((p) =>
    crumbs.push({
      label: localize(p.title),
      to: p.path,
      icon: p.icon || findRouteIcon(p.path)
    })
  )

  const visible = matched.filter(
    (m) =>
      m.meta?.title &&
      m.meta?.breadcrumb !== false &&
      (!m.meta?.hidden || m === leaf)
  )
  visible.forEach((m, idx) => {
    const isLast = idx === visible.length - 1
    const redirect = typeof m.redirect === 'string' ? m.redirect : undefined
    crumbs.push({
      label: localize(m.meta?.title as string),
      to: isLast ? undefined : redirect,
      icon: m.meta?.icon as string | undefined
    })
  })

  return crumbs.filter((v, i) => i === 0 || v.label !== crumbs[i - 1].label)
})

const crumbItems = computed<Crumb[]>(() => {
  const list = items.value
  const homeLabel = localize('router.home')
  return list.length && list[0].label === homeLabel ? list.slice(1) : list
})

const goHome = () => {
  if (router.currentRoute.value.path !== '/index') router.push('/index')
}

const isClickable = (item: Crumb, i: number) => !!item.to && i !== crumbItems.value.length - 1

const onClick = (item: Crumb, i: number) => {
  if (isClickable(item, i)) router.push(item.to as string)
}
</script>

<style lang="scss" scoped>
.arco-breadcrumb-bar {
  display: flex;
  align-items: center;
  min-width: 0;
  background: transparent;
}

.container-breadcrumb {
  display: flex;
  min-width: 0;
  white-space: nowrap;
  align-items: center;
  flex-wrap: nowrap;

  :deep(.arco-breadcrumb-item) {
    display: inline-flex;
    padding: 2px 4px;
    font-size: 13px;
    line-height: 1.4;
    color: var(--bm-text-3);
    border-radius: 4px;
    transition: color var(--bm-dur-fast) var(--bm-ease-out);
    align-items: center;
    gap: 4px;

    &:last-child {
      font-weight: 500;
      color: var(--color-text-1, #1d2129);
    }
  }

  :deep(.arco-breadcrumb-item.clickable) {
    cursor: pointer;

    &:hover {
      color: var(--color-text-1, #1d2129);
    }
  }

  :deep(.bc-icon) {
    font-size: 16px; /* 偶数尺寸，减少 1x 屏亚像素发虚 */
    flex-shrink: 0;
  }

  :deep(.bc-home) {
    display: inline-flex;
    align-items: center;
    padding: 2px 4px;
    color: var(--color-text-2, #4e5969);

    &:hover {
      color: rgb(var(--primary-6, 22 93 255));
    }

    .bc-home-fill {
      display: block;
      width: 18px;
      height: 18px;
      flex-shrink: 0;

      /* 房子视觉重心偏下，几何居中会显「坠」，上移 1px 与文字齐平 */
      transform: translateY(-1px);
    }
  }

  :deep(.arco-breadcrumb-item-separator) {
    display: inline-flex;
    margin: 0 2px;
    color: var(--color-text-4, #c9cdd4);
    align-items: center;

    .bc-sep {
      font-size: 12px;
    }
  }

}
</style>
