<template>
  <div>
    <ul class="list">
      <li
        class="item"
        v-for="item in presetsList"
        :key="`${item.type}_${item.value}`"
        :style="getItemStyle(item)"
        :class="{
          active: isItemActive(item),
        }"
        @click="itemClick(item)">
        {{ item.label }}
      </li>
    </ul>

    <Teleport v-if="isCustomActive" :to="`#pick-custom-placeholder-${id}`">
      <div class="custom">
        <select class="select no-border">
          <option value="pass">过去</option>
        </select>
        <select class="select no-border" v-model="customValue">
          <option
            v-for="item in customRangeOptions"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </option>
        </select>
        <select class="select no-border" v-model="customType">
          <option v-for="item in customMode" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, watch, watchEffect, computed } from 'vue'
import { presetOptions, customRangeOptions, customMode } from '../config'
import { getStartDate, getEndDate } from '../utils'
import dayjs from 'dayjs'

const emits = defineEmits(['click'])
const props = defineProps({
  id: Number,
  value: {
    type: Array,
    default: () => [],
  },
  extra: {
    type: Object,
    default: () => ({}),
  },
  utcOffset: {
    type: Number,
    default: 8,
    validator: str => {
      return typeof str === 'number'
    },
  },
  // 一些配置参数
  options: {
    type: Object,
    default: () => ({}),
  },
})
// 自定义值
const customValue = ref(-1)
const customType = ref('day')
// 自定义激活
const isCustomActive = ref(false)

// 预设列表
const presetsList = computed(() => {
  return presetOptions
    .filter(t => {
      if (props.options.dt === false && t.type === 'dt') return false

      if (t.hidden) {
        if (typeof t.hidden === 'function') {
          return !t.hidden()
        }

        return !Boolean(t.hidden)
      }

      return true
    })
    .map(t => ({ ...t, active: false }))
})

const getItemStyle = item => {
  return {
    flexBasis: item.col ? item.col - 2 + '%' : '100%',
  }
}
// 激活状态
const isItemActive = item => {
  if (props.extra.dt) {
    return item.type === 'dt'
  } else if (isCustomActive.value) {
    return item.type === 'custom'
  } else {
    return item.active
  }
}

watch(
  () => props.extra,
  e => {
    const { isCustom, current } = e

    isCustomActive.value = isCustom

    if (current) {
      const [type, offset] = current.toLowerCase().split('_')
      customType.value = type
      customValue.value = +offset
    }
  },
  { immediate: true, deep: true }
)

watch([customValue, customType], val => {
  const [v, t] = val

  emits('click', { offset: v, type: t, custom: isCustomActive.value })
})

watchEffect(() => {
  const [start, end] = props.value

  // 未选中日期
  if (!start || !end) {
    presetsList.value.forEach(item => (item.active = false))

    return
  }

  presetsList.value.forEach(item => {
    const { type, value } = item

    // 有数的一天或自定义， 不需要判断
    if (type == 'dt' || type === 'custom') return

    const s = getStartDate({ type, offset: value }, props.utcOffset)
    const e = getEndDate({ type, offset: value }, props.utcOffset)
    const sFormat = s.format('YYYY-MM-DD')
    const eFormat = e.format('YYYY-MM-DD')

    const startFormat = dayjs(start).format('YYYY-MM-DD')
    const endFormat = dayjs(end).format('YYYY-MM-DD')

    if (startFormat === sFormat && endFormat === eFormat) {
      item.active = true
    } else {
      item.active = false
    }
  })
})

const itemClick = item => {
  const { type, value } = item

  isCustomActive.value = type === 'custom'

  if (type === 'custom') {
    customValue.value = -1
    customType.value = 'day'
  } else {
    emits('click', { type, offset: value })
  }
}
</script>

<style lang="scss" scoped>
.list {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin: 0;
  font-size: 12px;
  text-align: center;
}

.item {
  background-color: #f3f9ff;
  line-height: 30px;
  margin-top: 10px;
  border-radius: 4px;
  transition: all 0.2s;
  cursor: pointer;
  &.active {
    background-color: #1677ff;
    color: #fff;
    font-weight: 600;
  }
}

.custom {
  display: inline-block;
}
</style>
