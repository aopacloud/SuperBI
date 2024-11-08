<template>
  <div class="edit-table-wrapper">
    <div class="title">
      <div>
        共计 {{ list.length }} 个字段，其中维度 {{ dimensions.length }} 个，指标
        {{ measures.length }} 个
        <a-button type="primary" ghost size="small" @click="toSort">
          排序
        </a-button>
      </div>

      <a-input-search
        style="width: 360px"
        placeholder="输入字段名称进行搜索"
        allow-clear
        v-model:value="keyword"
        @search="onKeywordSearch"
      />
    </div>

    <div class="table-container">
      <vxe-table
        size="small"
        height="auto"
        show-overflow="title"
        ref="vxeTable"
        :cell-class-name="cellClassName"
        :scroll-y="{ enabled: true, gt: 0, oSize: 10 }"
        :loading="loading"
        :data="list"
      >
        <vxe-column type="seq" width="60" title="序号" align="center" />

        <vxe-column
          field="displayName"
          title="展示名称"
          :class-name="onNameCellClass"
        >
          <template #default="{ row }">
            <input
              class="input"
              maxLength="20"
              v-model="row.displayName"
              placeholder="展示名称不能为空,且长度不能超过20个字符"
            />
          </template>
        </vxe-column>

        <vxe-column field="dataType" title="字段类型" width="300">
          <template #default="{ row }">
            <i
              :class="['iconfont', getIconByFieldType(row.dataType)?.['icon']]"
              :style="{
                marginRight: '8px',
                color: getIconByFieldType(row.dataType)?.['color']
              }"
            />
            <a-cascader
              style="width: 220px"
              popupClassName="field-dataType-cascader"
              :allowClear="false"
              :options="getFieldDataTypeOptions(row)"
              v-model:value="row.dataType"
            />
          </template>
        </vxe-column>

        <vxe-column field="category" title="数据类型" width="400">
          <template #default="{ row }">
            <a-select
              style="width: 90px"
              v-model:value="row.category"
              @click="e => onCategoryChange(row, e)"
            >
              <a-select-option value="DIMENSION">维度</a-select-option>
              <a-select-option value="MEASURE">指标</a-select-option>
            </a-select>
          </template>
        </vxe-column>
      </vxe-table>
    </div>

    <!-- 排序 -->
    <RowSortModal
      :data-source="originList"
      v-model:open="sortOpen"
      @ok="onRowSorted"
    />
  </div>
</template>

<script setup>
import { shallowRef, useTemplateRef } from 'vue'
import { message } from 'ant-design-vue'
import {
  getIconByFieldType,
  getFieldDataTypeOptions
} from '@/views/dataset/utils'
import RowSortModal from '@/views/dataset/modify/components/RowSortModal.vue'
import { CATEGORY } from '@/CONST.dict'

const { previewFields, loading } = defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  previewFields: {
    type: Array,
    default: () => []
  }
})

const originList = shallowRef([])

const list = ref(originList.value)

const dimensions = computed(() =>
  list.value.filter(item => item.category === 'DIMENSION')
)
const measures = computed(() =>
  list.value.filter(item => item.category === 'MEASURE')
)

const keyword = ref('')
const onKeywordSearch = e => {
  const s = e.trim()
  if (!s.length) {
    list.value = [...originList.value]
  } else {
    list.value = originList.value.filter(item => item.displayName.includes(s))
  }
}

const onNameCellClass = ({ row }) => {
  return !row.displayName?.trim()?.length ? 'unValidate' : ''
}

const onCategoryChange = (row, e) => {
  if (e === CATEGORY.PROPERTY) {
    row.category
  }
}

// 错误行的索引
const errorRowIndex = ref(null)

const cellClassName = ({ row, rowIndex, column, columnIndex }) => {
  if (rowIndex === errorRowIndex.value && columnIndex === 2) {
    return 'unValidate'
  }
  return null
}

watch(
  () => previewFields,
  val => {
    originList.value = [...val].map(v => {
      const {
        name,
        title,
        category = CATEGORY.PROPERTY,
        displayName = title,
        dataType = v.type ?? 'TEXT',
        databaseDataType
      } = v

      return {
        name,
        category,
        databaseDataType,
        dataType: dataType.split('_'),
        displayName
      }
    })
    keyword.value = ''
    errorRowIndex.value = null
    list.value = [...originList.value]
  },
  { immediate: true, deep: true }
)

// 字段排序
const sortOpen = ref(false)
const toSort = () => {
  sortOpen.value = true
}
const onRowSorted = payload => {
  // Map => 字段名: order
  const sortedMap = payload.reduce((acc, pre) => {
    const { name, sort } = pre

    acc[name] = sort
    return acc
  }, {})

  originList.value = originList.value
    .map(item => {
      item.sort = sortedMap[item.name]

      return item
    })
    .sort((a, b) => a.sort - b.sort)

  list.value = [...originList.value]
}

const validate = () => {
  const l = toRaw(originList.value)

  // 校验重复
  const checkedRepeat = a =>
    new Set(a.map(t => t.displayName)).size !== a.length

  if (l.some(t => !t.displayName?.trim()?.length)) {
    message.warn('展示名称不能为空')
    return false
  } else if (checkedRepeat(l)) {
    message.warn('展示名称不能重复')
    return false
  } else {
    return l.map((t, i) => {
      // delete t._X_ROW_KEY
      return {
        ...t,
        sort: i,
        type: 'ORIGIN',
        dataType: t.dataType.filter(Boolean).join('_')
      }
    })
  }
}

const vxeRef = useTemplateRef('vxeTable')

const validateRow = index => {
  if (typeof index === 'undefined') {
    errorRowIndex.value = null
    return
  }

  index = index - 1

  const row = list.value[index]
  errorRowIndex.value = index
  vxeRef.value.scrollToRow(row)
}

const resetValidate = () => {
  errorRowIndex.value = null
}

defineExpose({ validate, vxeRef, validateRow, resetValidate })
</script>

<style lang="scss" scoped>
.edit-table-wrapper {
  display: flex;
  flex-direction: column;

  :deep(.unValidate) {
    .input:not(:focus),
    .ant-select-selector {
      border-color: red;
    }

    .cell--error {
      background-color: #f56c6c;
    }
  }
}
.table-container {
  flex: 1;
}
.title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
