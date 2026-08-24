import { defineStore } from 'pinia'
import { store } from '../index'
import zhCn from '@arco-design/web-vue/es/locale/lang/zh-cn'
import en from '@arco-design/web-vue/es/locale/lang/en-us'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { LocaleDropdownType } from '@/types/localeDropdown'

const { wsCache } = useCache()

// Arco Design Vue 语言包：按当前语言取一份，最终传给 a-config-provider 的 locale
const elLocaleMap = {
  'zh-CN': zhCn,
  en: en
}
interface LocaleState {
  currentLocale: LocaleDropdownType
  localeMap: LocaleDropdownType[]
}

export const useLocaleStore = defineStore('locales', {
  state: (): LocaleState => {
    return {
      currentLocale: {
        lang: wsCache.get(CACHE_KEY.LANG) || 'zh-CN',
        elLocale: elLocaleMap[wsCache.get(CACHE_KEY.LANG) || 'zh-CN']
      },
      // 多语言
      localeMap: [
        {
          lang: 'zh-CN',
          name: '简体中文'
        },
        {
          lang: 'en',
          name: 'English'
        }
      ]
    }
  },
  getters: {
    getCurrentLocale(): LocaleDropdownType {
      return this.currentLocale
    },
    getLocaleMap(): LocaleDropdownType[] {
      return this.localeMap
    }
  },
  actions: {
    setCurrentLocale(localeMap: LocaleDropdownType) {
      // this.locale = Object.assign(this.locale, localeMap)
      this.currentLocale.lang = localeMap?.lang
      this.currentLocale.elLocale = elLocaleMap[localeMap?.lang]
      wsCache.set(CACHE_KEY.LANG, localeMap?.lang)
    }
  }
})

export const useLocaleStoreWithOut = () => {
  return useLocaleStore(store)
}
