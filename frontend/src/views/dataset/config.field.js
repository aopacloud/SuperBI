/**
 * 字段配置
 */

import { CATEGORY } from '@/CONST.dict.js'
import * as numberUtils from 'common/utils/number'

// 格式化前缀
export const FORMAT_PREFFIX = 'format'
// 数字格式化默认
export const FORMAT_DEFAULT_CODE = 'ORIGINAL'
// 自定义格式
export const FORMAT_CUSTOM_CODE = 'CUSTOM'

// 类型
export const categoryOptions = [
  {
    label: '维度',
    value: CATEGORY.PROPERTY,
  },
  { label: '指标', value: CATEGORY.INDEX, icon: '' },
]

// 类型Map
export const categoryMap = {
  [CATEGORY.PROPERTY]: {
    name: '维度',
    color: '#1677ff',
    icon: '',
  },
  [CATEGORY.PROPERTY_COLUMN]: {
    name: '维度',
    color: '#1677ff',
    icon: '',
  },
  [CATEGORY.INDEX]: {
    name: '指标',
    color: '#24ba88',
    icon: '',
  },
  [CATEGORY.FILTER]: {
    name: '筛选',
    color: '#e6a23c',
    icon: '',
  },
}

// 维度默认聚合
export const SUMMARY_PROPERTY_DEFAULT = 'COUNT'
// 指标默认聚合
export const SUMMARY_INDEX_DEFAULT = 'SUM'
// 默认汇总方式
export const SUMMARY_DEFAULT = 'EMPTY'

// 分位数的前缀
export const QUANTILE_PREFIX = 'QUANTILE_'

export const summaryQuantile = { label: '分位数', value: QUANTILE_PREFIX }
// 分位数
export const quantileOptions = [
  { label: '99分位数', value: QUANTILE_PREFIX + '99' },
  { label: '95分位数', value: QUANTILE_PREFIX + '95' },
  { label: '90分位数', value: QUANTILE_PREFIX + '90' },
  { label: '85分位数', value: QUANTILE_PREFIX + '85' },
  { label: '75分位数', value: QUANTILE_PREFIX + '75' },
  { label: '50分位数', value: QUANTILE_PREFIX + '50' },
  { label: '25分位数', value: QUANTILE_PREFIX + '25' },
  { label: '5分位数', value: QUANTILE_PREFIX + '5' },
  { label: '自定义', value: QUANTILE_PREFIX },
]

// 快速计算
export const quickCalculateOptions = [
  { label: '占比', value: 'ratio' },
  { label: '组内占比', value: 'ratio_group' },
  { label: '排名', value: 'rank' },
  { label: '组内排名', value: 'rank_group' },
  // { label: '累计值', value: 'total', tooltip: '只有时间类型的分组可被累计' },
  // {
  //   label: '组内累计值',
  //   value: 'total_group',
  //   tooltip: '只有时间类型的分组可被累计',
  // },
  { label: '无', value: undefined },
]

// 汇总方式
export const summaryOptions = [
  { label: '聚合', value: 'EMPTY', hidden: true },
  { label: '求和', value: 'SUM' }, // 1
  { label: '平均', value: 'AVG' }, // 2
  // { label: '中位数', value: 'MID' }, // 3
  { label: '最大值', value: 'MAX' }, // 4
  { label: '最小值', value: 'MIN' }, // 5
  { label: '计数', value: 'COUNT' }, // 6
  { label: '去重计数', value: 'COUNT_DISTINCT' }, // 7
]

// 维度汇总
export const propertySummaryOptions = [
  { label: '计数', value: 'COUNT' },
  { label: '去重计数', value: 'COUNT_DISTINCT' },
]
// 维度文字汇总
export const propertyTextSummaryOptions = [
  { label: '最大值', value: 'MAX' },
  { label: '最小值', value: 'MIN' },
]
// 维度数字汇总
export const propertyNumberSummaryOptions = [
  { label: '求和', value: 'SUM' },
  { label: '平均', value: 'AVG' },
  { label: '最大值', value: 'MAX' },
  { label: '最小值', value: 'MIN' },
]

// 排除
export const NOT_IN = 'NOT_IN'
// 选中
export const IN = 'IN'
// 无值
export const IS_NOT_NULL = 'IS_NOT_NULL'
// 有值
export const IS_NULL = 'IS_NULL'

// 条件过滤操作符
export const operatorMap = {
  TEXT: {
    EQUAL: '等于', // 1
    NOT_EQUAL: '不等于', // 2
    START_WITH: '开头是', // 3
    END_WITH: '结尾是', // 4
    CONTAIN: '包含', // 5
    NOT_CONTAIN: '不包含', // 6
    IS_NOT_NULL: '有值', // 13
    IS_NULL: '无值', // 14
  },
  NUMBER: {
    EQUAL: '=', // 1
    NOT_EQUAL: '≠', // 2
    GT: '>', // 7
    LT: '<', // 8
    GTE: '≥', // 9
    LTE: '≤', // 10
    IS_NOT_NULL: '有值', // 13
    IS_NULL: '无值', // 14
  },
  DEFAULT: {
    EQUAL: '等于', // 1
    NOT_EQUAL: '不等于', // 2
    START_WITH: '开头是', // 3
    END_WITH: '结尾是', // 4
    CONTAIN: '包含', // 5
    NOT_CONTAIN: '不包含', // 6
    GT: '>', // 7
    LT: '<', // 8
    GTE: '≥', // 9
    LTE: '≤', // 10
    IN: '选中', // 11
    NOT_IN: '排除', // 12
    IS_NOT_NULL: '有值', // 13
    IS_NULL: '无值', // 14
  },
}

// 日期类型时分秒
export const YYYYMMDD_HHMMSS = 'TIME_YYYYMMDD_HHMMSS'

export const isTime_HHMMSS = dateType => dateType === YYYYMMDD_HHMMSS

// 数据类型
export const dataTypeOptions = [
  { label: '文本', value: 'TEXT', icon: 'icon-text', color: '#84bedb' },
  { label: '数值', value: 'NUMBER', icon: 'icon-number', color: '#5a9f76' },
  {
    label: '时间',
    value: 'TIME',
    icon: 'icon-date',
    color: '#f2c96b',
    children: [
      { label: '默认', value: '' },
      { label: 'YY-MM-DD', value: 'YYYYMMDD' },
      {
        label: 'YY-MM-DD HH:MM:SS',
        value: YYYYMMDD_HHMMSS.split('_').slice(1).join('_'),
      },
    ],
  },
]

// 数字格式化配置
// ORIGINAL(0,"{}","default"),    // 原始
// INTEGER(1,"cast({} as bigint)","integer"),  // 整数
// DECIMAL_1(2,"cast({} as decimal(20,1))","retain 1 decimal place"), // 一位小数
// DECIMAL_2(3,"cast({} as decimal(20,2))","retain 2 decimal places"),  // 两位小数
// PERCENT(4,"{} * 100","percent"),   // 百分比
// PERCENT_DECIMAL_1(5,"cast({} *100 as decimal(20,1))","percent 1 decimal place"),  // 百分比一位小数
// PERCENT_DECIMAL_2(6,"cast({} *100 as decimal(20,2))","percent 2 decimal places"),  // 百分比两位小数
// CUSTOM(7,"","custom"); // 自定义

export const formatterOptions = [
  {
    label: '默认',
    value: FORMAT_DEFAULT_CODE,
    format: num => {
      return numberUtils.toThousand(num)
    },
  },
  {
    label: '整数',
    value: 'INTEGER',
    format: num => {
      return numberUtils.toDigit(num, 0)
    },
  },
  {
    label: '保留1位小数',
    value: 'DECIMAL_1',
    format: num => {
      return numberUtils.toDigit(num, 1)
    },
  },
  {
    label: '保留2位小数',
    value: 'DECIMAL_2',
    format: num => {
      return numberUtils.toDigit(num, 2)
    },
  },
  {
    label: '百分比',
    value: 'PERCENT',
    format: num => {
      return numberUtils.toPercent(num)
    },
  },
  {
    label: '百分比1位小数',
    value: 'PERCENT_DECIMAL_1',
    format: num => {
      return numberUtils.toPercent(num, 1)
    },
  },
  {
    label: '百分比2位小数',
    value: 'PERCENT_DECIMAL_2',
    format: num => {
      return numberUtils.toPercent(num, 2)
    },
  },
  {
    label: '自定义',
    value: FORMAT_CUSTOM_CODE,
    format: (num, config) => {
      if (!config) return num

      return formatterByCustom(num, config)
    },
  },
]

/** 数字格式化自定义
 * @param {number} 数字
 * @param {object} 自定义配置
 */
export const formatterByCustom = (num, config) => {
  config = typeof config === 'string' ? JSON.parse(config) : config

  // type: 数字、百分比, digit: 小数位, thousand: 千分位
  const { type, digit, thousand } = config

  if (type === 0) {
    return numberUtils.toDigit(num, digit, thousand)
  } else {
    return numberUtils.toPercent(num, digit, thousand)
  }
}
