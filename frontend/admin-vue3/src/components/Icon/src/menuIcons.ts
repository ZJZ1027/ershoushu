import type { Component } from 'vue'
import {
  IconApps,
  IconArchive,
  IconAt,
  IconAttachment,
  IconBarChart,
  IconBook,
  IconBookmark,
  IconBranch,
  IconBug,
  IconBulb,
  IconCalendar,
  IconCalendarClock,
  IconCamera,
  IconClockCircle,
  IconCloud,
  IconCloudDownload,
  IconCode,
  IconCodeSquare,
  IconCommand,
  IconCommon,
  IconCompass,
  IconComputer,
  IconCopyright,
  IconCustomerService,
  IconDashboard,
  IconDelete,
  IconDesktop,
  IconDownload,
  IconDriveFile,
  IconEdit,
  IconEmail,
  IconExperiment,
  IconExport,
  IconEye,
  IconEyeInvisible,
  IconFile,
  IconFileAudio,
  IconFileImage,
  IconFilePdf,
  IconFileVideo,
  IconFilter,
  IconFire,
  IconFolder,
  IconFolderAdd,
  IconGift,
  IconHeart,
  IconHistory,
  IconHome,
  IconIdcard,
  IconImage,
  IconImport,
  IconInteraction,
  IconLanguage,
  IconLaunch,
  IconLayers,
  IconLayout,
  IconLink,
  IconList,
  IconLiveBroadcast,
  IconLocation,
  IconLock,
  IconMan,
  IconMenu,
  IconMessage,
  IconMessageBanned,
  IconMindMapping,
  IconMobile,
  IconMusic,
  IconNav,
  IconNotification,
  IconNotificationClose,
  IconPalette,
  IconPhone,
  IconPoweroff,
  IconPrinter,
  IconPublic,
  IconPushpin,
  IconQrcode,
  IconRefresh,
  IconRelation,
  IconRobot,
  IconSafe,
  IconSave,
  IconScan,
  IconSchedule,
  IconSearch,
  IconSend,
  IconSettings,
  IconShareAlt,
  IconSkin,
  IconStamp,
  IconStar,
  IconStorage,
  IconSubscribed,
  IconSwap,
  IconSync,
  IconTag,
  IconTags,
  IconThunderbolt,
  IconTool,
  IconTranslate,
  IconTrophy,
  IconUnlock,
  IconUnorderedList,
  IconUpload,
  IconUser,
  IconUserAdd,
  IconUserGroup,
  IconVideoCamera,
  IconVoice,
  IconWifi,
  IconWoman
} from '@arco-design/web-vue/es/icon'

/**
 * 菜单图标注册表：`sys_menu.icon` 的唯一取值来源。
 *
 * 为什么是「注册表」而不是「映射表」：
 * 早先菜单图标存 Iconify 名（ep:* / fa:*），选择器提供三整套上千个图标，而侧栏另有一张
 * 手写的 Iconify→Arco 映射表来统一视觉风格。两边的集合对不上，选到映射表外的图标时侧栏
 * 静默退回默认图标 —— 「配置的」和「显示的」不是一个东西。
 *
 * 现在选择器与所有渲染点共用本表：能选中的必定能渲染，集合一致由结构保证，不靠人工同步。
 * 另外图标是编译期静态引入的 Arco 组件，不依赖 Iconify 运行时取图，内网离线部署同样可用。
 *
 * 只收录适合做菜单图标的具象图标；方向箭头、编辑器工具、状态提示、品牌 logo 这类纯控件
 * 字形不在其中（放进来只会让选择器难用）。缺什么在下面补一行 import 与分组即可。
 */
const MENU_ICONS = {
  IconDashboard,
  IconHome,
  IconApps,
  IconLayout,
  IconMenu,
  IconNav,
  IconList,
  IconUnorderedList,
  IconCommon,
  IconCompass,
  IconLaunch,
  IconPushpin,
  IconBookmark,
  IconStar,
  IconHeart,
  IconTrophy,
  IconGift,
  IconFire,
  IconBulb,
  IconThunderbolt,

  IconUser,
  IconUserGroup,
  IconUserAdd,
  IconMan,
  IconWoman,
  IconIdcard,
  IconRelation,
  IconMindMapping,
  IconCustomerService,
  IconRobot,
  IconCommand,
  IconSafe,
  IconLock,
  IconUnlock,
  IconStamp,
  IconPublic,
  IconSubscribed,

  IconFile,
  IconFileImage,
  IconFilePdf,
  IconFileAudio,
  IconFileVideo,
  IconDriveFile,
  IconFolder,
  IconFolderAdd,
  IconArchive,
  IconStorage,
  IconCloud,
  IconCloudDownload,
  IconUpload,
  IconDownload,
  IconImport,
  IconExport,
  IconAttachment,
  IconSave,
  IconPrinter,
  IconImage,
  IconCamera,
  IconVideoCamera,
  IconMusic,
  IconVoice,
  IconQrcode,

  IconBarChart,
  IconExperiment,
  IconFilter,
  IconSearch,
  IconHistory,
  IconClockCircle,
  IconCalendar,
  IconCalendarClock,
  IconSchedule,
  IconInteraction,
  IconBranch,
  IconCode,
  IconCodeSquare,
  IconScan,

  IconSettings,
  IconTool,
  IconDesktop,
  IconComputer,
  IconMobile,
  IconPhone,
  IconWifi,
  IconBug,
  IconPoweroff,
  IconSkin,
  IconPalette,
  IconLanguage,
  IconTranslate,
  IconLink,
  IconShareAlt,
  IconSync,
  IconRefresh,
  IconSwap,
  IconLocation,
  IconLayers,

  IconMessage,
  IconMessageBanned,
  IconNotification,
  IconNotificationClose,
  IconEmail,
  IconSend,
  IconAt,
  IconBook,
  IconTag,
  IconTags,
  IconEdit,
  IconDelete,
  IconEye,
  IconEyeInvisible,
  IconCopyright,
  IconLiveBroadcast
} satisfies Record<string, Component>

export type MenuIconName = keyof typeof MENU_ICONS

/** 分组仅供选择器分区呈现；漏收或写错名字会在此处编译报错，不会漏到运行时 */
export const MENU_ICON_GROUPS: { label: string; names: MenuIconName[] }[] = [
  {
    label: '常规',
    names: [
      'IconDashboard',
      'IconHome',
      'IconApps',
      'IconLayout',
      'IconMenu',
      'IconNav',
      'IconList',
      'IconUnorderedList',
      'IconCommon',
      'IconCompass',
      'IconLaunch',
      'IconPushpin',
      'IconBookmark',
      'IconStar',
      'IconHeart',
      'IconTrophy',
      'IconGift',
      'IconFire',
      'IconBulb',
      'IconThunderbolt'
    ]
  },
  {
    label: '用户与权限',
    names: [
      'IconUser',
      'IconUserGroup',
      'IconUserAdd',
      'IconMan',
      'IconWoman',
      'IconIdcard',
      'IconRelation',
      'IconMindMapping',
      'IconCustomerService',
      'IconRobot',
      'IconCommand',
      'IconSafe',
      'IconLock',
      'IconUnlock',
      'IconStamp',
      'IconPublic',
      'IconSubscribed'
    ]
  },
  {
    label: '文件与媒体',
    names: [
      'IconFile',
      'IconFileImage',
      'IconFilePdf',
      'IconFileAudio',
      'IconFileVideo',
      'IconDriveFile',
      'IconFolder',
      'IconFolderAdd',
      'IconArchive',
      'IconStorage',
      'IconCloud',
      'IconCloudDownload',
      'IconUpload',
      'IconDownload',
      'IconImport',
      'IconExport',
      'IconAttachment',
      'IconSave',
      'IconPrinter',
      'IconImage',
      'IconCamera',
      'IconVideoCamera',
      'IconMusic',
      'IconVoice',
      'IconQrcode'
    ]
  },
  {
    label: '数据与统计',
    names: [
      'IconBarChart',
      'IconExperiment',
      'IconFilter',
      'IconSearch',
      'IconHistory',
      'IconClockCircle',
      'IconCalendar',
      'IconCalendarClock',
      'IconSchedule',
      'IconInteraction',
      'IconBranch',
      'IconCode',
      'IconCodeSquare',
      'IconScan'
    ]
  },
  {
    label: '系统与运维',
    names: [
      'IconSettings',
      'IconTool',
      'IconDesktop',
      'IconComputer',
      'IconMobile',
      'IconPhone',
      'IconWifi',
      'IconBug',
      'IconPoweroff',
      'IconSkin',
      'IconPalette',
      'IconLanguage',
      'IconTranslate',
      'IconLink',
      'IconShareAlt',
      'IconSync',
      'IconRefresh',
      'IconSwap',
      'IconLocation',
      'IconLayers'
    ]
  },
  {
    label: '消息与内容',
    names: [
      'IconMessage',
      'IconMessageBanned',
      'IconNotification',
      'IconNotificationClose',
      'IconEmail',
      'IconSend',
      'IconAt',
      'IconBook',
      'IconTag',
      'IconTags',
      'IconEdit',
      'IconDelete',
      'IconEye',
      'IconEyeInvisible',
      'IconCopyright',
      'IconLiveBroadcast'
    ]
  }
]

/** 存库格式：`arco:IconUser`。带前缀是为了和迁移前的 Iconify 名一眼区分开 */
export const MENU_ICON_PREFIX = 'arco:'

/** 未配置图标时的占位，保证侧栏每项都有图标、不出现参差的空位 */
export const DEFAULT_MENU_ICON = IconMenu

/**
 * 迁移前（Flyway V3 之前）`sys_menu.icon` 存的是 Iconify 名，这里按语义等价换成 Arco 图标，
 * 使未跑迁移的库与跑过迁移的库呈现完全一致。表内取值与 V3 的 UPDATE 一一对应。
 * 新数据一律走 arco: 前缀，本表只为兼容旧值，不要再往里加新条目。
 */
const LEGACY_ICONIFY_ALIAS: Record<string, MenuIconName> = {
  'ep:tools': 'IconSettings', // 系统管理
  'ep:monitor': 'IconDesktop', // 基础设施
  'ep:document': 'IconHistory', // 审计日志
  'ep:avatar': 'IconUser', // 用户管理
  'ep:coordinate': 'IconUserGroup', // 角色管理
  'ep:menu': 'IconMenu', // 菜单管理
  'ep:office-building': 'IconMindMapping', // 部门管理
  'ep:briefcase': 'IconIdcard', // 岗位管理
  'ep:collection': 'IconBook', // 字典管理
  'ep:chat-line-square': 'IconNotification', // 通知公告
  'ep:document-copy': 'IconStamp', // 操作日志
  'ep:key': 'IconSafe', // 登录日志
  'ep:house': 'IconHome', // 租户管理
  'ep:setting': 'IconTool', // 参数配置
  'ep:files': 'IconFolder' // 文件管理
}

const icons: Record<string, Component> = MENU_ICONS
const warned = new Set<string>()

/** 取图标名用于展示 / 搜索：去掉统一的 Icon 前缀，`IconUserGroup` -> `UserGroup` */
export const menuIconLabel = (name: string) => name.replace(/^Icon/, '')

/** 注册表键 -> 存库值 */
export const toMenuIconValue = (name: MenuIconName) => `${MENU_ICON_PREFIX}${name}`

/**
 * 存库值 -> 注册表键，识别不了返回空串。
 * 旧的 Iconify 名一并归一，选择器打开旧数据时也能正确高亮、显示友好名而不是 `ep:coordinate`。
 */
export const toMenuIconName = (value?: string): string => {
  if (!value) return ''
  const bare = value.startsWith(MENU_ICON_PREFIX) ? value.slice(MENU_ICON_PREFIX.length) : value
  return icons[bare] ? bare : LEGACY_ICONIFY_ALIAS[value] || ''
}

/** 把 `sys_menu.icon` 解析成可渲染的组件，供侧栏 / 面包屑 / 菜单管理列表共用 */
export const resolveMenuIcon = (icon?: string): Component => {
  const name = toMenuIconName(icon)
  if (name) return icons[name]

  // 有值却认不出，说明库里存着渲染不出的名字（多为手工改库）。仍给占位图标以免菜单缺项，
  // 但开发期出声提醒，避免又变成「配置的和显示的不一样」还没人发现。
  if (icon && import.meta.env.DEV && !warned.has(icon)) {
    warned.add(icon)
    console.warn(`[menuIcons] 未识别的菜单图标 "${icon}"，已回退为默认图标`)
  }
  return DEFAULT_MENU_ICON
}
