export const graphOption = {
  autoResize: true, // 自动调整大小
  // background: {
  //   // 背景
  //   color: '#F2F7FA', // 背景颜色
  // },
  panning: true, // 拖拽
  // mousewheel: true, // 滚轮缩放
  connecting: {
    allowBlank: false, // 不允许空连接
    allowLoop: false, // 不允许循环连接
    allowNode: false, // 不允许节点连接
    allowEdge: false, // 不允许边连接
    allowPort: false, // 不允许端口连接
    allowMulti: false // 不允许多连接
  },
  interacting: {
    nodeMovable: false, // 节点不可被移动
    magnetConnectable: false, // 连接桩是否可以添加连线
    edgeMovable: false // 边不可被移动
  },
  scaling: {
    max: 1.6,
    min: 0.6
  },
  grid: {
    // 网格
    visible: true, // 网格是否可见
    type: 'doubleMesh', // 网格类型
    args: [
      //
      {
        color: '#eee', // 主网格线颜色
        thickness: 1 // 主网格线宽度
      },
      {
        color: '#ddd', // 次网格线颜色
        thickness: 1, // 次网格线宽度
        factor: 4 // 网格线间隔
      }
    ]
  },
  grid: true
}

export const customNodeWidth = 360
export const customNodeHeight = 30

// 横向间隔
export const xGap = 150
// 纵向间隔
export const yGap = 40
// 检测范围半径
export const radius = 250

export const portAttrs = {
  circle: {
    magnet: true,
    stroke: '#8f8f8f',
    r: 0
    // fill: 'transparent',
    // stroke: 'transparent',
  }
}

export const portsOption = {
  groups: {
    right: {
      position: [customNodeWidth, customNodeHeight / 2],
      attrs: { ...portAttrs }
    },
    top: {
      position: [customNodeWidth / 2, 0],
      attrs: { ...portAttrs }
    },
    bottom: {
      position: [customNodeWidth / 2, customNodeHeight],
      attrs: { ...portAttrs }
    },
    left: {
      position: [0, customNodeHeight / 2],
      attrs: { ...portAttrs }
    }
  }
}
