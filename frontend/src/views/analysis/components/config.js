/**
 * 对比字段的聚合方式
 * {key}: '' 原值，1 年，2 季，3 月，4 周，5 天
 * {value}: 对比 ratioOptions 映射关系
 */
export const dateGroupTypeMap = {
  ORIGIN: ['CHAIN', 'WEEK_ON_WEEK', 'MONTH_ON_MONTH', 'YEAR_ON_YEAR'], // 原值
  YEAR: ['CHAIN'], // 年
  QUARTER: ['CHAIN', 'YEAR_ON_YEAR'], // 季
  MONTH: ['CHAIN', 'YEAR_ON_YEAR'], // 月
  WEEK: ['CHAIN', 'YEAR_ON_YEAR'], // 周
  DAY: ['CHAIN', 'WEEK_ON_WEEK', 'MONTH_ON_MONTH', 'YEAR_ON_YEAR'], // 天
}
