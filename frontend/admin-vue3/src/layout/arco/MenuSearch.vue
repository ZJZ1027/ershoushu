<template>
  <a-modal
    :visible="visible"
    :footer="false"
    :width="560"
    title="命令面板"
    title-align="start"
    modal-class="menu-search-modal"
    @cancel="close"
    @close="close"
  >
    <a-input
      ref="inputRef"
      v-model="keyword"
      placeholder="搜索页面，或执行动作…  ↑↓ 选择，回车执行"
      allow-clear
      @keydown="onKeydown"
    >
      <template #prefix><icon-search /></template>
    </a-input>

    <div v-if="results.flat.length" class="cmd-body">
      <!-- 最近访问分组（仅空搜索时）-->
      <template v-if="results.recent.length">
        <div class="cmd-group">最近访问</div>
        <div
          v-for="(item, i) in results.recent"
          :key="item.key"
          class="cmd-item"
          :class="{ active: i === activeIndex }"
          @mouseenter="activeIndex = i"
          @click="execute(item)"
        >
          <component :is="item.icon" class="cmd-icon" />
          <span class="cmd-title">{{ item.title }}</span>
          <span class="cmd-hint">{{ item.hint }}</span>
        </div>
      </template>

      <!-- 快捷动作分组 -->
      <template v-if="results.actions.length">
        <div class="cmd-group">快捷动作</div>
        <div
          v-for="(item, i) in results.actions"
          :key="item.key"
          class="cmd-item"
          :class="{ active: results.recent.length + i === activeIndex }"
          @mouseenter="activeIndex = results.recent.length + i"
          @click="execute(item)"
        >
          <component :is="item.icon" class="cmd-icon" />
          <span class="cmd-title">{{ item.title }}</span>
          <span class="cmd-hint">{{ item.hint }}</span>
        </div>
      </template>

      <!-- 页面（菜单）分组 -->
      <template v-if="results.menus.length">
        <div class="cmd-group">页面</div>
        <div
          v-for="(item, i) in results.menus"
          :key="item.key"
          class="cmd-item"
          :class="{ active: results.recent.length + results.actions.length + i === activeIndex }"
          @mouseenter="activeIndex = results.recent.length + results.actions.length + i"
          @click="execute(item)"
        >
          <component :is="item.icon" class="cmd-icon" />
          <span class="cmd-title">{{ item.title }}</span>
          <span class="cmd-hint">{{ item.hint }}</span>
        </div>
      </template>
    </div>
    <a-empty v-else description="未找到匹配的页面或动作" />

    <div class="cmd-footer">
      <span><b>↑</b><b>↓</b> 选择</span>
      <span><b>↵</b> 执行</span>
      <span><b>esc</b> 关闭</span>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed, inject, markRaw, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import type { Component } from 'vue'
import { useRouter } from 'vue-router'
import { useFullscreen } from '@vueuse/core'
import { Modal as AModal, Input as AInput, Empty as AEmpty } from '@arco-design/web-vue'
import {
  IconSearch,
  IconPalette,
  IconUser,
  IconMoonFill,
  IconFullscreen,
  IconClockCircle,
  IconRefresh,
  IconClose,
  IconExport
} from '@arco-design/web-vue/es/icon'
import { resolveMenuIcon } from '@/components/Icon'
import { usePermissionStore } from '@/store/modules/permission'
import { useAppStore } from '@/store/modules/app'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { useI18n } from '@/hooks/web/useI18n'
import { useCache } from '@/hooks/web/useCache'
import { loginOut } from '@/api/login'
import { removeToken } from '@/utils/auth'
import { pathResolve } from '@/utils/routerHelper'
import { isUrl } from '@/utils/is'

defineOptions({ name: 'ArcoMenuSearch' })

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'update:visible', v: boolean): void }>()

const router = useRouter()
const permissionStore = usePermissionStore()
const appStore = useAppStore()
const tagsViewStore = useTagsViewStore()
const { wsCache } = useCache()
const { t } = useI18n()

/** 无感刷新：布局层 provide('reload')；命令面板作为其后代注入调用 */
const reloadPage = inject<() => void>('reload', () => {})

/** 面板条目：动作(action) 或 页面(menu) 统一结构，便于键盘在合并列表上导航 */
interface CommandItem {
  key: string
  kind: 'action' | 'menu'
  title: string
  hint: string
  icon: Component
  keywords?: string
  run?: () => void
  path?: string
  url?: boolean
}

const keyword = ref('')
const activeIndex = ref(0)
const inputRef = ref()

/** 标题翻译：remaining 路由在 i18n 就绪前会留下 'router.home' 这类 raw key，渲染时再翻一次 */
const localize = (raw?: string): string => {
  if (!raw) return ''
  const r = t(raw)
  return typeof r === 'string' ? r : raw
}

// ===== 快捷动作（全局、立即生效，不依赖顶栏本地状态）=====
const { toggle: toggleFullScreen } = useFullscreen()

/** 品牌风格：商务蓝 → 靛紫 → 琥珀 → 科技青 → 玫瑰品红 → 石墨灰蓝 → 葡萄紫 循环（与顶栏共用 body[data-brand] + localStorage） */
const toggleBrand = () => {
  const cycle = ['default', 'linear', 'amber', 'teal', 'rose', 'slate', 'violet']
  const cur = document.body.getAttribute('data-brand') || 'default'
  const next = cycle[(cycle.indexOf(cur) + 1) % cycle.length]
  if (next === 'default') {
    document.body.removeAttribute('data-brand')
  } else {
    document.body.setAttribute('data-brand', next)
  }
  localStorage.setItem('bm-brand-style', next)
}

/** 深色 / 浅色：body[arco-theme] 与持久化由 appStore.setIsDark 统一处理 */
const toggleDark = () => {
  appStore.setIsDark(!appStore.getIsDark)
}

/** 关闭其他标签页：保留当前所在页，清掉其余（与 TabItem 右键「关闭其它」一致） */
const closeOtherTabs = () => {
  tagsViewStore.delOthersViews(router.currentRoute.value as any)
}

/** 退出登录：与顶栏头像菜单同一流程（登出接口 + 清 token/缓存 + 跳登录页带 base 前缀） */
const doLogout = async () => {
  try {
    await loginOut()
  } catch {
    /* 登出接口异常不阻断本地清理 */
  }
  removeToken()
  wsCache.clear()
  location.replace(`${import.meta.env.BASE_URL}login`)
}

const actionDefs: CommandItem[] = [
  {
    key: 'act-brand',
    kind: 'action',
    // 七套主题名以前全塞在标题里，条目宽度不够会连右侧的分类提示一起截断成「主…」；
    // 这些名字在 keywords 里已经可搜，标题只保留动作本身。
    title: '切换品牌风格',
    hint: '主题',
    icon: markRaw(IconPalette),
    keywords:
      'linear 靛紫 商务蓝 琥珀 落日 amber 科技青 teal qing 玫瑰 品红 rose meigui 石墨 灰蓝 slate huilan 葡萄紫 violet putaozi 主题 皮肤 品牌 zhuti pifu pinpai theme brand',
    run: toggleBrand
  },
  {
    key: 'act-dark',
    kind: 'action',
    title: '切换深色 / 浅色模式',
    hint: '外观',
    icon: markRaw(IconMoonFill),
    keywords: '深色 浅色 暗色 dark light 外观 waiguan mode',
    run: toggleDark
  },
  {
    key: 'act-fullscreen',
    kind: 'action',
    title: '全屏 / 退出全屏',
    hint: '视图',
    icon: markRaw(IconFullscreen),
    keywords: '全屏 quanping fullscreen 视图',
    run: toggleFullScreen
  },
  {
    key: 'act-reload',
    kind: 'action',
    title: '刷新当前页',
    hint: '视图',
    icon: markRaw(IconRefresh),
    keywords: '刷新 重载 reload refresh shuaxin',
    run: () => reloadPage()
  },
  {
    key: 'act-close-others',
    kind: 'action',
    title: '关闭其他标签页',
    hint: '标签',
    icon: markRaw(IconClose),
    keywords: '关闭标签 关闭其他 close tabs guanbi biaoqian',
    run: closeOtherTabs
  },
  {
    key: 'act-profile',
    kind: 'action',
    title: '前往个人中心',
    hint: '账户',
    icon: markRaw(IconUser),
    keywords: '个人中心 gerenzhongxin profile 账户 zhanghu',
    run: () => router.push('/user/profile')
  },
  {
    key: 'act-logout',
    kind: 'action',
    title: '退出登录',
    hint: '账户',
    icon: markRaw(IconExport),
    keywords: '退出 登出 注销 logout exit tuichu',
    run: doLogout
  }
]

/** 把后端动态路由树打平为「可跳转的叶子页面」列表（与侧边栏同一数据源、同一隐藏规则） */
const allMenus = computed<CommandItem[]>(() => {
  const list: CommandItem[] = []
  const seen = new Set<string>()
  // parentIcon：叶子自己没配图标时沿用最近的上级目录图标，与侧栏「单子项提升」时的
  // `子.icon || 父.icon` 一致，避免这里退化成一排默认图标
  const walk = (nodes: any[], parentPath: string, crumbs: string[], parentIcon?: string) => {
    for (const n of nodes || []) {
      if (n?.meta?.hidden) continue
      const full = isUrl(n.path) ? n.path : pathResolve(parentPath, n.path)
      const title = localize(n?.meta?.title as string) || (n?.name as string) || full
      const icon = (n?.meta?.icon as string) || parentIcon
      const visibleChildren = (n.children || []).filter((c: any) => !c?.meta?.hidden)
      if (visibleChildren.length > 0) {
        walk(visibleChildren, full, [...crumbs, title], icon)
      } else if (!seen.has(full)) {
        seen.add(full)
        list.push({
          key: `menu-${full}`,
          kind: 'menu',
          title,
          hint: crumbs.join(' / '),
          icon: markRaw(resolveMenuIcon(icon)),
          path: full,
          url: isUrl(full)
        })
      }
    }
  }
  walk((permissionStore.getRouters as any[]) || [], '', [])
  return list
})

// ===== 最近访问（Linear Recent）：全局监听路由，记录去过的正经菜单页到 localStorage =====
const RECENT_KEY = 'bm-cmd-recent'
const RECENT_MAX = 6
interface RecentEntry {
  path: string
  title: string
  hint: string
}
const recentRaw = ref<RecentEntry[]>([])

const loadRecent = () => {
  try {
    const arr = JSON.parse(localStorage.getItem(RECENT_KEY) || '[]')
    recentRaw.value = Array.isArray(arr) ? arr : []
  } catch {
    recentRaw.value = []
  }
}

/** 只记录能在菜单树里命中的叶子页面（过滤详情页/无标题页），最新在前、按 path 去重 */
const recordRecent = (path: string) => {
  const hit = allMenus.value.find((m) => m.path === path)
  if (!hit) return
  const entry: RecentEntry = { path, title: hit.title, hint: hit.hint }
  const next = [entry, ...recentRaw.value.filter((r) => r.path !== path)].slice(0, RECENT_MAX)
  recentRaw.value = next
  try {
    localStorage.setItem(RECENT_KEY, JSON.stringify(next))
  } catch {
    /* localStorage 不可用时忽略 */
  }
}

/** 展示用：转成 CommandItem，并剔除当前所在页（避免置顶自己） */
const recentItems = computed<CommandItem[]>(() =>
  recentRaw.value
    .filter((r) => r.path !== router.currentRoute.value.path)
    .map((r) => ({
      key: `recent-${r.path}`,
      kind: 'menu' as const,
      title: r.title,
      hint: r.hint,
      // 与「页面」分组同用菜单自己的图标；分组名已经说明是最近访问，不必再拿时钟占位。
      // 菜单被删或权限收回后仍留在本地记录里，此时才回落时钟
      icon: allMenus.value.find((m) => m.path === r.path)?.icon || markRaw(IconClockCircle),
      path: r.path,
      url: isUrl(r.path)
    }))
)

const results = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const matchAction = (a: CommandItem) =>
    !kw || a.title.toLowerCase().includes(kw) || (a.keywords || '').toLowerCase().includes(kw)
  const matchMenu = (m: CommandItem) =>
    !kw ||
    m.title.toLowerCase().includes(kw) ||
    (m.path || '').toLowerCase().includes(kw) ||
    m.hint.toLowerCase().includes(kw)

  const recent = kw ? [] : recentItems.value
  const actions = actionDefs.filter(matchAction)
  const menus = allMenus.value.filter(matchMenu).slice(0, 50)
  return { recent, actions, menus, flat: [...recent, ...actions, ...menus] }
})

let unregisterAfterEach: (() => void) | null = null
onMounted(() => {
  loadRecent()
  recordRecent(router.currentRoute.value.path)
  unregisterAfterEach = router.afterEach((to) => recordRecent(to.path))
})
onBeforeUnmount(() => {
  unregisterAfterEach?.()
})

watch(results, () => (activeIndex.value = 0))

watch(
  () => props.visible,
  (v) => {
    if (v) {
      keyword.value = ''
      activeIndex.value = 0
      nextTick(() => inputRef.value?.focus?.())
    }
  }
)

const close = () => emit('update:visible', false)

const execute = (item?: CommandItem) => {
  if (!item) return
  if (item.kind === 'action') {
    item.run?.()
    close()
    return
  }
  if (item.url) {
    window.open(item.path as string, '_blank')
  } else {
    router.push(item.path as string)
  }
  close()
}

const onKeydown = (e: KeyboardEvent) => {
  const len = results.value.flat.length
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    if (len) activeIndex.value = (activeIndex.value + 1) % len
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    if (len) activeIndex.value = (activeIndex.value - 1 + len) % len
  } else if (e.key === 'Enter') {
    e.preventDefault()
    execute(results.value.flat[activeIndex.value])
  }
}
</script>

<style lang="scss" scoped>
.cmd-body {
  max-height: 380px;
  margin-top: 12px;
  overflow-y: auto;
}

.cmd-group {
  padding: 8px 12px 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.4px;
  color: var(--bm-text-3);
  text-transform: uppercase;
}

.cmd-item {
  display: flex;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color var(--bm-dur-fast) var(--bm-ease-out);
  align-items: center;
  gap: 10px;

  .cmd-icon {
    font-size: 16px;
    color: var(--bm-text-3);
    flex-shrink: 0;
  }

  .cmd-title {
    min-width: 0;
    overflow: hidden;
    font-size: 14px;
    color: var(--color-text-1, #1d2129);
    text-overflow: ellipsis;
    white-space: nowrap;

    /* 空间不够时优先截断标题，别去截右侧那几个字的分类提示 */
    flex: 1 1 auto;
  }

  .cmd-hint {
    max-width: 50%;
    padding-left: 12px;
    margin-left: auto;
    overflow: hidden;
    font-size: 12px;
    color: var(--bm-text-3);
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 0 0 auto;
  }

  &.active {
    background-color: rgb(var(--primary-1, 232 243 255));

    .cmd-icon,
    .cmd-title {
      color: rgb(var(--primary-6, 22 93 255));
    }
  }
}

.cmd-footer {
  display: flex;
  padding-top: 10px;
  margin-top: 8px;
  font-size: 12px;
  color: var(--bm-text-3);
  border-top: 1px solid var(--color-border-2, #e5e6eb);
  gap: 16px;

  b {
    display: inline-block;
    min-width: 18px;
    padding: 0 4px;
    margin-right: 3px;
    font-weight: 500;
    line-height: 18px;
    text-align: center;
    background: var(--color-fill-2, #f2f3f5);
    border-radius: 4px;
  }
}
</style>
