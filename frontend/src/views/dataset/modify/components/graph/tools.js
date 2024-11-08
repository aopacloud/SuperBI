import { Graph, Shape } from '@antv/x6'
import { register, getTeleport } from '@antv/x6-vue-shape'
import { Button } from '@antv/x6/es/registry/tool/button'
import TableNode from './cell/TableNode.vue'
import { customNodeWidth, customNodeHeight, portsOption } from './config'
import { joinOptions } from '../../config'

// 注册节点
export const registerTableNode = () => {
  register({
    shape: 'shape-node',
    width: customNodeWidth,
    height: customNodeHeight,
    ports: { ...portsOption },
    attrs: {
      body: {
        style: {
          backgroundColor: '#fff',
        },
      },
    },
    component: TableNode,
  })
}

export const registerPlaceholderNode = () => {
  Shape.HTML.register({
    shape: 'shape-placeholder',
    width: customNodeWidth,
    height: customNodeHeight,

    html() {
      const div = document.createElement('div')
      div.style =
        'border:1px dashed #1677ff;height: 100%;border-radius: 4px;display: flex;align-items: center;justify-content: center;font-size: 12px;color:#606266'
      div.innerHTML = '拖入此处进行关联'
      return div
    },
  })
}

export const getJoinBtnOption = type => {
  const item = joinOptions.find(item => item.value === type)

  return {
    distance: -60,
    offset: { y: -12 },
    markup: [
      {
        tagName: 'rect',
        selector: 'button',
        attrs: {
          width: 40,
          height: 20,
          rx: 4,
          fill: 'white',
          stroke: '#fe854f',
          strokeWidth: 1,
          cursor: 'pointer',
        },
      },
      {
        tagName: 'text',
        selector: 'text',
        textContent: item?.label ?? type,
        attrs: {
          fill: '#fe854f',
          fontSize: 10,
          textAnchor: 'middle',
          pointerEvents: 'none',
          x: 20,
          y: 13,
        },
      },
    ],
  }
}

export const registerJoinBtn = () => {
  const joinBtn = joinOptions.map(({ value, label }) => {
    return {
      name: value,
      register() {
        return Button.define({
          ...getJoinBtnOption(value),
        })
      },
    }
  })

  joinBtn.forEach(({ name, register }) => {
    Graph.registerEdgeTool(name, register(), true)
    Graph.registerNodeTool(name, register(), true)
  })
}
