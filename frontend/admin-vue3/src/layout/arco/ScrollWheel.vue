<template>
  <transition name="sw-pop">
    <div v-if="visible" class="scroll-wheel" :class="'dir-' + (direction || 'none')">
      <button
        class="sw-half sw-up"
        :class="{ active: direction === 'up' }"
        title="向上滚动"
        @click="$emit('up')"
      >
        <icon-up />
      </button>
      <button
        class="sw-half sw-down"
        :class="{ active: direction === 'down' }"
        title="向下滚动"
        @click="$emit('down')"
      >
        <icon-down />
      </button>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { IconUp, IconDown } from '@arco-design/web-vue/es/icon'

defineOptions({ name: 'ScrollWheel' })
defineProps<{ visible?: boolean; direction?: 'up' | 'down' | 'none' }>()
defineEmits<{ (e: 'up'): void; (e: 'down'): void }>()
</script>

<style scoped>
.scroll-wheel {
  position: absolute;
  right: 18px;
  bottom: 22px;
  z-index: 60;
  display: flex;
  width: 40px;
  height: 56px;
  overflow: hidden;
  background: var(--color-bg-2, #fff);
  border: 1px solid var(--color-border-2, #e5e6eb);
  border-radius: 16px;
  box-shadow: 0 6px 20px -6px rgb(0 0 0 / 18%);
  flex-direction: column;
}

.sw-half {
  display: flex;
  padding: 0;
  font-size: 13px;
  color: var(--color-text-4, #c9cdd4);
  cursor: pointer;
  background: transparent;
  border: none;
  transition: background-color var(--bm-dur) var(--bm-ease-out),
    color var(--bm-dur) var(--bm-ease-out);
  flex: 1;
  align-items: center;
  justify-content: center;
}

.sw-half:hover {
  color: var(--color-text-2, #4e5969);
  background: var(--color-fill-2, #f2f3f5);
}

.sw-up {
  align-items: flex-end;
  padding-bottom: 2px;
}

.sw-down {
  align-items: flex-start;
  padding-top: 2px;
}

.sw-half.active {
  color: rgb(var(--primary-6));
  background: rgba(var(--primary-6), 0.12);
}

/* 中间分隔线，像一个滚轮 */
.scroll-wheel::before {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 14px;
  height: 1px;
  background: var(--color-border-2, #e5e6eb);
  content: '';
  transform: translate(-50%, -50%);
}

/* 进场用 out（起步快收尾稳）、退场用 in（起步稳收尾快），符合动效刻度里的曲线约定 */
.sw-pop-enter-active {
  transition: opacity var(--bm-dur) var(--bm-ease-out), transform var(--bm-dur) var(--bm-ease-out);
}

.sw-pop-leave-active {
  transition: opacity var(--bm-dur-fast) var(--bm-ease-in),
    transform var(--bm-dur-fast) var(--bm-ease-in);
}

.sw-pop-enter-from,
.sw-pop-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
