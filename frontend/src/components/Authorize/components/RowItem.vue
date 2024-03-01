<template>
  <div class="row-item">
    <Compact block style="display: flex">
      <!-- 过滤类型 -->
      <a-select
        class="row-select"
        style="width: 100px"
        v-model:value="item.type"
        @change="onTypeChange">
        <a-select-option value="ENUM">枚举过滤</a-select-option>
        <a-select-option value="TEXT">条件过滤</a-select-option>
      </a-select>

      <!-- 字段 -->
      <a-select
        class="row-select"
        style="flex: 1; max-width: 167px; overflow: hidden"
        placeholder="请选择字段"
        show-search
        :filter-option="fieldsFilterOption"
        v-model:value="item.field"
        @change="onFieldChanged">
        <a-select-option v-for="item in fields" :key="item.name">
          {{ item.displayName }}
        </a-select-option>
      </a-select>

      <!-- 行（字段枚举值） -->
      <a-select
        v-if="item.type === 'ENUM'"
        style="flex: 1; width: 167px; overflow: hidden"
        placeholder="请选择行"
        mode="multiple"
        v-model:value="item.value">
        <a-select-option v-for="item in enumList" :key="item">{{ item }}</a-select-option>
      </a-select>

      <template v-else>
        <!-- 操作符 -->
        <a-select style="width: 90px" v-model:value="item.operator">
          <a-select-option v-for="(value, key) in operators" :key="key">
            {{ value }}
          </a-select-option>
        </a-select>

        <a-input style="flex: 1" placeholder="请输入" v-model:value="item.value" />
      </template>

      <a-button
        v-if="level === 1"
        type="text"
        :icon="h(FilterOutlined)"
        :disabled="level === 1 && !removable"
        @click="split" />
      <a-button
        v-else
        type="text"
        :icon="h(PlusOutlined)"
        :disabled="!addable"
        @click="add" />

      <a-button
        type="text"
        :icon="h(MinusOutlined)"
        :disabled="level === 1 && !removable"
        @click="remove" />
    </Compact>
  </div>
</template>

<script setup>
import { h, computed, ref, shallowRef } from 'vue'
import { Compact } from 'ant-design-vue'
import { FilterOutlined, MinusOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { RELATION } from '@/CONST.dict'
import { operatorMap } from '@/views/dataset/config.field'
import { postAnalysisQuery } from '@/apis/analysis'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
  item: {
    type: Object,
    default: () => {
      return {
        type: 'ENUM',
        field: undefined,
        fieldType: 'TIME',
        operator: RELATION.EQUAL,
      }
    },
  },
  level: {
    type: Number,
    default: 1,
  },
  maxLevel: {
    type: Number,
  },
  // 可删除的
  removable: {
    type: Boolean,
    default: true,
  },
  // 可增加的
  addable: {
    type: Boolean,
    default: true,
  },
})

const operators = computed(() => {
  return operatorMap[props.item.type]
})

const emits = defineEmits(['update:item', 'split', 'add', 'remove'])

const fields = computed(() => props.dataset.fields || [])
const enumListLoading = ref(false)
const enumList = shallowRef([])
const onFieldChange = async e => {
  try {
    enumListLoading.value = true

    const field = fields.value.find(t => t.name === e)
    const { rows } = await postAnalysisQuery({
      type: 'SINGLE_FIELD',
      datasetId: props.dataset.id,
      fromSource: 'temporary',
      dimensions: [field],
    })

    enumList.value = rows.map(t => t[0] + '').filter(Boolean)
  } catch (error) {
    console.error('枚举值获取错误', error)
  } finally {
    enumListLoading.value = false
  }
}
const onFieldChanged = () => {
  if (props.item.type === 'ENUM') {
    props.item.value = []
  }
}

const fieldsFilterOption = (input, option) => {
  return option.children()[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

watch(
  () => props.item.field,
  e => {
    if (!e || props.item.type !== 'ENUM') return

    onFieldChange(e)
  },
  { immediate: true }
)

const onTypeChange = e => {
  props.item.operator = e === 'TEXT' ? RELATION.EQUAL : undefined
  props.item.value = e === 'TEXT' ? undefined : []
}

const split = () => {
  emits('split')
}

const add = () => {
  emits('add')
}
const remove = () => {
  emits('remove')
}
</script>

<style lang="scss" scoped>
.row-item:not(:only-child) {
  margin-bottom: 10px;
}

// .item:last-child {
//   margin-bottom: 0;
// }
.action {
  font-size: 16px;
  text-align: center;
}
.row-select {
  :deep(.ant-select-selector) {
    height: 100% !important;
    .ant-select-selection-search,
    .ant-select-selection-item {
      display: flex;
      align-items: center;
    }
  }
}
</style>
