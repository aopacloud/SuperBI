<template>
  <div class="basisRatio-setting">
    <section class="setting-panel-section">
      <div class="setting-panel-item" v-if="chartType === 'table'">
        显示内容
      </div>
      <a-radio-group
        class="setting-panel-content"
        :disabled="disabled"
        v-model:value="cMode"
      >
        <a-radio :value="0">数值</a-radio>
        <a-radio :value="1">差值</a-radio>
        <a-radio :value="2">差值百分比</a-radio>
      </a-radio-group>
    </section>

    <section v-if="chartType === 'table'" class="setting-panel-section">
      <div class="setting-panel-item">显示单位</div>
      <a-radio-group
        class="setting-panel-content"
        :disabled="disabled"
        v-model:value="cMerge"
      >
        <a-radio :value="false">单独显示</a-radio>
        <a-radio :value="true">合并显示</a-radio>
      </a-radio-group>
    </section>
  </div>
</template>

<script setup>
import { ref, watchEffect, computed, watch, onUnmounted } from 'vue'
import { COMPARE } from '@/CONST.dict'

const props = defineProps({
  chartType: {
    type: String
  },
  disabled: {
    type: Boolean,
    default: false
  },
  value: {
    type: Object,
    default: () => ({})
  }
})

const cMode = ref(0)
const cMerge = ref(false)

const initWatcher = watchEffect(() => {
  const { mode, merge } = props.value

  cMode.value = mode ?? COMPARE.MODE.ORIGIN
  cMerge.value = merge ?? COMPARE.MERGE.FALSE
})
onUnmounted(initWatcher)

const modelValue = computed(() => {
  return {
    mode: cMode.value,
    merge: cMerge.value
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
    margin: 0 0 8px;
    & ~ p {
      margin-top: 16px;
    }
  }
}
</style>
