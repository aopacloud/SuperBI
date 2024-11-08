<template>
  <section class="setting-panel-section">
    <p class="setting-panel-item">柱图类型</p>
    <ul class="setting-panel-item bar-list">
      <li
        class="bar-item"
        v-for="item in barStyle"
        :key="item.value"
        :class="{
          active:
            cValue.bar.type === item.value ||
            (!cValue.bar.type && item.value === 'bar-clustered')
        }"
        @click="toggleBarType(item.value)"
      >
        <div class="img"><SvgIcon :name="item.icon" /></div>
        {{ item.label }}
      </li>
    </ul>
    <div
      v-if="cValue.bar.type === 'stacked'"
      class="setting-panel-item"
      style="margin-top: 16px"
    >
      <a-checkbox v-model:checked="cValue.bar.flat"> 指标并列展示 </a-checkbox>
    </div>
  </section>
</template>

<script setup>
import { watch, watchEffect } from 'vue'
import { defaultChartOptions } from '@/views/analysis/defaultOptions'
import { deepClone } from '@/common/utils/help'
import { barStyleOptions } from './config'

const barStyle = barStyleOptions.filter(t => !t.hidden)

const emits = defineEmits(['update:modelValue'])
const props = defineProps({
  modelValue: {
    type: Object
  }
})

const cValue = ref({})

watch(
  cValue,
  e => {
    emits('update:modelValue', e)
  },
  { deep: true }
)

watchEffect(() => {
  if (typeof props.modelValue === 'undefined') {
    cValue.value = deepClone(defaultChartOptions.style)
  } else {
    cValue.value = props.modelValue
  }
})

const toggleBarType = e => {
  cValue.value.bar.type = e
  cValue.value.bar.flat = undefined
}
</script>

<style lang="scss" scoped>
.bar-list {
  display: flex;
  align-items: center;
  justify-content: space-evenly;
  margin: 0;
}
.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 5px;
  cursor: pointer;

  .img {
    display: inline-flex;
    width: 36px;
    height: 36px;
    margin-bottom: 4px;
    align-items: center;
    justify-content: center;
    border: 2px solid transparent;
    border-radius: 4px;
    .anticon {
      font-size: 26px;
    }
  }
  &:hover,
  &.active {
    color: #1677ff;
    .img {
      border-color: #1677ff;
    }
  }
}
</style>
