<template>
  <section class="bottom-section">
    <header><h3 style="margin: 0 0 16px">查询条件</h3></header>

    <a-empty v-if="typeof item.id === 'undefined'" />

    <a-form
      v-else
      ref="formRef"
      :model="formState"
      :rules="formRules"
      :label-col="{ style: { width: '100px' } }">
      <a-form-item class="item" label="筛选类型">
        <a-select
          size="small"
          style="width: 120px"
          :options="filterTypeOptions"
          :value="formState.filterType"
          @change="onFilterTypeChange"></a-select>
        <a-checkbox
          style="margin-left: 10px"
          v-model:checked="formState.required"
          @change="onRequiredChange"
          >必填
        </a-checkbox>
        <a-checkbox
          v-if="formState.filterType === 'ENUM'"
          v-model:checked="formState.single"
          >单选
        </a-checkbox>
      </a-form-item>

      <a-form-item
        v-if="formState.filterType === 'TEXT' || formState.filterType === 'NUMBER'"
        class="item"
        label="筛选方式">
        <a-radio-group
          :options="filterMethodsOptions"
          v-model:value="formState.filterMethod"
          @change="onFilterMethodChange">
        </a-radio-group>
      </a-form-item>

      <a-form-item
        v-if="formState.filterType === 'ENUM'"
        class="item"
        label="枚举值来源">
        <a-radio-group v-model:value="formState.enumResourceType">
          <a-radio value="AUTO">自动解析</a-radio>
          <a-radio value="MANUAL">手工录入</a-radio>
        </a-radio-group>

        <EnumResource
          v-if="formState.enumResourceType === 'MANUAL'"
          :dataSource="formState.enumList_input"
          @ok="onEnumInputOk">
          <a>输入枚举值</a>
        </EnumResource>
      </a-form-item>

      <a-form-item class="item" label="" name="value">
        <div class="flex align-center">
          <a-form-item-rest>
            <a-checkbox
              v-model:checked="formState.setDefault"
              @change="onDefaultChange">
              设置默认值
            </a-checkbox>
          </a-form-item-rest>

          <FilterRender
            v-if="formState.setDefault"
            class="flex-1"
            size="small"
            from="setting"
            :item="formState"
            v-model:value="formState.value" />
        </div>
      </a-form-item>
    </a-form>
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { RELATION } from '@/CONST.dict'
import { filterTypeOptions, filterMethodsOptions } from '../config'
import EnumResource from './EnumResource.vue'
import FilterRender from '../../components/FilterRender.vue'

const defaultValueItem = { operator: RELATION.EQUAL, value: '' }

const props = defineProps({
  item: {
    type: Object,
    default: () => ({}),
  },
})

// 表单数据
const formState = computed(() => props.item)
// 默认值的校验
const valueValidator = rule => {
  const { required, filterType, value = [] } = formState.value

  if (!required) return Promise.resolve()

  const errorMessage = rule.message

  // 日期筛选
  if (filterType === 'TIME') {
    return value?.date?.length ? Promise.resolve() : Promise.reject(errorMessage)
  }

  // 文本\数值
  if (filterType === 'TEXT' || filterType === 'NUMBER') {
    if (!value.length || value.some(t => !t.value.trim?.().length)) {
      return Promise.reject(errorMessage)
    } else {
      return Promise.resolve()
    }
  } else {
    return value.length || value.trim?.().length
      ? Promise.resolve()
      : Promise.reject(errorMessage)
  }
}
// 表单校验规则
const formRules = {
  value: [
    {
      required: formState.value.required,
      message: '请输入默认值',
      validator: valueValidator,
    },
  ],
}

// 筛选类型改变
const onFilterTypeChange = type => {
  // 本文类型和数值类型间的切换只改变类型
  if (
    (formState.value.filterType === 'TEXT' ||
      formState.value.filterType === 'NUMBER') &&
    (type === 'TEXT' || type === 'NUMBER')
  ) {
    formState.value.filterType = type

    return
  }

  // 枚举需要设置枚举值来源
  if (type === 'ENUM') {
    formState.value.enumResourceType = formState.value.enumResourceType ?? 'AUTO'
    formState.value.value = []
  }

  formState.value.filterType = type
  setValue(true)
}

// 必填校验
const onRequiredChange = e => {
  const checked = e.target.checked
  formRules.value[0]['required'] = checked

  if (checked) {
    formState.value.setDefault = true
  }
}
// 必填改变时触发校验
watch(
  () => formState.value.required,
  checked => {
    if (!checked) {
      formRef.value?.clearValidate(['value'])
    } else {
      formRef.value?.validateFields(['value'])
    }
  }
)

// 筛选方式改变
const onFilterMethodChange = e => {
  // 设置文本和数值的默认值
  setTextNumberValue()
}

// 枚举手工录入
const onEnumInputOk = (listStr = '[]') => {
  const list = JSON.parse(listStr)

  formState.value.enumList = formState.value.enumList_input = list.map(v => ({
    label: v.desc,
    value: v.name,
  }))
}

// 设置默认值改变
const onDefaultChange = () => {
  setValue(true)
}

/**
 * 设置默认值
 * @param {boolean} reset 是否重置
 */
const setValue = (reset = false) => {
  switch (formState.value.filterType) {
    case 'CUSTOM':
      formState.value.value = []
      break
    case 'ENUM':
      formState.value.value = []
      break
    case 'TIME':
      formState.value.value = { mode: 0 }
      break
    case 'TEXT':
    case 'NUMBER':
      setTextNumberValue(reset)
      break
    default:
      break
  }
}

/**
 * 设置文本和数值的默认值
 * @param {boolean} reset 是否重置
 */
const setTextNumberValue = reset => {
  const { filterMethod, value } = formState.value
  const first = reset
    ? { ...defaultValueItem }
    : value[0]
    ? { ...value[0] }
    : { ...defaultValueItem }

  if (filterMethod === RELATION.OR || filterMethod === RELATION.AND) {
    formState.value.value = [first, { ...defaultValueItem }]
  } else {
    formState.value.value = [first]
  }
}

const formRef = ref(null)
const validate = async e => {
  return formRef.value
    .validate()
    .then(() => {
      return true
    })
    .catch(err => {
      console.error('err', err)

      return false
    })
}

defineExpose({ validate })
</script>

<style lang="scss" scoped>
.item {
  margin-bottom: 12px;
}
</style>
