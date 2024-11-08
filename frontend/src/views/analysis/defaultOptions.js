// 默认展示类型
export const defaultRenderType = 'table'

// 默认查询数量
export const defaultQueryTotal = 10000

/**
 * 同环比配置
 * mode: 显示模式, 0 默认，1 差值， 2 差值百分比
 * merge: 合并显示, false 单独显示， true 合并显示
 */
export const defaultCompareOptions = {
  mode: 0,
  merge: false
}

/**
 * 表格默认配置
 * layout: 表格布局方式; auto 自动列宽，fixed 等列宽,
 * fixed: 固定列配置; [firstRow] 首行固定, [column] 列固定, 0 无冻结, 1 左冻结, 2 自定义冻结, [colSpan] 列合定数量
 * align: 文字对齐方式; left 左对齐, center 居中, right 右对齐
 */
export const defaultTableOptions = {
  layout: 'auto',
  bordered: true,
  showSummary: false,
  pager: {
    show: true,
    pageSize: 20
  },
  fixed: {
    firstRow: true,
    columnMode: 0,
    columnSpan: [0, 0]
  },
  summary: false, // 汇总默认不显示
  align: 'center',
  sorter: [],
  formatter: [{ field: 'project_id', code: 5 }],
  special: {
    dimension: undefined, // 默认原始值
    measure: 0 // 默认0
  }
}

/**
 * 图表默认配置
 * legend: 图例; position 位置, layout 展示布局
 * labelShow: 显示数值
 * splited: 指标聚合(一图多Y轴 \ 一图一Y轴)
 * axis: 坐标轴配置
 */
export const defaultChartOptions = {
  legend: {
    position: 'top'
  },
  labelShow: false,
  splited: false,
  axis: [],
  xAxis: {
    show: true, // 轴显示
    nameShow: true, // 轴名称显示
    labelCount: 'auto' // 轴标签数量 auto 自动, thin 稀疏, max 最多展示
  },
  yAxis: {
    show: true,
    nameShow: true, // 轴名称显示
    // 轴值范围
    range: {
      max: undefined, // auto|undefined 自动
      min: undefined
    },
    // 轴值间隔
    interval: 'auto', // 轴值间隔 auto 自动, count 数量, step 步长
    intervalCount: undefined, // 轴值间隔数量, 默认5,
    intervalStep: undefined, // 轴值间隔步长,
    // 多Y轴配置
    multipleY: undefined, // 多Y轴 true | false
    axis: undefined
  },
  // 基础样式
  style: {
    bar: {
      type: 'clustered', // 柱图类型  cluster 簇形， stacked 堆叠,  percentStacked 百分比堆叠
      flat: false //指标并列展示
    }
  },
  lineEmptyWith: 'break' // connect 空值连接, 0 置为0， break 断开
}

export const defaultIndexCardOptions = {
  labelShow: true
}
