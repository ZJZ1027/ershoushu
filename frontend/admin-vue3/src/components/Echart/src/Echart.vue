<script lang="ts" setup>
import type { EChartsOption } from 'echarts'
import echarts from '@/plugins/echarts'
import { debounce } from 'lodash-es'
import { propTypes } from '@/utils/propTypes'
import { PropType } from 'vue'
import { useAppStore } from '@/store/modules/app'
import { isString } from '@/utils/is'
import { useDesign } from '@/hooks/web/useDesign'

import 'echarts/lib/component/markPoint'
import 'echarts/lib/component/markLine'
import 'echarts/lib/component/markArea'

defineOptions({ name: 'EChart' })

const { getPrefixCls, variables } = useDesign()

const prefixCls = getPrefixCls('echart')

const appStore = useAppStore()

const props = defineProps({
  options: {
    type: Object as PropType<EChartsOption>,
    required: true
  },
  width: propTypes.oneOfType([Number, String]).def(''),
  height: propTypes.oneOfType([Number, String]).def('500px')
})

const isDark = computed(() => appStore.getIsDark)

const theme = computed(() => {
  const echartTheme: boolean | 'auto' = unref(isDark) ? true : 'auto'

  return echartTheme
})

const options = computed(() => {
  // 不能用 Object.assign(props.options, ...)：props 是只读的，改写会触发 Vue readonly 告警；
  // 且 options 作为深层 watch 的源，对同一个响应式对象反复「读 + 写」会让依赖收集与触发
  // 互相嵌套，极端情况下把组件树拖进更新风暴（表现为切页后卡死、接口不再触发）。
  // 这里用展开复制出一份新对象再附上 darkMode，不污染 props，watch 每次也能拿到新引用。
  return {
    ...props.options,
    darkMode: unref(theme)
  }
})

const elRef = ref<ElRef>()

let echartRef: Nullable<echarts.ECharts> = null

const contentEl = ref<Element>()

const styles = computed(() => {
  const width = isString(props.width) ? props.width : `${props.width}px`
  const height = isString(props.height) ? props.height : `${props.height}px`

  return {
    width,
    height
  }
})

const initChart = () => {
  if (unref(elRef) && props.options) {
    echartRef = echarts.init(unref(elRef) as HTMLElement)
    // 空数据/非法配置（如空 indicator 的雷达图）会让 ECharts 在 setOption 内抛错。
    // 若放任它从 mounted 钩子抛出，会打断 Vue 的挂载/更新，导致后续切页卡死、接口不触发。
    // 这里兜底捕获，等数据到位后 watch 会再次 setOption 重绘。
    try {
      echartRef?.setOption(unref(options), { notMerge: true })
    } catch (e) {
      console.warn('[EChart] setOption 失败（数据可能未就绪），已忽略', e)
    }
  }
}

watch(
  () => options.value,
  (options) => {
    if (echartRef) {
      try {
        echartRef?.setOption(options, { notMerge: true })
        echartRef?.resize()
      } catch (e) {
        console.warn('[EChart] setOption 失败，已忽略', e)
      }
    }
  },
  {
    deep: true
  }
)

const resizeHandler = debounce(() => {
  if (echartRef) {
    echartRef.resize()
  }
}, 100)

const contentResizeHandler = async (e: TransitionEvent) => {
  if (e.propertyName === 'width') {
    resizeHandler()
  }
}

onMounted(() => {
  initChart()

  window.addEventListener('resize', resizeHandler)

  contentEl.value = document.getElementsByClassName(`${variables.namespace}-layout-content`)[0]
  unref(contentEl) &&
    (unref(contentEl) as Element).addEventListener('transitionend', contentResizeHandler)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
  unref(contentEl) &&
    (unref(contentEl) as Element).removeEventListener('transitionend', contentResizeHandler)
})

onActivated(() => {
  if (echartRef) {
    echartRef.resize()
  }
})
</script>

<template>
  <div ref="elRef" :class="[$attrs.class, prefixCls]" :style="styles"></div>
</template>
