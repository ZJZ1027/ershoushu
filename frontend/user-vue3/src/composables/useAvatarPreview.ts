import { ref } from 'vue'

const visible = ref(false)
const src = ref('')

/** 全站共用：点击头像放大查看 */
export function useAvatarPreview() {
  const open = (url?: string | null) => {
    const u = (url || '').trim()
    if (!u) return
    src.value = u
    visible.value = true
  }

  const close = () => {
    visible.value = false
    src.value = ''
  }

  return { visible, src, open, close }
}
