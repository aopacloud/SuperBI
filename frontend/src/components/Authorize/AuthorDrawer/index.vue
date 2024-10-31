<template>
  <a-drawer
    width="1000"
    :title="title"
    :open="open"
    :maskClosable="false"
    :body-style="{ display: 'flex', flexDirection: 'column', paddingRight: 0 }"
    @close="close"
  >
    <h3 v-if="!multiple" style="margin-top: 0">
      {{ resourceLabel }}: {{ initData.name || '-' }}
    </h3>

    <div class="flex" style="height: 100%">
      <div class="flex-column" style="width: 250px">
        <SelectList
          style="height: 450px"
          :loading="selectListLoading"
          :disabled="selectedDisabled"
          :keyField="isUserTab ? 'username' : 'id'"
          :labelField="isUserTab ? 'aliasName' : 'name'"
          :data-source="selectListDataSource"
          v-model:value="selected"
        >
          <template #tabs>
            <a-tabs
              class="select-tabs"
              size="small"
              v-model:activeKey="selectTabKey"
            >
              <a-tab-pane key="USER" tab="用户"></a-tab-pane>
              <a-tab-pane key="GROUP" tab="用户组"></a-tab-pane>
            </a-tabs>
          </template>
        </SelectList>

        <div class="select-preview flex-1">
          <p v-show="userSelected.length > 0">
            已选中的用户：
            <a v-if="!selectedDisabled" @click="userSelected = []">清空</a>
          </p>
          <a-tag
            class="tag"
            color="blue"
            v-for="(username, i) in userSelected"
            :key="username"
            :closable="!selectedDisabled"
            @close.prevent="handleSelectClose(i, 'USER')"
          >
            {{
              displaySelected(
                selectListUsers,
                t => t.username === username,
                t => t.aliasName
              ) || username
            }}
          </a-tag>

          <p v-show="groupSelected.length > 0">
            已选中的用户组：
            <a v-if="!selectedDisabled" @click="groupSelected = []">清空</a>
          </p>
          <a-tag
            class="tag"
            color="blue"
            v-for="(id, i) in groupSelected"
            :key="id"
            :closable="!selectedDisabled"
            @close.prevent="handleSelectClose(i, 'GROUP')"
          >
            {{
              displaySelected(
                selectListGroups,
                t => t.id === id,
                t => t.name
              ) || id
            }}
          </a-tag>
        </div>
      </div>

      <a-form
        class="flex-1 scroll"
        style="height: 100%; padding-right: 24px"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item
          v-if="currentResource === 'DASHBOARD' && multiple"
          class="form-item-column"
          label="看板列表"
          :colon="false"
          v-bind="validateInfos.dashboardIds"
        >
          <!-- 看板列表 -->
          <DashboardChoosedList
            :disabled="selectedDisabled || multipleDashboardDisabled"
            :workspaceId="workspaceId"
            v-model:value="formState.dashboardIds"
            @change="onDashboardIdsChange"
          />
        </a-form-item>

        <!-- 看板中数据集开关 -->
        <a-form-item
          v-if="currentResource === 'DASHBOARD'"
          :colon="false"
          label=" "
          :label-col="{ style: { paddingLeft: '60px' } }"
        >
          <a-space>
            同时开通看板内的数据集权限
            <a-switch
              :disabled="
                (multiple && !formState.dashboardIds.length) ||
                mode === 'modify'
              "
              v-model:checked="formState.enableDataset"
            />
            <span
              v-show="formState.enableDataset"
              style="color: rgba(0, 0, 0, 0.45)"
            >
              以下是看板内的数据集列表
            </span>
          </a-space>
        </a-form-item>

        <a-form-item
          v-if="enableDatasetMultiple"
          class="form-item-column"
          label="数据集列表"
          :colon="false"
          v-bind="validateInfos.datasetIds"
        >
          <!-- 数据集列表 -->
          <DatasetChoosedList
            :resource="resource"
            :disabled="selectedDisabled || multipleDatasetDisabled"
            :workspaceId="workspaceId"
            :dashboardIds="formState.dashboardIds"
            v-model:value="formState.datasetIds"
            @change="onDatasetIdsChange"
          />
        </a-form-item>

        <template
          v-if="
            currentResource === 'DATASET' ||
            (currentResource === 'DASHBOARD' && formState.enableDataset)
          "
        >
          <a-form-item label="粒度" v-bind="validateInfos.privilegeType">
            <a-select
              :value="formState.privilegeType"
              :options="currentLevels"
              @change="onPrivilegeChange"
            >
            </a-select>
          </a-form-item>

          <a-form-item
            class="form-item-column"
            v-if="
              formState.privilegeType === 'COLUMN' ||
              formState.privilegeType === 'COLUMN_ROW'
            "
            label="字段权限"
            v-bind="validateInfos.columns"
          >
            <SelectList
              style="height: 420px"
              keyField="name"
              labelField="displayName"
              :loading="datasetIntersectionFieldsLoading"
              :data-source="datasetIntersectionFields"
              v-model:value="formState.columns"
            >
              <template #footer>
                <div
                  style="
                    display: inline-flex;
                    align-items: center;
                    margin: 2px 0;
                  "
                >
                  新增字段默认授权
                  <a-switch
                    style="margin-left: 4px"
                    v-model:checked="formState.autoAuth"
                  />
                </div>
              </template>
            </SelectList>
          </a-form-item>

          <a-form-item
            class="form-item-row"
            v-if="
              formState.privilegeType === 'ROW' ||
              formState.privilegeType === 'COLUMN_ROW'
            "
            label="行权限"
            v-bind="validateInfos.rows"
          >
            <Row
              v-model:row="formState.rows"
              :datasetIds="formState.datasetIds"
              :fields="datasetIntersectionFields"
              :max="ROW_MAX"
            />
          </a-form-item>

          <a-form-item label="有效期" v-bind="validateInfos.expireDuration">
            <a-radio-group
              :options="expireDays"
              v-model:value="formState.expireDuration"
            ></a-radio-group>
          </a-form-item>
        </template>
      </a-form>
    </div>

    <template #footer>
      <a-space style="float: right">
        <a-button @click="close">取消</a-button>
        <a-tooltip
          placement="topRight"
          :title="confirmDisabled ? '请选择用户或用户组' : ''"
        >
          <a-button
            type="primary"
            :loading="confirmLoading"
            :disabled="confirmDisabled"
            @click="ok"
            >确认
          </a-button>
        </a-tooltip>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup>
import { computed, provide, reactive, ref, watch, shallowRef } from 'vue'
import { Form, message } from 'ant-design-vue'
import useAppStore from '@/store/modules/app'
import SelectList from 'common/components/ExtendSelect'
import Row from './components/Row.vue'
import DashboardChoosedList from './components/DashboardChoosedList.vue'
import DatasetChoosedList from './components/DatasetChoosedList.vue'
import { levels, expireDays, resourceMap } from './config'
import { dayToSec } from '@/components/Authorize/utils'
import {
  getDetailById,
  getDetailByDatasetIdAndUsername,
  postAuthorize,
  putAuthorize,
  renewAuthorize
} from '@/apis/dataset/authorize'
import { getUserByWorkspaceId } from '@/apis/user'
import { getRoleByWorkspaceId } from '@/apis/role'
import {
  postDashboardMultiple,
  getIntersectionFields,
  postDatasetMultiple,
  putDatasetMultiple
} from '@/apis/authorize'

const appStore = useAppStore()

const emits = defineEmits(['update:open', 'success'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  // 授权的对象 用户，用户组
  type: {
    type: String,
    default: 'USER',
    validator: s => ['USER', 'GROUP'].includes(s)
  },
  // 批量授权
  multiple: {
    type: Boolean,
    default: false
  },
  // 授权的资源 数据集，看板
  resource: {
    type: String,
    default: 'DATASET',
    validator: s => ['DATASET', 'DASHBOARD'].includes(s)
  },
  initData: {
    type: Object,
    default: () => ({})
  },
  // 可选
  selectable: {
    type: Boolean,
    default: true
  },
  // 提交前的回调函数
  beforeSubmit: {
    type: Function
  },
  // 空间id
  workspaceId: {
    type: Number
  },
  dashboardIds: {
    type: Array,
    default: () => []
  },
  datasetIds: {
    type: Array,
    default: () => []
  },
  mode: {
    type: String,
    default: 'insert',
    validator: s => ['insert', 'modify', 'renew'].includes(s)
  },
  // 列表批量数据集授权时的初始化时，数据集不可再选择
  multipleDatasetDisabled: {
    type: Boolean,
    default: false
  },
  // 列表批量看板授权时的初始化时，看板不可再选择
  multipleDashboardDisabled: {
    type: Boolean,
    default: false
  }
})

const workspaceId = computed(() => props.workspaceId || appStore.workspaceId)

// 当前授权的资源类型
const currentResource = ref(props.resource)

// 授权的资源文本
const resourceLabel = computed(() => resourceMap[currentResource.value])

// 抽屉标题
const title = computed(() => {
  return resourceLabel.value + `${props.multiple ? '批量' : ''}授权`
})

// 不可选择用户或用户组
const selectedDisabled = computed(
  () => !!props.initData.id || !props.selectable
)
const selectTabKey = ref('USER')
const isUserTab = computed(() => selectTabKey.value === 'USER')

// 可选的用户和用户组
const selectListLoading = ref(false)
const selectListUsers = shallowRef([])
const selectListGroups = shallowRef([])
const selectListDataSource = computed(() =>
  isUserTab.value ? selectListUsers.value : selectListGroups.value
)
// 获取用户
const fetchUsers = async () => {
  try {
    selectListLoading.value = true
    const { data = [] } = await getUserByWorkspaceId({
      workspaceId: workspaceId.value,
      pageSize: 10000
    })

    selectListUsers.value = data
  } catch (error) {
    console.error('获取用户错误', error)
  } finally {
    selectListLoading.value = false
  }
}
// 获取用户组
const fetchGroups = async () => {
  try {
    selectListLoading.value = true
    const { data = [] } = await getRoleByWorkspaceId({
      workspaceId: workspaceId.value,
      pageSize: 10000
    })

    selectListGroups.value = data
  } catch (error) {
    console.error('获取用户组错误', error)
  } finally {
    selectListLoading.value = false
  }
}

// 获取可选的用户和用户组
const fetchSelectListDataSource = async () => {
  try {
    selectListLoading.value = true

    await Promise.all([fetchUsers(), fetchGroups()])
  } catch (error) {
    console.error('获取可选的用户和用户组错误', error)
  } finally {
    selectListLoading.value = false
  }
}

// 已选中的
const userSelected = ref([])
const groupSelected = ref([])
const selected = computed({
  get() {
    return isUserTab.value ? userSelected.value : groupSelected.value
  },
  set(vals) {
    if (isUserTab.value) {
      userSelected.value = vals
    } else {
      groupSelected.value = vals
    }
  }
})
// 显示已选中的别名
const displaySelected = (list, condition, cb) => {
  const item = list.find(condition)
  if (!item) return ''

  return cb(item)
}
const handleSelectClose = (i, type) => {
  if (type === 'USER') {
    userSelected.value.splice(i, 1)
  } else {
    groupSelected.value.splice(i, 1)
  }
}

// 初始化行数据
const _transformRowsInit = (res, level = 1) => {
  if (typeof res !== 'object') return

  return {
    relation: res.relation,
    children: (res?.children ?? []).map(child => {
      if (child.children && child.children.length === 1) {
        const item = child.children[0]

        return {
          ...item,
          value:
            item.type === 'TEXT' && Array.isArray(item.value)
              ? item.value[0]
              : item.value
        }
      } else {
        return level === 1 ? _transformRowsInit(child, ++level) : child
      }
    })
  }
}

// 获取授权详情
const loading = ref(false)
const fetchDetail = async () => {
  try {
    loading.value = true

    const { authorized, id, datasetId, username } = props.initData
    // 从数据集角度，有过授权，从数据集id和申请人获取最新一条记录
    // 从授权记录角度，获取授权记录详情
    const fn =
      authorized === true
        ? () => getDetailByDatasetIdAndUsername({ datasetId, username })
        : () => getDetailById(id)

    const {
      username: un,
      roleId,
      privilegeType,
      expireDuration,
      rows,
      datasetId: dId,
      columnPrivilege: columns,
      autoAuth
    } = await fn()

    userSelected.value = un ? [un] : []
    groupSelected.value = roleId ? [roleId] : []

    initFormState({
      enableDataset: true,
      datasetIds: [dId],
      privilegeType,
      expireDuration,
      rows: _transformRowsInit(rows),
      columns,
      autoAuth
    })
  } catch (error) {
    console.error('授权详情获取错误', error)
  } finally {
    loading.value = false
  }
}

const close = () => {
  loading.value = false
  confirmLoading.value = false
  datasetIntersectionFields.value = []
  reset()
  emits('update:open', false)
}

// 数据集字段交叉字段
const datasetIntersectionFieldsLoading = ref(false)
const datasetIntersectionFields = shallowRef([])
const onDatasetIdsChange = async e => {
  try {
    datasetIntersectionFieldsLoading.value = true

    const res = await getIntersectionFields(e)

    datasetIntersectionFields.value = res
  } catch (error) {
    console.error('获取数据集交叉字段失败', error)
  } finally {
    datasetIntersectionFieldsLoading.value = false
  }
}

const formState = reactive({
  dashboardIds: [],
  enableDataset: false,
  datasetIds: [],
  privilegeType: 'TABLE',
  columns: [],
  autoAuth: undefined,
  rows: {},
  expireDuration: dayToSec(7)
})

// 初始化表单数据
const initFormState = payload => {
  for (const key in formState) {
    const value = payload[key]

    if (key === 'columns') {
      formState[key] = value?.split(',')
    } else {
      formState[key] = payload[key]
    }
  }
}

// 看板列表选择
const onDashboardIdsChange = e => {
  if (!e.length) {
    formState.enableDataset = false
  }
}

// 数据集列表多选
const enableDatasetMultiple = computed(() => {
  return (
    props.open &&
    ((currentResource.value === 'DASHBOARD' && formState.enableDataset) ||
      (currentResource.value === 'DATASET' && props.multiple))
  )
})

// 粒度选择
const onPrivilegeChange = e => {
  // 不在 行级、字段与行级 间切换，清空行数据
  if (!formState.privilegeType.includes('ROW') || !e.includes('ROW')) {
    formState.rows = {}
    clearValidate(['rows'])
  }

  // 不在 字段级、字段与行级 间切换，清空字段数据
  if (!formState.privilegeType.includes('COLUMN') || !e.includes('COLUMN')) {
    formState.columns = []
    clearValidate(['columns'])
  }

  formState.autoAuth = e.includes('COLUMN') ? false : undefined
  formState.privilegeType = e
}

// 行校验
const rowValidator = (rule, value, cb) => {
  if (rowUnValidated(value)) {
    return Promise.reject(rule.message)
  } else {
    return Promise.resolve()
  }
}
// 行数据校验
const rowUnValidated = ({ children = [] } = {}) => {
  if (!children.length) return true

  if (
    children.some(v => {
      if (v.children?.length) {
        return rowUnValidated(v)
      } else {
        return !v.field || !v.value?.length
      }
    })
  ) {
    return true
  }

  return false
}

// 看板列表校验
const dashboardIdsValidator = (rule, value) => {
  return new Promise((resolve, reject) => {
    if (!value?.length) {
      reject(rule.message)
    } else {
      resolve()
    }
  })
}

// 数据集列表校验
const datasetIdsValidator = (rule, value) => {
  return new Promise((resolve, reject) => {
    if (!value?.length) {
      reject(rule.message)
    } else {
      resolve()
    }
  })
}

const init = () => {
  currentResource.value = props.resource
  selectTabKey.value = props.type === 'GROUP' ? 'GROUP' : 'USER'

  fetchSelectListDataSource()

  const { authorized, id, username, roleId } = props.initData

  // 看板授权
  if (currentResource.value === 'DASHBOARD') {
    // formState.dashboardIds = props.multiple ? [] : props.dashboardIds
    formState.dashboardIds = props.dashboardIds
    formState.datasetIds = props.datasetIds
    formState.enableDataset = !!formState.datasetIds.length

    if (authorized || id) {
      // TODO 看板授权(共享)详情
    } else {
      if (username)
        userSelected.value = Array.isArray(username) ? username : [username]
      if (roleId)
        groupSelected.value = Array.isArray(roleId) ? roleId : [roleId]
    }
  } else {
    // 数据集授权
    formState.enableDataset = true
    formState.datasetIds = props.datasetIds
    onDatasetIdsChange(props.datasetIds)

    // 编辑时选择多个数据集时，无需获取授权详情，直接默认到 TABLE
    if (
      props.multiple &&
      (formState.datasetIds > 1 || props.initData.ids?.length > 1)
    ) {
      formState.privilegeType = 'TABLE'

      if (username)
        userSelected.value = Array.isArray(username) ? username : [username]
      if (roleId)
        groupSelected.value = Array.isArray(roleId) ? roleId : [roleId]
    } else {
      // 有授权过或有授权记录
      if (authorized || id) {
        fetchDetail()
      } else {
        if (username)
          userSelected.value = Array.isArray(username) ? username : [username]
        if (roleId)
          groupSelected.value = Array.isArray(roleId) ? roleId : [roleId]
      }
    }
  }
}

const reset = () => {
  userSelected.value = []
  groupSelected.value = []
  resetFields()
}

const formRules = reactive({
  columns: [{ required: true, message: '字段不能为空' }],
  rows: [
    { required: true, validator: rowValidator, message: '必填项不能为空' }
  ],
  dashboardIds: [
    {
      required: currentResource.value === 'DASHBOARD',
      validator: dashboardIdsValidator,
      message: '看板不能为空'
    }
  ],
  datasetIds: [
    {
      required: false,
      validator: datasetIdsValidator,
      message: '数据集不能为空'
    }
  ]
})

// 粒度 [多个数据集或多个授权记录时只能 表级 和 行级]
const currentLevels = computed(() =>
  formState.datasetIds?.length > 1 || props.initData.ids?.length > 1
    ? levels.filter(t => t.value === 'TABLE' || t.value === 'ROW')
    : levels
)

watch(
  () => formState.enableDataset,
  checked => {
    if (currentResource.value === 'DATASET') return

    formState.datasetIds = checked ? [] : undefined
    formRules.datasetIds[0]['required'] = checked
  }
)

// 两个以上的数据集只允许表级和行级，重置为表级
watch(
  () => formState.datasetIds,
  ids => {
    if (
      ids?.length > 1 &&
      formState.privilegeType !== 'TABLE' &&
      formState.privilegeType !== 'ROW'
    ) {
      formState.privilegeType = 'TABLE'
      formState.rows = {}
      formState.columns = []
      clearValidate(['columns', 'rows'])
    }
  },
  { deep: true }
)

const { validate, resetFields, clearValidate, validateInfos } = Form.useForm(
  formState,
  formRules
)

watch(
  () => props.open,
  async open => {
    if (open) {
      init()
    } else {
      reset()
    }
  },
  { immediate: true }
)

const ROW_MAX = 10 // 最大的行条件数量
const getChildLength = (list = []) =>
  list.reduce((acc, pre) => {
    return (acc += pre.children ? getChildLength(pre.children) : 1)
  }, 0)
const canRowAddable = computed(() => {
  return getChildLength(formState.rows.children) < ROW_MAX
})
provide('canaddable', canRowAddable)

// 处理行权限数据
const _transformRows = (rows, level = 1) => {
  if (typeof rows !== 'object') return

  const { relation: rel = 'OR', children: list = [] } = rows

  return {
    relation: rel,
    children: list.map(item => {
      const { relation, children = [], value } = item

      if (children.length && children.length > 1) {
        return _transformRows({ relation, children }, ++level)
      } else {
        return Object.assign(
          {
            relation: rel
          },
          level === 1
            ? {
                children: [
                  {
                    ...item,
                    value: Array.isArray(value) ? value : [value]
                  }
                ]
              }
            : { ...item, value: Array.isArray(value) ? value : [value] }
        )
      }
    })
  }
}

// 提交
const confirmLoading = ref(false)
const confirmDisabled = computed(() => {
  return !userSelected.value.length && !groupSelected.value.length
})
const ok = () => {
  const namesMap = {
    TABLE: ['table'],
    COLUMN: ['columns'],
    ROW: ['rows'],
    COLUMN_ROW: ['columns', 'rows']
  }

  const fieldNames = []
    .concat(
      formState.enableDataset
        ? [
            'datasetIds',
            'privilegeType',
            'expireDuration',
            ...namesMap[formState.privilegeType]
          ]
        : []
    )
    .concat(currentResource.value === 'DASHBOARD' ? 'dashboardIds' : undefined)

  validate(fieldNames.filter(Boolean)).then(res => {
    submit({
      ...res,
      autoAuth: formState.autoAuth ?? false,
      columnPrivilege: res.columns?.join(','),
      columns: undefined,
      rows: _transformRows(res.rows),
      usernames: userSelected.value,
      roleIds: groupSelected.value
    })
  })
}

const submit = async payload => {
  try {
    confirmLoading.value = true

    if (typeof props.beforeSubmit === 'function') {
      await props.beforeSubmit()
    }

    if (currentResource.value === 'DASHBOARD') {
      const { dashboardIds, usernames, roleIds, ...rest } = payload

      await postDashboardMultiple({
        dashboardIds,
        usernames,
        roleIds,
        datasetBatchAuthorizeUO: formState.enableDataset ? rest : undefined
      })
    } else {
      // 批量
      if (props.multiple) {
        // 编辑
        if (props.initData.ids?.length) {
          await putDatasetMultiple({
            ...payload,
            authorizeIds: props.initData.ids
          })
        } else {
          // 新增
          await postDatasetMultiple(payload)
        }
      } else {
        if (props.mode === 'renew') {
          // 续期
          await renewAuthorize(props.initData.id, {
            ...payload,
            datasetId: props.datasetIds[0]
          })
        } else {
          // 编辑
          if (props.initData.id) {
            await putAuthorize(props.initData.id, {
              ...payload,
              datasetId: props.datasetIds[0]
            })
          } else {
            // 新增
            await postAuthorize({ ...payload, datasetId: props.datasetIds[0] })
          }
        }
      }
    }

    message.success('授权成功')
    emits('success')
    close()
  } catch (error) {
    console.error('授权提交错误', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.select-tabs {
  :deep(.ant-tabs-nav) {
    margin-bottom: 0;
    .ant-tabs-tab {
      padding: 6px 10px;
      margin: 0 8px;
    }
  }
}

.select-preview {
  overflow: auto;
  margin-top: 10px;
  .tag {
    margin: 0 8px 10px 0;
  }
}

.form-item-row {
  :deep(.ant-form-item-control-input-content) {
    padding: 6px;
    border: 1px solid transparent;
  }
  &.ant-form-item-has-error {
    :deep(.ant-form-item-control-input-content) {
      border: 1px solid #ff4d4f;
    }
    :deep(.row) {
      .ant-select-selector,
      .ant-input {
        border-color: #d9d9d9 !important;
      }
    }
  }
}

.form-item-column {
  &.ant-form-item-has-error {
    :deep(.select-layout) {
      border-color: #ff4d4f;
      .ant-input-affix-wrapper {
        border-color: #d9d9d9 !important;
      }
    }
  }
}
</style>
