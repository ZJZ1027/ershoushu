<template>
  <div class="empty-state" :class="[`is-${size}`, `tone-${tone}`]">
    <span v-if="icon || $slots.icon" class="es-icon">
      <slot name="icon"><component :is="icon" /></slot>
    </span>
    <div class="es-title">{{ title }}</div>
    <div v-if="description" class="es-desc">{{ description }}</div>
    <div v-if="actionText || $slots.action" class="es-action">
      <slot name="action">
        <a-button type="primary" :size="size === 'sm' ? 'small' : 'medium'" @click="emit('action')">
          <template v-if="actionIcon" #icon><component :is="actionIcon" /></template>
          {{ actionText }}
        </a-button>
      </slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { Component } from 'vue'

/**
 * 统一空态。
 *
 * 替代各处直接用的 `<a-empty description="暂无 X，点击右上角「新建 X」开始" />`：
 * 那种写法是用文案去替代一个本该就在视线落点上的按钮——用户读完还得把视线挪回右上角找。
 * 这里把「图标 + 一句话说清是什么 + 为什么需要它 + 就地可点的按钮」收成一个组件，
 * 顺带统一各页面空态的留白与字号（原先每处 a-empty 的外距都各写各的）。
 *
 * 没有下一步动作的场景（如「暂无日志」）只传 title 即可，不给按钮。
 */
defineOptions({ name: 'EmptyState' })

withDefaults(
  defineProps<{
    title: string
    description?: string
    /** 主图标；不传则只显示标题与描述 */
    icon?: Component
    actionText?: string
    actionIcon?: Component
    /** sm：卡片内 / 抽屉内的小空位；default：整页或整块的主空态 */
    size?: 'default' | 'sm'
    /**
     * warning：给「进错门」的场景（缺参数、对象不存在/无权访问）。
     * 这类不是空态而是错误态，图标片若还用品牌蓝会被读成「这里可以有东西」。
     */
    tone?: 'brand' | 'warning'
  }>(),
  { description: '', size: 'default', tone: 'brand' }
)

const emit = defineEmits<{ (e: 'action'): void }>()
</script>

<style scoped lang="scss">


@keyframes es-enter {
  from {
    opacity: 0;
    transform: translateY(6px);
  }

  to {
    opacity: 1;
    transform: none;
  }
}

.empty-state {
  display: flex;
  padding: 52px 24px;
  text-align: center;

  /* 空态是「加载完才知道是空」的结论，硬切出现像闪了一下；按动效刻度的入场档淡入上浮。
     减弱动态偏好由全局那条 prefers-reduced-motion 兜住，这里不重复写。 */
  animation: es-enter var(--bm-dur-enter) var(--bm-ease-out) both;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 图标用品牌淡底圆角块托一下，比 Arco 默认那个灰盒子更像「这里可以有东西」而不是「出错了」 */
.es-icon {
  display: inline-flex;
  width: 52px;
  height: 52px;
  margin-bottom: 16px;
  font-size: 24px;
  color: var(--bm-brand);
  background: var(--bm-brand-bg);
  border-radius: var(--bm-radius-lg, 14px);
  align-items: center;
  justify-content: center;
}

.empty-state.tone-warning .es-icon {
  color: var(--bm-warning-text);
  background: var(--bm-warning-bg);
}

.es-title {
  font-size: var(--bm-fs-md, 14px);
  font-weight: 600;
  line-height: var(--bm-lh-tight, 1.35);
  color: var(--bm-text-1);
}

/* balance：让两三行的短文案自动均分长度，避免出现「末行只剩两个字」的参差断行 */
.es-desc {
  max-width: 380px;
  margin-top: 6px;
  font-size: var(--bm-fs-xs, 12px);
  line-height: var(--bm-lh-body, 1.6);
  color: var(--bm-text-3);
  text-wrap: balance;
}

.es-action {
  margin-top: 16px;
}

/* 小尺寸：卡片 / 抽屉内的局部空位，压缩留白与图标 */
.empty-state.is-sm {
  padding: 28px 16px;

  .es-icon {
    width: 40px;
    height: 40px;
    margin-bottom: 12px;
    font-size: 20px;
    border-radius: var(--bm-radius, 10px);
  }

  .es-desc {
    margin-top: 4px;
  }

  .es-action {
    margin-top: 12px;
  }
}
</style>
