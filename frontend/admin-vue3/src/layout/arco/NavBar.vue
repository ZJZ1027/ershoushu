<template>
  <div class="arco-navbar">
    <!-- 左侧：Logo + 系统名称 + 折叠按钮 -->
    <div class="left-side">
      <router-link to="/" class="brand" :class="{ 'brand-col': !isMobile }">
        <!-- 图形块 + 实文字分开排：顶栏只有 56px 高，带文字的整图缩进去必糊 -->
        <BrandLogo :size="32" class="brand-logo" />
        <!-- 手机端只留 Logo，避免 390 宽顶栏被中英双行标题挤爆 -->
        <span v-if="!isMobile" class="brand-text">
          <span class="brand-title">{{ title }}</span>
          <span class="brand-sub">{{ brandSubtitle }}</span>
        </span>
      </router-link>
      <!-- 折叠：移动端唤起抽屉；桌面端与侧栏底折叠钮同效（对齐 Arco Pro 顶栏操作组） -->
      <a-tooltip :content="menuOpened ? '收起侧栏' : '展开侧栏'">
        <a-button class="nav-tool-btn" type="text" @click="toggleCollapse">
          <template #icon>
            <icon-menu-fold v-if="menuOpened" />
            <icon-menu-unfold v-else />
          </template>
        </a-button>
      </a-tooltip>
      <!-- 刷新当前页：无感重挂载，与命令面板「刷新当前页」同一通道 -->
      <a-tooltip v-if="!isMobile" content="刷新当前页">
        <a-button class="nav-tool-btn" type="text" @click="reloadPage">
          <template #icon>
            <icon-refresh />
          </template>
        </a-button>
      </a-tooltip>
      <!-- 面包屑并入顶栏；移动端太挤不显示 -->
      <Breadcrumb v-if="!isMobile" class="nav-breadcrumb" />
    </div>

    <!-- 右侧：无边框图标按钮组（悬停浅灰底，降低视觉噪音） -->
    <ul class="right-side">
      <li v-if="!isMobile">
        <a-tooltip :content="`搜索菜单 (${cmdKeyLabel}K)`">
          <a-button
            class="nav-btn"
            type="text"
            shape="circle"
            @click="searchVisible = true"
          >
            <template #icon>
              <icon-search />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="isDark ? '切换浅色' : '切换深色'">
          <a-button
            class="nav-btn"
            type="text"
            shape="circle"
            @click="toggleTheme"
          >
            <template #icon>
              <icon-moon-fill v-if="isDark" />
              <icon-sun-fill v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li v-if="!isMobile">
        <a-dropdown
          trigger="click"
          position="br"
          :popup-max-height="false"
          @select="onBrandSelect"
        >
          <a-tooltip content="切换主题风格">
            <a-button
              class="nav-btn"
              :class="{ 'nav-btn-active': brandStyle !== 'default' }"
              type="text"
              shape="circle"
            >
              <template #icon>
                <icon-palette />
              </template>
            </a-button>
          </a-tooltip>
          <template #content>
            <a-doption v-for="opt in brandOptions" :key="opt.value" :value="opt.value">
              <div
                style="display: flex; align-items: center; gap: 8px; min-width: 132px"
              >
                <span
                  :style="{
                    width: '10px',
                    height: '10px',
                    borderRadius: '50%',
                    background: opt.color,
                    flex: '0 0 auto'
                  }"
                ></span>
                <span style="flex: 1">{{ opt.label }}</span>
                <icon-check
                  v-if="brandStyle === opt.value"
                  style=" font-size: 14px;color: var(--bm-brand)"
                />
              </div>
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-dropdown trigger="click" position="br">
          <div class="avatar-wrap">
            <a-avatar
              :size="32"
              class="nav-avatar"
              :style="{ backgroundColor: 'var(--bm-brand-solid)' }"
            >
              <img v-if="avatar" :src="avatar" alt="avatar" />
              <span v-else>{{ initialChar }}</span>
            </a-avatar>
            <!-- 手机端只留头像，昵称进下拉 -->
            <span v-if="!isMobile" class="nickname">{{ nickname }}</span>
            <icon-down v-if="!isMobile" style="font-size: 12px; color: var(--color-text-3)" />
          </div>
          <template #content>
            <a-doption @click="goProfile">
              <template #icon><icon-user /></template>
              <span>个人中心</span>
            </a-doption>
            <a-divider style="margin: 4px 0" />
            <a-doption @click="handleLogout">
              <template #icon><icon-export /></template>
              <span>退出登录</span>
            </a-doption>
          </template>
        </a-dropdown>
      </li>
    </ul>

    <!-- 菜单搜索弹层（顶部放大镜触发） -->
    <MenuSearch v-model:visible="searchVisible" />
  </div>
</template>

<script lang="ts" setup>
import { computed, inject, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import { loginOut } from '@/api/login'
import { removeToken } from '@/utils/auth'
import { useCache } from '@/hooks/web/useCache'
import {
  Button as AButton,
  Tooltip as ATooltip,
  Avatar as AAvatar,
  Dropdown as ADropdown,
  Doption as ADoption,
  Divider as ADivider,
  Message
} from '@arco-design/web-vue'
import {
  IconSearch,
  IconMoonFill,
  IconSunFill,
  IconMenuFold,
  IconMenuUnfold,
  IconRefresh,
  IconDown,
  IconUser,
  IconExport,
  IconPalette,
  IconCheck
} from '@arco-design/web-vue/es/icon'
import MenuSearch from '@/layout/arco/MenuSearch.vue'
import Breadcrumb from '@/layout/arco/Breadcrumb.vue'
import BrandLogo from '@/components/BrandLogo/index.vue'
defineOptions({ name: 'ArcoNavBar' })

const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()
const { wsCache } = useCache()
const reloadPage = inject<() => void>('reload', () => {})

const title = computed(() => appStore.getTitle)
// 品牌英文副标题（展示在中文标题下方）；如需改词只改这里
const brandSubtitle = 'Admin Pro'
const collapsed = computed(() => appStore.getCollapse)
// 折叠按钮图标状态：移动端看抽屉是否展开，桌面端看侧栏是否展开
const menuOpened = computed(() =>
  appStore.getMobile ? appStore.getMobileMenuVisible : !appStore.getCollapse
)
// 移动端不展示内联面包屑（顶栏空间不够）
const isMobile = computed(() => appStore.getMobile)

const avatar = computed(() => userStore.user?.avatar || '')
const nickname = computed(() => userStore.user?.nickname || '用户')
const initialChar = computed(() => (nickname.value || 'U').slice(0, 1).toUpperCase())

// 移动端：折叠按钮切换 overlay 抽屉；桌面端：正常折叠/展开侧栏
const toggleCollapse = () => {
  if (appStore.getMobile) {
    appStore.setMobileMenuVisible(!appStore.getMobileMenuVisible)
  } else {
    appStore.setCollapse(!collapsed.value)
  }
}

// 顶部放大镜：菜单搜索弹层开关
const searchVisible = ref(false)
// 键盘优先（Linear 招牌）：⌘K / Ctrl+K 全局唤起菜单搜索。Mac 显示 ⌘、其余显示 Ctrl+
const cmdKeyLabel = /Mac|iPhone|iPad/i.test(navigator.platform) ? '⌘' : 'Ctrl+'
const onGlobalKeydown = (e: KeyboardEvent) => {
  if ((e.metaKey || e.ctrlKey) && (e.key === 'k' || e.key === 'K')) {
    e.preventDefault()
    searchVisible.value = true
  }
}

// 主题切换：body[arco-theme] 与持久化都由 appStore.setIsDark 统一处理，这里只管本地按钮态。
const isDark = ref(appStore.getIsDark)
const toggleTheme = () => {
  isDark.value = !isDark.value
  appStore.setIsDark(isDark.value)
}

// 品牌风格切换：商务蓝(default) / Linear 靛紫(linear) / 落日琥珀金(amber) 等多选一。
// 用 body[data-brand] 切换各套设计 token（default 即移除属性），持久化到 localStorage；
// 无记录时默认商务蓝(default)。
type BrandStyle =
  | 'default'
  | 'linear'
  | 'amber'
  | 'teal'
  | 'rose'
  | 'slate'
  | 'violet'
  | 'visual'
const BRAND_KEY = 'bm-brand-style'
const brandOptions: { value: BrandStyle; label: string; color: string }[] = [
  { value: 'default', label: '商务蓝', color: '#165DFF' },
  { value: 'linear', label: 'Linear 靛紫', color: '#5E6AD2' },
  { value: 'amber', label: '落日琥珀', color: '#F59E0B' },
  { value: 'teal', label: '科技青', color: '#0D9488' },
  { value: 'rose', label: '玫瑰品红', color: '#EB2F96' },
  { value: 'slate', label: '石墨灰蓝', color: '#64748B' },
  { value: 'violet', label: '葡萄紫', color: '#7C3AED' },
  {
    value: 'visual',
    label: '视觉体验版',
    color: 'linear-gradient(135deg, #6366F1, #8B5CF6, #06B6D4)'
  }
]
const brandStyle = ref<BrandStyle>('default')
const applyBrand = (style: BrandStyle) => {
  if (style === 'default') {
    document.body.removeAttribute('data-brand')
  } else {
    document.body.setAttribute('data-brand', style)
  }
}
const onBrandSelect = (value: string | number | Record<string, any> | undefined) => {
  const v = (value as BrandStyle) || 'default'
  brandStyle.value = v
  localStorage.setItem(BRAND_KEY, v)
  applyBrand(v)
}

const goProfile = () => router.push('/user/profile')

const handleLogout = async () => {
  try {
    await loginOut()
  } catch (e) {
    // ignore network error on logout
  } finally {
    removeToken()
    wsCache.clear()
    Message.success('已退出登录')
    // 退出后跳登录页：必须带上部署的 base 前缀（VITE_BASE_PATH），
    // 否则会跳到根路径 /login，被网关透传给后端命中 404。
    location.replace(`${import.meta.env.BASE_URL}login`)
  }
}

onMounted(() => {
  // 品牌风格：读持久化（无记录默认商务蓝 default；兼容旧值）并应用到 body
  const saved = localStorage.getItem(BRAND_KEY)
  const valid: BrandStyle[] = [
    'default',
    'linear',
    'amber',
    'teal',
    'rose',
    'slate',
    'violet',
    'visual'
  ]
  brandStyle.value = valid.includes(saved as BrandStyle) ? (saved as BrandStyle) : 'default'
  applyBrand(brandStyle.value)
  // ⌘K / Ctrl+K 全局唤起菜单搜索
  window.addEventListener('keydown', onGlobalKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onGlobalKeydown)
})
</script>

<style lang="scss" scoped>
.arco-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
  padding: 0 16px 0 0;
  background-color: var(--color-bg-2, #fff);
  //border-bottom: 1px solid var(--color-border, #e5e6eb);
}

.left-side {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 10px;
  flex: 1;
  min-width: 0; /* 允许内联面包屑在窄窗口下收缩省略 */

  .brand {
    display: flex;
    height: 100%;
    padding: 0 20px;
    color: var(--color-text-1, #1d2129);
    text-decoration: none;
    align-items: center;
    gap: 12px;
    flex: 0 0 auto;

    /* Logo：简洁圆角 + 一层轻投影即可（原玻璃高光/blur 特效噪音大，已移除） */
    .brand-logo {
      box-shadow: 0 1px 4px rgb(0 0 0 / 10%);
    }

    .brand-text {
      display: flex;
      flex-direction: column;
      justify-content: center;
      line-height: 1.15;
    }

    .brand-title {
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 0.5px;
      white-space: nowrap;
    }

    /* 英文副标题：小字号、弱色、Outfit 拉丁字体，与中文标题上下对齐 */
    .brand-sub {
      margin-top: 1px;
      font-family: var(--bm-font-num);
      font-size: 11px;
      font-weight: 500;
      letter-spacing: 0.6px;
      color: var(--bm-text-3);
      white-space: nowrap;
    }
  }

  /* 手机：Logo 区收紧，给右侧铃铛/头像留位 */
  .brand:not(.brand-col) {
    padding: 0 8px 0 12px;
    gap: 0;
  }

  /* 桌面端：Logo 区固定与侧栏同宽（220px），面包屑正好从内容区左缘起步，
     与下方页面内容左对齐 */
  .brand-col {
    width: 220px;
    flex: 0 0 220px;
    box-sizing: border-box;
  }

  /* 顶栏工具钮：折叠 / 刷新，对齐 Arco Pro 面包屑前操作组 */
  .nav-tool-btn {
    flex: 0 0 auto;
    width: 32px;
    height: 32px;
    font-size: 18px;
    color: var(--color-text-2, #4e5969);
    border-radius: 6px;

    &:hover {
      color: var(--color-text-1, #1d2129);
      background-color: var(--color-fill-2, #f2f3f5);
    }

    /* 线框图标再加粗一档，避免顶栏小图标发虚 */
    :deep(.arco-icon) {
      stroke-width: 5.5;
    }
  }

  /* 内联面包屑：窄窗口下可收缩，内部项自带省略号 */
  .nav-breadcrumb {
    min-width: 0;
    margin-left: 2px;
    overflow: hidden;
    flex: 0 1 auto;
  }
}

.right-side {
  display: flex;
  padding: 0;
  margin: 0;
  list-style: none;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;

  li {
    display: flex;
    align-items: center;
  }

  /* 无边框图标按钮：常态弱灰，悬停浅底 + 品牌色，噪音最小 */
  .nav-btn {
    width: 32px;
    height: 32px;
    font-size: 16px;
    color: var(--color-text-2, #4e5969);

    &:hover {
      color: rgb(var(--primary-6, 22 93 255));
      background-color: var(--color-fill-2, #f2f3f5);
    }
  }

  /* 站内信角标：默认偏大，收到和 32px 铃铛匹配的小尺寸 */
  :deep(.arco-badge-number) {
    height: 16px;
    min-width: 16px;
    padding: 0 4px;
    font-size: 11px;
    line-height: 16px;
    box-shadow: 0 0 0 1px var(--color-bg-2, #fff);
  }

  /* 品牌风格切换钮：非默认风格时高亮，提示当前主题色 */
  .nav-btn-active {
    color: rgb(var(--primary-6, 22 93 255));
    background-color: var(--bm-brand-bg);
  }

  /* 头像与图标组之间留出呼吸位 */
  li:last-child {
    margin-left: 6px;
  }

  .avatar-wrap {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;

    .nickname {
      font-size: 14px;
      color: var(--color-text-1, #1d2129);
      transition: color var(--bm-dur-fast) var(--bm-ease-out);
    }

    &:hover .nickname {
      color: rgb(var(--primary-6, 22 93 255));
    }
  }
}

/* 头像首字恒定白字：底色改用 --bm-brand-solid，它在明暗两态都是「白字站得住」的那一档
   （亮 -7 / 暗 -4，实测 7.7）。原先底色用 --bm-brand，暗色色板会把它翻成浅蓝、白字只剩
   2.96，于是暗色下改用深墨字兜底；但视觉体验版又给头像刷了深色渐变底，深墨字反而掉到
   3.09 —— 明暗各一套字色本身就是这类反复的根源，统一到「深底 + 白字」后不再需要翻转。 */
.nav-avatar,
.nav-avatar :deep(.arco-avatar-text) {
  color: #fff;
}
</style>
