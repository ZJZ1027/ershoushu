<script lang="ts" setup>
import { IconPlus } from '@arco-design/web-vue/es/icon'
import {
  MENU_ICON_GROUPS,
  menuIconLabel,
  resolveMenuIcon,
  toMenuIconName,
  toMenuIconValue,
  type MenuIconName
} from './menuIcons'

defineOptions({ name: 'IconSelect' })

const props = defineProps({
  modelValue: {
    require: false,
    type: String
  },
  clearable: {
    require: false,
    type: Boolean
  }
})
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const visible = ref(false)
const keyword = ref('')

/** 输入框里展示去前缀的图标名（`arco:IconUserGroup` -> `UserGroup`），存库值不暴露给使用者 */
const displayName = computed(() => menuIconLabel(toMenuIconName(props.modelValue)))
const currentName = computed(() => toMenuIconName(props.modelValue))

/** 搜索按图标名匹配（Arco 名即语义，如 user / folder / lock）；命中时拉平成一组，不再分区 */
const groups = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return MENU_ICON_GROUPS
  const hit = MENU_ICON_GROUPS.flatMap((g) => g.names).filter((n) => n.toLowerCase().includes(kw))
  return hit.length ? [{ label: `搜索结果（${hit.length}）`, names: hit }] : []
})

const onPick = (name: MenuIconName) => {
  emit('update:modelValue', toMenuIconValue(name))
  visible.value = false
}

const onClear = () => {
  emit('update:modelValue', '')
  visible.value = false
}

// 关闭时清掉搜索词，下次打开回到完整分组视图
watch(visible, (v) => {
  if (!v) keyword.value = ''
})
</script>

<template>
  <a-popover
    v-model:popup-visible="visible"
    trigger="click"
    position="bl"
    content-class="icon-select-popover"
  >
    <!-- 只读输入框当触发器：值是注册表里的图标名，不允许手输，避免又出现库里存着渲染不出的名字。
         清除按钮放在面板内而不是输入框内：输入框整体是 popover 的触发区，
         框内的清除按钮点下去会连带切换面板显隐。 -->
    <a-input
      :model-value="displayName"
      class="icon-select-input"
      placeholder="点击选择图标"
      readonly
    >
      <template #prefix>
        <!-- 未选择时给个「加」而不是默认菜单图标，否则空态看着像已经选了个 ≡ -->
        <component
          :is="currentName ? resolveMenuIcon(props.modelValue) : IconPlus"
          :class="['icon-select-preview', { 'is-empty': !currentName }]"
        />
      </template>
    </a-input>

    <template #content>
      <div class="icon-select-panel">
        <a-input v-model="keyword" allow-clear placeholder="搜索图标，如 user / folder / lock">
          <template #prefix><icon-search /></template>
        </a-input>

        <div class="icon-select-scroll">
          <template v-for="group in groups" :key="group.label">
            <div class="icon-select-group">{{ group.label }}</div>
            <div class="icon-select-grid">
              <button
                v-for="name in group.names"
                :key="name"
                :class="['icon-select-cell', { 'is-active': name === currentName }]"
                :title="menuIconLabel(name)"
                type="button"
                @click="onPick(name)"
              >
                <component :is="resolveMenuIcon(name)" />
              </button>
            </div>
          </template>
          <a-empty v-if="!groups.length" description="没有匹配的图标" />
        </div>

        <div class="icon-select-foot">
          <span class="icon-select-current">{{ displayName || '未选择' }}</span>
          <a-button v-if="props.clearable" size="mini" type="text" @click="onClear">清除</a-button>
        </div>
      </div>
    </template>
  </a-popover>
</template>

<style lang="scss" scoped>
.icon-select-input {
  cursor: pointer;

  :deep(.arco-input) {
    cursor: pointer;
  }
}

.icon-select-preview {
  font-size: 15px;
  color: var(--bm-text-2);

  &.is-empty {
    color: var(--bm-text-4);
  }
}

.icon-select-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 316px;
}

.icon-select-scroll {
  max-height: 264px;
  overflow-y: auto;
}

/* 分区名钉在滚动容器顶部：一屏放不下六组，滚动时要能一直看出当前在哪一组。
   底色取 popover 自身的 --color-bg-popup（暗色主题下它和卡片底不是一个值），
   否则滚动时图标会从色差里穿出来。 */
.icon-select-group {
  position: sticky;
  top: 0;
  z-index: 1;
  padding: 4px 0;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.5px;
  color: var(--bm-text-3);
  background: var(--color-bg-popup, var(--bm-bg-card, #fff));
}

.icon-select-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  padding-bottom: 8px;
}

.icon-select-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0;
  font-size: 16px;
  color: var(--bm-text-2);
  cursor: pointer;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  transition:
    color var(--bm-dur-fast) var(--bm-ease-out),
    background-color var(--bm-dur-fast) var(--bm-ease-out);

  &:hover {
    color: var(--bm-brand-text);
    background: var(--bm-fill-light, #f7f8fa);
  }

  &.is-active {
    color: var(--bm-brand-text);
    background: var(--bm-brand-bg, rgb(22 93 255 / 8%));
    border-color: rgb(var(--primary-6));
  }
}

.icon-select-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid var(--bm-border-light);
}

.icon-select-current {
  font-size: 12px;
  color: var(--bm-text-3);
}
</style>
