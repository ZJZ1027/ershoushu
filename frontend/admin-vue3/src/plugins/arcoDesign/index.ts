import type { App } from 'vue'
// 全量样式：Arco 外壳（Layout/Menu/TabBar/NavBar）大量使用 Button/Dropdown/Avatar/Tabs 等组件，
// 必须加载完整组件样式，否则组件「有结构无皮肤」。arco.css 已包含 message/modal/drawer 等命令式组件样式。
import '@arco-design/web-vue/dist/arco.css'

import { Message, Modal, Notification } from '@arco-design/web-vue'

// 命令式组件需要在 mount 之后再使用，这里挂常用的全局别名，方便业务代码统一调用
export const setupArcoDesign = (app: App<Element>) => {
  app.config.globalProperties.$amessage = Message
  app.config.globalProperties.$amodal = Modal
  app.config.globalProperties.$anotification = Notification
}

export { Message as AMessage, Modal as AModal, Notification as ANotification }
