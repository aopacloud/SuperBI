// 默认展示类型
export const defaultRenderType = 'table'

/**
 * 同环比配置
 * mode: 显示模式, 0 默认，1 差值， 2 差值百分比
 * merge: 合并显示, false 单独显示， true 合并显示
 */
export const defaultCompareOptions = {
  mode: 0,
  merge: false,
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
  pager: {
    show: true,
    pageSize: 20,
  },
  fixed: {
    firstRow: true,
    columnMode: 0,
    columnSpan: [0, 0],
  },
  align: 'center',
  sorter: {},
  formatter: [{ field: 'project_id', code: 5 }],
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
    position: 'top',
  },
  labelShow: true,
  splited: false,
  axis: [],
}
