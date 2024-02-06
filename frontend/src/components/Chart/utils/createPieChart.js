import { CATEGORY } from '@/CONST.dict.js'
import {
  gridCommon,
  legendCommon,
  legendPositionMap,
  colors,
} from 'common/components/Charts/utils/default.js'

/**
 * 创建图表配置
 * @param  {{ instance: Chart, originFields: array<any>, originData: array<any>, config: object, datasetFields: array<any> }}
 * @param  {Chart} params.Chart               图表实例
 * @param  {array<any>} params.originFields   原始字段
 * @param  {array<any>} params.originData     原始数据
 * @param  {object} params.config             图表配置
 * @param  {array<any>} params.datasetFields  图表配置
 * @return {[type]}                           渲染图表配置项
 */
export default function createPieChart({
  originFields = [],
  originData = [],
  config = {},
  datasetFields = [],
}) {
  const fields = originFields.map((item, index) => {
    item.indexKey = index + ''

    return item
  })
  if (Array.isArray(originData) && originData.length) {
    let legends = []
    const propertyKeys = fields
      .filter(item => item.category === CATEGORY.PROPERTY)
      .map(item => item.indexKey)
    const indexKeys = fields
      .filter(item => item.category === CATEGORY.INDEX)
      .map(item => item.indexKey)

    let seriesData = originData.map(v => {
      const dName =
        propertyKeys.reduce((str, curItem, curIndex) => {
          return str + v[curItem] + (curIndex < propertyKeys.length - 1 ? ' ' : '')
        }, '') +
        ' ' +
        (fields[indexKeys[0]]?.displayName || '')
      legends.push(dName)
      return {
        name: dName,
        value: v[indexKeys[0]],
      }
    })
    if (seriesData.length > 20) {
      const arr = seriesData.slice(0, 19)
      const otherObj = seriesData
        .sort((a, b) => {
          return b.value - a.value
        })
        .slice(19)
        .reduce(
          (initObj, curItem) => {
            return { name: '其他', value: initObj.value + Number(curItem.value) }
          },
          { name: '其他', value: 0 }
        )
      seriesData = [...arr, otherObj]
      legends = [...legends.slice(0, 19), '其他']
    }

    const { pieStyle, legend } = config

    return {
      tooltip: {
        trigger: 'item',
        appendToBody: true,
      },
      color: colors,
      grid: gridCommon,

      legend: {
        ...legendCommon,
        ...legendPositionMap[legend.position],
        textStyle: {
          color: '#556677',
          align: 'left',
          verticalAlign: 'middle',
          rich: {
            name: {
              color: 'rgba(255,255,255,0.5)',
              fontSize: 10,
            },
            value: {
              color: 'rgba(255,255,255,0.5)',
              fontSize: 10,
            },
            rate: {
              color: 'rgba(255,255,255,0.9)',
              fontSize: 10,
            },
          },
        },
        data: legends,
      },
      series: [
        {
          name: '',
          type: 'pie',
          radius: pieStyle === 'ring' ? ['40%', '70%'] : [0, '70%'],
          roseType: pieStyle === 'rose' ? 'radius' : false,
          itemStyle:
            pieStyle === 'rose'
              ? {
                  borderRadius: 8,
                }
              : undefined,
          label: {
            color: 'inherit',
            formatter: '{b}：{d}%',
          },
          data: seriesData,
        },
      ],
    }
  } else {
    return null
  }
}
