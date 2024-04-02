<template>
  <a-form class="tab-form" :labelCol="{ style: { width: '120px' } }">
    <section class="section">
      <div class="title">分区信息</div>
      <a-form-item label="是否分区">
        <a-radio-group
          v-model:value="formState.hasPartition"
          @change="onHasPartitionChange">
          <a-radio :value="true">有分区</a-radio>
          <a-radio :value="false">无分区</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item
        v-if="!!formState.hasPartition"
        label="分区字段"
        v-bind="validateInfos.partitionFields">
        <a-select
          style="width: 320px"
          mode="multiple"
          show-search
          placeholder="请选择分区字段"
          :filterOption="filterOption"
          v-model:value="formState.partitionFields">
          <a-select-option
            v-for="field in enableFields"
            :key="field.name"
            :label="field.displayName">
            {{ field.displayName }}
          </a-select-option>
        </a-select>
        <span style="margin-left: 6px; color: rgba(0, 0, 0, 0.45)"
          >若有多个分组字段可配置多个
        </span>
      </a-form-item>

      <a-form-item label="分区范围">
        <Datepicker
          style="width: 320px"
          placeholder="请选择分区范围"
          :disabled="!formState.hasPartition || !formState.partitionFields.length"
          :moda="+(formState.partitionRanges[0].conditions[0].timeType === 'EXACT')"
          :hms="formState.partitionRanges[0].conditions[0].timeParts"
          :value="formState.partitionRanges[0].conditions[0].args"
          :offset="formState.partitionRanges[0].conditions[0].args"
          :showTIme="
            formState.partitionRanges[0].dataType === 'TIME_YYYYMMDD_HHMMSS'
          "
          :extra="{
            dt: formState.partitionRanges[0].conditions[0].useLatestPartitionValue,
            current: formState.partitionRanges[0].conditions[0]._this,
            isCustom: formState.partitionRanges[0].conditions[0]._current,
            until: formState.partitionRanges[0].conditions[0]._until,
          }"
          @ok="onDatepickerOk"
          @clear="onPartitionRangeClear" />
        <span style="margin-left: 6px; color: rgba(0, 0, 0, 0.45)">
          用户查询数据的时候只能查到对应范围下的数据
        </span>
      </a-form-item>
    </section>

    <section class="section">
      <div class="title">分区信息</div>
    </section>
  </a-form>
</template>

<script setup>
import { reactive, shallowRef, computed, toRaw } from 'vue'
import { Form, message } from 'ant-design-vue'
import Datepicker from 'common/components/DatePickers/index.vue'
import { versionJs } from '@/versions'

const emits = defineEmits(['update:dataset'])
const props = defineProps({
  dataset: { type: Object, default: () => ({}) },
  fields: { type: Array, default: () => [] },
})

// 隐藏的字段不显示
const enableFields = computed(() =>
  props.fields
    .filter(field => field.status !== 'HIDE')
    .filter(t => t.dataType.includes('TIME'))
)

const filterOption = (input, option) =>
  option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0

const formState = reactive({
  hasPartition: true,
  partitionFields: [],
  partitionRanges: [],
})
watch(formState, state => {
  const { hasPartition, partitionFields, partitionRanges } = state

  formRules.partitionFields[0]['required'] = hasPartition

  emits('update:dataset', {
    ...props.dataset,
    _extraConfig: {
      ...extraConfig.value,
      hasPartition,
      partitionFields,
      partitionRanges: partitionRanges.map((t, i) => {
        const { name, dataType } =
          enableFields.value.find(t => t.name === partitionFields[i]) || {}

        return {
          name,
          dataType,
          conditions: t.conditions,
        }
      }),
    },
  })
})

const formRules = reactive({
  partitionFields: [
    { required: !!formState.hasPartition, message: '分区字段不能为空' },
  ],
})

// 额外配置信息
const extraConfig = computed(() => props.dataset._extraConfig || {})

const initFormState = () => {
  const {
    hasPartition = true,
    partitionFields = [],
    partitionRanges = [],
  } = extraConfig.value

  formState.hasPartition = hasPartition

  // 无分区
  if (!formState.hasPartition) {
    formState.partitionRanges = [{ conditions: [{}] }] // 为兼容模板上的取值
  } else {
    // 初始化
    if (typeof extraConfig.value.partitionFields === 'undefined') {
      const dtField = enableFields.value.find(versionJs.ViewsDatasetModify.isDt)

      formState.partitionFields = dtField ? [dtField['name']] : []
    } else {
      formState.partitionFields = partitionFields.filter(t =>
        enableFields.value.some(f => f.name === t)
      )
    }

    if (typeof extraConfig.value.partitionRanges === 'undefined') {
      formState.partitionRanges = [{ conditions: [{}] }]
    } else {
      formState.partitionRanges = partitionRanges
    }
  }
}

const onHasPartitionChange = e => {
  const value = e.target.value

  if (!value) {
    formState.partitionFields = []
    formState.partitionRanges = [{ conditions: [{}] }]
  } else {
    const dtField = enableFields.value.find(versionJs.ViewsDatasetModify.isDt)

    formState.partitionFields = dtField ? [dtField['name']] : []
  }
}

watch(enableFields, initFormState, { deep: true, immediate: true })

const onDatepickerOk = (e, item) => {
  const { moda, date, offset, hms, extra } = e

  formState.partitionRanges = formState.partitionRanges.map(range => {
    return {
      ...range,
      conditions: [
        {
          useLatestPartitionValue: extra.dt,
          timeType: moda === 1 ? 'EXACT' : 'RELATIVE',
          args: moda === 1 ? date : offset,
          timeParts: hms,
          _this: extra.current,
          _current: extra.isCustom,
          _until: extra.until,
        },
      ],
    }
  })
}
const onPartitionRangeClear = conditions => {
  formState.partitionRanges[0].conditions = [{}]
}

const {
  validate: formValidate,
  resetFields,
  validateInfos,
} = Form.useForm(formState, formRules)

const validate = async () => {
  return new Promise((resolve, reject) => {
    formValidate()
      .then(r => {
        resolve({
          ...toRaw(formState),
          partitionRanges: extraConfig.value.partitionRanges,
        })
      })
      .catch(e => {
        message.error(e.errorFields[0].errors[0])
        reject(e)
      })
  })
}

defineExpose({ validate })
</script>

<style lang="scss" scoped>
.tab-form {
  margin: 30px 0 0 20px;
}
.title {
  position: relative;
  padding: 4px 12px;
  margin-bottom: 12px;
  font-size: 16px;
  color: #1677ff;
  background-color: #f2f2f2;
  &:before {
    content: '';
    position: absolute;
    left: 0;
    top: 2px;
    bottom: 2px;
    width: 3px;
    background-color: currentColor;
  }
}
.section {
  &:not(:last-child) {
    margin-bottom: 60px;
  }
}
</style>
