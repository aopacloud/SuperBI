export const MIN_WIDTH = 600 // 初始宽度
export const MIN_HEIGHT = 400 // 初始高度

export const CHART_GRID_HEIGHT = 220

export const gridCommon = {
  left: 80,
  right: 80,
  bottom: 55,
  top: 55,
  containLabel: true,
}

export const legendCommon = {
  type: 'scroll',
  orient: 'horizontal', // horizontal, vertical
  animationDurationUpdate: 300,
  icon: 'circle',
  itemWidth: 10,
  itemGap: 20,
  textStyle: {
    color: '#556677',
  },
}

export const legendPositionMap = {
  topLeft: { top: 0, left: 0 },
  top: { top: 0 },
  topRight: { top: 0, right: 0 },
  bottomLeft: { bottom: 0, left: 0 },
  bottom: { bottom: 0 },
  bottomRight: { bottom: 0, right: 0 },
}

export const xAxisCommon = {
  type: 'category',
  nameLocation: 'middle',
  splitLine: {
    show: false,
  },
  axisTick: {
    show: false,
  },
  axisLine: {
    show: false,
  },
  axisLabel: {
    hideOverlap: true,
    showMinLabel: true,
    showMaxLabel: true,
  },
}

export const yAxisCommon = {
  scale: true,
  _barMaxWidth: 40,
  nameLocation: 'middle',
  // 刻度线对齐
  alignTicks: true,
  // 网格分割线
  splitLine: {
    lineStyle: {
      color: '#F3F4F4',
    },
  },
  // 轴刻度
  axisTick: {
    show: false,
  },
  // 轴线
  axisLine: {
    show: false,
  },
}

export const colors = [
  '#219AF2',
  '#34CDC5',
  '#A9DEF4',
  '#F6E1AA',
  '#9975CF',
  '#5AD8A6',
  '#F56580',
  '#F98349',
  '#FEC1DB',
  '#415982',
  '#137BD1',
  '#00B3A6',
  '#20B3E4',
  '#E8BC39',
  '#6D38B9',
  '#00C47E',
  '#EA2F4D',
  '#F98349',
  '#FF70AB',
  '#415982',
  '#004CA1',
  '#009486',
  '#0094DC',
  '#DFA100',
  '#572BAA',
  '#00A164',
  '#C52348',
  '#FFEEC0',
  '#FF3681',
  '#1D2E50',
  '#BCE0FA',
  '#006657',
  '#0075BC',
  '#E08300',
  '#381895',
  '#006F40',
  '#8E1041',
  '#FFDB74',
  '#D72F76',
  '#C3CBD9',
  '#66B9F5',
  '#B2E9E7',
  '#004688',
  '#DE5000',
  '#D3C4EA',
  '#BEEED8',
  '#FCBCC7',
  '#FFCA59',
  '#9A2668',
  '#798AA4',
]
