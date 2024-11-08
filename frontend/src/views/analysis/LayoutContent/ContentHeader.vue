<template>
  <div class="sections" :class="{ disabled: !hasDatasetAnalysis }">
    <template v-if="renderType !== 'statistic'">
      <!-- 维度 -->
      <SectionList
        :group="renderType === 'intersectionTable' ? 'row' : undefined"
        :dataset="dataset"
        :category="CATEGORY.PROPERTY"
        :data-source="dimensions"
      />

      <!-- 维度 -->
      <SectionList
        v-if="renderType === 'intersectionTable'"
        group="column"
        :dataset="dataset"
        :category="CATEGORY.PROPERTY"
        :data-source="dimensions"
      />
    </template>

    <!-- 指标 -->
    <SectionList
      style="margin-top: 8px"
      :open="customFormatterOpen"
      :dataset="dataset"
      :category="CATEGORY.INDEX"
      :data-source="measures"
    />

    <!-- 筛选 -->
    <SectionList
      style="margin-top: 8px"
      :dataset="dataset"
      :category="CATEGORY.FILTER"
      :data-source="filters"
    />
  </div>

  <!-- 重命名 -->
  <FieldRenameModal
    v-model:open="renameModalVisible"
    :field="renameField"
    @ok="onRenameOk"
    @reset="onRenameReset"
  />

  <Teleport to="body">
    <CustomFormatterVue
      :style="customFormatStyle"
      :value="customFormatConfig.config"
      v-model:open="customFormatterOpen"
      @ok="onCustomFormatterOk"
    />
  </Teleport>
</template>

<script setup>
import { provide, ref, inject, computed, defineAsyncComponent } from 'vue'
import SectionList from './components/SectionList.vue'
import FieldRenameModal from '@/views/analysis/components/FieldRenameModal.vue'
import { CATEGORY } from '@/CONST.dict.js'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({})
  },
  dimensions: {
    type: Array,
    default: () => []
  },
  measures: {
    type: Array,
    default: () => []
  },
  filters: {
    type: Array,
    default: () => []
  }
})

const {
  options,
  permissions,
  requestResponse: indexRequestResponse
} = inject('index')

const renderType = computed(() => options.get('renderType'))
const hasDatasetAnalysis = computed(() => permissions.dataset.hasAnalysis())

const renameModalVisible = ref(false)
const renameField = ref({})

const fieldRenameHandler = field => {
  renameModalVisible.value = true
  renameField.value = field
}

const indexRequest = computed(() => indexRequestResponse.get().request)

// 无需重新查询, 更新 requestResponse 中信息
const updateRequest = field => {
  if (!indexRequest.value) return

  const indexList = indexRequest.value[CATEGORY.INDEX.toLowerCase() + 's']
  const item = indexList.find(
    t => t.name === field.name && t.aggregator === field.aggregator
  )

  if (!item) return

  item.displayName = field.displayName
  item._modifyDisplayName = field._modifyDisplayName
}

const onRenameOk = (field, e) => {
  field.displayName = e.displayName
  field._modifyDisplayName = e.displayName

  updateRequest({ ...field, ...e })
}
const onRenameReset = field => {
  const originFields = props.dataset.fields
  const item = props.measures.find(t => t.name === field.name)
  const originItem = originFields.find(t => t.name === item.name)

  if (!originItem) return

  field.displayName = originItem.displayName
  field._modifyDisplayName = undefined
  updateRequest(field)
}

// 格式化
const formatters = computed(() => options.get('formatters') || [])
// 异步加载 CustomFormatter, 避免页面初始化时左侧伸缩
const CustomFormatterVue = defineAsyncComponent(
  () => import('@/components/CustomFormatter/index.vue')
)
const customFormatterOpen = ref(false)
const customFormatConfig = ref({ field: undefined, config: {} })
const customFormatStyle = ref()
const onCustomFormatterShow = (field, e) => {
  const fieldName = field.name + '.' + field.aggregator
  const formatterItem = formatters.value.find(t => t.field === fieldName)
  if (formatterItem) {
    customFormatConfig.value = {
      ...formatterItem,
      config: formatterItem.config
        ? JSON.parse(formatterItem.config)
        : undefined
    }
  } else {
    customFormatConfig.value = {
      field: fieldName
    }
  }

  const { left, top } = e.currentTarget.getBoundingClientRect()
  const x = `${left}px`,
    y = `${top - 240}px`

  customFormatStyle.value = {
    position: 'fixed',
    transform: `translate(${x}, ${y})`,
    top: 0,
    opacity: 1
  }
  customFormatterOpen.value = true
}
const onCustomFormatterOk = payload => {
  const item = formatters.value.find(
    t => t.field === customFormatConfig.value.field
  )
  if (item) {
    item.code = 'CUSTOM'
    item.config = JSON.stringify(payload)
  } else {
    formatters.value.push({
      field: customFormatConfig.value.field,
      code: 'CUSTOM',
      config: JSON.stringify(payload)
    })
  }
  customFormatterOpen.value = false
}

provide('contentHeader', {
  fieldRename: fieldRenameHandler,
  onCustomFormatterShow,
  getCustomFormatter() {
    return {
      open: customFormatterOpen.value,
      item: customFormatConfig.value
    }
  }
})
</script>

<style lang="scss" scoped>
.sections {
  padding: 10px;
  &.disabled {
    background-color: rgba(211, 211, 211, 0.3);
    cursor: not-allowed;
    & > * {
      pointer-events: none;
    }
  }
}
</style>
