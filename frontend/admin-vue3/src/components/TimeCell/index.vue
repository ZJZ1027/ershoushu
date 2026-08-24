<template>
  <span v-if="!parsed" class="bm-cell-empty">—</span>
  <div v-else class="time-cell" :class="{ 'time-cell--inline': inline }" :title="parsed.full">
    <span class="time-cell__date">{{ parsed.date }}</span>
    <span class="time-cell__time">{{ parsed.time }}</span>
  </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'

/**
 * 表格时间戳单元格：日期一行、时刻一行。
 *
 * 为什么不继续用单行 `YYYY-MM-DD HH:mm:ss`：那串字面宽约 140px，加上单元格内边距，
 * 一个时间列就得占 170px 的刚性宽度。而列表页普遍有 7~9 列，时间列一列就吃掉容器的
 * 五分之一，是各页面横向滚动条的主要来源之一（详见 docs/manual/shots/_tablesweep.mjs
 * 量出的溢出账）。拆两行后 100px 就够，且不丢秒。
 *
 * 行高不受影响：列表行本身是 56px，主体列（名称+编号那种）早已是两行，这里正好对齐。
 */
defineOptions({ name: 'TimeCell' })

const props = defineProps<{
  value?: string | number | Date | null
  /** 单行模式：日期与时刻并排，给行高很矮或时间是唯一信息的表格用 */
  inline?: boolean
}>()

const parsed = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') return null
  const d = dayjs(props.value)
  if (!d.isValid()) return null
  return {
    date: d.format('YYYY-MM-DD'),
    time: d.format('HH:mm:ss'),
    full: d.format('YYYY-MM-DD HH:mm:ss')
  }
})
</script>

<style lang="scss" scoped>
/* 用 inline-block + 继承 text-align 来跟随列的 align，而不是去挂 Arco 的对齐类名：
   外层按 td 的 text-align 摆放整块，内部两行按同一个 text-align 各自对齐，
   居中列和左对齐列都不用改组件。 */
.time-cell {
  display: inline-block;
  line-height: 1.4;

  /* 等宽数位：比例数字会让相邻行的日期逐位错开，扫一列时很晃眼 */
  font-variant-numeric: tabular-nums;
  font-feature-settings: 'tnum';
  white-space: nowrap;
}

.time-cell__date,
.time-cell__time {
  display: block;
}

.time-cell--inline {
  .time-cell__date,
  .time-cell__time {
    display: inline;
  }

  .time-cell__time {
    margin-left: 6px;
  }
}

.time-cell__date {
  font-size: 13px;
  color: var(--bm-text-1);
}

.time-cell__time {
  font-size: 12px;
  color: var(--bm-text-3);
}
</style>
