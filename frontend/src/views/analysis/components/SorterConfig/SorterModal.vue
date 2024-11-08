<template>
  <a-modal
    title="排序设置"
    :width="600"
    :maskClosable="false"
    :open="open"
    :bodyStyle="{ minHeight: '200px', paddingBottom: '24px' }"
    @cancel="onCancel"
  >
    <template #footer>
      <a-button danger @click="onClear"> 关闭功能 </a-button>
      <a-button @click="onCancel">取消</a-button>
      <a-button type="primary" @click="onOk">确认</a-button>
    </template>
    <section style="margin-top: 36px" class="sort-table">
      <TTable
        ref="tableRef"
        :fields="allChoose"
        v-model:dataSource="list"
        @custom="onCustom"
      />
    </section>
  </a-modal>

  <CustomSorterModal
    v-model="orderInfo.custom"
    v-model:open="customSorterModalOpen"
    @ok="onCustomOrderOk"
  />
</template>

<script setup>
import { computed, ref, shallowRef, toRaw } from 'vue'
import { deepClone } from '@/common/utils/help'
import { CATEGORY } from '@/CONST.dict'
import CustomSorterModal from './CustomSorterModal.vue'
import { isDtField } from '@/views/dataset/utils'
import {
  summaryOptions,
  quickCalculateOptions
} from '@/views/dataset/config.field'
import { unique } from 'common/utils/array'

const emits = defineEmits(['update:open', 'cancel', 'ok', 'clear'])
const props = defineProps({
  open: Boolean,
  options: {
    type: Object,
    default: () => ({})
  },
  choosed: {
    type: Object,
    default: () => ({})
  },
  value: {
    type: [Array, Object], // { row = [], column = [] } }
    default: () => []
  }
})

const renderType = computed(() => props.options.renderType)
const TTablePath = computed(() => {
  const t = renderType.value
  if (t === 'groupTable') return 'GroupTableSort'
  if (t === 'intersectionTable') return 'IntersectionTableSort'
  if (t === 'bar' || t === 'line') return 'ChartSort'

  return 'TableSort'
})

const tableRef = ref(null)
const TTable = shallowRef(null)
watch(
  renderType,
  () => {
    import(`./components/${TTablePath.value}.vue`).then(m => {
      TTable.value = m.default
    })
  },
  { immediate: true }
)

const getDisplayNameSuffix = item => {
  const { aggregator, fastCompute } = item
  const aggregatorItem = summaryOptions.find(t => t.value === aggregator)
  // const quickItem = quickCalculateOptions.find(t => t.value === fastCompute)

  return aggregatorItem ? '(' + aggregatorItem.label + ')' : ''

  // : '' + (quickItem ? '-' + quickItem.label : '')
}

const _toMapItem = t => ({ value: t.name, label: t.displayName })

const allChoose = computed(() => {
  const t = renderType.value

  // 指标卡和饼图不排序
  if (t === 'statistics' || t === 'pie') return []

  const pList = props.choosed[CATEGORY.PROPERTY]
  let iList = props.choosed[CATEGORY.INDEX]

  iList = iList.map(t => {
    const { name, aggregator } = t

    return {
      ...t,
      displayName: t.displayName + getDisplayNameSuffix(t),
      name: name + (aggregator ? '.' + aggregator : ''),
      _key: name + (aggregator ? '.' + aggregator : '')
    }
  })

  // 去除重复的指标
  iList = unique(iList, '_key')

  // 柱状体、折线图
  if (t === 'bar' || t === 'line') {
    return {
      row: pList.map(_toMapItem),
      column: iList.map(_toMapItem)
    }
  }

  // 分组表格
  if (t === 'groupTable') {
    return pList.map(_toMapItem)
  }

  // 交叉表格
  if (t === 'intersectionTable') {
    return {
      row: pList.filter(t => t._group !== 'column').map(_toMapItem),
      column: pList.filter(t => t._group === 'column').map(_toMapItem)
    }
  }

  // 明细表格
  return pList.concat(iList).map(_toMapItem)
})

const list = ref([])

const reset = () => {
  list.value = []
}
const init = () => {
  const t = renderType.value
  // 指标卡和饼图不排序
  if (t === 'statistics' || t === 'pie') return

  const val = props.value

  // 柱状图、折线图
  if (t === 'bar' || t === 'line') {
    if (val.row.length) {
      list.value = deepClone(val)
    } else {
      const defaultItem =
        allChoose.value.row.find(isDtField) || allChoose.value.row[0]

      list.value = {
        row: [{ field: defaultItem?.value, order: 'asc' }],
        column: []
      }
    }
  } else if (t === 'groupTable') {
    if (val.length) {
      list.value = deepClone(val)
    } else {
      // 分组表格、柱状图、折线图
      const defaultItem = allChoose.value[0]
      list.value = [
        {
          field: defaultItem?.value,
          order: t === 'groupTable' ? 'desc' : 'asc'
        }
      ]
    }
  } else if (t === 'intersectionTable') {
    list.value = {}
    if (val.row?.length) {
      list.value.row = deepClone(val.row)
    } else {
      // 交叉表格
      const rowDefaultItem = allChoose.value.row[0]
      list.value.row = rowDefaultItem
        ? [{ field: rowDefaultItem.value, order: 'desc' }]
        : []
    }

    if (val.column?.length) {
      list.value.column = deepClone(val.column)
    } else {
      // 交叉表格
      const columnDefaultItem = allChoose.value.column[0]
      list.value.column = [{ field: columnDefaultItem?.value, order: 'desc' }]
    }
  } else {
    if (val.length) {
      list.value = deepClone(val)
    } else {
      // 明细表格
      const defaultItem = props.choosed[CATEGORY.INDEX][0]
      list.value = [
        {
          field: defaultItem
            ? defaultItem.name + '.' + defaultItem.aggregator
            : undefined,
          order: 'desc'
        }
      ]
    }
  }
}

const onCancel = () => {
  emits('update:open', false)
  emits('cancel')
}
const onOk = () => {
  if (!tableRef.value?.validate()) {
    return
  }

  emits('update:open', false)
  emits('ok', toRaw(list.value))
}
const onClear = () => {
  emits('update:open', false)
  emits('clear')
}

watch(
  () => props.open,
  e => {
    if (e) {
      init()
    } else {
      reset()
    }
  },
  { immediate: true }
)

// 排序信息
const orderInfo = ref({})
const customSorterModalOpen = ref(false)

const onCustom = e => {
  orderInfo.value = { ...e }
  customSorterModalOpen.value = true
}
const onCustomOrderOk = e => {
  let item = {}
  if (
    renderType.value === 'intersectionTable' ||
    renderType.value === 'bar' ||
    renderType.value === 'line'
  ) {
    const { row = [], column = [] } = list.value
    item = [...row, ...column].find(t => t.field === orderInfo.value.field)
  } else {
    item = list.value.find(t => t.field === orderInfo.value.field)
  }
  item.custom = e
  item.order = 'custom'
}
</script>

<style lang="scss" scoped>
.sort-table {
  :deep(.select-no-border) {
    .ant-select-selector {
      border: none;
      box-shadow: none !important;
    }
  }
}
</style>
