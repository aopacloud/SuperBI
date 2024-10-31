<template>
  <a-space>
    <template v-for="field in dataSource">
      <DatePickers
        v-if="isDate(field)"
        pickerOnly
        :placeholder="[field.displayName, '']"
        :size="config.size"
        :value="cValue[field.name]"
        :show-time="
          isTime_HHMMSS(field.dataType)
            ? { valueFormat: 'HH:mm:ss', format: 'HH:mm:ss' }
            : false
        "
        @ok="e => onChange(field, e)"
        @clear="onChange(field, [])"
      />
      <a-select
        v-else
        style="width: 240px"
        allow-clear
        show-search
        mode="multiple"
        :size="config.size"
        :filterOption="filterOption"
        :placeholder="field.displayName"
        :value="cValue[field.name]"
        @change="e => onChange(field, e)"
      >
        <a-select-option
          v-for="item in field._dynamicOptions"
          :key="item"
          :label="item"
        >
          {{ item }}
        </a-select-option>
      </a-select>
    </template>
  </a-space>
</template>

<script setup>
import { ref, watchEffect } from 'vue'
import DatePickers from 'common/components/DatePickers/index.vue'
import { isTime_HHMMSS } from '@/views/dataset/config.field'

const emits = defineEmits(['change'])

const modelValue = defineModel('modelValue', { type: Object, default: {} })

const props = defineProps({
  dataSource: {
    type: Array,
    default: () => []
  },
  config: {
    type: Object,
    default: () => ({
      size: 'middle'
    })
  }
})

const filterOption = (s, o) =>
  o.label.toLowerCase().indexOf(s.toLowerCase()) >= 0

const isDate = field => field.dataType?.includes('TIME')

const cValue = ref({})

watchEffect(() => {
  cValue.value = { ...modelValue.value }
})

const onChange = (field = {}, v) => {
  cValue.value[field.name] = v

  emits('change', field, v)
}
</script>

<style lang="scss" scoped></style>
