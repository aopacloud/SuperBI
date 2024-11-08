<template>
  <a-form class="tab-form" :labelCol="{ style: { width: '120px' } }">
    <section class="section">
      <div class="title">数据申请</div>
      <a-form-item label="是否开放申请">
        <a-radio-group v-model:value="formState.enableApply">
          <a-radio :value="1">全员可申请</a-radio>
          <a-radio :value="0">不可申请</a-radio>
        </a-radio-group>

        <template v-if="!formState.enableApply" #help>
          当数据集不可申请时，无权限的用户在全部列表下看不见该数据集
        </template>
      </a-form-item>
    </section>

    <section class="section">
      <div class="title">数据查询</div>

      <a-form-item label="过滤条件">
        <a-radio-group v-model:value="formState.force" @change="onForceChange">
          <a-radio :value="true">强制过滤</a-radio>
          <a-radio :value="false">不强制</a-radio>
        </a-radio-group>

        <template #extra>{{ forceLabel }}</template>
      </a-form-item>

      <template v-if="formState.force">
        <a-form-item style="margin-bottom: -18px" label=" " :colon="false">
          <a-space class="f-fields">
            <div class="f-name required">过滤字段</div>
            <div class="f-value-type">值选择</div>
            <div class="f-default">默认选中</div>
            <div></div>
          </a-space>
        </a-form-item>

        <a-form-item label=" " :colon="false" v-bind="validateInfos.filters">
          <a-form-item
            style="margin-bottom: -36px"
            v-for="(item, index) in formState.filters"
            :key="item._id"
            :colon="false">
            <a-space align="start">
              <a-form-item class="f-name">
                <a-select
                  placeholder="请选择字段"
                  show-search
                  :filterOption="filterOption"
                  v-model:value="item.name"
                  @change="e => onFieldNameChange(e, index)">
                  <a-select-option
                    v-for="f in enableFields"
                    :key="f.name"
                    :label="f.displayName">
                    {{ f.displayName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item class="f-value-type">
                <a-select
                  v-model:value="item.filterMode"
                  @change="e => onDefaultModeChange(e, item)">
                  <a-select-option value="single">单个值</a-select-option>
                  <a-select-option value="multiple">多个值</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item class="f-default">
                <a-dropdown
                  trigger="click"
                  :overlayStyle="{ minWidth: 'initial' }"
                  v-model:open="item._open">
                  <a-input
                    style="width: 100%"
                    class="i-input"
                    readonly
                    :placeholder="!item.name ? '请先选择字段' : ''"
                    :value="
                      displayFilter(
                        {
                          ...item,
                          filterMode: item.filterMode,
                        },
                        { format: 'YYYY-MM-DD' }
                      )
                    ">
                    <template #suffix>
                      <div class="i-suffix">
                        <DownOutlined class="i-suffix-down" />
                        <CloseCircleFilled
                          class="i-suffix-close"
                          v-if="
                            item.conditions?.[0].useLatestPartitionValue ||
                            item.conditions?.[0].args?.length > 0
                          "
                          @click.stop="clearCondition(item)" />
                      </div>
                    </template>
                  </a-input>

                  <template v-if="item.name" #overlay>
                    <FilterVue
                      v-model:open="item._open"
                      :value="item.defaultValue"
                      :single="item.filterMode === 'single'"
                      :dataset="dataset"
                      :field="item">
                    </FilterVue>
                  </template>
                </a-dropdown>
              </a-form-item>

              <a-button
                style="margin-top: 5px"
                size="small"
                type="text"
                :disabled="formState.filters.length <= 1"
                :icon="h(MinusCircleOutlined)"
                @click="remove(index)" />
            </a-space>
          </a-form-item>
          <a-button
            style="padding-left: 0"
            size="small"
            type="link"
            :icon="h(PlusOutlined)"
            @click="add">
            过滤字段
          </a-button>
        </a-form-item>
      </template>
    </section>
  </a-form>
</template>

<script setup>
import { computed, h, reactive, watch, toRaw } from 'vue'

import { Form, message } from 'ant-design-vue'
import {
  PlusOutlined,
  DownOutlined,
  MinusCircleOutlined,
  CloseCircleFilled,
} from '@ant-design/icons-vue'
import FilterVue from '@/components/Filter/index.vue'
import { displayFilter } from '@/components/Filter/utils'
import { getRandomKey } from 'common/utils/string'
import { versionJs } from '@/versions'
import dayjs from 'dayjs'

const emits = defineEmits(['update:dataset'])
const props = defineProps({
  dataset: { type: Object, default: () => ({}) },
  fields: { type: Array, default: () => [] },
})
const createFilterItem = info => {
  return {
    _id: getRandomKey(),
    _open: false,
    filterMode: 'multiple',
    conditions: [{ args: [] }],
    ...info,
  }
}

// 隐藏的字段不显示
const enableFields = computed(() =>
  props.fields.filter(field => field.status !== 'HIDE')
)

const formState = reactive({
  enableApply: 1,
  force: true,
  filters: [],
})
watch(formState, state => {
  const { enableApply, force, filters } = state

  emits('update:dataset', {
    ...props.dataset,
    enableApply,
    _extraConfig: {
      ...extraConfig.value,
      force,
      filters: toRaw(filters),
    },
  })
})

// 强制过滤文本
const forceLabel = computed(() => {
  if (formState.force) {
    return '用户查询数据集的时候必须对以下字段进行过滤'
  } else {
    return '不强制过滤可能导致用户查询条件使用不当带来的数据歧义或者性能问题，建议至少强制过滤日期分区'
  }
})

const filtersValidator = (rule, value) => {
  if (!formState.force) return Promise.resolve()

  if (!value.length) return Promise.reject(rule.message)

  if (value.some(item => !item.name)) {
    return Promise.reject(rule.message)
  } else {
    const names = [...new Set(value.map(t => t.name))]

    if (names.length !== value.length) {
      return Promise.reject(rule.message)
    }

    return Promise.resolve()
  }
}

const formRules = reactive({
  filters: [
    {
      message: '请检查过滤条件',
      validator: filtersValidator,
    },
  ],
})

const onForceChange = e => {
  const value = e.target.value

  if (!value) {
    formState.filters = []
  } else {
    const dtField = enableFields.value.find(versionJs.ViewsDatasetModify.isDt)
    if (!dtField) {
      formState.filters = []
    } else {
      let offset = [1, 1]
      if (dtField.latestPartitionValue) {
        offset = getOffsetByDate(dtField.latestPartitionValue)
      }

      formState.filters = [
        createFilterItem({
          ...dtField,
          filterMode: 'multiple',
          conditions: [{ timeType: 'RELATIVE', args: offset }],
        }),
      ]
    }
  }
}

const onFieldNameChange = (e, index) => {
  const info = enableFields.value.find(f => f.name === e)
  const item = formState.filters[index]

  formState.filters[index] = {
    ...item,
    ...info,
    conditions: item.dataType !== info.dataType ? [{ args: [] }] : item.conditions,
  }
}

const filterOption = (input, option) =>
  option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0

const onDefaultModeChange = (v, field) => {
  if (v === 'single') {
    field.conditions = [field.conditions[0]]
  }
}

const clearCondition = field => {
  field.conditions = [{}]
}

// 额外配置信息
const extraConfig = computed(() => props.dataset._extraConfig || {})

const getOffsetByDate = date => {
  const offset = dayjs().diff(date, 'day')

  return [offset, offset]
}

const initFormState = () => {
  const { enableApply } = props.dataset
  const { force = true, filters } = extraConfig.value

  formState.enableApply = enableApply
  formState.force = force ?? true

  if (force) {
    if (typeof filters === 'undefined') {
      const dtField = enableFields.value.find(versionJs.ViewsDatasetModify.isDt)

      if (!dtField) {
        formState.filters = []
      } else {
        let offset = [1, 1]
        if (dtField.latestPartitionValue) {
          offset = getOffsetByDate(dtField.latestPartitionValue)
        }

        formState.filters = [
          createFilterItem({
            ...dtField,
            filterMode: 'multiple',
            conditions: [{ timeType: 'RELATIVE', args: offset }],
          }),
        ]
      }
    } else {
      formState.filters = filters
    }
  }
}

watch(enableFields, initFormState, { deep: true, immediate: true })

const {
  validate: formValidate,
  resetFields,
  validateInfos,
} = Form.useForm(formState, formRules)

const validate = async () => {
  return new Promise((resolve, reject) => {
    formValidate()
      .then(r => {
        resolve(toRaw(formState))
      })
      .catch(e => {
        message.error(e.errorFields[0].errors[0])
        reject(e)
      })
  })
}

defineExpose({ validate })

const add = () => {
  formState.filters.push(createFilterItem())
}
const remove = index => {
  formState.filters.splice(index, 1)
}
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

.f-name {
  width: 240px;
}
.f-value-type {
  width: 100px;
}
.f-default {
  width: 360px;
}

.i-suffix {
  position: relative;
  &-close {
    position: absolute;
    top: 4px;
    left: 0;
    cursor: pointer;
    opacity: 0;
  }
}
.i-input:hover {
  .i-suffix-down:not(:only-child) {
    visibility: hidden;
  }
  .i-suffix-close {
    opacity: 1;
  }
}

.required {
  &::before {
    content: '*';
    margin-right: 2px;
    font-size: 14px;
    color: #ff4d4f;
    font-size: 14px;
    font-family: SimSun, sans-serif;
  }
}
</style>
