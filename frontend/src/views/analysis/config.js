import { CATEGORY } from '@/CONST.dict'
import { isDateField } from '@/views/dataset/utils'

// 字段选中区域label
export const sectionListLabelMap = {
  default: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
    [CATEGORY.FILTER]: '筛选条件',
  },
  table: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
  },
  bar: {
    [CATEGORY.PROPERTY]: '横轴/分组',
    [CATEGORY.INDEX]: '纵轴/值',
  },
  line: {
    [CATEGORY.PROPERTY]: '横轴/分组',
    [CATEGORY.INDEX]: '纵轴/值',
  },
  pie: {
    [CATEGORY.PROPERTY]: '扇形占比/指标',
    [CATEGORY.INDEX]: '扇形数量/分组',
  },
  statistic: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
  },
  groupTable: {
    [CATEGORY.PROPERTY]: '分组',
    [CATEGORY.INDEX]: '指标',
  },
  intersectionTable: {
    [CATEGORY.PROPERTY]: '行分组',
    [CATEGORY.PROPERTY + '_COLUMN']: '列分组',
    [CATEGORY.INDEX]: '指标',
  },
}

/**
 * 获取字段选中区域label
 * @param {string} renderType 渲染类型
 * @returns {(category: string) => string} category => {}
 */
export const getSectionListLabel = renderType => {
  const labelObj = sectionListLabelMap[renderType] || sectionListLabelMap['default']
  const filterObj = {
    [CATEGORY.FILTER]: sectionListLabelMap['default'][CATEGORY.FILTER],
  }

  return category => ({ ...labelObj, ...filterObj }[category])
}

export const GROUP_MONTH = 'MONTH' // 月分组

export const GROUP_WEEK = 'WEEK' // 周分组

export const GROUP_DAY = 'DAY' // 天分组

export const GROUP_HOUR = 'HOUR' // 小时分组

export const GROUP_MINUTE = 'MINUTE' // 分钟分组

export const DEFAULT_DATE_GROUP = 'ORIGIN' // 默认日期分组方式

// 日期维度聚合方式
export const dateGroupOptions = [
  {
    label: '原值',
    value: DEFAULT_DATE_GROUP,
  },
  {
    label: '年',
    value: 'YEAR',
  },
  {
    label: '季度',
    value: 'QUARTER',
  },
  {
    label: '月',
    value: 'MONTH',
  },
  {
    label: '周',
    value: GROUP_WEEK,
    width: 130,
    children: [
      {
        label: '周一 至 周日',
        value: 1,
      },
      {
        label: '周二 至 周一',
        value: 2,
      },
      {
        label: '周三 至 周二',
        value: 3,
      },
      {
        label: '周四 至 周三',
        value: 4,
      },
      {
        label: '周五 至 周四',
        value: 5,
      },
      {
        label: '周六 至 周五',
        value: 6,
      },
      {
        label: '周日 至 周六',
        value: 7,
      },
    ],
  },
  {
    label: '日',
    value: GROUP_DAY,
  },
]

// 时分秒分组
export const dateGroupOptions_HHMMSS = [
  {
    label: '小时',
    value: GROUP_HOUR,
  },
  {
    label: '分钟',
    value: GROUP_MINUTE,
    children: [
      { label: '30m', value: '30' },
      { label: '20m', value: '20' },
      { label: '15m', value: '15' },
      { label: '10m', value: '10' },
      { label: '5m', value: '5' },
    ],
  },
]

// 默认日期展示
export const DEFAULT_WEEK_DISPLAY = 'WEEK_SEQUENCE' // 默认周显示
export const DEFAULT_DAY_DISPLAY = 'DAY_SEQUENCE' // 默认日显示
export const DEFAULT_MONTH_DISPLAY = 'MONTH_SEQUENCE' // 默认月显示
export const DEFAULT_WEEK_DAY_DISPLAY = 'WEEK_DAY_SEQUENCE' // 默认周的日显示

export const DEFAULT_WEEK_START = 'MONDAY' // 默认周起始日
// 周的起始日显示选项
export const WEEK_START_OPTIONS = [
  { label: '当周周一', value: DEFAULT_WEEK_START },
  { label: '当周周日', value: 'SUNDAY' },
  { label: '起始日', value: 'START' },
  { label: '结束日', value: 'END' },
]

export const DEFAULT_MONTH_START = 'START' // 默认月起始日
export const MONTH_START_OPTIONS = [
  { label: '当月首日', value: 'START' },
  { label: '当月末日', value: 'END' },
]

// 日期展示
// WEEK_SEQUENCE(1, '第x周')
// WEEK_SEQUENCE_YEAR(2, 'xxxx年第x周')
// WEEK_RANGE(3, '2022/04/17 - 2022/04/23')
// WEEK_RANGE_WITHOUT_YEAR(4, '04/17 - 04/23')
// DAY_SEQUENCE(5, 'xxxx-xx-xx')
// DAY_WEEK(6, '周x')
export const dateDisplayOptions = [
  {
    label: '第 * 周',
    value: DEFAULT_WEEK_DISPLAY,
    group: GROUP_WEEK,
  },
  {
    label: 'YYYY 年 第 * 周',
    value: 'WEEK_SEQUENCE_YEAR',
    group: GROUP_WEEK,
  },
  {
    label: 'YYYY/MM/DD - YYYY/MM/DD',
    value: 'WEEK_RANGE',
    group: GROUP_WEEK,
  },
  {
    label: 'MM/DD - MM/DD',
    value: 'WEEK_RANGE_WITHOUT_YEAR',
    group: GROUP_WEEK,
  },
  {
    label: 'YYYY/MM/DD',
    group: GROUP_WEEK,
    value: DEFAULT_WEEK_DAY_DISPLAY,
    width: 110,
    children: WEEK_START_OPTIONS,
  },
  {
    label: 'YYYY/MM/DD',
    group: GROUP_DAY,
    value: DEFAULT_DAY_DISPLAY,
  },
  {
    label: 'YYYY/MM/DD（周 *）',
    group: GROUP_DAY,
    value: 'DAY_WEEK',
  },
  {
    label: 'YYYY/MM/DD',
    group: GROUP_MONTH,
    value: DEFAULT_MONTH_DISPLAY,
    width: 110,
    children: MONTH_START_OPTIONS,
  },
  {
    label: 'YYYY-MM',
    group: GROUP_MONTH,
    value: 'MONTH_YEAR',
  },
  {
    label: 'YYYY年MM月',
    group: GROUP_MONTH,
    value: 'MONTH_YEAR_CN',
  },
]

// 对比字段
export const toContrastFiled = field => isDateField(field)

// 默认对比类型
export const DEFAULT_RATIO_TYPE = 'CHAIN'

export const COMPARE_RATIO_PERIOD = {
  SAME: 'SAME',
  WHOLE: 'WHOLE',
  DEFAULT: 'SAME',
}

// 对比类型
export const ratioOptions = [
  { label: '环比', value: DEFAULT_RATIO_TYPE }, // 1
  { label: '天同比', value: 'DAY_ON_DAY' }, // 2
  { label: '周同比', value: 'WEEK_ON_WEEK' }, // 2
  { label: '月同比', value: 'MONTH_ON_MONTH' }, // 3
  { label: '年同比', value: 'YEAR_ON_YEAR' }, // 4
]

// 查询展示前条数
export const queryTotalOptions = [
  { label: 10000, value: 10000 },
  { label: 50000, value: 50000 },
  { label: 100000, value: 100000 },
]

/**
 * 对比字段的聚合方式
 * {key}: '' 原值，1 年，2 季，3 月，4 周，5 天
 * {value}: 对比 ratioOptions 映射关系
 */
export const dateGroupTypeMap = {
  // 原值
  ORIGIN: {
    CHAIN: [{ label: '上一时段', value: COMPARE_RATIO_PERIOD.SAME }],
    WEEK_ON_WEEK: [
      { label: '上周同天', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上周同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    MONTH_ON_MONTH: [
      { label: '上月同天', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上月同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同天', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 年
  YEAR: {
    CHAIN: [
      { label: '去年整年', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 季
  QUARTER: {
    CHAIN: [
      { label: '上季整季', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上季同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同季', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 月
  MONTH: {
    CHAIN: [
      { label: '上月整月', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上月同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同月', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 周
  WEEK: {
    CHAIN: [
      { label: '上周整周', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上周同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同周', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  // 天
  DAY: {
    CHAIN: [
      { label: '昨天整天', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '昨天同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    WEEK_ON_WEEK: [
      { label: '上周同天', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上周同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    MONTH_ON_MONTH: [
      { label: '上月同天', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '上月同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
    YEAR_ON_YEAR: [
      { label: '去年同天', value: COMPARE_RATIO_PERIOD.WHOLE },
      { label: '去年同期', value: COMPARE_RATIO_PERIOD.SAME },
    ],
  },
  HOUR: {
    CHAIN: [{ label: '上一小时', value: COMPARE_RATIO_PERIOD.WHOLE }],
    DAY_ON_DAY: [{ label: '昨天同小时', value: COMPARE_RATIO_PERIOD.SAME }],
  },
  MINUTE: {
    CHAIN: [{ label: '上一时段', value: COMPARE_RATIO_PERIOD.WHOLE }],
    DAY_ON_DAY: [{ label: '昨天同时段', value: COMPARE_RATIO_PERIOD.SAME }],
  },
}
