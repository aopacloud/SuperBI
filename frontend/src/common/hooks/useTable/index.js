import { ref, reactive, computed, unref } from 'vue'

export default function useTable(requestApi, initParams = {}) {
  const {
    sorter: initSorter,
    filters: initFilters,
    pager: initPager,
    keyword: initKeyword,
    initQueryParams,
    transform,
    onSuccess,
    onError,
  } = initParams

  // 请求loading
  const loading = ref(false)

  // 查询关键字
  const keyword = ref(initKeyword)

  // 排序
  const sorter = ref(initSorter || {})

  // 筛选
  const filters = ref(initFilters || {})

  // 分页
  const pager = reactive(
    Object.assign(
      {
        current: 1,
        pageSize: 20,
        total: 0,
        showSizeChanger: true,
      },
      initPager
    )
  )

  // 处理过来字段
  const _transformFilters = filters => {
    const filterKeys = Object.keys(filters)

    if (!filterKeys.length) return

    return filterKeys.reduce((acc, key) => {
      const value = filters[key]

      acc[key] = value?.join(',')

      return acc
    }, {})
  }

  // 请求参数
  const queryParams = computed(() => {
    const { current: pageNum, pageSize } = pager
    const { field, order } = sorter.value || {}
    const kw = keyword.value?.trim()
    const flts = _transformFilters(filters.value)

    return {
      pageNum,
      pageSize,
      keyword: kw,
      sortField: field,
      sortType: order?.slice(0, -3),
      ...flts,
      ...unref(initQueryParams),
    }
  })

  // 数据列表
  const list = ref([])

  // 查询请求
  const fetchList = async () => {
    try {
      loading.value = true

      let payload = queryParams.value

      if (typeof initQueryParams === 'function') {
        payload = {
          ...payload,
          ...initQueryParams(),
        }
      }

      const res = await requestApi(payload)

      if (transform && typeof transform === 'function') {
        const { total = 0, data = [] } = transform(res)

        list.value = data
        pager.total = total
      } else {
        list.value = res.data || []
        pager.total = res.total || 0
      }

      onSuccess && onSuccess()
    } catch (e) {
      console.error(`${requestApi.name} 请求失败`, e)
      onError && onError(e)
    } finally {
      loading.value = false
    }
  }

  const onTableChange = (p, f, s) => {
    pager.current = p.current
    pager.pageSize = p.pageSize

    filters.value = f
    sorter.value = s

    fetchList()
  }

  return {
    loading,
    keyword,
    pager,
    sorter,
    filters,
    queryParams,
    list,
    onTableChange,
    fetchList,
  }
}
