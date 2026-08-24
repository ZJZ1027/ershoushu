<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { useConfigGlobal } from '@/hooks/web/useConfigGlobal'
import type { ZxcvbnResult } from '@zxcvbn-ts/core'
import { zxcvbn } from '@zxcvbn-ts/core'
import { useDesign } from '@/hooks/web/useDesign'

defineOptions({ name: 'InputPassword' })

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('input-password')

const props = defineProps({
  // 是否显示密码强度
  strength: propTypes.bool.def(false),
  modelValue: propTypes.string.def('')
})

const { configGlobal } = useConfigGlobal()

const emit = defineEmits(['update:modelValue'])

// 输入框的值
const valueRef = ref(props.modelValue)

watch(
  () => props.modelValue,
  (val: string) => {
    if (val === unref(valueRef)) return
    valueRef.value = val
  }
)

// 监听
watch(
  () => valueRef.value,
  (val: string) => {
    emit('update:modelValue', val)
  }
)

// 获取密码强度
const getPasswordStrength = computed(() => {
  const value = unref(valueRef)
  const zxcvbnRef = zxcvbn(unref(valueRef)) as ZxcvbnResult
  return value ? zxcvbnRef.score : -1
})
</script>

<template>
  <div :class="[prefixCls, `${prefixCls}--${configGlobal?.size}`]">
    <!-- Arco a-input-password 自带显隐切换（眼睛），无需自定义 suffix -->
    <a-input-password v-model="valueRef" allow-clear v-bind="$attrs" />
    <div
      v-if="strength"
      :class="`${prefixCls}__bar`"
      class="relative mb-6px ml-auto mr-auto mt-10px h-6px"
    >
      <div :class="`${prefixCls}__bar--fill`" :data-score="getPasswordStrength"></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-input-password;

.#{$prefix-cls} {
  &__bar {
    background-color: var(--color-fill-3);
    border-radius: 4px;

    &::before,
    &::after {
      position: absolute;
      z-index: 10;
      display: block;
      width: 20%;
      height: inherit;
      background-color: transparent;
      border-color: #fff;
      border-style: solid;
      border-width: 0 5px;
      content: '';
    }

    &::before {
      left: 20%;
    }

    &::after {
      right: 20%;
    }

    &--fill {
      position: absolute;
      width: 0;
      height: inherit;
      background-color: transparent;
      border-radius: inherit;
      transition: width var(--bm-dur-enter) var(--bm-ease-out), background var(--bm-dur-slow) var(--bm-ease-out);

      &[data-score='0'] {
        width: 20%;
        background-color: rgb(var(--red-6));
      }

      &[data-score='1'] {
        width: 40%;
        background-color: rgb(var(--red-6));
      }

      &[data-score='2'] {
        width: 60%;
        background-color: rgb(var(--orange-6));
      }

      &[data-score='3'] {
        width: 80%;
        background-color: rgb(var(--green-6));
      }

      &[data-score='4'] {
        width: 100%;
        background-color: rgb(var(--green-6));
      }
    }
  }
}
</style>
