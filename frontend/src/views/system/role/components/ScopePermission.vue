<template>
  <a-table bordered :columns="columns" :data-source="list" :pagination="false">
    <template #bodyCell="{ text, column, record }">
      <template v-if="column.dataIndex === 'permissionName'">
        {{ text }}
        <a-tooltip v-if="record.remark" :title="record.remark">
          <InfoCircleOutlined style="margin-left: 4px" />
        </a-tooltip>
      </template>

      <template v-if="column.dataIndex === 'scope'">
        <span
          class="item"
          v-for="scope in record.scopes"
          :key="scope.id"
          style="margin-right: 16px">
          <a-radio
            :name="record.module + ':' + record.permission"
            :value="scope.code"
            :disabled="disabled"
            :checked="modelValue.includes(scope.code)"
            @click="onCheckChange">
            {{ scope.name }}
          </a-radio>
          <a-tooltip v-if="scope.comment" :title="scope.comment">
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
    width: 240,
    customCell: (row, index) => {
      const prev = list.value[index - 1]

      if (row.module === prev?.module) {
        return {
          rowSpan: 0,
        }
      } else {
        const tp = list.value.filter(t => t.module === row.module)

        return {
          rowSpan: tp.length || 1,
        }
      }
    },
  },
  {
    title: '权限',
    dataIndex: 'permissionName',
    width: 240,
  },
  {
    title: '分析范围',
    dataIndex: 'scope',
  },
])

const list = ref([])

const transformDatasource = list => {
  const res = list.reduce((acc, cur) => {
    const { module, moduleName, permission, permissionName, remark } = cur
    const gourpKey = module + '_' + permission

    if (!acc[gourpKey] || !acc[gourpKey]?.scopes.length) {
      acc[gourpKey] = {
        module,
        moduleName,
        permission,
        permissionName,
        remark,
        scopes: [],
      }
    }

    acc[gourpKey].scopes.push(cur)

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
  const { value, name } = e.target

  const group = name.split(':').slice(0, 2).join(':')
  const index = modelValue.value.findIndex(t => t.startsWith(group))

  if (index > -1) {
    const groupVal = modelValue.value[index]

    if (groupVal === value) {
      modelValue.value.splice(index, 1)
    } else {
      modelValue.value[index] = value
    }
  } else {
    modelValue.value.push(value)
  }
}
</script>

<style lang="scss" scoped>
.item {
  display: inline-block;
  width: 160px;
}
</style>
