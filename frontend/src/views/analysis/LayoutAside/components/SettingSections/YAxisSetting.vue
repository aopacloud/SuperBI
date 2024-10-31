<template>
  <div class="setting-yAxis">
    <section class="setting-panel-section">
      <div class="setting-panel-item">
        轴名称
        <a-switch
          size="small"
          style="margin-left: 4px"
          v-model:checked="cValue.nameShow"
        />
      </div>
    </section>

    <section class="setting-panel-section">
      <p class="setting-panel-item">轴值范围</p>
      <div class="setting-panel-content list">
        <div class="item">
          最大值
          <a-input-number
            size="small"
            style="flex: 1; min-width: 20px; max-width: 150px"
            :disabled="isRangeAuto('max')"
            v-model:value.lazy="cValue.range.max"
          />
          <a-checkbox
            :checked="isRangeAuto('max')"
            @change="e => onRangeAutoChange(e, 'max')"
            >自适应
          </a-checkbox>
        </div>
        <div class="item">
          最小值
          <a-input-number
            size="small"
            style="flex: 1; min-width: 20px; max-width: 150px"
            :disabled="isRangeAuto('min')"
            v-model:value.lazy="cValue.range.min"
          />
          <a-checkbox
            :checked="isRangeAuto('min')"
            @change="e => onRangeAutoChange(e, 'min')"
            >自适应
          </a-checkbox>
        </div>
      </div>
    </section>

    <section class="setting-panel-section">
      <p class="setting-panel-item">轴值间隔</p>
      <div class="setting-panel-content">
        <a-radio-group
          v-model:value="cValue.interval"
          @change="onIntervalChange"
        >
          <a-radio value="auto">自动</a-radio>
          <!-- <a-radio value="count">按数量</a-radio> -->
          <a-radio value="step">按步长</a-radio>
        </a-radio-group>

        <!-- <div style="margin-top: 8px" v-if="cValue.interval === 'count'">
          轴值将自动分为
          <a-input-number
            size="small"
            :min="1"
            :max="99"
            :precision="0"
            v-model:value.lazy="cValue.intervalCount"
          />
          段
        </div> -->

        <div style="margin-top: 8px" v-if="cValue.interval === 'step'">
          轴值将自动按照
          <a-input-number
            size="small"
            :min="0"
            v-model:value.lazy="cValue.intervalStep"
          />
          一个间隔进行分段
        </div>
      </div>
    </section>

    <section class="setting-panel-section">
      <p class="setting-panel-item">
        双Y轴
        <a-switch
          size="small"
          style="margin-left: 4px"
          v-model:checked="modelValue.multipleY"
          @change="onDoubleYChange"
        />
      </p>

      <ChartAxis
        class="setting-panel-content"
        v-if="modelValue.multipleY"
        :data-source="modelValue.axis"
      />
    </section>
  </div>
</template>

<script setup>
import { watchEffect, ref } from 'vue'
import { defaultChartOptions } from '@/views/analysis/defaultOptions'
import { deepClone } from '@/common/utils/help'
import ChartAxis from './ChartAxis.vue'

const emits = defineEmits(['update:modelValue'])
const props = defineProps({
  renderType: String,
  modelValue: {
    type: Object,
    default: () => deepClone(defaultChartOptions.yAxis)
  }
})

const cValue = ref({})

watchEffect(() => {
  cValue.value = props.modelValue
})

watch(
  () => cValue,
  e => {
    emits('update:modelValue', e)
  },
  { deep: true, immediate: true }
)

const isRangeAuto = k => typeof cValue.value.range[k] === 'undefined'

const onRangeAutoChange = (e, k) => {
  const checked = e.target.checked

  cValue.value.range[k] = checked ? undefined : ''
}

const onIntervalChange = () => {
  cValue.value.intervalCount = undefined
  cValue.value.intervalStep = undefined
}

const onDoubleYChange = () => {
  cValue.value.axis.forEach(t => {
    t.chartType = props.renderType
    t.yAxisPosition = 'left'
  })
}
</script>

<style lang="scss" scoped>
.title {
  margin: 0 0 8px;
  & ~ & {
    margin-top: 16px;
  }
}

.list {
  .item {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
    margin-bottom: 6px;
  }
}
</style>
