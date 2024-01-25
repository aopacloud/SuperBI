<template>
  <div class="custom--wrapper" v-outclick="onOutclick" v-show="open" @click.stop>
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
        <a-button size="small" @click="cancel">取消</a-button>
        <a-button size="small" type="primary" @click="ok">确认</a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, toRaw, watch } from 'vue'
import VOutclick from 'common/directives/outclick'
import { deepCloneByJson } from 'common/utils/help'

// 配置项
const DEFAULT_CONFIG = {
  TYPE: 0,
  THOUSAND: true,
  DIGIT: 2,
}

const emits = defineEmits(['ok', 'update:open'])
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

const onOutclick = () => {
  emits('update:open', false)
}

const cType = ref(DEFAULT_CONFIG.TYPE) // 0 数字， 1 百分比
const cThousand = ref(DEFAULT_CONFIG.THOUSAND) // 千分位
const cDigit = ref(DEFAULT_CONFIG.DIGIT) // 保留小数位

const CODE_MAP = {
  1: { type: 0, digit: 0 },
  2: { type: 0, digit: 1 },
  3: { type: 0, digit: 2 },
  4: { type: 1, digit: 0 },
  5: { type: 1, digit: 1 },
  6: { type: 1, digit: 2 },
}

const modelValue = reactive({
  type: DEFAULT_CONFIG.TYPE,
  thousand: DEFAULT_CONFIG.THOUSAND,
  digit: DEFAULT_CONFIG.DIGIT,
})
const init = () => {
  let { type = 0, thousand = true, digit = 2 } = props.value

  modelValue.type = type
  modelValue.thousand = thousand
  modelValue.digit = digit
}

watch(
  () => props.open,
  v => {
    if (v) {
      init()
    }
  }
)

const onSelectChanged = value => {
  // 数字默认千分位，百分比默认不千分位
  modelValue.thousand = value === '0'
}

const ok = () => {
  emits('ok', deepCloneByJson(toRaw(modelValue)))
  cancel()
}

const cancel = () => {
  modelValue.type = DEFAULT_CONFIG.TYPE
  modelValue.thousand = DEFAULT_CONFIG.THOUSAND
  modelValue.digit = DEFAULT_CONFIG.DIGIT

  emits('update:open', false)
}
</script>

<style scoped lang="scss">
.custom--wrapper {
  position: relative;
  width: 300px;
  padding: 12px 15px;
  background-color: #fff;
  border: 1px solid #d8d8d8;
  z-index: 999;
}
.custom--type-select {
  :deep(.ant-select-selection-item) {
    text-align: left;
  }
}
</style>
