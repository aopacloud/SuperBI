<template>
  <div class="custom--wrapper" v-show="open" @click.stop>
    <a-input-number
      style="display: block"
      placeholder="请输入位数"
      v-model:value="modelValue.digit"
      :min="0"
      :max="99"
      :precision="0">
      <template #addonBefore>
        <a-select
          style="width: 85px"
          class="custom--type-select"
          v-model:value="modelValue.type"
          :getPopupContainer="e => e.parentElement"
          @change="onSelectChanged">
          <a-select-option :value="0">数字</a-select-option>
          <a-select-option :value="1">百分比</a-select-option>
        </a-select>
      </template>

      <template #addonAfter>
        <span>位小数</span>
      </template>
    </a-input-number>

    <a-checkbox style="margin-top: 10px" v-model:checked="modelValue.thousand"
      >千分位</a-checkbox
    >

    <div style="margin-top: 10px; text-align: right">
      <a-space>
        <a-button size="small" @click="handleCancel">取消</a-button>
        <a-button size="small" type="primary" @click="handleOk">确认</a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup>
import { reactive, toRaw, watch } from 'vue'
import { deepCloneByJson } from 'common/utils/help'

// 默认配置项
const dfaultOption = {
  type: 0,
  thousand: true,
  digit: 2,
}

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  value: {
    type: Object,
    default: () => ({}),
  },
})

const modelValue = reactive({
  type: dfaultOption.type,
  thousand: dfaultOption.thousand,
  digit: dfaultOption.digit,
})

const init = ({ config = {} }) => {
  let { type = 0, thousand = true, digit = 2 } = config

  modelValue.type = type
  modelValue.thousand = thousand
  modelValue.digit = digit
}

watch(
  () => props.open,
  v => {
    if (v) {
      init(props.value)
    }
  }
)

const onSelectChanged = value => {
  // 数字默认千分位，百分比默认不千分位
  cThousand.value = value === '0'

  modelValue.thousand = value === '0'
}

const emits = defineEmits(['ok', 'update:open'])
const handleOk = () => {
  const payload = toRaw(modelValue)

  emits('ok', { field: props.value.field, config: deepCloneByJson(payload) })
  handleCancel()
}

const handleCancel = () => {
  modelValue.type = dfaultOption.type
  modelValue.thousand = dfaultOption.thousand
  modelValue.digit = dfaultOption.digit

  emits('update:open', false)
}
</script>

<style scoped lang="scss">
.custom--wrapper {
  width: 300px;
  padding: 12px 15px;
  border: 1px solid #d8d8d8;
}
.custom--type-select {
  :deep(.ant-select-selection-item) {
    text-align: left;
  }
}
</style>
