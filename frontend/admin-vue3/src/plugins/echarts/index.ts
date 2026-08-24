import * as echarts from 'echarts/core'

import {
  BarChart,
  FunnelChart,
  GaugeChart,
  HeatmapChart,
  LineChart,
  MapChart,
  PictorialBarChart,
  PieChart,
  RadarChart
} from 'echarts/charts'

import {
  AriaComponent,
  DataZoomComponent,
  GridComponent,
  LegendComponent,
  ParallelComponent,
  PolarComponent,
  TitleComponent,
  ToolboxComponent,
  TooltipComponent,
  VisualMapComponent
} from 'echarts/components'

import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  ToolboxComponent,
  DataZoomComponent,
  GridComponent,
  PolarComponent,
  AriaComponent,
  ParallelComponent,
  VisualMapComponent,
  BarChart,
  LineChart,
  PieChart,
  MapChart,
  CanvasRenderer,
  PictorialBarChart,
  RadarChart,
  GaugeChart,
  FunnelChart,
  HeatmapChart
])

export default echarts
