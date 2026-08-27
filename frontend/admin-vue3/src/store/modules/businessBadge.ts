import { defineStore } from 'pinia'
import { getAdminBadges } from '@/api/business/book'
import { store } from '@/store'

const MEMBER_SEEN_KEY = 'ershou-admin-member-seen'

export interface AdminBadgeCounts {
  book: number
  order: number
  inquiry: number
  member: number
  report: number
  avatar: number
}

const empty: AdminBadgeCounts = { book: 0, order: 0, inquiry: 0, member: 0, report: 0, avatar: 0 }

const readMemberSeen = () => {
  const raw = localStorage.getItem(MEMBER_SEEN_KEY)
  const n = raw ? Number(raw) : 0
  if (n > 0) return n
  const now = Date.now()
  localStorage.setItem(MEMBER_SEEN_KEY, String(now))
  return now
}

export const useBusinessBadgeStore = defineStore('business-badge', {
  state: (): AdminBadgeCounts => ({ ...empty }),
  getters: {
    total(): number {
      return this.book + this.order + this.inquiry + this.member + this.report + this.avatar
    }
  },
  actions: {
    countOf(path: string) {
      if (path === '/business') return this.total
      if (path === '/business/book') return this.book
      if (path === '/business/order') return this.order
      if (path === '/business/inquiry') return this.inquiry
      if (path === '/business/member') return this.member + this.avatar
      if (path === '/business/report') return this.report
      return 0
    },
    markMemberSeen() {
      localStorage.setItem(MEMBER_SEEN_KEY, String(Date.now()))
    },
    async refresh() {
      try {
        const data = await getAdminBadges(readMemberSeen())
        this.book = Number(data?.book) || 0
        this.order = Number(data?.order) || 0
        this.inquiry = Number(data?.inquiry) || 0
        this.member = Number(data?.member) || 0
        this.report = Number(data?.report) || 0
        this.avatar = Number(data?.avatar) || 0
      } catch {
        /* 角标失败不影响后台使用 */
      }
    },
    reset() {
      Object.assign(this, empty)
    }
  }
})

export const useBusinessBadgeStoreWithOut = () => useBusinessBadgeStore(store)
