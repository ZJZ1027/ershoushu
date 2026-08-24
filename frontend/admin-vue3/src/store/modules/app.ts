import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { ComponentSize } from '@/types/dict'
import { LayoutType } from '@/types/layout'
import { ThemeTypes } from '@/types/theme'
import { humpToUnderline, setCssVar } from '@/utils'
import { Message } from '@arco-design/web-vue'
import { defineStore } from 'pinia'
import { store } from '../index'

const { wsCache } = useCache()

interface AppState {
  breadcrumb: boolean
  breadcrumbIcon: boolean
  collapse: boolean
  uniqueOpened: boolean
  hamburger: boolean
  screenfull: boolean
  search: boolean
  size: boolean
  locale: boolean
  message: boolean
  tagsView: boolean
  tagsViewImmerse: boolean
  tagsViewIcon: boolean
  logo: boolean
  fixedHeader: boolean
  greyMode: boolean
  pageLoading: boolean
  layout: LayoutType
  title: string
  userInfo: string
  isDark: boolean
  currentSize: ComponentSize
  sizeMap: ComponentSize[]
  mobile: boolean
  mobileMenuVisible: boolean
  footer: boolean
  theme: ThemeTypes
  fixedMenu: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => {
    return {
      userInfo: 'userInfo', // 登录信息存储字段-建议每个项目换一个字段，避免与其他项目冲突
      sizeMap: ['default', 'large', 'small'],
      mobile: false, // 是否是移动端
      mobileMenuVisible: false, // 移动端侧栏抽屉是否展开
      title: import.meta.env.VITE_APP_TITLE, // 标题
      pageLoading: false, // 路由跳转loading

      // Arco Pro 风：精简顶栏（去掉全屏/缩放/搜索图标，去掉页脚）
      breadcrumb: true, // 面包屑
      breadcrumbIcon: true, // 面包屑图标
      collapse: false, // 折叠菜单
      uniqueOpened: true, // 是否只保持一个子菜单的展开
      hamburger: true, // 折叠图标
      screenfull: false, // 全屏图标（精简移除）
      search: false, // 搜索图标（精简移除）
      size: false, // 尺寸图标（精简移除）
      locale: false, // 多语言图标（精简移除）
      message: true, // 消息图标
      tagsView: true, // 标签页
      tagsViewImmerse: false, // 标签页沉浸
      tagsViewIcon: true, // 是否显示标签图标
      logo: true, // logo
      fixedHeader: true, // 固定toolheader
      footer: false, // 显示页脚（精简移除）
      greyMode: false, // 是否开始灰色模式，用于特殊悼念日
      fixedMenu: wsCache.get('fixedMenu') || false, // 是否固定菜单

      layout: wsCache.get(CACHE_KEY.LAYOUT) || 'classic', // layout布局
      isDark: wsCache.get(CACHE_KEY.IS_DARK) || false, // 是否是暗黑模式
      currentSize: wsCache.get('default') || 'default', // 组件尺寸
      theme: wsCache.get(CACHE_KEY.THEME) || {
        // Arco Pro 风浅色侧栏主题。主色不在这里配：Arco 组件跟随 --arcoblue-*，
        // 自定义样式跟随 --bm-brand（见 styles/design-tokens.scss），改主色改那一处即可
        // 左侧菜单边框颜色
        leftMenuBorderColor: '#E5E6EB',
        // 左侧菜单背景颜色（白底）
        leftMenuBgColor: '#FFFFFF',
        // 左侧菜单浅色背景颜色（hover/sub）
        leftMenuBgLightColor: '#F7F8FA',
        // 左侧菜单选中背景颜色（淡蓝）
        leftMenuBgActiveColor: '#E8F3FF',
        // 左侧菜单收起选中背景颜色
        leftMenuCollapseBgActiveColor: '#E8F3FF',
        // 左侧菜单字体颜色（深灰）
        leftMenuTextColor: '#4E5969',
        // 左侧菜单选中字体颜色（主蓝）
        leftMenuTextActiveColor: '#165DFF',
        // logo字体颜色（深色）
        logoTitleTextColor: '#1D2129',
        // logo边框颜色
        logoBorderColor: '#E5E6EB',
        // 头部背景颜色
        topHeaderBgColor: '#FFFFFF',
        // 头部字体颜色
        topHeaderTextColor: '#1D2129',
        // 头部悬停颜色
        topHeaderHoverColor: '#F2F3F5',
        // 头部边框颜色
        topToolBorderColor: '#E5E6EB'
      }
    }
  },
  getters: {
    getBreadcrumb(): boolean {
      return this.breadcrumb
    },
    getBreadcrumbIcon(): boolean {
      return this.breadcrumbIcon
    },
    getCollapse(): boolean {
      return this.collapse
    },
    getUniqueOpened(): boolean {
      return this.uniqueOpened
    },
    getHamburger(): boolean {
      return this.hamburger
    },
    getScreenfull(): boolean {
      return this.screenfull
    },
    getSize(): boolean {
      return this.size
    },
    getLocale(): boolean {
      return this.locale
    },
    getMessage(): boolean {
      return this.message
    },
    getTagsView(): boolean {
      return this.tagsView
    },
    getTagsViewImmerse(): boolean {
      return this.tagsViewImmerse
    },
    getTagsViewIcon(): boolean {
      return this.tagsViewIcon
    },
    getLogo(): boolean {
      return this.logo
    },
    getFixedHeader(): boolean {
      return this.fixedHeader
    },
    getGreyMode(): boolean {
      return this.greyMode
    },
    getFixedMenu(): boolean {
      return this.fixedMenu
    },
    getPageLoading(): boolean {
      return this.pageLoading
    },
    getLayout(): LayoutType {
      return this.layout
    },
    getTitle(): string {
      return this.title
    },
    getUserInfo(): string {
      return this.userInfo
    },
    getIsDark(): boolean {
      return this.isDark
    },
    getCurrentSize(): ComponentSize {
      return this.currentSize
    },
    getSizeMap(): ComponentSize[] {
      return this.sizeMap
    },
    getMobile(): boolean {
      return this.mobile
    },
    getMobileMenuVisible(): boolean {
      return this.mobileMenuVisible
    },
    getTheme(): ThemeTypes {
      return this.theme
    },
    getFooter(): boolean {
      return this.footer
    }
  },
  actions: {
    setBreadcrumb(breadcrumb: boolean) {
      this.breadcrumb = breadcrumb
    },
    setBreadcrumbIcon(breadcrumbIcon: boolean) {
      this.breadcrumbIcon = breadcrumbIcon
    },
    setCollapse(collapse: boolean) {
      this.collapse = collapse
    },
    setUniqueOpened(uniqueOpened: boolean) {
      this.uniqueOpened = uniqueOpened
    },
    setHamburger(hamburger: boolean) {
      this.hamburger = hamburger
    },
    setScreenfull(screenfull: boolean) {
      this.screenfull = screenfull
    },
    setSize(size: boolean) {
      this.size = size
    },
    setLocale(locale: boolean) {
      this.locale = locale
    },
    setMessage(message: boolean) {
      this.message = message
    },
    setTagsView(tagsView: boolean) {
      this.tagsView = tagsView
    },
    setTagsViewImmerse(tagsViewImmerse: boolean) {
      this.tagsViewImmerse = tagsViewImmerse
    },
    setTagsViewIcon(tagsViewIcon: boolean) {
      this.tagsViewIcon = tagsViewIcon
    },
    setLogo(logo: boolean) {
      this.logo = logo
    },
    setFixedHeader(fixedHeader: boolean) {
      this.fixedHeader = fixedHeader
    },
    setGreyMode(greyMode: boolean) {
      this.greyMode = greyMode
    },
    setFixedMenu(fixedMenu: boolean) {
      wsCache.set('fixedMenu', fixedMenu)
      this.fixedMenu = fixedMenu
    },
    setPageLoading(pageLoading: boolean) {
      this.pageLoading = pageLoading
    },
    setLayout(layout: LayoutType) {
      if (this.mobile && layout !== 'classic') {
        Message.warning('移动端模式下不支持切换其他布局')
        return
      }
      this.layout = layout
      wsCache.set(CACHE_KEY.LAYOUT, this.layout)
    },
    setTitle(title: string) {
      this.title = title
    },
    setIsDark(isDark: boolean) {
      this.isDark = isDark
      if (this.isDark) {
        document.documentElement.classList.add('dark')
        document.documentElement.classList.remove('light')
        // 暗色开关有两处，必须成对写：body[arco-theme='dark'] 驱动 Arco 组件与设计令牌，
        // 上面 html 的 dark/light 类驱动 var.css 里的布局变量覆盖和 UnoCSS 的 dark: 工具类。
        // 两处都收在这里，登录页这类布局之外的入口、以及刷新后的持久化恢复才能一致生效。
        document.body.setAttribute('arco-theme', 'dark')
      } else {
        document.documentElement.classList.add('light')
        document.documentElement.classList.remove('dark')
        document.body.removeAttribute('arco-theme')
      }
      wsCache.set(CACHE_KEY.IS_DARK, this.isDark)
    },
    setCurrentSize(currentSize: ComponentSize) {
      this.currentSize = currentSize
      wsCache.set('currentSize', this.currentSize)
    },
    setMobile(mobile: boolean) {
      this.mobile = mobile
    },
    setMobileMenuVisible(visible: boolean) {
      this.mobileMenuVisible = visible
    },
    setTheme(theme: ThemeTypes) {
      this.theme = Object.assign(this.theme, theme)
      wsCache.set(CACHE_KEY.THEME, this.theme)
    },
    setCssVarTheme() {
      for (const key in this.theme) {
        setCssVar(`--${humpToUnderline(key)}`, this.theme[key])
      }
    },
    setFooter(footer: boolean) {
      this.footer = footer
    }
  },
  persist: false
})

export const useAppStoreWithOut = () => {
  return useAppStore(store)
}
