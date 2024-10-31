import { h } from 'vue'
import {
  PieChartOutlined,
  TrademarkCircleOutlined,
  RadarChartOutlined
} from '@ant-design/icons-vue'
import {
  YYYYMMDD_HHMMSS,
  summaryOptions as summary
} from '@/views/dataset/config.field'

// 图表类型
export const chartTypeMap = {
  table: {
    label: '表格',
    icon: () => h(<SvgIcon name='明细表' />)
  },
  groupTable: {
    label: '分组表格',
    icon: () => h(<SvgIcon name='分组表' />)
  },
  intersectionTable: {
    label: '交叉表格',
    icon: () => h(<SvgIcon name='交叉表' />)
  },
  bar: {
    label: '柱状图',
    icon: () => h(<SvgIcon name='簇型柱图' />)
  },
  line: {
    label: '折线图',
    icon: () => h(<SvgIcon name='折线图' />)
  },
  pie: {
    label: '饼图',
    icon: () => h(<SvgIcon name='饼图' />)
  },
  statistic: {
    label: '统计数值',
    icon: () => h(<SvgIcon name='指标卡' />)
  }
}

// 表格布局
export const tableLayoutOptions = [
  {
    label: '自适应',
    value: 'auto'
  },
  {
    label: '等宽',
    value: 'fixed'
  }
]

// 表格文字对齐方式
export const tableAlignOptions = [
  {
    label: '左对齐',
    value: 'left'
  },
  {
    label: '居中',
    value: 'center'
  },
  {
    label: '右对齐',
    value: 'right'
  }
]

// 图表图例布局
export const chartLegendLayout = {
  horizontal: {
    label: '水平',
    value: 'horizontal'
  },
  vertical: {
    label: '垂直',
    value: 'vertical'
  }
}

// 图表图例位置
export const chartLayoutPositionOptions = [
  {
    label: '左上',
    value: 'topLeft'
  },
  {
    label: '上',
    value: 'top'
  },
  {
    label: '右上',
    value: 'topRight'
  },
  {
    label: '左下',
    value: 'bottomLeft'
  },
  {
    label: '下',
    value: 'bottom'
  },
  {
    label: '右下',
    value: 'bottomRight'
  }
]

export const pieTypeOptions = [
  {
    label: '饼图',
    value: 'pie',
    icon: PieChartOutlined
  },
  {
    label: '环图',
    value: 'ring',
    icon: TrademarkCircleOutlined
  },
  {
    label: '玫瑰图',
    value: 'rose',
    icon: RadarChartOutlined
  }
]

// 图表坐标轴设置 表格列
export const chartAxisColumns = [
  { title: '指标名称', dataIndex: 'displayName', width: 145 },
  {
    title: '图表类型',
    dataIndex: 'chartType',
    width: 100
  },
  {
    title: '坐标轴',
    dataIndex: 'yAxisPosition',
    width: 100
  }
]

export const summaryOptions = [
  {
    label: '自动',
    value: 'auto'
  }
].concat(summary.filter(t => !t.hidden))

export const specialNumbers = ['', 'NaN', 'Infinity', null]
export const isSpecialNumber = t => specialNumbers.includes(String(t))
export const specialNumberOptions = [
  // { label: '空值', value: '', hidden: true },
  { label: '原值', value: 'original' },
  // { label: 'NaN', value: 'NaN' },
  // { label: 'Infinity', value: 'Infinity' },
  { label: '-', value: '-' },
  { label: 'null', value: 'null' },
  { label: '0', value: '0' }
  // { label: 'UNKNOWN', value: 'UNKNOWN' }
]

export const barStyleOptions = [
  { label: '簇形柱图', value: 'clustered', icon: '簇形柱状图' },
  { label: '堆积柱图', value: 'stacked', icon: '堆积柱状图' },
  {
    label: '百分比柱图',
    value: 'stackedPercent',
    icon: '百分比柱状图',
    hidden: true
  }
]
