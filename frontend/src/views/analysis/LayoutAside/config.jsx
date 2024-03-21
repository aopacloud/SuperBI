import { h } from 'vue'
import Icon, {
  TableOutlined,
  BarChartOutlined,
  LineChartOutlined,
  PieChartOutlined,
  FieldNumberOutlined,
  TrademarkCircleOutlined,
  RadarChartOutlined,
} from '@ant-design/icons-vue'
import GroupTableIcon from './icon/GroupTableIcon.vue'

// 字段类型
export const fieldTypeMap = {
  TEXT: {
    icon: '文本',
    color: '#84bedb',
  },
  NUMBER: {
    icon: '数字',
    color: '#5a9f76',
  },
  TIME: {
    icon: '日期',
    color: '#f2c96b',
  },
  TIME_YYYY: {
    icon: '日期',
    color: '#f2c96b',
  },
  TIME_YYYYMM: {
    icon: '日期',
    color: '#f2c96b',
  },
  TIME_YYYYMMDD: {
    icon: '日期',
    color: '#f2c96b',
  },
  TIME_YYYYMMDD_HHMMSS: {
    icon: '日期',
    color: '#f2c96b',
  },
  ARRAY: {
    icon: '数组',
    color: '#9265af',
  },
  TUPLE: {
    icon: '元组',
    color: '#df6e6a',
  },
}

// 图表类型
export const chartTypeMap = {
  table: {
    label: '表格',
    color: '#666666',
    icon: TableOutlined,
  },
  groupTable: {
    label: '分组表格',
    color: '#666666',
    icon: () => h(GroupTableIcon),
  },
  // intersectionTable: {
  //   label: '交叉表格',
  //   color: '#666666',
  //   icon: () =>
  //     h(
  //       <img
  //         style='width: 22px; height: 22px;vertical-align: -5px;'
  //         src={intersectionTableImg}
  //       />
  //     ),
  // },
  bar: {
    label: '柱状图',
    color: '#666666',
    icon: BarChartOutlined,
  },
  line: {
    label: '折线图',
    color: '#666666',
    icon: LineChartOutlined,
  },
  pie: {
    label: '饼图',
    color: '#666666',
    icon: PieChartOutlined,
  },
  statistic: {
    label: '统计数值',
    color: '#666666',
    icon: FieldNumberOutlined,
  },
}

// 表格布局
export const tableLayoutOptions = [
  {
    label: '自适应',
    value: 'auto',
  },
  {
    label: '等宽',
    value: 'fixed',
  },
]

// 表格文字对齐方式
export const tableAlignOptions = [
  {
    label: '左对齐',
    value: 'left',
  },
  {
    label: '居中',
    value: 'center',
  },
  {
    label: '右对齐',
    value: 'right',
  },
]

// 图表图例布局
export const chartLegendLayout = {
  horizontal: {
    label: '水平',
    value: 'horizontal',
  },
  vertical: {
    label: '垂直',
    value: 'vertical',
  },
}

// 图表图例位置
export const chartLayoutPositionOptions = [
  {
    label: '左上',
    value: 'topLeft',
  },
  {
    label: '上',
    value: 'top',
  },
  {
    label: '右上',
    value: 'topRight',
  },
  {
    label: '左下',
    value: 'bottomLeft',
  },
  {
    label: '下',
    value: 'bottom',
  },
  {
    label: '右下',
    value: 'bottomRight',
  },
]

export const pieTypeOptions = [
  {
    label: '饼图',
    value: 'pie',
    icon: PieChartOutlined,
  },
  {
    label: '环图',
    value: 'ring',
    icon: TrademarkCircleOutlined,
  },
  {
    label: '玫瑰图',
    value: 'rose',
    icon: RadarChartOutlined,
  },
]

// 图表坐标轴设置 表格列
export const chartAxisColumns = [
  { title: '指标名称', dataIndex: 'displayName', width: 145 },
  {
    title: '图表类型',
    dataIndex: 'chartType',
    width: 100,
  },
  {
    title: '坐标轴',
    dataIndex: 'yAxisPosition',
    width: 100,
  },
]

// /**
//  * 表格默认配置
//  * layout: 表格布局方式; fixed 固定列宽, auto 自动列宽，默认撑满容器
//  * fixed: 固定列配置; [firstRow] 首行固定, [column] 列固定, 0 无冻结, 1 左冻结, 2 自定义冻结, [colSpan] 列合定数量
//  * align: 文字对齐方式; left 左对齐, center 居中, right 右对齐
//  * compare: 同环比配置; [mode] 内容显示, 0 默认, 1 差值, 2 差值百分比, [merge] 是否合并显示
//  */
// export const defaultTableOptions = {
//   layout: 'fixed',
//   fixed: {
//     firstRow: true,
//     columnMode: 0,
//     columnSpan: [0, 0],
//   },
//   align: 'left',
//   compare: {
//     mode: 0, // 0 默认，1 差值， 2 差值百分比
//     merge: false, // false 单独显示， true 合并显示
//   },
// }

// /**
//  * 图表默认配置
//  * legend: 图例; position 位置, layout 展示布局
//  * labelShow: 显示数值
//  * compare: 同环比配置
//  * splited: 指标聚合(一图多Y轴 \ 一图一Y轴)
//  * axis: 坐标轴配置
//  */
// export const defaultChartOptions = {
//   legend: {
//     position: 'bottom',
//   },
//   labelShow: true,
//   compare: {
//     mode: 0, // 0 默认，1 差值， 2 差值百分比
//     merge: false, // false 单独显示， true 合并显示
//   },
//   splited: false,
//   axis: [],
// }
