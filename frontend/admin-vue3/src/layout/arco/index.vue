<template>
  <a-layout class="arco-app-layout">
    <!-- 顶部固定 NavBar -->
    <a-layout-header class="arco-app-header">
      <NavBar />
    </a-layout-header>

    <a-layout class="arco-app-body">
      <!-- 侧栏（桌面端常驻；移动端改用下方 overlay 抽屉） -->
      <a-layout-sider
        v-if="!mobile"
        class="arco-app-sider"
        :width="siderWidth"
        :collapsed-width="48"
        :collapsed="collapsed"
        :collapsible="true"
        :hide-trigger="true"
        breakpoint="xl"
        @collapse="setCollapsed"
      >
        <SideMenu />
      </a-layout-sider>

      <!-- 主体（面包屑已并入顶栏 NavBar，主体不再有独立导航行） -->
      <a-layout class="arco-app-main">
        <a-layout-content class="arco-app-content">
          <div ref="scrollRef" class="content-scroll" @scroll="onScroll">
            <!-- 页面切换动画：不能用 <transition> 包页面组件——basepro 系统管理等页面
                 是多根 Fragment，Transition 拿不到单根元素时 out-in 离场会卡死导致切页空白。
                 改为在常驻包裹层上重放 CSS 入场动画（见 fullPath watcher），对页面结构零要求，
                 keep-alive 缓存也不受影响。 -->
            <div ref="pageHolderRef" class="page-holder page-anim">
              <router-view v-if="routerAlive">
                <template #default="{ Component, route }">
                  <keep-alive :include="cacheList">
                    <component :is="Component" :key="route.fullPath" />
                  </keep-alive>
                </template>
              </router-view>
            </div>
          </div>
          <ScrollWheel
            :visible="canScroll"
            :direction="scrollDir"
            @up="scrollByDir('up')"
            @down="scrollByDir('down')"
          />
        </a-layout-content>
        <a-layout-footer v-if="showFooter" class="arco-app-footer">
          <Footer />
        </a-layout-footer>
      </a-layout>
    </a-layout>

    <!-- 移动端 overlay 抽屉侧栏：折叠按钮唤起，点选菜单或路由变化后自动关闭 -->
    <a-drawer
      v-if="mobile"
      v-model:visible="mobileMenuVisible"
      class="arco-mobile-menu-drawer"
      placement="left"
      :width="240"
      :header="false"
      :footer="false"
      :closable="false"
      unmount-on-close
    >
      <SideMenu />
    </a-drawer>
  </a-layout>
</template>

<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, onMounted, provide, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { useBusinessBadgeStore } from '@/store/modules/businessBadge'
import { Footer } from '@/layout/components/Footer'
import NavBar from './NavBar.vue'
import SideMenu from './SideMenu.vue'
import ScrollWheel from './ScrollWheel.vue'

defineOptions({ name: 'ArcoLayout' })

const appStore = useAppStore()
const tagsViewStore = useTagsViewStore()
const badgeStore = useBusinessBadgeStore()
const layoutRoute = useRoute()
let badgeTimer: ReturnType<typeof setInterval> | undefined

/* ===== 页面切换入场动画：路由变化时在常驻包裹层上重放 CSS 动画 ===== */
const pageHolderRef = ref<HTMLElement | null>(null)
const replayPageAnim = () => {
  const el = pageHolderRef.value
  if (!el) return
  el.classList.remove('page-anim')
  // 强制 reflow，让浏览器认为动画被重置，下一帧重新播放
  void el.offsetWidth
  el.classList.add('page-anim')
}

/* ===== 自定义滚轮指示器（替代主内容区滚动条） ===== */
const scrollRef = ref<HTMLElement | null>(null)
const canScroll = ref(false)
const scrollDir = ref<'up' | 'down' | 'none'>('none')
let lastTop = 0
let dirTimer: any = null
let resizeObserver: ResizeObserver | null = null

const recompute = () => {
  const el = scrollRef.value
  if (!el) return
  canScroll.value = el.scrollHeight - el.clientHeight > 8
}

const onScroll = () => {
  const el = scrollRef.value
  if (!el) return
  const top = el.scrollTop
  if (top > lastTop + 1) scrollDir.value = 'down'
  else if (top < lastTop - 1) scrollDir.value = 'up'
  lastTop = top
  recompute()
  if (dirTimer) clearTimeout(dirTimer)
  dirTimer = setTimeout(() => (scrollDir.value = 'none'), 700)
}

const scrollByDir = (dir: 'up' | 'down') => {
  const el = scrollRef.value
  if (!el) return
  const delta = Math.round(el.clientHeight * 0.8)
  el.scrollBy({ top: dir === 'up' ? -delta : delta, behavior: 'smooth' })
}

// 内容/视口尺寸变化时重新判断是否可滚动（监听滚动容器的直接子元素）
const observeContent = () => {
  if (!('ResizeObserver' in window)) {
    recompute()
    return
  }
  if (resizeObserver) resizeObserver.disconnect()
  resizeObserver = new ResizeObserver(() => recompute())
  const el = scrollRef.value
  if (el) {
    resizeObserver.observe(el)
    const child = el.firstElementChild
    if (child) resizeObserver.observe(child)
  }
  recompute()
}

watch(
  () => layoutRoute.fullPath,
  () => {
    lastTop = 0
    scrollDir.value = 'none'
    // 移动端选中菜单跳转后自动收起抽屉
    appStore.setMobileMenuVisible(false)
    replayPageAnim()
    nextTick(() => {
      observeContent()
      // 内容异步加载时延迟再校正一次
      setTimeout(recompute, 400)
    })
  }
)

onMounted(() => {
  observeContent()
  window.addEventListener('resize', recompute)
  badgeStore.refresh()
  badgeTimer = setInterval(() => badgeStore.refresh(), 15000)
})

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
  if (dirTimer) clearTimeout(dirTimer)
  if (badgeTimer) clearInterval(badgeTimer)
  window.removeEventListener('resize', recompute)
})

const collapsed = computed(() => appStore.getCollapse)
/* 固定侧栏时忽略断点自动折叠，只响应用户手动折叠 */
const setCollapsed = (val: boolean) => {
  if (val && appStore.getFixedMenu) return
  appStore.setCollapse(val)
}

/* ===== 移动端 overlay 抽屉侧栏 ===== */
const mobile = computed(() => appStore.getMobile)
const mobileMenuVisible = computed({
  get: () => appStore.getMobileMenuVisible,
  set: (v: boolean) => appStore.setMobileMenuVisible(v)
})
// 离开移动端时确保抽屉关闭，避免回到移动端时残留展开态
watch(mobile, (m) => {
  if (!m) appStore.setMobileMenuVisible(false)
})

const siderWidth = computed(() => {
  const w = appStore.getLayout === 'classic' ? 220 : 220
  return collapsed.value ? 48 : w
})

const showFooter = computed(() => appStore.getFooter)

const cacheList = computed(() => tagsViewStore.getCachedViews)

// 无感刷新（与 basepro 原 AppView 一致）
const routerAlive = ref(true)
const reload = () => {
  routerAlive.value = false
  nextTick(() => (routerAlive.value = true))
}
provide('reload', reload)
</script>

<style lang="scss" scoped>

@keyframes page-enter {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.arco-app-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background-color: var(--color-fill-2, #f7f8fa);
}

/* 中部（侧栏 + 主体）填满剩余高度，min-height:0 让内部可滚动 */
.arco-app-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.arco-app-header {
  height: 56px;
  padding: 0;
  line-height: 56px;
  background-color: var(--color-bg-2, #fff);
  border-bottom: 1px solid var(--color-border, #e5e6eb);
}

.arco-app-sider {
  background-color: var(--color-bg-2, #fff);
  border-right: 1px solid var(--color-border, #e5e6eb);
  transition: width var(--bm-dur) var(--bm-ease-out);

  /* 滚动交给菜单内部（.arco-menu-inner），底部折叠横条常驻可见 */
  :deep(.arco-layout-sider-children) {
    overflow: hidden;
  }
}

.arco-app-main {
  display: flex;
  min-height: 0;
  overflow: hidden;
  background-color: var(--color-fill-2, #f7f8fa);
  flex-direction: column;
}

.arco-app-content {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background-color: var(--color-fill-2, #f7f8fa);
}

/* 真正的滚动容器：隐藏原生滚动条，由 ScrollWheel 指示滚动 */
.content-scroll {
  height: 100%;
  padding: 16px;
  overflow: hidden auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.content-scroll::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

/* ===== 路由页面入场动画：淡入 + 8px 上浮 =====
 * 在常驻包裹层 .page-holder 上重放 keyframes（路由切换时 JS 移除再加回 .page-anim），
 * 兼容多根 Fragment 页面（basepro 系统管理等），避免 Transition out-in 卡死白屏。
 * 只动 opacity/transform（合成层属性），不触发布局回流。 */
.page-holder {
  min-height: 100%;
}

.page-anim {
  animation: page-enter 0.24s cubic-bezier(0.16, 1, 0.3, 1);
}

.arco-app-footer {
  padding: 0;
  background: transparent;
}
</style>

<!-- 非 scoped：移动端菜单抽屉被 teleport 到 body，scoped 作用域够不到，需全局按类名限定。 -->
<style lang="scss">
.arco-mobile-menu-drawer .arco-drawer-body {
  padding: 0;
  overflow: hidden;
  background-color: var(--color-bg-2, #fff);
}
</style>
