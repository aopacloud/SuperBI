import { VS_FIELD_SUFFIX } from '@/components/Chart/utils'

// 字段与聚合方式连接符(后端返回的拼接规则)
export const AGGREGATOR_SPLIT = '@'

//
export const VS_SPLIT = '#'

// 获取拼接了聚合方式的字段名
export const getNameByJoinAggregator = ({ name, aggregator }) => {
  return name + '.' + aggregator
}

/**
 * 去重指标(名称相同，聚合方式相同)
 * @param {<T>} list
 * @param {Function} cb 相同后的处理
 * @returns <T>
 */
export const repeatIndex = (list = [], cb) => {
  return list.reduce((acc, cur) => {
    const item = acc.find(t => t.name === cur.name)

    if (!item) {
      acc.push(cur)
    } else {
      if (item.aggregator !== cur.aggregator) {
        acc.push(cur)
      } else {
        cb(cur)
      }
    }

    return acc
  }, [])
}

// 处理渲染列
export const transformColumns = ({ fields = [], fieldNames = [] } = {}) => {
  return fieldNames.map(fieldName => {
    let originName = '' // 原始字段名，用于查找选中的字段
    let aggregator = '' // 指标汇总方式
    let vs = '' // vs
    let fullName = '' // 拼接指标汇总方式 和 vs 的全部名

    if (fieldName.endsWith === AGGREGATOR_SPLIT) {
      // 维度字段
      fullName = originName = fieldName.slice(0, -1)
    } else {
      const [pre, ...res] = fieldName.split(VS_SPLIT)
      if (res.length) {
        fieldName = pre
        vs = VS_FIELD_SUFFIX + res.join('.')
      }

      const [oName, oAggregator] = fieldName.split(AGGREGATOR_SPLIT)
      originName = oName
      aggregator = oAggregator
    }

    fullName = originName + (aggregator ? '.' + aggregator : '') + vs

    const field = fields.find(
      t => t.name === originName && (aggregator ? t.aggregator === aggregator : true)
    )

    return {
      ...field,
      renderName: fullName,
    }
  })
}

/**
 * 渲染表格
 * @param {string} type 渲染类型
 * @returns {boolean}
 */
export const isRenderTable = type => {
  return ['table', 'groupTable', 'intersectionTable'].includes(type)
}
