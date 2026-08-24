<script lang="tsx">
import { defineComponent, ref, computed, watch, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Menu as AMenu,
  MenuItem as AMenuItem,
  SubMenu as ASubMenu,
  Button as AButton,
  Tooltip as ATooltip
} from '@arco-design/web-vue'
import { IconMenuFold, IconMenuUnfold, IconPushpin } from '@arco-design/web-vue/es/icon'
import { usePermissionStore } from '@/store/modules/permission'
import { useAppStore } from '@/store/modules/app'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'
import { pathResolve } from '@/utils/routerHelper'
import { isUrl } from '@/utils/is'
import { useI18n } from '@/hooks/web/useI18n'
import { resolveMenuIcon } from '@/components/Icon'

/**
 * Arco 风侧栏菜单：基于 basepro 后台动态路由 (`permissionStore.routers`) 渲染 `a-menu`。
 * 这里只负责渲染外壳，菜单项的可见范围由权限过滤后的路由表与 `meta.hidden` 决定。
 */
export default defineComponent({
  name: 'ArcoSideMenu',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const permissionStore = usePermissionStore()
    const appStore = useAppStore()
    const badgeStore = useBusinessBadgeStore()
    const { t } = useI18n()

    // 标题翻译：basepro 的 remaining 路由在模块顶层调 t() 时 i18n 还未就绪，
    // 留下 'router.home' 这种 raw key，需要在 render 时再翻译一次
    const localize = (raw?: string) => {
      if (!raw) return ''
      const result = t(raw)
      // i18n 找不到 key 会返回 raw —— 此时直接显示 raw
      return typeof result === 'string' ? result : raw
    }

    // 移动端用 overlay 抽屉承载完整菜单，不走 48px 折叠态（getCollapse 在移动端被强制 true，
    // 这里显式忽略，保证抽屉里始终是展开的完整菜单）
    const collapsed = computed(() => !appStore.getMobile && appStore.getCollapse)

    const routers = computed(() => permissionStore.getRouters || [])

    /**
     * 顶级菜单分组：仅前端展示层按 path「归类」，组内顺序完全跟随后端菜单排序
     * （菜单管理里配置的 sort），前端不做二次排序。未收录的顶级路径按原顺序
     * 追加到「系统」组末尾，保证新菜单不丢。
     */
    const MENU_GROUPS: { label: string; paths: string[] }[] = [
      { label: '', paths: ['/'] }, // 首页，不加组名
      { label: '系统', paths: ['/system', '/infra'] }
      // 新增业务模块时在此加一组，例如 { label: '业务', paths: ['/order', '/product'] }
    ]

    const topPath = (n: any) => (isUrl(n?.path) ? n.path : pathResolve('', n?.path || ''))

    const groupedRoot = computed(() => {
      const nodes = (routers.value as any[]).filter((n) => !n?.meta?.hidden)
      const groups: { label: string; items: any[] }[] = MENU_GROUPS.map((g) => ({
        label: g.label,
        items: []
      }))
      const rest: any[] = []
      // 按后端返回顺序逐个归组，组内自然保持 sort 顺序
      for (const n of nodes) {
        const p = topPath(n)
        const gi = MENU_GROUPS.findIndex((g) => g.paths.includes(p))
        if (gi >= 0) groups[gi].items.push(n)
        else rest.push(n)
      }
      if (rest.length) groups[groups.length - 1].items.push(...rest)
      return groups.filter((g) => g.items.length)
    })

    const openKeys = ref<string[]>([])
    const selectedKeys = ref<string[]>([])

    // 把后端动态路由树打平，方便用 path 反查
    const flatRoutes = computed(() => {
      const list: { path: string; parents: string[] }[] = []
      const walk = (nodes: any[], parentPath: string, parents: string[]) => {
        for (const n of nodes || []) {
          if (n?.meta?.hidden) continue
          const full = isUrl(n.path) ? n.path : pathResolve(parentPath, n.path)
          const next = [...parents, full]
          list.push({ path: full, parents: next })
          if (n.children?.length) walk(n.children, full, next)
        }
      }
      walk(routers.value as any[], '', [])
      return list
    })

    // 路由变化时，自动展开 + 选中。
    // 详情页挂在 hidden 父级下、路径不在菜单里，直接按 route.path 会算不出高亮，
    // 故优先取 meta.activeMenu，其次取 meta.parents 末级（面包屑已指向对应列表菜单），最后才回落 route.path。
    const syncSelected = () => {
      const meta: any = route.meta || {}
      const parents = Array.isArray(meta.parents) ? meta.parents : []
      const activePath =
        (meta.activeMenu as string) ||
        (parents.length ? parents[parents.length - 1]?.path : '') ||
        route.path
      const hit =
        flatRoutes.value.find((r) => r.path === activePath) ||
        flatRoutes.value.find((r) => r.path === route.path)
      if (hit) {
        selectedKeys.value = [hit.path]
        // 合并已展开的 + 新路径父链，避免折叠用户已经展开的兄弟项
        const merged = new Set([...openKeys.value, ...hit.parents.slice(0, -1)])
        openKeys.value = [...merged]
      } else {
        selectedKeys.value = [activePath]
      }
    }

    watch(
      () => route.path,
      () => syncSelected(),
      { immediate: true }
    )

    const goto = (path: string) => {
      if (isUrl(path)) {
        window.open(path, '_blank')
        return
      }
      if (route.path === path) return
      router.push(path)
    }

    // 渲染图标：与菜单管理的选择器共用同一张 Arco 图标注册表，所配置即所显示
    const renderIcon = (icon?: string) => {
      const Comp = resolveMenuIcon(icon)
      return () => h(Comp)
    }

    const formatBadge = (n: number) => (n > 99 ? '99+' : String(n))

    const renderTitle = (title: string, path: string) => {
      const n = badgeStore.countOf(path)
      return (
        <>
          <span class="menu-title-wrap">
            <span class="menu-title-text">{title}</span>
            {n > 0 ? <span class="menu-badge">{formatBadge(n)}</span> : null}
          </span>
          {n > 0 ? <span class="menu-badge menu-badge-float">{formatBadge(n)}</span> : null}
        </>
      )
    }

    // 单层递归渲染：目录 -> SubMenu，叶子 -> MenuItem
    const renderNodes = (nodes: any[], parentPath: string) => {
      const result: any[] = []
      for (const n of nodes || []) {
        if (n?.meta?.hidden) continue
        const full = isUrl(n.path) ? n.path : pathResolve(parentPath, n.path)
        const visibleChildren = (n.children || []).filter((c: any) => !c?.meta?.hidden)

        // 父目录下只有一个可见子项、且该子项自身没有下级时，直接把它提升为根级菜单项，
        // 消除「XX 管理 > XX 管理」这种冗余两层（单子项一律不再展开子菜单，
        // 不再受 alwaysShow 影响；若唯一子项自身还有下级，则保留为子菜单避免吞掉孙级）
        const soleChild = visibleChildren.length === 1 ? visibleChildren[0] : null
        const soleChildHasChildren =
          !!soleChild && (soleChild.children || []).some((g: any) => !g?.meta?.hidden)
        const onlyOneShown = !!soleChild && !soleChildHasChildren
        if (onlyOneShown) {
          const c = visibleChildren[0]
          const cFull = isUrl(c.path) ? c.path : pathResolve(full, c.path)
          const cTitle =
            localize((c?.meta?.title as string) || (n?.meta?.title as string)) ||
            (c?.name as string) ||
            cFull
          result.push(
            <AMenuItem
              key={cFull}
              v-slots={{ icon: renderIcon(c?.meta?.icon || n?.meta?.icon) }}
              onClick={() => goto(cFull)}
            >
              {renderTitle(cTitle, cFull)}
            </AMenuItem>
          )
          continue
        }

        const title =
          localize(n?.meta?.title as string) || (n?.name as string) || full
        if (visibleChildren.length > 0) {
          result.push(
            <ASubMenu
              key={full}
              v-slots={{
                icon: renderIcon(n?.meta?.icon),
                title: () => renderTitle(title, full)
              }}
            >
              {renderNodes(visibleChildren, full)}
            </ASubMenu>
          )
        } else {
          result.push(
            <AMenuItem
              key={full}
              v-slots={{ icon: renderIcon(n?.meta?.icon) }}
              onClick={() => goto(full)}
            >
              {renderTitle(title, full)}
            </AMenuItem>
          )
        }
      }
      return result
    }

    // 顶级按分组渲染：展开态显示小号组名，折叠态改为细分隔线（首组前不加）
    const renderGrouped = () => {
      const out: any[] = []
      groupedRoot.value.forEach((g, gi) => {
        if (collapsed.value) {
          if (gi > 0) out.push(<div class="menu-group-divider" key={`gd-${gi}`}></div>)
        } else if (g.label) {
          out.push(
            <div class="menu-group-label" key={`gl-${gi}`}>
              {g.label}
            </div>
          )
        }
        out.push(...renderNodes(g.items, ''))
      })
      return out
    }

    return () => (
      <div class="side-menu-wrap">
        <AMenu
          mode="vertical"
          collapsed={collapsed.value}
          selectedKeys={selectedKeys.value}
          openKeys={openKeys.value}
          showCollapseButton={false}
          levelIndent={28}
          style="flex: 1 1 0; min-height: 0; width: 100%; border-right: none;"
          onMenuItemClick={(key: string) => (selectedKeys.value = [key])}
          onSubMenuClick={(_key: string, keys: string[]) => (openKeys.value = keys)}
        >
          {renderGrouped()}
        </AMenu>
        {/* 底部操作条：折叠 + 固定（对齐 Arco Pro 侧栏底） */}
        {!appStore.getMobile && (
          <div class={['side-menu-foot', { 'is-collapsed': collapsed.value }]}>
            <ATooltip
              content={collapsed.value ? '展开侧栏' : '收起侧栏'}
              position="top"
              mini
            >
              <AButton
                class="foot-btn"
                type="text"
                onClick={() => appStore.setCollapse(!collapsed.value)}
                v-slots={{
                  icon: () => (collapsed.value ? <IconMenuUnfold /> : <IconMenuFold />)
                }}
              />
            </ATooltip>
            {!collapsed.value && (
              <ATooltip
                content={appStore.getFixedMenu ? '取消固定侧栏' : '固定侧栏'}
                position="top"
                mini
              >
                <AButton
                  class={['foot-btn', { 'is-pinned': appStore.getFixedMenu }]}
                  type="text"
                  onClick={() => appStore.setFixedMenu(!appStore.getFixedMenu)}
                  v-slots={{
                    icon: () => <IconPushpin />
                  }}
                />
              </ATooltip>
            )}
          </div>
        )}
      </div>
    )
  }
})
</script>

<style lang="scss" scoped>
/* 侧栏纵向骨架：菜单占满剩余高度自行滚动，底部折叠横条常驻不随滚 */
.side-menu-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.side-menu-foot {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 10px;
  border-top: 1px solid var(--color-border-2, #e5e6eb);

  &.is-collapsed {
    justify-content: center;
    padding: 4px 0;
  }

  .foot-btn {
    width: 28px;
    height: 28px;
    font-size: 14px;
    color: var(--bm-text-3);
    border-radius: 4px;

    &:hover {
      color: var(--color-text-1, #1d2129);
      background-color: var(--color-fill-2, #f2f3f5);
    }

    &.is-pinned {
      color: rgb(var(--primary-6, 22 93 255));
      background-color: var(--bm-brand-bg, rgb(22 93 255 / 8%));
    }
  }
}

:deep(.arco-menu-light) {
  /* 跟侧栏同色；勿用 Arco 写死的 --color-menu-light-bg（暗色品牌下会是中性灰 #232324） */
  background-color: var(--color-bg-2, var(--color-menu-light-bg, #fff));
}

/* ===== 顶级菜单分组 ===== */

/* 展开态组名：小号弱灰大写风格，与 Linear/Arco Pro 侧栏一致 */
:deep(.menu-group-label) {
  padding: 14px 12px 6px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1;
  letter-spacing: 1px;
  color: var(--bm-text-3);
  user-select: none;
}

/* 折叠态（48px）：组名放不下，退化为居中细分隔线 */
:deep(.menu-group-divider) {
  width: 24px;
  height: 1px;
  margin: 8px auto;
  background: var(--color-border-2, #e5e6eb);
}

:deep(.arco-menu) {
  overflow-x: hidden;
}

:deep(.arco-menu-inner) {
  padding: 8px;
  overflow-x: hidden;
}

/* 折叠态收窄内边距，避免 48px 宽度下图标项溢出产生横向滚动条 */
:deep(.arco-menu-collapsed .arco-menu-inner) {
  padding: 8px 4px;
}

:deep(.arco-menu-item),
:deep(.arco-menu-pop-header),
:deep(.arco-menu-inline-header) {
  position: relative;
  height: 40px;
  margin-bottom: 2px;
  font-size: 14px;
  line-height: 40px;
  border-radius: 6px;
}

:deep(.menu-title-wrap) {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-width: 0;
  gap: 8px;
}

:deep(.menu-title-text) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.menu-badge) {
  flex: none;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 11px;
  font-weight: 600;
  line-height: 16px;
  text-align: center;
  color: #fff;
  background: #f53f3f;
  border-radius: 8px;
}

:deep(.arco-menu-item .arco-menu-title),
:deep(.arco-menu-inline-header .arco-menu-title) {
  flex: 1;
  min-width: 0;
}

:deep(.arco-menu-collapsed .menu-title-wrap .menu-badge) {
  display: none;
}

:deep(.menu-badge-float) {
  display: none;
}

:deep(.arco-menu-collapsed .menu-badge-float) {
  display: block;
  position: absolute;
  top: 4px;
  right: 2px;
  z-index: 2;
}

/* 覆盖 Arco `.arco-menu-light .arco-menu-item { background: var(--color-menu-light-bg) }`：
   暗色品牌下该变量仍是中性灰 #232324，会在有色侧栏上叠出灰药丸块。 */
:deep(.arco-menu-light .arco-menu-item),
:deep(.arco-menu-light .arco-menu-pop-header),
:deep(.arco-menu-light .arco-menu-inline-header) {
  background-color: transparent;
}

/* 悬停（未选中）：浅灰底 */
:deep(.arco-menu-item:not(.arco-menu-selected):hover),
:deep(.arco-menu-pop-header:hover),
:deep(.arco-menu-inline-header:not(.arco-menu-selected):hover) {
  background-color: var(--color-fill-2, #f7f8fa) !important;
}

/* 选中路径上的父级目录：安静版——品牌色文字+图标标记归属，不填底色不加投影，
   把唯一的视觉焦点留给真正选中的子项（品牌色文字+左侧竖条）。
   多级场景下祖先链整条同款着色，即「选中路径」惯例（Arco Pro / Linear 同款）。 */
:deep(.arco-menu-inline-header.arco-menu-selected) {
  font-weight: 500;

  /* 分组标题/叶子标签都是 14px 正文号，饱和 -6 档当文字撑不住：靛紫主题实测 3.87，
     琥珀这类暖色主题的 -6 更低。文字统一走 --bm-brand-text（-7 档，暗色下自动翻亮），
     只有下面那根 3px 竖条属纯装饰、继续用 -6 保持醒目。 */
  color: var(--bm-brand-text) !important;
  background: transparent !important;
}

:deep(.arco-menu-inline-header.arco-menu-selected:hover) {
  background-color: var(--color-fill-2, #f7f8fa) !important;
}

:deep(.arco-menu-inline-header.arco-menu-selected .arco-menu-icon),
:deep(.arco-menu-inline-header.arco-menu-selected .arco-menu-icon .arco-icon),
:deep(.arco-menu-inline-header.arco-menu-selected .arco-menu-title),
:deep(.arco-menu-inline-header.arco-menu-selected .arco-menu-icon-suffix),
:deep(.arco-menu-inline-header.arco-menu-selected .arco-menu-icon-suffix .arco-icon),
:deep(.arco-menu-inline-header.arco-menu-selected svg) {
  color: var(--bm-brand-text) !important;
}

/* 选中的菜单项（叶子）：白底 + 蓝字蓝图标 + 左侧竖条 */
:deep(.arco-menu-item.arco-menu-selected) {
  font-weight: 500;
  color: var(--bm-brand-text) !important;
  background-color: transparent !important;
}

:deep(.arco-menu-item.arco-menu-selected:hover) {
  background-color: var(--color-fill-1, #f7f8fa) !important;
}

:deep(.arco-menu-item.arco-menu-selected .arco-menu-icon),
:deep(.arco-menu-item.arco-menu-selected .arco-menu-icon .arco-icon),
:deep(.arco-menu-item.arco-menu-selected svg) {
  color: var(--bm-brand-text) !important;
}

:deep(.arco-menu-item.arco-menu-selected::before) {
  position: absolute;
  top: 50%;
  left: 0;
  z-index: 1;
  width: 3px;
  height: 18px;
  background-color: rgb(var(--primary-6, 22 93 255));
  border-radius: 0 4px 4px 0;
  content: '';
  transform: translateY(-50%);
}

/* 折叠态（48px）：去掉竖条，避免溢出 */
:deep(.arco-menu-collapsed .arco-menu-item.arco-menu-selected::before) {
  display: none;
}
</style>

<!-- 非 scoped：
     1) 侧栏滚动条统一为「细」样式（此前只有 linear 主题配了细条，默认主题露原生粗条）；
     2) 折叠态弹出的二级菜单被 Arco teleport 到 body 成 .arco-trigger-menu（脱离侧栏 scoped
        作用域），选中态默认与主菜单不一致——这里对齐主菜单：叶子=品牌色文字+左竖条，
        子目录头=品牌渐变+白字。配色统一走 --bm-brand / --primary-6，随主题（商务蓝/靛紫）自动切换。 -->
<style lang="scss">
/* ===== 1) 侧栏滚动条统一细样式（两主题一致） ===== */
.arco-menu-inner,
.arco-menu-inline-content,
.arco-app-sider .arco-layout-sider-children {
  scrollbar-width: thin; /* Firefox */
  scrollbar-color: var(--bm-border, #e5e6eb) transparent;
}

.arco-menu-inner::-webkit-scrollbar,
.arco-menu-inline-content::-webkit-scrollbar,
.arco-app-sider .arco-layout-sider-children::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.arco-menu-inner::-webkit-scrollbar-thumb,
.arco-menu-inline-content::-webkit-scrollbar-thumb,
.arco-app-sider .arco-layout-sider-children::-webkit-scrollbar-thumb {
  background: var(--bm-border, #e5e6eb);
  border-radius: 4px;
}

.arco-menu-inner::-webkit-scrollbar-thumb:hover,
.arco-menu-inline-content::-webkit-scrollbar-thumb:hover,
.arco-app-sider .arco-layout-sider-children::-webkit-scrollbar-thumb:hover {
  background: var(--bm-text-4, #c9cdd4);
}

.arco-menu-inner::-webkit-scrollbar-track,
.arco-menu-inline-content::-webkit-scrollbar-track,
.arco-app-sider .arco-layout-sider-children::-webkit-scrollbar-track {
  background: transparent;
}

/* ===== 2) 折叠态弹出子菜单（.arco-trigger-menu）选中态对齐主菜单 ===== */

/* 叶子选中：透明底 + 品牌色文字 + 左侧竖条 */
.arco-trigger-menu .arco-trigger-menu-item.arco-trigger-menu-selected {
  position: relative;
  font-weight: 500;
  color: var(--bm-brand-text) !important;
  background-color: transparent !important;
}

.arco-trigger-menu .arco-trigger-menu-item.arco-trigger-menu-selected .arco-trigger-menu-icon,
.arco-trigger-menu
  .arco-trigger-menu-item.arco-trigger-menu-selected
  .arco-trigger-menu-icon
  .arco-icon,
.arco-trigger-menu .arco-trigger-menu-item.arco-trigger-menu-selected svg {
  color: var(--bm-brand-text) !important;
}

.arco-trigger-menu .arco-trigger-menu-item.arco-trigger-menu-selected::before {
  position: absolute;
  top: 50%;
  left: 0;
  width: 3px;
  height: 18px;
  background-color: rgb(var(--primary-6, 22 93 255));
  border-radius: 0 4px 4px 0;
  content: '';
  transform: translateY(-50%);
}

.arco-trigger-menu .arco-trigger-menu-item.arco-trigger-menu-selected:hover {
  background-color: var(--bm-fill-light, #f7f8fa) !important;
}

/* 子目录头选中（三级菜单时）：与主菜单父级同款安静版——品牌色文字、无填色 */
.arco-trigger-menu .arco-trigger-menu-pop-header.arco-trigger-menu-selected {
  font-weight: 500;
  color: var(--bm-brand-text) !important;
  background: transparent !important;
}

.arco-trigger-menu .arco-trigger-menu-pop-header.arco-trigger-menu-selected:hover {
  background-color: var(--bm-fill-light, #f7f8fa) !important;
}

.arco-trigger-menu .arco-trigger-menu-pop-header.arco-trigger-menu-selected .arco-trigger-menu-icon,
.arco-trigger-menu
  .arco-trigger-menu-pop-header.arco-trigger-menu-selected
  .arco-trigger-menu-icon
  .arco-icon,
.arco-trigger-menu
  .arco-trigger-menu-pop-header.arco-trigger-menu-selected
  .arco-trigger-menu-icon-suffix,
.arco-trigger-menu .arco-trigger-menu-pop-header.arco-trigger-menu-selected svg {
  color: var(--bm-brand-text) !important;
}
</style>
