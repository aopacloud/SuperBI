<template>
  <div class="summary-setting">
    <section class="setting-panel-section">
      <div class="setting-panel-item">
        <a-checkbox
          :checked="rowSummary.enable"
          @change="onRowSummaryEnableChange"
        >
          行汇总
        </a-checkbox>
      </div>

      <div v-if="rowSummary.enable" class="setting-panel-content">
        <div class="setting-panel-item">计算方式</div>
        <div class="setting-panel-item summary-list" style="display: block">
          <SummarySettingItem
            block
            size="small"
            class="summary-item"
            v-for="(item, index) in rowSummary.list"
            :onlyOne="rowSummary.length === 1"
            :item="item"
            :fields="fields"
            :selectedNames="rowSummary.list.map(t => t.name)"
          >
            <template #action>
              <a-space-compact
                size="small"
                style="width: 48px; margin-left: 4px"
              >
                <a-button
                  type="text"
                  style="color: #1677ff"
                  v-if="index === rowSummary.list.length - 1"
                  :icon="h(PlusCircleOutlined)"
                  @click="insertAfter"
                />
                <a-button
                  type="text"
                  danger
                  :icon="h(MinusCircleOutlined)"
                  :disabled="rowSummary.list.length === 1"
                  @click="remove(index)"
                />
              </a-space-compact>
            </template>
          </SummarySettingItem>
        </div>
      </div>
    </section>

    <section class="setting-panel-section">
      <div class="setting-panel-item">
        <a-checkbox
          :checked="column.enable"
          @change="onColumnSummaryEnableChange"
          >列汇总
        </a-checkbox>
      </div>
      <div v-if="column.enable" class="setting-panel-content">
        <div class="setting-panel-item">
          <span style="width: 60px">计算方式</span>
          <AggregatorSelector
            style="width: 95px; margin-left: 8px"
            size="small"
            :hasAuto="false"
            v-model:value="column.aggregator"
            v-model:customValue="column._aggregator"
          />
        </div>
        <div
          v-if="renderType !== 'table' || !reversed"
          class="setting-panel-item"
        >
          <span style="width: 60px">位置</span>
          <a-radio-group
            defaultValue="afterInDimension"
            v-model:value="column.position"
          >
            <a-radio value="first">首列</a-radio>
            <a-radio value="afterInDimension">维度列后</a-radio>
            <a-radio value="last">最后</a-radio>
          </a-radio-group>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { h, watchEffect, inject } from 'vue'
import { MinusCircleOutlined, PlusCircleOutlined } from '@ant-design/icons-vue'
import { getRandomKey } from '@/common/utils/string'
import SummarySettingItem from './SummarySettingItem.vue'
import { summaryOptions } from '@/views/dataset/config.field'
import AggregatorSelector from '@/components/AggregatorSelector/index.vue'

const setDefaultItem = payload => {
  return {
    _key: getRandomKey(),
    ...payload
  }
}

const emits = defineEmits(['update:row', 'update:column'])
const props = defineProps({
  indexFields: {
    type: Array,
    default: () => []
  },
  row: {
    type: Object,
    default: () => ({})
  },
  column: {
    type: Object,
    default: () => ({})
  },
  renderType: String, // 渲染类型
  reversed: Boolean // 是否行列转置
})

const { autoRun, requestResponse: indexRequestResponse } = inject('index', {})

const request = computed(() => indexRequestResponse.get('request') || {})

const fields = computed(() =>
  props.indexFields
    .filter(t => !t.fastCompute)
    .map(t => {
      const { name, displayName, aggregator } = t
      const summaryItem = summaryOptions.find(t => t.value === aggregator)

      return {
        ...t,
        value: name + '.' + aggregator,
        label: displayName + (summaryItem ? '(' + summaryItem.label + ')' : '')
      }
    })
)

// 行汇总
const rowSummary = ref({})

watchEffect(() => {
  const rowS = props.row || {}
  rowSummary.value = rowS

  if (rowS.enable) {
    const rowList = rowSummary.value.list || []
    if (!rowList.length) {
      rowSummary.value.list = [
        setDefaultItem({
          name: fields.value.map(t => t.value),
          _all: true, // 全部指标
          aggregator: 'auto'
        })
      ]
    } else {
      rowSummary.value.list = rowList
      rowSummary.value.list.forEach(t => {
        // 全部指标值选中左右指标名
        if (t._all) {
          t.name = fields.value.map(t => t.value)
        }
      })
    }
  }
})

const insertAfter = () => {
  rowSummary.value.list.push(setDefaultItem())
}
const remove = index => {
  rowSummary.value.list.splice(index, 1)
}

const onRowSummaryEnableChange = e => {
  const checked = e.target.checked
  // 行汇总、列汇总必须出现一个
  if (!props.column.enable && !checked) return

  rowSummary.value.enable = checked
  if (!checked) rowSummary.value.list = []

  // 上一次查询未显示汇总行则重新查询
  checked &&
    request.value.status === 'SUCCESS' &&
    !request.value.summary &&
    autoRun()
}

const onColumnSummaryEnableChange = e => {
  const checked = e.target.checked
  // 行汇总、列汇总必须出现一个
  if (!rowSummary.value.enable && !checked) return

  props.column.enable = checked
  if (checked) {
    props.column.position = 'afterInDimension'
    props.column.aggregator = 'SUM'
  }
}
</script>

<style lang="scss" scoped>
.summary-list {
  :deep(.summary-item) {
    & + .summary-item {
      margin-top: 6px;
    }
  }
}
</style>
