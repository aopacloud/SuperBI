<template>
  <a-dropdown
    trigger="click"
    :overlayStyle="{ width: '140px', minWidth: 'initial' }"
    v-model:open="open">
    <div class="property-dropdown-trigger" :class="{ open: open }">
      <slot></slot>

      <DownOutlined class="dropdown-trigger-icon" />
    </div>
    <template #overlay>
      <a-menu>
        <!-- selectable :selectedKeys="selectedKeys" -->
        <a-sub-menu key="group" title="日期分组">
          <div class="list">
            <div
              class="item"
              style="width: 100px"
              v-for="item in weekBefore"
              :key="item.value"
              :class="{ active: selectedKeys.includes(item.value) }"
              @click="onMenuClick(item.value)">
              <span v-if="item.value !== WEEK">{{ item.label }}</span>
            </div>

            <div
              class="item"
              style="padding-right: 30px"
              :class="{ active: selectedKeys.includes('WEEK') }">
              <div>
                周 <RightOutlined style="position: absolute; right: 8px; top: 8px" />
              </div>
              <div class="item-sublist" style="width: 150px">
                <div
                  class="item"
                  v-for="item in week.children"
                  :key="item.value"
                  :class="{ active: selectedKeys.includes(item.value) }"
                  @click="onMenuClick(item.value)">
                  {{ item.label }}
                </div>
              </div>
            </div>

            <div
              class="item"
              v-for="item in weekAfter"
              :key="item.value"
              :class="{ active: selectedKeys.includes(item.value) }"
              @click="onMenuClick(item.value)">
              <span v-if="item.value !== WEEK">{{ item.label }}</span>
            </div>
          </div>
        </a-sub-menu>
        <a-sub-menu v-if="curDateDisplayOptions.length" key="display" title="日期显示">
          <div class="list" style="width: 250px">
            <div
              class="item"
              v-for="item in curDateDisplayOptions"
              :key="item.value"
              :class="{ active: selectedKeys.includes(item.value) }"
              @click="onMenuClick(item.value)">
              {{ item.label }}
            </div>
          </div>
        </a-sub-menu>
      </a-menu>
    </template>
  </a-dropdown>
</template>

<script setup>
import { ref, computed, inject, watchEffect } from 'vue'
import { CheckOutlined, DownOutlined, RightOutlined } from '@ant-design/icons-vue'
import {
  dateGroupOptions,
  dateDisplayOptions,
  WEEK,
  DEFAULT_WEEK_DISPLAY,
  DAY,
  DEFAULT_DAY_DISPLAY,
  toContrastFiled,
  DEAULT_RATIO_TYPE,
} from '@/views/analysis/config'
import { dateGroupTypeMap } from '@/views/analysis/components/config'
import { CATEGORY } from '@/CONST.dict.js'

const weekIndex = dateGroupOptions.findIndex(item => item.value === WEEK)
const weekBefore = dateGroupOptions.slice(0, weekIndex)
const week = dateGroupOptions[weekIndex]
const weekAfter = dateGroupOptions.slice(weekIndex + 1)
const weekChildrenValues = week.children.map(t => t.value)

const props = defineProps({
  field: {
    type: Object,
    default: () => ({}),
  },
  category: {
    type: String,
    default: CATEGORY.INDEX,
  },
})

const { compare: indexCompare } = inject('index')

const open = ref(false)
const selectedKeys = ref([])
watchEffect(() => {
  const { dateTrunc, firstDayOfWeek, viewModel } = props.field

  selectedKeys.value = [dateTrunc, firstDayOfWeek, viewModel].filter(Boolean)
})

// 当前的日期显示类型
const curDateDisplayOptions = computed(() => {
  const isWeek = selectedKeys.value.includes(WEEK)
  const isDay = selectedKeys.value.includes(DAY)

  if (!isWeek && !isDay) return []

  return dateDisplayOptions.filter(t => t.group === (isWeek ? WEEK : DAY))
})
// 当前日期类型的值
const dateDisplayValues = computed(() => curDateDisplayOptions.value.map(t => t.value))

// 更新同环比
const updateCompare = e => {
  const compare = indexCompare.get()
  const valuesList = dateGroupTypeMap[e]

  if (!compare) return

  if (!valuesList.includes(compare.type)) {
    compare.type = DEAULT_RATIO_TYPE
  }

  indexCompare.set(compare)
}

const onMenuClick = key => {
  if (dateGroupOptions.some(t => t.value === key)) {
    // 切换日期分组，清空同环比(更新同环比)
    if (toContrastFiled(props.field) && !selectedKeys.value.includes(key)) {
      indexCompare.set()
      // updateCompare(key)
    }

    if (key === DAY) {
      selectedKeys.value = [key, , DEFAULT_DAY_DISPLAY]
    } else {
      selectedKeys.value = [key]
    }
  } else {
    const preKeys = selectedKeys.value

    // 是否是周几到周几
    const weekChild = preKeys.find(t => weekChildrenValues.includes(t))

    // 周的分组
    if (weekChildrenValues.includes(key)) {
      // 切换日期分组到周，清空同环比(更新同环比)
      if (toContrastFiled(props.field) && !preKeys.includes(WEEK)) {
        indexCompare.set()
        // updateCompare(WEEK)
      }

      // 当前的日期显示
      const item =
        preKeys.find(t => dateDisplayValues.value.includes(t)) || DEFAULT_WEEK_DISPLAY

      selectedKeys.value = [WEEK, key, item]
    } else {
      if (key.startsWith(WEEK)) {
        selectedKeys.value = [WEEK, weekChild, key]
      } else {
        // 日的显示
        selectedKeys.value = [DAY, , key]
      }
    }
  }

  const [a, b, c] = selectedKeys.value

  props.field.dateTrunc = a
  props.field.firstDayOfWeek = b
  props.field.viewModel = c
}
</script>

<style lang="scss" scoped>
.property-menu {
  .menu-item-with-submenu {
    padding: 0;
  }
}
.week-item-checkicon {
  float: right;
  margin-top: 4px;
}

.property-dropdown-trigger {
  display: inline-flex;
  &.open {
    .dropdown-trigger-icon {
      transform: rotate(180deg);
    }
  }
}
.dropdown-trigger-icon {
  margin-left: 12px;
  transition: all 0.2s;
}

.item {
  position: relative;
  padding: 5px 12px 5px 30px;
  cursor: pointer;
  transition: all 0.2s;
  &.active {
    &::after {
      content: '';
      position: absolute;
      top: 15px;
      left: 10px;
      display: table;
      width: 5.71428571px;
      height: 9.14285714px;
      border: 2px solid #1677ff;
      border-top: 0;
      border-left: 0;
      transform: rotate(45deg) scale(1) translate(-50%, -50%);
    }
  }
  &:hover {
    background-color: rgba(0, 0, 0, 0.04);

    & > .item-sublist {
      display: block;
    }
  }
  & > .item-sublist {
    display: none;
    position: absolute;
    left: 100%;
    transform: translateX(10px);
    top: 0;
    padding: 4px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12),
      0 9px 28px 8px rgba(0, 0, 0, 0.05);
    &::before {
      content: '';
      width: 10px;
      height: 100%;
      position: absolute;
      opacity: 0;
      left: 0;
      transform: translateX(-100%);
    }
  }
}
</style>
