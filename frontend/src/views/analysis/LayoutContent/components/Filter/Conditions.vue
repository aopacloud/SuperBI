<template>
  <div class="conditions">
    <div class="list-wrapper" :class="{ 'only-one': conditions.length === 1 }">
      <span
        class="relation"
        :class="{ active: relation === 'AND' }"
        @click="handleRelationToggle"
        >{{ relationLabel }}</span
      >
      <div class="list">
        <div class="item" v-for="(item, index) in conditions">
          <!-- 多层级递归 -->
          <!-- <FilterConditions
            v-if="item.children"
            v-model:relation="item.relation"
            :conditions="item.children" /> -->

          <a-input-group compact>
            <a-select
              :style="{
                width:
                  item.operator !== 'IS_NOT_NULL' && item.operator !== 'IS_NULL'
                    ? '85px'
                    : '270px',
              }"
              :get-popup-container="node => node.parentNode"
              v-model:value="item.operator">
              <a-select-option v-for="(label, key) in operators" :key="key" :value="key">
                {{ label }}
              </a-select-option>
            </a-select>
            <a-input
              v-show="item.operator !== 'IS_NOT_NULL' && item.operator !== 'IS_NULL'"
              style="width: 60%"
              placeholder="字符或值"
              v-model:value="item.value" />

            <a-button
              type="text"
              :disabled="conditions.length < 2"
              :icon="h(MinusCircleOutlined)"
              @click="handlerRemove(index)"></a-button>
          </a-input-group>
        </div>
      </div>
    </div>

    <slot name="add" v-bind="{ add: handleAdd, list: conditions }">
      <a
        style="display: inline-block; margin-top: 6px"
        v-show="conditions.length < 10"
        @click="handleAdd"
        ><plus-square-outlined /> 过滤条件</a
      >
    </slot>
  </div>
</template>

<script setup>
import { h, computed } from 'vue'
import { MinusCircleOutlined, PlusSquareOutlined } from '@ant-design/icons-vue'
import FilterConditions from './Conditions.vue'
import { operatorMap } from '@/views/dataset/config.field'
import { getRandomKey } from 'common/utils/help'

const emits = defineEmits(['update:conditions', 'update:relation'])
const props = defineProps({
  dataType: {
    type: String,
    default: 'TEXT',
  },
  relation: {
    type: String,
    default: 'OR',
  },
  conditions: {
    type: Array,
    default: () => [],
  },
  max: {
    type: Number,
    default: 10,
  },
})

const operators = computed(() => {
  return operatorMap[props.dataType]
})
const relationLabel = computed(() => {
  return props.relation === 'OR' ? '或' : props.relation === 'AND' ? '且' : '-'
})

const handleRelationToggle = () => {
  emits('update:relation', props.relation === 'OR' ? 'AND' : 'OR')
}

const handleAdd = () => {
  if (props.conditions.length >= props.max) return

  props.conditions.push({
    _id: getRandomKey(),
    operator: 'EQUAL',
    value: undefined,
  })
}

const handlerRemove = index => {
  props.conditions.splice(index, 1)
}
</script>

<style lang="scss" scoped>
.list-wrapper {
  display: flex;
  align-items: center;
}
.relation {
  width: 24px;
  height: 24px;
  line-height: 24px;
  border: 1px solid #d8d8d8;
  border-radius: 2px;
  text-align: center;
  color: #3d90ff;
  transition: all 0.2s;
  cursor: pointer;
  &:hover,
  &.active {
    color: #fff;
    background-color: #3d90ff;
  }
}
.list {
  position: relative;
  flex: 1;
  margin-left: calc(var(--w) * 2);
  --w: 10px;
  --lH: 34px;
  &::before {
    content: '';
    position: absolute;
    left: calc(var(--w) * -2);
    top: 50%;
    height: 1px;
    width: var(--w);
    background-color: #ddd;
  }
  &::after {
    content: '';
    position: absolute;
    width: 1px;
    top: calc(var(--lH) * 0.5);
    left: calc(var(--w) * -1);
    bottom: calc(var(--lH) * 0.5);
    background-color: #ddd;
  }
}
.item {
  position: relative;
  &:before {
    content: '';
    position: absolute;
    left: calc(var(--w) * -1);
    top: 50%;
    height: 1px;
    width: var(--w);
    background-color: #ddd;
  }
  & + .item {
    margin-top: 5px;
  }
  &-remove {
    display: inline-flex !important;
    align-items: center;
    justify-content: center;
    margin-left: 5px;
    color: #aaa;
    cursor: pointer;
    &.disabled {
      color: rgba(0, 0, 0, 0.25);
      cursor: not-allowed;
      pointer-events: none;
    }
  }
}
</style>
