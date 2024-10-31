<template>
  <a-modal
    :open="open"
    :title="title"
    :confirmLoading="confirmLoading"
    @cancel="cancel"
    @ok="ok"
  >
    <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-form-item label="名称" v-bind="validateInfos.name">
        <a-input placeholder="请输入名称" v-model:value="formState.name" />
      </a-form-item>

      <a-form-item label="描述">
        <a-textarea
          placeholder="请输入描述"
          :rows="3"
          v-model:value="formState.description"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, inject, watch, computed, toRaw } from 'vue'
import { Form, message } from 'ant-design-vue'
import { postReport, putReport } from '@/apis/report'
import { CATEGORY } from '@/CONST.dict'
import { isDateField } from '@/views/dataset/utils'
import { sortDimension, validateSummaryOptions } from '@/views/analysis/utils'

const emits = defineEmits(['update:open', 'cancel', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    fefault: false
  },
  initData: {
    type: Object,
    default: () => ({})
  }
})

const title = computed(() => {
  return props.initData.id ? '更新图表' : '保存图表'
})

const {
  choosed: indexChoosed,
  dataset: indexDataset,
  options: indexOptions,
  requestResponse: indexRequestResponse
} = inject('index', {})

const formState = reactive({
  name: '',
  description: ''
})
const formRules = reactive({
  name: [{ required: true, message: '请输入名称' }]
})
const { validate, resetFields, validateInfos } = Form.useForm(
  formState,
  formRules
)

watch(
  () => props.open,
  op => {
    if (op) {
      ;['name', 'description'].forEach(key => {
        const val = props.initData[key]
        if (key === 'name' && !props.initData.id) {
          formState[key] = val + ' 1'
        } else {
          formState[key] = val || ''
        }
      })
    } else {
      resetFields()
    }
  }
)

const cancel = () => {
  resetFields()
  emits('update:open', false)
}
const confirmLoading = ref(false)
const ok = () => {
  validate().then(() => {
    submit(toRaw(formState))
  })
}

// 处理重命名后的指标
const getIndexList = (list = []) => {
  // 选中的指标
  const choosedIndex = indexChoosed.get(CATEGORY.INDEX)

  return list.map(t => {
    const choosedItem = choosedIndex.find(a => a.name === t.name)

    return {
      ...t,
      _modifyDisplayName:
        choosedItem?._modifyDisplayName || t._modifyDisplayName,
      _formatter: choosedItem?._formatter ?? t._formatter
    }
  })
}

// 更新日期筛选项
const updateFilterDate = filterItem => {
  const { nested, tableFilter, children = [] } = filterItem
  if (nested) {
    return {
      ...filterItem,
      tableFilter: {
        ...tableFilter,
        children: (tableFilter.children || []).map(updateFilterDate)
      }
    }
  }

  if (children.length)
    return { ...filterItem, children: children.map(updateFilterDate) }

  if (!isDateField(filterItem)) return filterItem

  const choosedFilters = indexChoosed.get(CATEGORY.FILTER)
  const {
    name,
    conditions: [cond1]
  } = filterItem

  const { timeType, _until } = cond1
  // 自某日至*, 请求的静态日期类型与值更新为配置
  if (_until) {
    const item = choosedFilters.find(t => t.name === name)
    const {
      conditions: [cond2]
    } = item
    const { timeType: timeType2, args: args2 } = cond2

    if (item) {
      // 更新日期类型
      if (timeType !== timeType2) {
        cond1.timeType = timeType2
      }
      // 更新日期值
      cond1.args = [...args2]
    }
  }

  return {
    ...filterItem,
    conditions: [{ ...cond1 }]
  }
}

// 过滤从看板的筛选条件
const filterDashboardFilters = (list = []) =>
  list.filter(t => t._from !== 'dashboard').map(updateFilterDate)

const submit = async params => {
  try {
    confirmLoading.value = true

    const { id, name, workspaceId } = props.initData
    const options = indexOptions.get()
    const requestResponse = indexRequestResponse.get()

    // 只保存当前图表类型的排序配置
    const { sorters, renderType } = options
    options.sorters = { [renderType]: sorters[renderType] }

    if (!validateSummaryOptions(options)) {
      return
    }

    const payload = {
      name,
      workspaceId,
      ...params,
      datasetId: indexDataset.get().id,
      reportType: options.renderType,
      queryParam: JSON.stringify({
        ...requestResponse.request,
        [CATEGORY.PROPERTY.toLowerCase() + 's']: sortDimension(
          requestResponse.request[CATEGORY.PROPERTY.toLowerCase() + 's']
        ),
        [CATEGORY.INDEX.toLowerCase() + 's']: getIndexList(
          requestResponse.request[CATEGORY.INDEX.toLowerCase() + 's']
        ),
        [CATEGORY.FILTER.toLowerCase() + 's']: filterDashboardFilters(
          requestResponse.request[CATEGORY.FILTER.toLowerCase() + 's']
        )
      }),
      layout: JSON.stringify(options)
    }

    const fn = !!id ? () => putReport(id, payload) : () => postReport(payload)
    const res = await fn()

    message.success('保存成功')

    cancel()
    emits('ok', res)
  } catch (error) {
    console.error('图表保存\更新错误', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped></style>
