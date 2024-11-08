<template>
  <a-dropdown trigger="click">
    <slot v-bind="{ label: showAggregatorLabel }">
      <a-button
        :size="size"
        :disabled="disabled"
        :style="$attrs.style"
        @click.prevent
      >
        {{ showAggregatorLabel }}
        <DownOutlined
          style="
            vertical-align: -1px;
            font-size: 12px;
            color: rgba(0, 0, 0, 0.25);
          "
        />
      </a-button>
    </slot>
    <template #overlay>
      <a-menu :selectedKeys="selectedKeys" @click="onSummaryClick">
        <a-menu-item v-for="item in aggregatorOptions" :key="item.value">
          {{ item.label }}
        </a-menu-item>

        <a-sub-menu key="quantile" title="分位数">
          <a-menu-item
            v-for="q in quantileOptions"
            :key="q.value"
            :value="q.value"
            :disabled="q.value === QUANTILE_PREFIX"
          >
            <div v-if="q.value === QUANTILE_PREFIX">
              <a-input-number
                style="width: 100px"
                :placeholder="q.label"
                :min="1"
                :max="100"
                :precision="0"
                :value="customQuantileValue"
                :formatter="v => (v ? v + '分位数' : '')"
                :parser="v => v.replace('分位数', '')"
                @pressEnter.stop
                @blur="onQuantileBlur"
                @change="onQuantileChange"
              />
            </div>
            <span v-else>{{ q.label }} </span>
          </a-menu-item>
        </a-sub-menu>
      </a-menu>
    </template>
  </a-dropdown>
</template>

<script setup>
import { computed, ref, watchEffect } from 'vue'
import { DownOutlined } from '@ant-design/icons-vue'
import { quantileOptions, QUANTILE_PREFIX } from '@/views/dataset/config.field'
import { summaryOptions as summary } from '@/views/dataset/config.field'

const summaryOptions = summary.filter(t => !t.hidden)

const AUTO = {
  label: '自动',
  value: 'auto'
}

const emits = defineEmits(['update:value', 'update:customValue'])
const props = defineProps({
  size: {
    type: String,
    default: 'small',
    validator: s => ['small', 'default', 'large'].includes(s)
  },
  disabled: Boolean,
  hasAuto: {
    type: Boolean,
    default: true
  },
  value: String,
  customValue: String
})

const aggregatorOptions = computed(() =>
  props.hasAuto ? [AUTO].concat(summaryOptions) : summaryOptions
)

// 选中的值
const modelValue = ref()

// 显示文本
const showAggregatorLabel = computed(() => {
  const val = modelValue.value
  if (val?.includes(QUANTILE_PREFIX)) {
    const num = val.split(QUANTILE_PREFIX)[1]
    return num + '分位数'
  } else {
    const item = aggregatorOptions.value.find(item => item.value === val)
    return item.label
  }
})

// 自定义分位数值
const customQuantileValue = computed(() => {
  if (!props.customValue) return

  return modelValue.value.split(QUANTILE_PREFIX)[1]
})

// 高亮选中项
const selectedKeys = ref([])
const onSummaryClick = ({ key, keyPath }) => {
  selectedKeys.value = keyPath
  modelValue.value = key
  emits('update:value', key)
  emits('update:customValue')
}

// 自定义分位数
const onQuantileBlur = e => {
  let value = parseInt(e.target.value)
  if (!value) return

  if (value < 0) value = 0
  if (value > 100) value = 100

  selectedKeys.value = ['quantile', QUANTILE_PREFIX]
  const v = QUANTILE_PREFIX + value
  modelValue.value = v
  emits('update:value', v)
  emits('update:customValue', v)
}
// 自定义分位数
const onQuantileChange = e => {
  const value = parseInt(e)
  if (!value) return

  selectedKeys.value = ['quantile', QUANTILE_PREFIX]

  const v = QUANTILE_PREFIX + value
  modelValue.value = v
  emits('update:value', v)
  emits('update:customValue', v)
}

watchEffect(() => {
  const v = props.value,
    cV = props.customValue

  modelValue.value = props.value ?? (props.hasAuto ? AUTO.value : 'SUM')
  if (cV) {
    selectedKeys.value = [v, QUANTILE_PREFIX]
  } else {
    selectedKeys.value = [v]
  }
})
</script>

<style lang="scss" scoped></style>
