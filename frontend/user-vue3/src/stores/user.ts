import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, profile as profileApi, register as registerApi, getPendingOrderCount, getUnreadInquiryCount } from '@/api'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getAccessToken(),
    refreshToken: getRefreshToken(),
    profile: null as any,
    pendingWant: 0,
    unreadMsg: 0
  }),
  actions: {
    setToken(accessToken: string, refreshToken: string) {
      this.token = accessToken
      this.refreshToken = refreshToken
      setTokens(accessToken, refreshToken)
    },
    clear() {
      this.resetSession()
      clearTokens()
    },
    resetSession() {
      this.token = ''
      this.refreshToken = ''
      this.profile = null
      this.pendingWant = 0
      this.unreadMsg = 0
    },
    /** 其他标签页登录/退出时，同步当前页 Pinia 状态 */
    async syncFromStorage() {
      const token = getAccessToken()
      const refresh = getRefreshToken()
      if (!token) {
        this.resetSession()
        return
      }
      this.token = token
      this.refreshToken = refresh
      try {
        await this.loadProfile()
        await this.refreshBadges()
      } catch {
        this.resetSession()
        clearTokens()
      }
    },
    async login(username: string, password: string) {
      const data = await loginApi({ username, password })
      this.setToken(data.accessToken, data.refreshToken)
      await this.loadProfile()
      await this.refreshBadges()
    },
    async register(payload: any) {
      const data = await registerApi(payload)
      this.setToken(data.accessToken, data.refreshToken)
      await this.loadProfile()
      await this.refreshBadges()
    },
    async loadProfile() {
      if (!this.token) return
      try {
        this.profile = await profileApi()
      } catch (e) {
        this.clear()
        throw e
      }
    },
    async refreshBadges() {
      if (!this.token) {
        this.pendingWant = 0
        this.unreadMsg = 0
        return
      }
      try {
        const [want, msg] = await Promise.all([getPendingOrderCount(), getUnreadInquiryCount()])
        this.pendingWant = Number(want) || 0
        this.unreadMsg = Number(msg) || 0
      } catch {
        /* 角标失败不影响页面 */
      }
    },
    async refreshOrderBadge() {
      await this.refreshBadges()
    },
    async logout() {
      try {
        await logoutApi()
      } catch {
        /* ignore */
      }
      this.clear()
    }
  }
})
