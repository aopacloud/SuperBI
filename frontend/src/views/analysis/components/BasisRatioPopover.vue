<template>
  <div class="basis-ratio-setting---wrapper">
    <a-tooltip
      v-if="disabled"
      title="分组无日期类型字段，该功能暂不可用"
      placement="topRight">
      <span class="vs-icon disabled">VS</span>
    </a-tooltip>

    <a-popover v-else placement="bottomRight" trigger="click">
      <a-tooltip :mouseEnterDelay="0">
        <span class="vs-icon">VS</span>
      </a-tooltip>

      <template #content>
        <div class="basis-ratio-setting--content">
          <p>显示内容</p>
          <a-radio-group v-model:value="content">
            <a-radio value="0">数值</a-radio>
            <a-radio value="1">差值</a-radio>
            <a-radio value="2">差值百分比</a-radio>
          </a-radio-group>
          <template v-if="chartType === 'table'">
            <p>显示单位</p>
            <a-radio-group v-model:value="unity">
              <a-radio value="0">单独显示</a-radio>
              <a-radio value="1">合并显示</a-radio>
            </a-radio-group>
          </template>
        </div>
      </template>
    </a-popover>
  </div>
</template>

<script setup>
import { Popover, Radio, RadioGroup, Tooltip } from 'ant-design-vue'
import { ref, watchEffect, watch, computed, toRaw, unref, onUnmounted } from 'vue'

const props = defineProps({
  chartType: {
    type: String,
    default: 'table',
  },
  disabled: {
    type: Boolean,
  },
  value: {
    type: Object,
    default: () => ({}),
  },
})

const content = ref('0')
const unity = ref('0')

const initWatcher = watchEffect(() => {
  const { content: _content = '1', unity: _unity = '1' } = props.value

  content.value = _content
  unity.value = _unity
})
onUnmounted(initWatcher)

const modelValue = computed(() => {
  return {
    content: content.value,
    unity: unity.value,
  }
})
const emits = defineEmits(['ok'])
watch(modelValue, val => {
  emits('ok', val)
})
</script>

<style scoped lang="scss">
.vs-icon {
  display: inline-block;
  width: 30px;
  height: 20px;
  line-height: 20px;
  margin-left: 12px;
  border-radius: 2px;
  background-color: #409eff;
  text-align: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.1s;
  &.disabled {
    cursor: not-allowed;
    background-color: #d7d7d7;
  }
}

.basis-ratio-setting--content {
  p {
    margin-bottom: 3px;
    & ~ p {
      margin-top: 10px;
    }
  }
}
</style>
