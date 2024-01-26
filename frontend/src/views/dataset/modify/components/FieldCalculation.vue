<template>
  <a-input-group compact style="display: inline-flex; width: 100%; align-items: center">
    <a-select class="flex-1" v-model:value="type">
      <a-select-option value="">原值</a-select-option>
      <a-select-option value="+">+</a-select-option>
      <a-select-option value="-">-</a-select-option>
      <a-select-option value="*">x</a-select-option>
      <a-select-option value="/">÷</a-select-option>
    </a-select>
    <a-input-number
      v-if="type"
      style="width: 120px"
      v-model:value.number="cValue"
      @blur="onBlur" />
  </a-input-group>
</template>

<script setup>
import { computed, ref, watch, watchEffect, nextTick } from 'vue'
import { message } from 'ant-design-vue'

const props = defineProps({
  value: {
    type: String,
  },
  maxLength: {
    type: Number,
    default: 8,
  },
})
const emits = defineEmits(['update:value', 'change'])

const type = ref('')
const cValue = ref('')

watchEffect(() => {
  const val = props.value

  if (!val || !val.length) {
    type.value = ''
    cValue.value = ''
  } else {
    const [t, ...v] = val

    type.value = t
    cValue.value = +v.join('')
  }
})

const modelValue = computed(() => {
  return !type.value.length ? '' : type.value + cValue.value
})
watch(modelValue, val => {
  emits('update:value', val)
  emits('change', val)
})

const onBlur = e => {
  const value = e.target.value
  const max = props.maxLength

  if (max < 1) return

  if (value.length > max) {
    cValue.value = value.slice(0, max)

    message.info(`最多${max}位数(包含小数点)`)

    if (isNaN(cValue.value) || isNaN(value)) {
      nextTick(() => {
        cValue.value = ''
        e.target.value = ''
      })
    }
  }
}
</script>
