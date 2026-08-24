import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import '@arco-design/web-vue/dist/arco.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import { TOKEN_STORAGE_KEY } from '@/utils/auth'
import './styles.css'

const pinia = createPinia()
const app = createApp(App)
app.use(pinia)
app.use(router)
app.use(ArcoVue)

/** 任一标签页切换账号时，其余标签页跟随更新 */
window.addEventListener('storage', (e) => {
  if (e.key !== TOKEN_STORAGE_KEY) return
  const user = useUserStore()
  user.syncFromStorage().then(() => {
    if (!user.token && !location.pathname.startsWith('/login') && !location.pathname.startsWith('/register')) {
      router.replace({ path: '/login', query: { redirect: location.pathname + location.search } })
    }
  })
})

app.mount('#app')
