<template>
  <a-table :columns="columns" :data-source="list" :pagination="false">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'funcs'">
        <span
          class="item"
          v-for="func in record.funcs"
          :key="func.id"
          style="margin-right: 16px">
          <a-checkbox
            :value="func.code"
            :disabled="disabled"
            :checked="modelValue.includes(func.code)"
            @change="onCheckChange">
            {{ func.name }}
          </a-checkbox>
          <a-tooltip v-if="func.comment" :title="func.comment">
            <InfoCircleOutlined />
          </a-tooltip>
        </span>
      </template>
    </template>
  </a-table>
</template>

<script setup>
import { ref, watch, watchEffect } from 'vue'
import { InfoCircleOutlined } from '@ant-design/icons-vue'

const emits = defineEmits(['update:value'])

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false,
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
  value: {
    type: Array,
    default: () => [],
  },
})

const columns = ref([
  {
    title: '模块',
    dataIndex: 'moduleName',
    width: 360,
  },
  {
    title: '功能点',
    dataIndex: 'funcs',
  },
])

const list = ref([])

const transformDatasource = list => {
  const res = list.reduce((acc, cur) => {
    const { module, moduleName } = cur

    if (!acc[module]) {
      acc[module] = { module, moduleName, funcs: [] }
    }

    acc[module].funcs.push(cur)

    return acc
  }, {})

  return Object.values(res)
}

const modelValue = ref([])
watch(
  modelValue,
  e => {
    emits('update:value', e)
  },
  { deep: true }
)

watchEffect(() => {
  list.value = transformDatasource(props.dataSource)

  modelValue.value = props.value
})

const onCheckChange = e => {
  const { value, checked } = e.target
  if (checked) {
    modelValue.value.push(value)
  } else {
    const index = modelValue.value.findIndex(item => item === value)
    modelValue.value.splice(index, 1)
  }
}
</script>

<style lang="scss" scoped>
.item {
  display: inline-block;
  width: 120px;
}
</style>
