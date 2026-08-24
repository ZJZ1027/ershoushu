// 引入unocss css
import '@/plugins/unocss'

// 导入全局的svg图标
import '@/plugins/svgIcon'

// 初始化多语言
import { setupI18n } from '@/plugins/vueI18n'

// 引入状态管理
import { setupStore } from '@/store'

// 全局组件
import { setupGlobCom } from '@/components'

// 引入 Arco Design Vue（全站唯一的 UI 组件库）
import { setupArcoDesign } from '@/plugins/arcoDesign'

// 展示/数字字体（自托管打包，离线可用；中文回退系统字体）
import '@fontsource-variable/outfit'
// Linear 借鉴主题用的 UI 字体（Inter Variable，支持 510 字重 + cv01/ss03 字形）
import '@fontsource-variable/inter'

// 引入全局样式
import '@/styles/index.scss'

// 引入动画
import '@/plugins/animate.css'

// 路由
import router, { setupRouter } from '@/router'

// 指令
import { setupAuth, setupMountedFocus, setupLoading } from '@/directives'

import { createApp } from 'vue'

import App from './App.vue'

import './permission'

import Logger from '@/utils/Logger'

import VueDOMPurifyHTML from 'vue-dompurify-html' // 解决v-html 的安全隐患

// 品牌风格：在挂载前把持久化的选择打到 body 上，避免首屏主题闪烁。
// 无记录时默认商务蓝（default，不加 data-brand 属性）；其余主题原样应用到 body[data-brand]。
try {
  const saved = localStorage.getItem('bm-brand-style')
  const style = saved == null ? 'default' : saved
  if (style !== 'default') {
    document.body.setAttribute('data-brand', style)
  }
} catch {
  /* localStorage 不可用时忽略，退回默认样式 */
}

const setupAll = async () => {
  const app = createApp(App)

  await setupI18n(app)

  setupStore(app)

  setupGlobCom(app)

  setupArcoDesign(app)

  setupRouter(app)

  setupAuth(app)
  setupMountedFocus(app)
  setupLoading(app)

  await router.isReady()

  app.use(VueDOMPurifyHTML)

  app.mount('#app')
}

setupAll()

Logger.prettyPrimary(`欢迎使用`, import.meta.env.VITE_APP_TITLE)
