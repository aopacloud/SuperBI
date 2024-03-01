// 数据集中的dt字段
export const dtInDatasetFields = () => true

// 初始化筛选项
export const initFilters = () => []

// 清除筛选项
export const clearFilters = () => []

// 选中的tag是否可移除
export const sectionTagClosable = () => true

// 校验筛选项
export const valideteFilterItem = () => true

// 日期字段
export const isDateField = field => field.dataType.includes('TIME')
