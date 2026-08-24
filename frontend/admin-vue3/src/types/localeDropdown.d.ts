export interface Language {
  el: Recordable
  name: string
}

export interface LocaleDropdownType {
  lang: LocaleType
  name?: string
  // 语言包对象（Arco Design Vue locale）；字段名沿用历史 elLocale
  elLocale?: import('@arco-design/web-vue/es/locale/interface').ArcoLang
}
