export const keywordSearchLeftTip = '看板权限可私聊看板对应的创建人进行开通'

/**
 * 合并全局过滤条件
 * @param {array<any>} filters 全局的过滤条件
 * @param {function} toFilterItemCb 处理过滤条件
 * @returns {array<Filter>}
 */
export const mergeBoxFilters = (filters = [], _, mapCb) => {
  // 有效的全局过滤条件
  const validatePropFilters = filters.filter(t => {
    const { filterType, value } = t

    if (filterType === 'TIME') {
      const { extra = {}, mode, date, offset } = value

      if (extra.dt) return true
      return mode === 0 ? offset.length > 0 : date.length > 0
    } else if (filterType === 'TEXT' || filterType === 'NUMBER') {
      return value.every(t => t.value?.length > 0)
    } else {
      return value && value.length > 0
    }
  })

  return validatePropFilters.map(mapCb)
}

/**
 * 处理查询过滤条件
 * @param {array<T>} resFilters 图表的过滤条件
 * @param {array<T>} mergedFilters 合并后的过滤条件
 * @returns {array<T>}
 */
export const queryFilters = (resFilters = [], mergedFilters = []) => {
  //  图表自身无dt过滤直接合并
  return [...resFilters, ...mergedFilters]
}

/**
 * 副标题显示文本
 */
export const displaySubTitle = () => ''
