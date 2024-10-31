<template>
  <SettingSectionLayout class="table-section" title="表格设置">
    <a-radio-group
      class="setting-tabs"
      style="width: 100%; margin-bottom: 12px"
      v-model:value="settingTabKey"
    >
      <a-radio-button value="style" @click="scrollTo('style')">
        样式
      </a-radio-button>
      <a-radio-button value="data" @click="scrollTo('data')">
        数据
      </a-radio-button>
      <!-- <a-radio-button value="query" @click="scrollTo('query')">
        查询
      </a-radio-button> -->
    </a-radio-group>

    <a-collapse expand-icon-position="end" v-model:activeKey="activeKey">
      <a-collapse-panel
        header="列宽"
        key="layout"
        id="setting-table-section--style"
      >
        <a-radio-group
          :options="tableLayoutOptions"
          :value="modelValue.layout"
          @change="onTableLayoutChange"
        >
        </a-radio-group>
      </a-collapse-panel>

      <a-collapse-panel header="文字位置" key="align">
        <a-radio-group
          :options="tableAlignOptions"
          v-model:value="modelValue.align"
        />
      </a-collapse-panel>

      <a-collapse-panel header="冻结" key="fixed">
        <section class="setting-panel-section">
          <div class="setting-panel-section">
            <div class="setting-panel-item">行冻结</div>
            <a-radio class="setting-panel-content" disabled checked>
              行冻结
            </a-radio>
          </div>

          <div class="setting-panel-section">
            <div class="setting-panel-item">列冻结</div>
            <div class="setting-panel-content">
              <a-radio-group
                class="setting-panel-item"
                v-model:value="modelValue.fixed.columnMode"
                @change="onFixedModeChange"
              >
                <a-radio :value="0">无</a-radio>
                <a-radio :value="1">首列冻结</a-radio>
                <a-radio :value="2">自定义</a-radio>
              </a-radio-group>

              <div
                class="setting-panel-item"
                v-if="modelValue.fixed.columnMode === 2"
                style="display: flex"
              >
                前
                <a-input-number
                  size="small"
                  :min="0"
                  :precision="0"
                  v-model:value="modelValue.fixed.columnSpan[0]"
                  style="width: 65px; margin: 0 6px"
                />列，后
                <a-input-number
                  size="small"
                  :min="0"
                  :precision="0"
                  v-model:value="modelValue.fixed.columnSpan[1]"
                  style="width: 65px; margin: 0 6px"
                />列
              </div>
            </div>
          </div>
        </section>
      </a-collapse-panel>

      <a-collapse-panel
        key="summary"
        :collapsible="!summaryValue ? 'disabled' : ''"
      >
        <template #header>
          <span>汇总</span>
          <a-checkbox
            style="float: right"
            :checked="summaryValue"
            @change="onShowSummaryChange"
            @click.stop
            >显示
          </a-checkbox>
        </template>

        <SummarySetting
          v-if="summaryValue"
          :renderType="renderType"
          :reversed="modelValue.reverse"
          :indexFields="summaryIndexList"
          v-model:row="modelValue.summary.row"
          v-model:column="modelValue.summary.column"
        />
      </a-collapse-panel>

      <a-collapse-panel header="单元格展示" key="cell">
        <div>
          <a-checkbox v-model:checked="modelValue.orderSeq">序列号</a-checkbox>

          <a-checkbox
            v-if="renderType === 'table'"
            style="margin-left: 8px"
            v-model:checked="modelValue.reverse"
          >
            行列转置
          </a-checkbox>
        </div>

        <!-- <div :span="24" style="margin-top: 8px">
          <a-checkbox :checked="mergeValue" @change="onMergeChange">
            合并单元格
          </a-checkbox>

          <template v-if="mergeValue">
            <div class="panel-item">
              合并内容
              <a-checkbox-group
                style="margin-left: 8px"
                v-model:value="modelValue.merge.cell"
              >
                <a-checkbox value="dimension">维度列</a-checkbox>
                <a-checkbox value="measure">指标列</a-checkbox>
              </a-checkbox-group>
            </div>
            <div class="panel-item">
              展示位置
              <a-radio-group
                style="margin-left: 8px"
                v-model:value="modelValue.merge.align"
              >
                <a-radio value="top">居上</a-radio>
                <a-radio value="middle">居中</a-radio>
                <a-radio value="bottom">居下</a-radio>
              </a-radio-group>
            </div>
          </template>
        </div> -->
      </a-collapse-panel>

      <a-collapse-panel
        header="同环比"
        key="compare"
        id="setting-table-section--data"
        :collapsible="compareDisabled ? 'disabled' : ''"
      >
        <BasisRatio
          chartType="table"
          :disabled="compareDisabled"
          v-model:value="contrastModelValue"
        />
      </a-collapse-panel>

      <a-collapse-panel>
        <template #header>
          特殊值
          <a-tooltip :overlayStyle="{ maxWidth: 'unset' }">
            <template #title>
              特殊值是指原始明细中的 空值、NaN、Infinity <br />
              或计算后的 空值、NaN、Infinity等等
            </template>
            <QuestionCircleOutlined />
          </a-tooltip>
        </template>

        <section class="setting-panel-section">
          <div
            class="setting-panel-item"
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            维度特殊值显示
            <a-select
              size="small"
              style="width: 120px"
              placeholder="请选择"
              defaultValue="original"
              :options="cSpecial"
              v-model:value="modelValue.special.dimension"
            />
          </div>
          <div
            class="setting-panel-item"
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            指标特殊值显示
            <a-select
              size="small"
              style="width: 120px"
              placeholder="请选择"
              defaultValue="0"
              :options="cSpecial"
              v-model:value="modelValue.special.measure"
            />
          </div>
        </section>
      </a-collapse-panel>
    </a-collapse>
  </SettingSectionLayout>
</template>
<script setup>
import { computed, watch, inject, nextTick } from 'vue'
import { QuestionCircleOutlined } from '@ant-design/icons-vue'
import SettingSectionLayout from './SectionLayout.vue'
import BasisRatio from './BasisRatio.vue'
import { tableLayoutOptions, tableAlignOptions } from './config'
import { useVModel } from 'common/hooks/useVModel'
import SummarySetting from './SummarySetting.vue'
import { CATEGORY } from '@/CONST.dict'
import { specialNumberOptions } from './config'

const cSpecial = specialNumberOptions.filter(t => !t.hidden)

const props = defineProps({
  renderType: {
    type: String,
    default: 'table',
    validator: s => ['table', 'groupTable', 'intersectionTable'].includes(s)
  },
  options: {
    type: Object,
    default: () => ({})
  },
  compare: {
    type: Object,
    default: () => ({})
  }
})

const {
  autoRun,
  compare: indexCompare,
  requestResponse: indexRequestResponse
} = inject('index', {})

const compareDisabled = computed(() => {
  const compare = indexCompare.get() || {}

  return compare.measures?.length ? false : true
})

// 列冻结模式改变
const onFixedModeChange = e => {
  const value = e.target.value

  if (value === 0) {
    modelValue.value.fixed.columnSpan = [0, 0]
  } else if (value === 1) {
    modelValue.value.fixed.columnSpan = [1, 0]
  } else if (value === 2) {
    modelValue.value.fixed.columnSpan = [0, 0]
  }
}

const emits = defineEmits(['update:options', 'update:compare'])
const modelValue = useVModel(props, 'options', emits)
const contrastModelValue = useVModel(props, 'compare', emits)

watch(
  modelValue,
  val => {
    emits('update:options', val)
  },
  { deep: true }
)

watch(
  contrastModelValue,
  val => {
    emits('update:compare', val)
  },
  { deep: true }
)

const summaryValue = computed(() => {
  const s = modelValue.value.summary
  return s.row?.enable || s.column?.enable
})
watch(summaryValue, v => {
  if (!v) {
    const index = activeKey.value.indexOf('summary')
    activeKey.value.splice(index, 1)
  }
})

const request = computed(() => indexRequestResponse.get('request') || {})

const response = computed(() => indexRequestResponse.get('response') || {})

const summaryIndexList = computed(
  () => request.value[CATEGORY.INDEX.toLowerCase() + 's']
)
const onShowSummaryChange = e => {
  const checked = e.target.checked

  if (!checked) {
    modelValue.value.summary = false
  } else {
    modelValue.value.summary = {
      row: {
        enable: true,
        list: []
      },
      column: {
        enable: false
      }
    }
  }

  if (checked) {
    if (!activeKey.value.includes('summary')) {
      activeKey.value.push('summary')
    }
  } else {
    const index = activeKey.value.indexOf('summary')
    if (index > -1) activeKey.value.splice(index, 1)
  }

  // 上一次查询不显示汇总行则需重新查询
  if (checked && response.value.status === 'SUCCESS' && !request.value.summary)
    autoRun()
}

const activeKey = ref([])

const settingTabKey = ref('style')
const scrollTo = domId => {
  const dom = document.querySelector(`#setting-table-section--${domId}`)
  if (dom) dom.scrollIntoView()
}

const mergeValue = computed(() => !!modelValue.value.merge)
const onMergeChange = e => {
  const checked = e.target.checked
  if (!checked) {
    modelValue.value.merge = undefined
  } else {
    modelValue.value.merge = { cell: ['dimension'], align: 'middle' }
  }
}

const onTableLayoutChange = e => {
  modelValue.value.columnsWidth = undefined
  // 此处用 nextTick 是因为：
  // layout改变会立即触发表格的重新渲染，导致 columnsWith无法按照预期逻辑
  nextTick(() => {
    modelValue.value.layout = e.target.value
  })
}
</script>

<style lang="scss" scoped>
.setting-tabs {
  display: flex;
  :deep(.ant-radio-button-wrapper) {
    flex: 1;
    text-align: center;
  }
}
.table-section {
  :deep(.ant-collapse-header) {
    font-weight: 600;
  }
}
</style>
