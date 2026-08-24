import { h, ref } from 'vue'
import { Input, Message, Modal, Notification } from '@arco-design/web-vue'
import { useI18n } from './useI18n'

/**
 * 全局消息/确认框 Hook：把 Arco 的 Message / Modal / Notification 收拢成一组统一方法。
 *  - info / error / success / warning 是轻提示，notify* 是右上角通知，alert* 是单按钮弹窗
 *  - confirm / delConfirm / exportConfirm 返回 Promise（确认 resolve、取消 reject）
 *  - prompt 返回 Promise<{ value }>（Arco 无内置 prompt，用 Modal + Input 拼装）
 */
export const useMessage = () => {
  const { t } = useI18n()
  return {
    // 消息提示
    info(content: string) {
      Message.info(content)
    },
    // 错误消息
    error(content: string) {
      Message.error(content)
    },
    // 成功消息
    success(content: string) {
      Message.success(content)
    },
    // 警告消息
    warning(content: string) {
      Message.warning(content)
    },
    // 弹出提示
    alert(content: string) {
      return new Promise<void>((resolve) => {
        Modal.info({
          title: t('common.confirmTitle'),
          content,
          okText: t('common.ok'),
          onOk: () => resolve()
        })
      })
    },
    // 错误提示
    alertError(content: string) {
      return new Promise<void>((resolve) => {
        Modal.error({
          title: t('common.confirmTitle'),
          content,
          okText: t('common.ok'),
          onOk: () => resolve()
        })
      })
    },
    // 成功提示
    alertSuccess(content: string) {
      return new Promise<void>((resolve) => {
        Modal.success({
          title: t('common.confirmTitle'),
          content,
          okText: t('common.ok'),
          onOk: () => resolve()
        })
      })
    },
    // 警告提示
    alertWarning(content: string) {
      return new Promise<void>((resolve) => {
        Modal.warning({
          title: t('common.confirmTitle'),
          content,
          okText: t('common.ok'),
          onOk: () => resolve()
        })
      })
    },
    // 通知提示
    notify(content: string) {
      Notification.info({ content })
    },
    // 错误通知
    notifyError(content: string) {
      Notification.error({ content })
    },
    // 成功通知
    notifySuccess(content: string) {
      Notification.success({ content })
    },
    // 警告通知
    notifyWarning(content: string) {
      Notification.warning({ content })
    },
    // 确认窗体
    confirm(content: string, tip?: string) {
      return new Promise<string>((resolve, reject) => {
        Modal.confirm({
          title: tip ? tip : t('common.confirmTitle'),
          content,
          okText: t('common.ok'),
          cancelText: t('common.cancel'),
          onOk: () => resolve('confirm'),
          onCancel: () => reject('cancel')
        })
      })
    },
    // 删除窗体
    // 删除是不可逆动作：标题直接说「确认删除」而不是万能的「系统提示」，
    // 主按钮也压成危险色——同一个蓝按钮既用来「保存」又用来「删除」是误操作的温床。
    delConfirm(content?: string, tip?: string) {
      return new Promise<string>((resolve, reject) => {
        Modal.confirm({
          title: tip ? tip : '确认删除',
          content: content ? content : t('common.delMessage'),
          okText: '删除',
          cancelText: t('common.cancel'),
          okButtonProps: { status: 'danger' },
          onOk: () => resolve('confirm'),
          onCancel: () => reject('cancel')
        })
      })
    },
    // 导出窗体
    exportConfirm(content?: string, tip?: string) {
      return new Promise<string>((resolve, reject) => {
        Modal.confirm({
          title: tip ? tip : '确认导出',
          content: content ? content : t('common.exportMessage'),
          okText: '导出',
          cancelText: t('common.cancel'),
          onOk: () => resolve('confirm'),
          onCancel: () => reject('cancel')
        })
      })
    },
    // 提交内容（Arco 无内置 prompt，用 Modal + Input 拼装，确认后 resolve 出 { value }）
    prompt(content: string, tip: string) {
      return new Promise<{ value: string; action: string }>((resolve, reject) => {
        const inputValue = ref('')
        Modal.open({
          title: tip,
          okText: t('common.ok'),
          cancelText: t('common.cancel'),
          hideCancel: false,
          content: () =>
            h('div', null, [
              content
                ? h('div', { style: { marginBottom: '8px' } }, content)
                : null,
              h(Input, {
                defaultValue: '',
                onInput: (val: string) => {
                  inputValue.value = val
                }
              } as any)
            ]),
          onOk: () => resolve({ value: inputValue.value, action: 'confirm' }),
          onCancel: () => reject('cancel')
        })
      })
    }
  }
}
