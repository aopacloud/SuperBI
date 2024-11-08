/*
 * @Author: huanghe Huang.He@olaola.chat
 * @Date: 2024-03-28 11:46:32
 * @LastEditors: huanghe Huang.He@olaola.chat
 * @LastEditTime: 2024-04-08 10:45:25
 * @FilePath: /dm-BDP-front-feat/src/views/dataset/utils.js
 */

import { CATEGORY } from '@/CONST.dict'
import {
  dataTypeOptions,
  summaryOptions,
  propertySummaryOptions,
  propertyNumberSummaryOptions,
  propertyTextSummaryOptions
} from './config.field'
import { versionJs } from '@/versions'

// 显示自定义格式文本
export const displayCustomFormatterLabel = e => {
  if (e === undefined) return '自定义'

  const _e = typeof e === 'object' ? e : JSON.parse(e)
  const { type, digit } = _e
  const typeStr = type === 0 ? '数字' : '百分比'

  return `${typeStr}保留${digit}位小数`
}

/**
 * 获取字段的格式icon
 * @param {string} type
 * @returns
 */
export const getIconByFieldType = type => {
  const tp = Array.isArray(type) ? type[0] : type
  const item = dataTypeOptions.find(t => t.value === tp)

  if (!item) {
    if (tp && tp.startsWith('TIME')) {
      const res = dataTypeOptions.find(t => t.value === 'TIME')

      return {
        icon: res.icon,
        color: res.color
      }
    } else {
      return {}
    }
  } else {
    return {
      icon: item.icon,
      color: item.color
    }
  }
}

export const isDateField = t => versionJs.ViewsAnalysis.isDateField(t)

export const isDtField = t => versionJs.ViewsDatasetModify.isDt(t)

export const getAggregatorLabel = field => {
  const { category, dataType, aggregator } = field
  let allSummary = []
  if (category === CATEGORY.INDEX) {
    allSummary = summaryOptions
  } else {
    const isTextOrTime = dataType === 'TEXT' || dataType.includes('TIME')
    const isNumber = dataType === 'NUMBER'
    const summary = isNumber
      ? propertyNumberSummaryOptions
      : isTextOrTime
        ? propertyTextSummaryOptions
        : []

    allSummary = summary.concat(propertySummaryOptions)
  }

  const item = allSummary.find(t => t.value === aggregator)
  return item?.label ?? aggregator
}

export const getFieldDataTypeOptions = field => {
  return dataTypeOptions.map(t => {
    return {
      ...t,
      disabled: versionJs.ViewsDatasetModify.isDt(field) && t.value !== 'TIME'
    }
  })
}
