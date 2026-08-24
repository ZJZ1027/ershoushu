<script lang="ts" setup>
import { ref, watch, computed, nextTick } from 'vue'
import {
  IconUndo,
  IconRedo,
  IconFullscreen,
  IconFullscreenExit
} from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'MarkdownEditor' })

const props = defineProps({
  modelValue: { type: String, default: '' },
  height: { type: String, default: 'calc(85vh - 400px)' },
  placeholder: { type: String, default: '请输入 Markdown 内容...' },
  readonly: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const content = ref(props.modelValue)
const textareaRef = ref<HTMLTextAreaElement>()
const gutterInnerRef = ref<HTMLElement>()
const fullscreen = ref(false)

/* ============ 撤销 / 重做 历史 ============ */
const history = ref<string[]>([props.modelValue || ''])
const hidx = ref(0)
let applying = false
let histTimer: any = null
const canUndo = computed(() => hidx.value > 0)
const canRedo = computed(() => hidx.value < history.value.length - 1)

const recordHistory = (val: string) => {
  if (histTimer) clearTimeout(histTimer)
  histTimer = setTimeout(() => {
    history.value = history.value.slice(0, hidx.value + 1)
    history.value.push(val)
    if (history.value.length > 200) history.value.shift()
    hidx.value = history.value.length - 1
  }, 350)
}

watch(
  () => props.modelValue,
  (val) => {
    if (val !== content.value) content.value = val
  }
)
watch(content, (val) => {
  emit('update:modelValue', val)
  if (applying) {
    applying = false
    return
  }
  recordHistory(val)
})

const undo = () => {
  if (!canUndo.value) return
  hidx.value--
  applying = true
  content.value = history.value[hidx.value]
}
const redo = () => {
  if (!canRedo.value) return
  hidx.value++
  applying = true
  content.value = history.value[hidx.value]
}

/* ============ 行号 / 统计 ============ */
const lineCount = computed(() => Math.max(1, content.value.split('\n').length))
const lineNumbers = computed(() => Array.from({ length: lineCount.value }, (_, i) => i + 1))
const charCount = computed(() => content.value.length)

const onScroll = () => {
  const el = textareaRef.value
  if (el && gutterInnerRef.value) {
    gutterInnerRef.value.style.transform = `translateY(${-el.scrollTop}px)`
  }
}

/* ============ 工具栏插入操作 ============ */
const wrapSelection = (before: string, after: string, placeholder = '') => {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = content.value.substring(start, end) || placeholder
  const newText =
    content.value.substring(0, start) + before + selected + after + content.value.substring(end)
  content.value = newText
  nextTick(() => {
    el.focus()
    el.setSelectionRange(start + before.length, start + before.length + selected.length)
  })
}

const insertLine = (prefix: string, placeholder = '') => {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const lineStart = content.value.lastIndexOf('\n', start - 1) + 1
  const selected = content.value.substring(el.selectionStart, el.selectionEnd) || placeholder
  const newText =
    content.value.substring(0, lineStart) + prefix + content.value.substring(lineStart)
  content.value = newText
  nextTick(() => {
    el.focus()
    el.setSelectionRange(lineStart + prefix.length, lineStart + prefix.length + selected.length)
  })
}

const tools = [
  { icon: 'H', title: '标题', action: () => insertLine('## ', '标题') },
  { icon: 'B', title: '加粗', action: () => wrapSelection('**', '**', '粗体文字'), bold: true },
  { icon: 'I', title: '斜体', action: () => wrapSelection('*', '*', '斜体文字'), italic: true },
  { icon: '~', title: '删除线', action: () => wrapSelection('~~', '~~', '删除文字') },
  { sep: true },
  { icon: '•', title: '无序列表', action: () => insertLine('- ', '列表项') },
  { icon: '1.', title: '有序列表', action: () => insertLine('1. ', '列表项') },
  { icon: '☑', title: '任务列表', action: () => insertLine('- [ ] ', '任务') },
  { sep: true },
  { icon: '""', title: '引用', action: () => insertLine('> ', '引用内容') },
  { icon: '<>', title: '代码块', action: () => wrapSelection('\n```\n', '\n```\n', '代码') },
  { icon: '—', title: '分割线', action: () => wrapSelection('\n---\n', '') },
  { icon: '🔗', title: '链接', action: () => wrapSelection('[', '](url)', '链接文字') },
  {
    icon: '📊',
    title: '表格',
    action: () =>
      wrapSelection('\n| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| ', ' | | |\n', '内容')
  }
]

/* ============ 全屏 / 填充 ============ */
const toggleFullscreen = () => {
  fullscreen.value = !fullscreen.value
  nextTick(onScroll)
}

// height="100%" 时进入填充模式：编辑器撑满外层容器（由父级 flex 决定高度）
const fill = computed(() => props.height === '100%' || props.height === 'fill')
const bodyStyle = computed(() => {
  if (fullscreen.value) return { height: 'calc(100vh - 46px)' }
  if (fill.value) return {}
  return { height: props.height }
})

/* ============ 暴露：定位到指定行（供外部目录树点击联动） ============ */
const scrollToLine = (line: number) => {
  const el = textareaRef.value
  if (!el) return
  const lines = content.value.split('\n')
  let pos = 0
  for (let i = 0; i < Math.min(line - 1, lines.length); i++) pos += lines[i].length + 1
  el.focus()
  el.setSelectionRange(pos, pos)
  const lh = 20
  el.scrollTop = Math.max(0, (line - 1) * lh - el.clientHeight / 3)
  onScroll()
}

defineExpose({ scrollToLine, focus: () => textareaRef.value?.focus() })
</script>

<template>
  <div
    class="simple-md"
    :class="{
      'simple-md--readonly': readonly,
      'is-fullscreen': fullscreen,
      'is-fill': fill && !fullscreen
    }"
  >
    <div class="simple-md__toolbar" v-if="!readonly">
      <!-- 索引下划线前缀：vue-tsc 不把 <template> 上的 :key 算作引用，不加前缀会误报 noUnusedLocals -->
      <template v-for="(tool, _i) in tools" :key="_i">
        <div v-if="tool.sep" class="simple-md__sep"></div>
        <button
          v-else
          class="simple-md__btn"
          :title="tool.title"
          :style="{ fontWeight: tool.bold ? '700' : 'normal', fontStyle: tool.italic ? 'italic' : 'normal' }"
          @click.prevent="tool.action"
        >
          {{ tool.icon }}
        </button>
      </template>

      <div class="simple-md__spacer"></div>

      <button class="simple-md__btn" title="撤销" :disabled="!canUndo" @click.prevent="undo">
        <icon-undo />
      </button>
      <button class="simple-md__btn" title="重做" :disabled="!canRedo" @click.prevent="redo">
        <icon-redo />
      </button>
      <div class="simple-md__sep"></div>
      <button class="simple-md__btn" :title="fullscreen ? '退出全屏' : '全屏'" @click.prevent="toggleFullscreen">
        <icon-fullscreen-exit v-if="fullscreen" />
        <icon-fullscreen v-else />
      </button>
    </div>

    <div class="simple-md__body" :class="{ 'is-fill': fill && !fullscreen }" :style="bodyStyle">
      <div class="simple-md__gutter">
        <div ref="gutterInnerRef" class="simple-md__gutter-inner">
          <div v-for="n in lineNumbers" :key="n" class="simple-md__ln">{{ n }}</div>
        </div>
      </div>
      <textarea
        ref="textareaRef"
        v-model="content"
        class="simple-md__textarea"
        :placeholder="placeholder"
        :readonly="readonly"
        spellcheck="false"
        @scroll="onScroll"
      ></textarea>
    </div>

    <div class="simple-md__footer" v-if="!readonly">
      <span class="simple-md__stat">行 {{ lineCount }}</span>
      <span class="simple-md__stat">字符 {{ charCount }}</span>
      <div class="simple-md__spacer"></div>
      <span class="simple-md__stat simple-md__stat--muted">Markdown</span>
    </div>
  </div>
</template>

<style scoped>
.simple-md {
  display: flex;
  overflow: hidden;
  background: var(--color-bg-2, #fff);
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: 6px;
  flex-direction: column;
}

.simple-md.is-fullscreen {
  position: fixed;
  inset: 0;
  z-index: 2000;
  border-radius: 0;
}

.simple-md.is-fill {
  height: 100%;
}

.simple-md--readonly {
  background: var(--color-fill-1, #f7f8fa);
}

.simple-md__toolbar {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 8px;
  background: var(--color-fill-1, #f7f8fa);
  border-bottom: 1px solid var(--color-border-2, #e5e6eb);
  flex-wrap: wrap;
  flex-shrink: 0;
}

.simple-md__spacer {
  flex: 1;
}

.simple-md__btn {
  display: inline-flex;
  height: 28px;
  min-width: 28px;
  padding: 0 6px;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  color: var(--color-text-2, #4e5969);
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 4px;
  transition: all var(--bm-dur) var(--bm-ease-out);
  align-items: center;
  justify-content: center;
}

.simple-md__btn:hover {
  color: rgb(var(--primary-6, 22 93 255));
  background: rgb(var(--primary-1, 232 243 255));
}

.simple-md__btn:disabled {
  color: var(--color-text-4, #c9cdd4);
  cursor: not-allowed;
  background: transparent;
}

.simple-md__sep {
  width: 1px;
  height: 18px;
  margin: 0 4px;
  background: var(--color-border-2, #e5e6eb);
}

.simple-md__body {
  display: flex;
  min-height: 200px;
  overflow: hidden;
}

.simple-md__body.is-fill {
  flex: 1;
  min-height: 0;
}

.simple-md__gutter {
  flex-shrink: 0;
  width: 46px;
  overflow: hidden;
  background: var(--color-fill-1, #f7f8fa);
  border-right: 1px solid var(--color-border-2, #e5e6eb);
  user-select: none;
}

.simple-md__gutter-inner {
  padding: 12px 8px 12px 0;
  text-align: right;
  will-change: transform;
}

.simple-md__ln {
  height: 20px;
  font-family: 'Cascadia Code', 'Fira Code', 'Courier New', Consolas, monospace;
  font-size: 13px;
  line-height: 20px;
  color: var(--color-text-4, #c9cdd4);
}

.simple-md__textarea {
  display: block;
  width: 100%;
  height: 100%;
  padding: 12px 16px;
  font-family: 'Cascadia Code', 'Fira Code', 'Courier New', Consolas, monospace;
  font-size: 13px;
  line-height: 20px;
  color: var(--color-text-1, #1d2129);
  background: transparent;
  border: none;
  outline: none;
  box-sizing: border-box;
  resize: none;
  flex: 1;
  tab-size: 4;
}

.simple-md__textarea::placeholder {
  color: var(--color-text-4, #c9cdd4);
}

.simple-md__footer {
  display: flex;
  padding: 4px 12px;
  font-size: 12px;
  color: var(--bm-text-3);
  background: var(--color-fill-1, #f7f8fa);
  border-top: 1px solid var(--color-border-2, #e5e6eb);
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.simple-md__stat {
  white-space: nowrap;
}

.simple-md__stat--muted {
  color: var(--color-text-4, #c9cdd4);
}
</style>
