<template>
  <div class="sections" :class="{ disabled: !hasDatasetAnalysis }">
    <!-- 维度 -->
    <SectionList
      v-if="renderType !== 'statistic'"
      :dataset="dataset"
      :category="CATEGORY.PROPERTY"
      :data-source="dimensions" />

    <!-- 指标 -->
    <SectionList
      style="margin-top: 8px"
      :dataset="dataset"
      :category="CATEGORY.INDEX"
      :data-source="indexes" />

    <!-- 筛选 -->
    <SectionList
      style="margin-top: 8px"
      :dataset="dataset"
      :category="CATEGORY.FILTER"
      :data-source="filters" />
  </div>

  <!-- 重命名 -->
  <FieldRenameModal
    v-model:open="renameModalVisible"
    :field="renameField"
    @ok="onRenameOk"
    @reset="onRenameReset" />
</template>

<script setup>
import { provide, ref, inject, computed } from 'vue'
import SectionList from './components/SectionList.vue'
import FieldRenameModal from '@/views/analysis/components/FieldRenameModal.vue'
import { CATEGORY } from '@/CONST.dict.js'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
  dimensions: {
    type: Array,
    default: () => [],
  },
  indexes: {
    type: Array,
    default: () => [],
  },
  filters: {
    type: Array,
    default: () => [],
  },
})

const {
  options,
  permissions,
  requestResponse: indexRequestResponse,
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
  const item = props.indexes.find(t => t.name === field.name)
  const originItem = originFields.find(t => t.name === item.name)

  if (!originItem) return

  field.displayName = originItem.displayName
  updateRequest(field)
}

provide('contentHeader', {
  fieldRename: fieldRenameHandler,
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
