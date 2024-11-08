<template>
  <div
    class="graph-container"
    id="container"
    style="width: 100%; height: 100%"
  ></div>
</template>

<script setup lang="jsx">
import { onMounted, ref, inject, createApp } from 'vue'
import { Graph, Markup } from '@antv/x6'
import { Dnd } from '@antv/x6-plugin-dnd'
import { Snapline } from '@antv/x6-plugin-snapline'
import Tools from './components/Tools.vue'
import {
  registerTableNode,
  registerPlaceholderNode,
  getJoinBtnOption
} from './tools'
import { graphOption, xGap, yGap, radius } from './config'
import emittor from '@/common/plugins/emittor'
import JoinIcon from './components/JoinIcon.vue'
import VOutclick from 'common/directives/outclick'

// 注册表节点
registerTableNode()
// 注册占位符节点
registerPlaceholderNode()

const { graph: graphCore } = inject('index')

const emits = defineEmits(['joinBtnClick'])

// 获取基础信息
const getRect = {
  // 获取x
  x: n => n.position().x,
  // 获取y
  y: n => n.position().y,
  // 获取宽度
  width: n => n.size().width,
  // 获取高度
  height: n => n.size().height
}

/**
 * 创建占位符节点
 * @param id 占位符的id
 * @param node 原始节点
 * @returns 无返回值
 */
const insertPlaceholder = node => {
  const { x, y } = node.position()
  const { width, height } = node.size()
  const idPrefix = node.id + '-placeholder-'
  let ports = node.port.ports || []

  const getX = n => n.position().x // 获取x
  const getY = n => n.position().y // 获取y
  const getW = n => n.size().width // 获取宽度
  const getH = n => n.size().height // 获取高度

  const isExisted = (l = [], n) => {
    let hasTop, hasBottom, hasLeft, hasRight
    for (let i = 0; i < l.length; i++) {
      const t = l[i]
      const tX = getX(t),
        tY = getY(t),
        tW = getW(t),
        tH = getH(t)
      const nX = getX(n),
        nY = getY(n),
        nW = getW(n),
        nH = getH(n)

      hasTop = hasTop || (tX === nX && tY < nY && tY + tH + yGap === nY)
      hasBottom = hasBottom || (tX === nX && tY > nY && nY + nH + yGap === tY)
      hasLeft = hasLeft || (tY === nY && tX < nX && tX + tW + xGap === nX)
      hasRight = hasRight || (tY === nY && tX > nX && nX + nW + xGap === tX)
    }

    return { hasTop, hasBottom, hasLeft, hasRight }
  }

  const nodes = graph.value.getNodes().filter(n => n.id !== node.id)
  if (nodes.length) {
    const { hasTop, hasBottom, hasLeft, hasRight } = isExisted(nodes, node)

    if (hasBottom) ports = ports.filter(p => p.group !== 'bottom')
    if (hasRight) ports = ports.filter(p => p.group !== 'right')
    if (hasTop) ports = ports.filter(p => p.group !== 'top')
    if (hasLeft) ports = ports.filter(p => p.group !== 'left')
  }

  const createNode = (group, id) => {
    let pos = {}
    switch (group) {
      case 'right':
        pos = { x: x + width + xGap, y }
        break
      case 'bottom':
        pos = { x, y: y + height + yGap }
        break
      case 'left':
        pos = { x: x - xGap - width, y }
        break
      case 'top':
        pos = { x, y: y - yGap - height }
        break
      default:
        break
    }

    return {
      shape: 'shape-placeholder',
      id: id + group,
      ...pos
    }
  }

  ports.forEach(port => {
    const pN = createNode(port.group, idPrefix)

    graph.value.addNode(pN, { ref: node })
  })
}

const setAllPlaceholderStyle = () => {
  const nodes = graph.value.getNodes()
  const pNodes = nodes.filter(t => t.shape === 'shape-placeholder')
  pNodes.forEach(node => {
    setPlaceholderNodeActive(node, false)
  })
}

// 移除指定占位节点
const removePlaceholderNodes = (nodes = []) => {
  nodes.forEach(node => {
    graph.value.removeNode(node.id)
  })
}

// 清除所有占位节点
const clearPlaceholder = () => {
  const pNodes = graph.value
    .getNodes()
    .filter(t => t.shape === 'shape-placeholder')

  removePlaceholderNodes(pNodes)
}

/**
 * 计算鼠标坐标与节点中心点的距离
 * @param pointer 指针对象，包含x和y属性
 * @param node 节点对象，包含position和size方法
 * @returns 返回计算得到的距离（向上取整后的整数值）
 */
const getDistance = (pointer, node) => {
  const { x, y } = pointer
  const { width: sW, height: sH } = startNode.value.size()
  const pX = x - sW / 2,
    pY = y - sH / 2

  const { x: nX, y: nY } = node.position()
  const { width, height } = node.size()
  // 中心点坐标
  const originX = nX + width / 2,
    originY = nY + height / 2

  const dX = Math.abs(pX - originX)
  const dY = Math.abs(pY - originY)

  return Math.ceil(Math.sqrt(dX ** 2 + dY ** 2))
}

/**
 * 判断给定的坐标点是否在指定节点内部（移动的元素与目标节点的是否碰撞）
 * @param pointer 坐标点对象，包含 x 和 y 属性
 * @param node 节点对象，包含 position() 和 size() 方法
 * @returns 若坐标点在节点内部，则返回 true；否则返回 false
 */
const isInside = (pointer, node) => {
  const { x, y } = pointer
  const { x: nX, y: nY } = node.position()
  const { width, height } = node.size()

  // return x + width/2 > nX && x-width/2 < nX + width && y+height/2 > nY && y-height/2 < nY + height

  const { width: mWidth, height: mHeight } = startNode.value.size()

  return (
    x + mWidth / 2 > nX &&
    x - mWidth / 2 < nX + width &&
    y + mHeight / 2 > nY &&
    y - mHeight / 2 < nY + height
  )
}

const isInRange = ref(false) // 鼠标是否在检测范围内
const nearbyNode = ref(null) // 检测范围内节点
const isIn = ref(false) // 鼠标是否在节点内

const startNode = ref(null) // 开始插入的节点

const checkWhenMoving = e => {
  const g = graph.value
  // 鼠标页面位置转画布坐标系位置
  const { x, y } = g.pageToLocal(e.pageX, e.pageY)

  // 占位节点未激活时才进行检测
  if (!activePlaceholderNode.value) {
    const mainNodes = g
      .getNodes()
      .filter(t => t.shape === 'shape-node' && t.id !== startNode.value.id)

    const dis = mainNodes.map(node => getDistance({ x, y }, node))
    const minDis = Math.min(...dis)

    isInRange.value = minDis <= radius

    if (!isInRange.value) {
      nearbyNode.value = null
      clearPlaceholder()
    } else {
      const nearestNode = mainNodes[dis.indexOf(Math.min(...dis))]
      // 引用节点与最近节点不相同，清空占位节点
      if (nearbyNode.value && nearbyNode.value !== nearestNode)
        clearPlaceholder()

      nearbyNode.value = nearestNode
      insertPlaceholder(nearestNode)
    }
  }

  checkPlaceholderWhenMoving(x, y)
}

const graph = ref(null) // 画布
const dnd = ref(null) // dnd插件

const moveMode = ref('') // 插入模式 INSERT, MOVE

const init = () => {
  const g = new Graph({
    ...graphOption,
    container: document.querySelector('#container'), // 容器
    // 自定义标签
    onEdgeLabelRendered: args => {
      const { selectors, edge, label } = args
      const content = selectors.foContent
      if (content) {
        createApp(JoinIcon, {
          edge,
          joinType: label.text ?? 'LEFT',
          size: 30,
          onClick: edge => {
            const source = edge.getSourceNode()
            const target = edge.getTargetNode()
            emits('joinBtnClick', source, target)
          }
        }).mount(content)
      }
    }
  })

  g.use(new Snapline())
  g.zoomTo(1)

  dnd.value = new Dnd({
    getDragNode: node => node.clone({ keepId: true }),
    getDropNode: node => node.clone({ keepId: true }),
    validateNode: () => {
      const isEmpty = graph.value.getNodes().length === 0

      if (!isEmpty && !activePlaceholderNode.value) {
        return false
      }

      return true
    },
    target: g
  })
  g.__initNode__ = initNode
  g.__insertNode__ = insertNode
  // g.__updateEdgeTool__ = updateEdgeTool
  g.__updateEdgeLabel__ = updateEdgeLabel
  g.__removeCell__ = removeCell

  graph.value = g
  graphCore.set(g)
  // g.zoomToFit()
  bindGraphEvents()
}

/**
 * 绑定画布事件
 */
const bindGraphEvents = () => {
  if (!graph.value) return

  // .on('render:done', a => {
  //     // centerContent()
  //     const nodes = graph.value.getNodes()
  //     const allX = nodes.map(t => t.position().x)
  //     const minX = Math.min(...allX)
  //     const maxX = Math.max(...allX)
  //     const allY = nodes.map(t => t.position().y)
  //     const minY = Math.min(...allY)
  //     const maxY = Math.max(...allY)

  //     const centerX = (maxX - minX) / 2
  //     const centerY = (maxY - minY) / 2

  //     graph.value.centerPoint(centerX, centerY)
  //   })

  graph.value
    .on('node:added', ({ node }) => {
      // 节点添加时，如果移动模式为替换，则替换
      if (moveMode.value === 'MOVE') return
      // 添加占位节点不处理
      if (node.shape === 'shape-placeholder') return

      afterAdd(node)
    })
    .on('node:mousedown', ({ e, node }) => {
      // 根节点不允许移动
      if (node.id === 't1') return

      // 锁定画布拖动平移
      graph.value.disablePanning()
      moveMode.value = 'MOVE'

      const ports = [
        { id: `${node.id}-port-top`, group: 'top' },
        { id: `${node.id}-port-left`, group: 'left' }
      ]
      node.addPorts(ports)

      const cNode = node.clone({ keepId: true })
      startNode.value = cNode
      dnd.value.start(cNode, e)
    })
    .on('node:mousemove', ({ e, node }) => {
      if (moveMode.value !== 'MOVE') return

      checkWhenMoving(e, node)
    })
    .on('node:mouseup', ({ node }) => {
      // 替换且满足条件
      if (moveMode.value === 'MOVE' && isIn.value) onMoved(node)

      onMoveEnd()

      const ports = [
        { id: `${node.id}-port-top`, group: 'top' },
        { id: `${node.id}-port-left`, group: 'left' }
      ]
      node.removePorts(ports)
      // 释放画布拖定平移
      graph.value.enablePanning()
      moveMode.value = ''
    })
}

const getVertices = (s, t) => {
  if (!activePlaceholderNode.value) return

  const { x, y } = s.position()
  const { width, height } = s.size()
  const { y: tY } = t.position()
  const { height: tH } = t.size()
  // const { x: pX, y: pY } = activePlaceholderNode.value.position()

  if (y === tY) return []

  return [
    { x: x + width + xGap / 2, y: y + height / 2 },
    { x: x + width + xGap / 2, y: tY + tH / 2 }
  ]
}

const afterAdd = node => {
  // 生成的第一个节点
  if (graph.value.getNodes().length === 1) {
    node.addPorts([{ id: `${node.id}-port-right`, group: 'right' }])
    return
  }

  // 占位加点未激活时不处理
  if (!activePlaceholderNode.value) return

  let sourceNode = nearbyNode.value //graph.value.getCellById(nearbyNode.value.id)
  const type = activePlaceholderNode.value.id.split('-placeholder-')[1]
  if (type === 'bottom') {
    const parent = graph.value.getPredecessors(sourceNode)

    if (parent.length) sourceNode = parent[0]
  }

  // 添加连接桩(插入占位节点中有使用到)
  node.addPorts([
    { id: `${node.id}-port-right`, group: 'right' },
    { id: `${node.id}-port-bottom`, group: 'bottom' }
  ])

  const { x, y } = activePlaceholderNode.value.position()
  node.position(x, y)
  addEdge(sourceNode, node)
  insertCallback.value &&
    insertCallback.value(placeholderParentNode.value?.getData(), node.getData())
}

const addEdgeTool = (toolName = 'LEFT') => {
  return {
    // name: toolName,
    name: 'button',
    args: {
      ...getJoinBtnOption(toolName),
      onClick({ e, cell }) {
        // cell.removeTool('LEFT')
        // cell.addTools([addEdgeTool(type === 'left' ? 'right' : 'left')])
        const source = cell.getSourceNode()
        const target = cell.getTargetNode()
        emits('joinBtnClick', source, target)
      }
    }
  }
}

/**
 * 更新边工具
 * @param nodeId 节点ID
 * @param joinType 新的连接类型
 * @returns 无返回值
 */
const updateEdgeTool = (nodeId, joinType) => {
  const cell = graph.value.getCellById(nodeId)
  const sourceEdge = graph.value.getIncomingEdges(cell)[0]

  sourceEdge.removeTools()
  sourceEdge.addTools([addEdgeTool(joinType)])
}

/**
 * 更新边的标签
 * @param nodeId 节点ID
 * @param joinType 连接类型
 * @returns 无返回值
 */
const updateEdgeLabel = (nodeId, joinType) => {
  const payload = typeof joinType === 'string' ? { joinType } : joinType
  const cell = graph.value.getCellById(nodeId)
  const sourceEdge = graph.value.getIncomingEdges(cell)[0]

  sourceEdge.setData(payload)
  sourceEdge.setLabels([getEdgeLabelOption(payload.joinType)])
}

// 删除元素
const removeCell = (cellId, cb) => {
  const removed = graph.value.removeCell(cellId)
  cb && cb(removed.id)
}

/**
 * 获取边标签选项
 * @param joinType 连接类型，默认为 'LEFT'
 * @returns 返回连接标签选项对象
 */
const getEdgeLabelOption = (joinType = 'LEFT') => {
  return {
    text: joinType,
    markup: Markup.getForeignObjectMarkup(),
    attrs: {
      fo: {
        width: 30,
        height: 30,
        refY: -15
      }
    },
    position: {
      distance: -55
    }
  }
}

/**
 * 添加边
 * @param source 源节点
 * @param target 目标节点
 * @returns 目标节点
 */
const addEdge = (source, target) => {
  // 源节点连接桩
  const sourcePort = source.port.ports.find(t => t.group === 'right')
  // 路径线
  const vertices = getVertices(source, target)

  const id = target.id.replace('-cloned', '')
  graph.value.addEdge({
    shape: 'edge',
    id: `${target.id}-edge`,
    source: {
      cell: source.id,
      port: sourcePort.id
    },

    target: id,
    vertices,
    // tools: [addEdgeTool()],
    label: getEdgeLabelOption()
  })

  return target
}

const onMoved = node => {
  const { x, y } = activePlaceholderNode.value.position()

  // 移除节点的输出边
  const outgoingEdges = graph.value.getOutgoingEdges(node)
  if (outgoingEdges?.length) {
    outgoingEdges.forEach(edge => {
      graph.value.removeEdge(edge.id)
    })
  }

  node.position(x, y)

  const incomingEdges = graph.value.getIncomingEdges(node)
  if (incomingEdges?.length) {
    incomingEdges.forEach(edge => {
      const source = edge.getSourceNode()

      // 错乱位置的节点，清除连线
      if (placeholderParentNode.value.id === node.id) {
        graph.value.removeEdge(edge.id)

        graphCore.onNodeMoved({
          oldSource: source.data,
          newSource: placeholderParentNode.value.data,
          target: node.data
        })

        return
      }

      // 边的源节点发生改变
      if (source.id !== placeholderParentNode.value?.id) {
        // 更新边的源节点
        const sourceNode = placeholderParentNode.value.clone({ keepId: true })
        edge.setSource(sourceNode)
      }

      graphCore.onNodeMoved({
        oldSource: source.data,
        newSource: placeholderParentNode.value.data,
        target: node.data
      })
      // 更新边的路径
      updateEdgeVertices(edge)
    })
  } else {
    // 无边的源节点，添加边
    addEdge(placeholderParentNode.value, node)

    graphCore.onNodeMoved({
      newSource: placeholderParentNode.value.data,
      target: node.data
    })
  }
}

const onRemove = node => {
  graph.value.removeNode(node.id)
  graphCore.onNodeRemoved(node.data) // 从外层移除
}

// 更新边的路径
const updateEdgeVertices = edge => {
  const edges = edge ? [edge] : graph.value.getEdges()
  edges.forEach(edge => {
    const source = edge.getSourceNode()
    const target = edge.getTargetNode()
    const vertices = getVertices(source, target)
    edge.setVertices(vertices)
  })
}

onMounted(() => {
  init()

  emittor.on('on-table-node-remove', e => {
    onRemove(e)
  })
})

// 激活的占位节点
const activePlaceholderNode = ref(null)
// 激活的占位节点的父节点
const placeholderParentNode = ref(null)
const setPlaceholderParentNodeActive = (active = false, pNode) => {
  const initColor = '#fff'
  // 无父节点，清空上一次激活的占位节点父节点的样式
  if (!pNode) {
    placeholderParentNode.value?.attr('body', {
      style: { backgroundColor: initColor }
    })
    return
  }

  const activeColor = '#f3b663'
  const idDirection = pNode.id.split('-placeholder-')[1]

  if (idDirection === 'bottom') {
    const parents = graph.value.getPredecessors(nearbyNode.value)
    if (!parents || !parents.length) return
    placeholderParentNode.value = parents[0]
  } else {
    placeholderParentNode.value = nearbyNode.value
  }

  placeholderParentNode.value?.attr('body', {
    style: { backgroundColor: active ? activeColor : initColor }
  })
}
const setPlaceholderNodeActive = (node, active = true) => {
  const activeColor = '#afcff5', //'#c9e1ff',
    initColor = '#e6eef8' //

  node.attr('body/fill', active ? activeColor : initColor)
}
const checkPlaceholderWhenMoving = (x, y) => {
  activePlaceholderNode.value = null

  const pNodes = graph.value
    .getNodes()
    .filter(n => n.shape === 'shape-placeholder')
  if (!pNodes.length) return

  for (let i = 0; i < pNodes.length; i++) {
    const node = pNodes[i]

    isIn.value = isInside({ x, y }, node)

    if (isIn.value) {
      activePlaceholderNode.value = node
      setPlaceholderNodeActive(node)
      setPlaceholderParentNodeActive(true, node)
      break
    } else {
      setPlaceholderNodeActive(node, false)
      setPlaceholderParentNodeActive(false, node)
    }
  }
}

// 绑定鼠标事件
const bindMouseEvents = () => {
  document.addEventListener('mousemove', onMoveStart)
  document.addEventListener('mouseup', onMoveEnd)
}

// 解绑鼠标事件
const unbindMouseEvents = () => {
  document.removeEventListener('mousemove', onMoveStart)
  document.removeEventListener('mouseup', onMoveEnd)
}

const onMoveStart = e => {
  // 没有开始节点不处理
  if (!startNode.value) return

  checkWhenMoving(e)
}
const onMoveEnd = () => {
  clearPlaceholder()

  setPlaceholderParentNodeActive()
  startNode.value = null
  nearbyNode.value = null
  activePlaceholderNode.value = null
  isIn.value = false
  moveMode.value = ''
  unbindMouseEvents()
}

// 插入成功的回调
const insertCallback = ref()
const insertNode = (event, payload, cb) => {
  insertCallback.value = cb
  const nodeId = payload.alias

  const sNode = graph.value.createNode({
    shape: 'shape-node',
    id: nodeId,
    label: nodeId,
    data: { ...payload }
  })
  moveMode.value = 'INSERT'
  dnd.value.start(sNode, event)
  startNode.value = sNode

  bindMouseEvents()
}

const initNode = p => {
  const node = graph.value.addNode({
    id: p.alias,
    shape: 'shape-node',
    data: { ...p }
  })

  return Promise.resolve({ graph: graph.value, node })
}

defineExpose({
  initNode
})
</script>

<style lang="scss" scoped>
.t {
  padding: 2px 6px;
  margin-bottom: 6px;
  cursor: move;
  background-color: #afcff5;
}

.graph-tools {
  position: absolute;
  top: 6px;
  right: 6px;
  padding: 2px;
  background-color: #f5fbff;
}
</style>
