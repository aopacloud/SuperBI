<template>
  <div class="basis-ratio">
    <p v-if="chartType === 'table'">显示内容</p>
    <a-radio-group :disabled="disabled" v-model:value="cMode">
      <a-radio :value="0">数值</a-radio>
      <a-radio :value="1">差值</a-radio>
      <a-radio :value="2">差值百分比</a-radio>
    </a-radio-group>
    <template v-if="chartType === 'table'">
      <p>显示单位</p>
      <a-radio-group :disabled="disabled" v-model:value="cMerge">
        <a-radio :value="false">单独显示</a-radio>
        <a-radio :value="true">合并显示</a-radio>
      </a-radio-group>
    </template>
  </div>
</template>

<script setup>
import { ref, watchEffect, computed, watch, onUnmounted } from 'vue'
const props = defineProps({
  chartType: {
    type: String,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  value: {
    type: Object,
    default: () => ({}),
  },
})

const cMode = ref(0)
const cMerge = ref(false)

const initWatcher = watchEffect(() => {
  const { mode, merge } = props.value

  cMode.value = mode
  cMerge.value = merge
})
onUnmounted(initWatcher)

const modelValue = computed(() => {
  return {
    mode: cMode.value,
    merge: cMerge.value,
  }
})
const emits = defineEmits(['update:value'])
watch(modelValue, val => {
  emits('update:value', val)
})
</script>

<style lang="scss" scoped>
.basis-ratio {
  & > p {
    margin-bottom: 6px;
    &:first-child {
      margin: 0 0 6px;
    }
  }
}
</style>
