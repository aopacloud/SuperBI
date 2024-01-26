<template>
  <section class="field-table-section">
    <header class="flex justify-between align-center" style="padding: 10px 12px">
      <div>
        共计 {{ propertyFields.length + indexFields.length }} 个字段，其中维度
        {{ propertyFields.length }} 个，指标 {{ indexFields.length }} 个
        <a-button
          style="margin-left: 8px"
          size="small"
          type="primary"
          ghost
          @click="sortModalHandler">
          行排序
        </a-button>
      </div>

      <a-space>
        <a-input-search
          placeholder="输入过滤"
          allow-clear
          v-model:value="keyword"
          @search="onKeywordFilter">
          <template #addonBefore>
            <a-select v-model:value="filterStatus" @change="onKeywordFilter">
              <a-select-option value="">所有字段</a-select-option>
              <a-select-option value="VIEW">显示字段</a-select-option>
              <a-select-option value="HIDE">隐藏字段</a-select-option>
            </a-select>
          </template>
        </a-input-search>
        <a-button type="primary" @click="insert">新增字段</a-button>
      </a-space>
    </header>

    <!-- v-resize="initTableHeight" -->
    <main class="main-table" ref="mainRef">
      <a-spin :spinning="loading">
        <vxe-table
          ref="tableRef"
          class="fields-table"
          row-id="name"
          showFooter
          show-overflow
          :height="tableHeight"
          :scroll-y="{ enabled: true, gt: 0, oSize: 10 }"
          :tree-config="{
            rowField: 'name',
            parentField: 'parentName',
            transform: true,
            expandAll: true,
          }"
          :row-class-name="onRowClassname"
          @checkbox-change="onSelectChange"
          @checkbox-all="onSelectAllChange"
          :footerMethod="footerRenderMethod">
          <vxe-column type="checkbox" treeNode width="120">
            <template #footer>
              <span style="margin-left: 4px">已选中 {{ selectKeys.length }} 个</span>
            </template>
          </vxe-column>

          <!-- 字段名 -->
          <vxe-column field="name" title="字段名" width="200">
            <template #default="{ row }">{{ row.name }}</template>
            <template #footer>
              <a @click="clearSelect">取消选中</a>
            </template>
          </vxe-column>

          <!-- 展示名称 -->
          <vxe-column field="displayName" title="展示名称" width="240">
            <template #default="{ row }">
              <textarea
                class="input autoheight-input"
                v-if="row.id !== PropertyField.id && row.id !== IndexField.id"
                placeholder="请输入展示名称"
                :maxlength="50"
                v-model.lazy="row['displayName']"
                @change="e => updateField(row.name, 'displayName', e.target.value)" />
            </template>
          </vxe-column>

          <!-- 字段类型 -->
          <vxe-column field="dataType" title="字段类型" width="260">
            <template #default="{ row }">
              <i
                :class="['iconfont', getFieldTypeIcon(row.dataType)['icon']]"
                :style="{
                  marginRight: '8px',
                  color: getFieldTypeIcon(row.dataType)['color'],
                }"></i>
              <a-cascader
                v-if="row.id !== PropertyField.id && row.id !== IndexField.id"
                :allowClear="false"
                :options="getFieldDataTypeOptions(row)"
                v-model:value="row['dataType']"
                @change="e => updateField(row.name, 'dataType', e)"></a-cascader>
            </template>
            <template #footer>
              <a-cascader
                style="margin-left: 24px"
                placeholder="请选择"
                :options="dataTypeOptions"
                @change="e => onMultipleChange(e, 'dataType')"></a-cascader>
            </template>
          </vxe-column>

          <!-- 数据类型 -->
          <vxe-column field="category" title="数据类型" width="110">
            <template #default="{ row }">
              <a-select
                v-if="row.id !== PropertyField.id && row.id !== IndexField.id"
                class="w-100p"
                :options="categoryOptions"
                :disabled="versionJs.ViewsDatasetModify.canotChange(row.name)"
                v-model:value="row.category"
                @change="e => onCategoryChange(e, row)">
              </a-select>
            </template>
            <template #footer>
              <a-select
                class="w-100p"
                placeholder="请选择"
                :options="categoryOptions"
                @change="e => onMultipleChange(e, 'category')">
              </a-select>
            </template>
          </vxe-column>

          <!-- 数据格式 -->
          <vxe-column field="dataFormat" title="数据格式" width="180">
            <template #default="{ row }">
              <div>
                <a-select
                  :ref="el => (customTriggerRefs[row.name] = el)"
                  v-if="
                    row.id !== PropertyField.id &&
                    row.id !== IndexField.id &&
                    row.category === IndexField.id
                  "
                  class="w-100p"
                  :value="row['dataFormat']"
                  @change="e => onDataFormatCodeChange(e, row)">
                  <a-select-option
                    v-for="item in formatterOptions"
                    :key="item.value"
                    :value="item.value"
                    @click="onFormatterOptionClick(row, item)">
                    {{
                      item.value === FORMAT_CUSTOM_CODE &&
                      row['dataFormat'] === FORMAT_CUSTOM_CODE
                        ? row.customFormatterLabel || item.label
                        : item.label
                    }}
                  </a-select-option>
                </a-select>
              </div>
            </template>
            <template #footer>
              <a-select
                :ref="el => (customTriggerRefs['__multiple'] = el)"
                class="w-100p"
                placeholder="请选择"
                :value="customInfo.dataFormat"
                @change="e => onMultipleChange(e, 'dataFormat')">
                <a-select-option
                  v-for="item in formatterOptions"
                  :key="item.value"
                  :value="item.value"
                  @click="onFormatterOptionClick({}, item, true)">
                  {{
                    item.value === FORMAT_CUSTOM_CODE
                      ? customInfo.customFormatterLabel || item.label
                      : item.label
                  }}
                </a-select-option>
              </a-select>
            </template>
          </vxe-column>

          <!-- 聚合方式 -->
          <vxe-column field="aggregator" title="聚合方式" width="130">
            <template #default="{ row }">
              <template
                v-if="
                  row.id !== PropertyField.id &&
                  row.id !== IndexField.id &&
                  row.category === IndexField.id
                ">
                <a-select
                  v-if="row.aggregator !== SUMMARY_DEFAULT"
                  class="w-100p"
                  :options="summaries"
                  v-model:value="row['aggregator']"
                  @change="e => updateField(row.name, 'aggregator', e)">
                </a-select>

                <div v-else style="text-align: center">-</div>
              </template>
            </template>
            <template #footer>
              <a-select
                class="w-100p"
                placeholder="请选择"
                :options="summaries"
                @change="e => onMultipleChange(e, 'aggregator')">
              </a-select>
            </template>
          </vxe-column>

          <!-- 字段计算 -->
          <vxe-column field="computeExpression" title="字段计算" width="200">
            <template #default="{ row }">
              <FieldCalculation
                v-if="
                  row.id !== PropertyField.id &&
                  row.id !== IndexField.id &&
                  row.category === IndexField.id
                "
                class="w-100p"
                v-model:value="row['computeExpression']"
                @change="e => updateField(row.name, 'computeExpression', e)" />
            </template>
            <template #footer>
              <FieldCalculation
                class="w-100p"
                placeholder="请选择"
                @update:value="e => onMultipleChange(e, 'computeExpression')" />
            </template>
          </vxe-column>

          <!-- 字段说明 -->
          <vxe-column field="description" title="字段说明" minWidth="300">
            <template #default="{ row }">
              <input
                class="input"
                v-if="row.id !== PropertyField.id && row.id !== IndexField.id"
                v-model="row['description']"
                @change="e => updateField(row.name, 'description', e.target.value)" />
            </template>
          </vxe-column>

          <!-- 操作 -->
          <vxe-column field="action" fixed="right" title="操作" width="180">
            <template #default="{ row }">
              <a-space
                v-if="
                  row.id !== PropertyField.id &&
                  row.id !== IndexField.id &&
                  versionJs.ViewsDatasetModify.isRowEnableAction(row)
                "
                :size="12">
                <a v-if="row.status === 'HIDE'" @click="onHideStatusChange(row, 'VIEW')">
                  显示
                </a>
                <a v-else @click="onHideStatusChange(row, 'HIDE')">隐藏</a>

                <template v-if="row.type === 'ADD'">
                  <a @click="edit(row)">编辑</a>
                  <a @click="copy(row)">复制</a>
                  <a style="color: red" @click="del(row)">删除</a>
                </template>
              </a-space>
            </template>
            <template #footer>
              <a-space :size="12">
                <a @click="onMultipleChange('HIDE', 'status')">隐藏</a>
                <a @click="onMultipleChange('VIEW', 'status')">显示</a>
              </a-space>
            </template>
          </vxe-column>
        </vxe-table>
      </a-spin>
    </main>

    <!-- 字段新增 -->
    <FieldInsertModal
      v-model:open="modifyOpen"
      :dataset="dataset"
      :fields="[PropertyField, ...propertyFields, IndexField, ...indexFields]"
      :init-data="modifyInfo"
      @ok="onFieldInserted" />

    <!-- 排序 -->
    <RowSortModal
      v-model:open="sortOpen"
      :propertyField="PropertyField"
      :indexField="IndexField"
      :data-source="allList"
      @ok="onRowSorted" />

    <!-- 自定义格式 -->
    <CustomFormatter
      :style="customStyle"
      :value="customInfo.config"
      v-model:open="customOpen"
      @ok="onCustomFormatterOk" />
  </section>
</template>

<script setup lang="jsx">
import { onMounted, ref, shallowReactive, watch, computed } from 'vue'
import { Modal } from 'ant-design-vue'
import CustomFormatter from '@/components/CustomFormatter/index.vue'
import FieldCalculation from './FieldCalculation.vue'
import RowSortModal from './RowSortModal.vue'
import FieldInsertModal from './FieldInsertModal.vue'
import {
  SUMMARY_DEFAULT,
  SUMMARY_PROPERTY_DEFAULT,
  SUMMARY_INDEX_DEFAULT,
  dataTypeOptions,
  categoryOptions,
  summaryOptions,
  formatterOptions,
  FORMAT_DEFAULT_CODE,
  FORMAT_CUSTOM_CODE,
  displayCustomFormatterLabel,
  getFieldTypeIcon,
} from '@/views/dataset/config.field'
import { copyText, deepClone } from 'common/utils/help'
import { findLastIndex } from 'common/utils/compatible'
import { CATEGORY } from '@/CONST.dict.js'
import { versionJs } from '@/versions'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
  loading: Boolean,
  originFields: {
    type: Array,
    default: () => [],
  },
})

const summaries = computed(() => summaryOptions.filter(t => !t.hidden))

const getFieldDataTypeOptions = field => {
  return dataTypeOptions.map(t => {
    return {
      ...t,
      disabled:
        versionJs.ViewsDatasetModify.canotChange(field.name) && t.value !== 'TIME',
    }
  })
}

const tableRef = ref(null)
// const fields = ref([])

// 维度字段 - 父级
const PropertyField = shallowReactive({
  name: '维度',
  id: CATEGORY.PROPERTY,
})
// 指标字段 - 父级
const IndexField = shallowReactive({
  name: '指标',
  id: CATEGORY.INDEX,
})

// 所有数据
const allList = ref([])

// 维度字段
const propertyFields = computed(() =>
  allList.value.filter(t => t.category === PropertyField.id)
)
// 指标字段
const indexFields = computed(() =>
  allList.value.filter(t => t.category === IndexField.id)
)

// 渲染数据
const list = ref([])

// 转换数据表字段
const _initOriginFieldd = l => {
  return props.originFields.map(item => {
    const isIndex = item.dataType === 'NUMBER'

    return {
      ...item,
      originDataType: item.dataType,
      type: 'ORIGIN', // 字段来源： ORIGIN 原始字段, ADD 新增字段
      category: isIndex ? IndexField.id : PropertyField.id,
      displayName: item.description || item.name,
      parentName: isIndex ? IndexField.name : PropertyField.name,
      aggregator: isIndex ? SUMMARY_INDEX_DEFAULT : SUMMARY_PROPERTY_DEFAULT, // 聚合
      dataFormat: FORMAT_DEFAULT_CODE, // 格式
      status: 'VIEW', // 显状态: 'VIEW' 显示， 'HIDE' 隐藏
    }
  })
}

// 根据自定义格式配置生成显示文字
const _showCustomFormatLabel = t => {
  t.customFormatterLabel = displayCustomFormatterLabel(t.customFormatConfig)

  return t
}

/**
 * 合并字段 - 编辑时将源数据表中新增的字段合并进数据集字段中
 * @param {[]} origin
 * @param {[]} target
 */
const _mergeFields = (origin = [], target = []) => {
  const result = target.slice()

  for (let i = 0; i < origin.length; i++) {
    const originItem = origin[i]

    if (result.every(item => item.name !== originItem.name)) {
      result.push(originItem)

      continue
    }
  }

  return result.map(t => {
    const dataType = t.dataType

    if (!dataType.includes('TIME')) {
      return t
    } else {
      const [time, ...res] = dataType.split('_')

      return {
        ...t,
        dataType: [time, res.join('_')],
      }
    }
  })
}

// 初始化
const init = async () => {
  clearFilter()
  // 初始化原表中字段，添加默认值
  const originFields = _initOriginFieldd()

  // 合并后的字段
  const fields = _mergeFields(originFields, props.dataset.fields)

  // 处理字段数据
  const pFields = fields
    .filter(t => t.category === PropertyField.id)
    .map(t => ((t.parentName = PropertyField.name), t))
  const iFields = fields
    .filter(t => t.category === IndexField.id)
    .map(t => ((t.parentName = IndexField.name), t))
    .map(_showCustomFormatLabel)

  // 全局数据
  allList.value = [...pFields, ...iFields]
  // 渲染数据
  list.value = [{ ...PropertyField }, ...pFields, { ...IndexField }, ...iFields]

  tableRef.value.reloadData(list.value)
}

const renderTable = (data = allList.value) => {
  const [ps, is] = data.reduce(
    (acc, cur) => {
      if (cur.category === PropertyField.id) {
        acc[0].push(cur)
      }

      if (cur.category === IndexField.id) {
        acc[1].push(cur)
      }

      return acc
    },
    [[], []]
  )

  list.value = [{ ...PropertyField }, ...ps, { ...IndexField }, ...is]
  tableRef.value.loadData(list.value)
}

const mainRef = ref(null)
const tableHeight = ref(0)
onMounted(() => {
  tableHeight.value = mainRef.value.offsetHeight
})

const initTableHeight = () => {
  tableHeight.value = mainRef.value.offsetHeight
}

// 选中的keys - name
const selectKeys = ref([])
watch(selectKeys, keys => {
  if (keys.length === 0) customInfo.value.isMultiple = false
})
const onSelectChange = ({ records }) => {
  selectKeys.value = records
    .filter(t => t.id !== PropertyField.id && t.id !== IndexField.id)
    .map(t => t.name)
  // 更新表尾渲染
  tableRef.value.updateFooter()
}
const onSelectAllChange = ({ checked, records }) => {
  if (checked) {
    selectKeys.value = records
      .filter(t => t.id !== PropertyField.id && t.id !== IndexField.id)
      .map(t => t.name)
  } else {
    selectKeys.value = []
  }

  tableRef.value.updateFooter()
}

const onRowClassname = ({ type, row, rowIndex, $rowIndex }) => {
  if (row.status === 'HIDE') {
    return 'row-visible--hidden'
  } else {
    return ''
  }
}

// 表尾渲染方法
const footerRenderMethod = ({ columns, data }) => {
  return selectKeys.value.length
    ? [
        columns.map(() => {
          return null
        }),
      ]
    : []
}
// 清除选中
const clearSelect = () => {
  selectKeys.value = []
  tableRef.value.clearCheckboxRow()
  tableRef.value.updateFooter()
}

const customOpen = ref(false)
const customStyle = ref({})
const customInfo = ref({})
const customTriggerRefs = ref({})
// 自定义格式选项事件
const onFormatterOptionClick = (row, item, isMultiple) => {
  if (item.value !== FORMAT_CUSTOM_CODE) return

  // 当前点击的dom
  const customTriggerRef = isMultiple
    ? customTriggerRefs.value['__multiple']
    : customTriggerRefs.value[row.name]

  if (!customTriggerRef.$el) return

  // 获取触发下拉框dom的几何信息
  const { left, top, bottom, height } = customTriggerRef.$el.getBoundingClientRect()

  const F_HEIGHT = 124 // CustomFormatter高度
  const bodyHeight = document.body.offsetHeight // 视窗高度
  const p =
    bottom + height + F_HEIGHT > bodyHeight
      ? { bottom: bodyHeight - bottom + height + 'px' }
      : { top: top + height + 'px' }

  // 设置自定义格式样式
  customStyle.value = {
    position: 'fixed',
    left: left + 'px',
    ...p,
  }

  customOpen.value = true

  if (isMultiple) {
    customInfo.value = {
      isMultiple: true,
      dataFormat: customInfo.value.dataFormat,
      config: {},
    }
  } else {
    customInfo.value = { ...row, config: JSON.parse(row.customFormatConfig || '{}') }
  }
}
// 确认自定义格式设置
const onCustomFormatterOk = (payload = {}) => {
  const _setSingleField = fieldName => {
    const item = list.value.find(item => item.name === fieldName)

    const _config = JSON.stringify(payload)
    const _configLabel = displayCustomFormatterLabel(payload)

    item.dataFormat = FORMAT_CUSTOM_CODE
    item.customFormatConfig = _config
    item.customFormatterLabel = _configLabel
  }

  // 批量设置
  if (customInfo.value.isMultiple) {
    selectKeys.value.forEach(key => {
      _setSingleField(key)

      customInfo.value.dataFormat = FORMAT_CUSTOM_CODE
      customInfo.value.customFormatterLabel = displayCustomFormatterLabel(payload)
    })
  } else {
    // 单个设置
    _setSingleField(customInfo.value.name)
  }
}

// 数据格式发生改变
const onDataFormatCodeChange = (val, row) => {
  if (val === FORMAT_CUSTOM_CODE) return

  row.dataFormat = val
  row.customFormatConfig = '{}'
  row.customFormatterLabel = ''
  updateField(row.name, 'dataFormat', val)
}

// 数据类型发生改变
const onCategoryChange = (val, row) => {
  row.parentName = val === PropertyField.id ? PropertyField.name : IndexField.name

  const index = allList.value.findIndex(t => t.name === row.name)

  allList.value.splice(index, 1)
  allList.value.push({ ...row })

  renderTable()
  clearSelect()
}

// 显示隐藏
const onHideStatusChange = (row, val) => {
  row.status = val

  updateField(row.name, 'status', val)
}

// 批量操作
const onMultipleChange = (val, key) => {
  // 批量格式化自定义触发时不更新
  const formatCustom = key === 'dataFormat' && val === FORMAT_CUSTOM_CODE

  selectKeys.value.forEach(name => {
    const item = list.value.find(t => t.name === name)

    if (!versionJs.ViewsDatasetModify.canChangeByField(name, key)) return

    if (
      item.category !== IndexField.id &&
      (key === 'dataFormat' || key === 'aggregator' || key === 'computeExpression')
    ) {
      return
    }

    if (key === 'aggregator' && item.aggregator === SUMMARY_DEFAULT) return

    if (key === 'category') {
      item.category = val
      item.parentName = val === PropertyField.id ? PropertyField.name : IndexField.name
      item.children = undefined

      const index = allList.value.findIndex(t => t.name === item.name)
      allList.value.splice(index, 1)
      allList.value.push({ ...item })
    } else if (key === 'dataFormat') {
      if (val !== FORMAT_CUSTOM_CODE) {
        customInfo.value.dataFormat = val
        customInfo.value.customFormatterLabel = ''

        item.dataFormat = val
        item.customFormatConfig = '{}'
        item.customFormatterLabel = ''
      }
    } else {
      item[key] = val
    }

    if (!formatCustom) updateField(name, key, val)
  })

  // 切换维度，需要重新刷新表格
  if (key === 'category') {
    renderTable()
    // 清除选中
    clearSelect()
  }
}

// 更新字段信息
const updateField = (name, key, val) => {
  // 更新源数据字段信息
  const item = allList.value.find(t => t.name === name)

  item[key] = val
}

// 过滤筛选
const filterStatus = ref('')
const keyword = ref('')
// 关键字筛选函数
const _keywordFilter = item => {
  const { name = '', displayName = '', description = '' } = item
  const s = keyword.value.trim()

  return s.trim().length === 0
    ? true
    : name.includes(s) || displayName.includes(s) || description.includes(s)
}
// 状态筛选函数
const _statusFilter = item => {
  return filterStatus.value === '' || item.status === filterStatus.value
}
const onKeywordFilter = () => {
  clearSelect()

  const noFilter = filterStatus.value === '' && keyword.value.trim() === ''

  const list = allList.value.filter(item => {
    return noFilter || (_statusFilter(item) && _keywordFilter(item))
  })

  renderTable(list)
}
const clearFilter = () => {
  keyword.value = ''
  filterStatus.value = ''
}

// 排序
const sortOpen = ref(false)
const sortModalHandler = () => {
  sortOpen.value = true
}
// 行排序确认
const onRowSorted = (sortedList = []) => {
  // Map => 字段名: order
  const sortedMap = sortedList.reduce((acc, pre) => {
    const { name, sort } = pre

    acc[name] = sort
    return acc
  }, {})

  allList.value = allList.value
    .map(item => {
      item.sort = sortedMap[item.name]

      return item
    })
    .sort((a, b) => a.sort - b.sort)

  renderTable()
}

// 新增、编辑、复制
const modifyOpen = ref(false)
const modifyInfo = ref({})
// 新增
const insert = () => {
  modifyOpen.value = true
  modifyInfo.value = { _mode: 'insert' }
}
// 编辑
const edit = row => {
  modifyOpen.value = true
  modifyInfo.value = { ...row, _mode: 'edit' }
}
// 复制
const copy = row => {
  const names = allList.value.filter(t => t.category === row.category).map(t => t.name)

  modifyOpen.value = true
  modifyInfo.value = {
    ...row,
    _mode: 'copy',
    name: copyText(row.name, { joinStr: '', exited: names }),
  }
}
// 字段插入、编辑、复制 确认
const onFieldInserted = payload => {
  const { _mode, category, name: payloadName } = payload
  // 插入
  if (_mode === 'insert') {
    if (category === PropertyField.id) {
      const allPIndex = findLastIndex(allList.value, t => t.category === PropertyField.id)

      allList.value.splice(allPIndex + 1, 0, payload)
    } else {
      const allIIndx = findLastIndex(allList.value, t => t.category === IndexField.id)

      allList.value.splice(allIIndx + 1, 0, { ...payload, id: Date.now() })
    }

    renderTable()
  } else if (_mode === 'edit') {
    // 编辑
    const allListItem = allList.value.find(t => t.name === payloadName)
    const listItem = list.value.find(t => t.name === payloadName)

    // 是否需要刷新表格
    let shouldUpdate = false
    for (const key in payload) {
      if (key in allListItem) {
        if (key === 'category' && allListItem[key] !== payload[key]) {
          shouldUpdate = true
        }

        allListItem[key] = payload[key]
      }

      if (shouldUpdate) continue

      if (key in listItem) {
        listItem[key] = payload[key]
      }
    }

    if (shouldUpdate) {
      renderTable()
    }
  } else if (_mode === 'copy') {
    // 复制
    const allIndex = allList.value.findIndex(t => t.id === payload.id)

    allList.value.splice(allIndex + 1, 0, { ...payload, id: Date.now() })
    renderTable()
  }
}
// 删除
const del = row => {
  Modal.confirm({
    title: '提示？',
    content: (
      <p>
        确定删除字段 <span style='color: #1677ff;font-weight: 600'>{row.name}</span>{' '}
        吗？删除后无法恢复，请谨慎操作
      </p>
    ),
    okText: '确定',
    onOk() {
      const allIndex = allList.value.findIndex(t => t.name === row.name)
      if (allIndex < 0) return

      allList.value.splice(allIndex, 1)
      renderTable()
    },
  })
}

const getTableData = () => {
  return deepClone(allList.value)
    .filter(t => t.id !== PropertyField.id && t.id !== IndexField.id)
    .map((t, i) => {
      delete t.children
      delete t.customFormatterLabel
      delete t.parentName

      if (Array.isArray(t.dataType)) {
        t.dataType = t.dataType.join('_').replace(/_$/, '')
      }

      return {
        ...t,
        sort: i,
      }
    })
}

defineExpose({
  init,
  getTableData,
})
</script>

<style lang="scss" scoped>
.field-table-section {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.main-table {
  flex: 1;
  overflow: auto;
}
.fields-table {
  :deep(.vxe-table--main-wrapper) {
    .vxe-body--row.row-visible--hidden {
      opacity: 0.35;
    }
  }

  .autoheight-input {
    position: absolute;
    top: 12px;
    left: 10px;
    right: 10px;
    width: initial !important;
    min-height: 32px;
    height: 32px;
    transition: height 0.2s !important;
    resize: none;
    &:focus {
      height: 100px;
      z-index: 1;
    }
  }
}
.w-100p {
  width: 100%;
}
</style>
