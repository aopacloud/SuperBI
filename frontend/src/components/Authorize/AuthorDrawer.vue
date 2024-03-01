<template>
  <a-drawer width="1000" title="授权" :open="open" @close="close">
    <h3>数据集: {{ dataset.name || initData.name || '-' }}</h3>

    <div class="flex" style="align-items: flex-start; height: calc(100% - 60px)">
      <div class="flex-column" style="width: 250px">
        <SelectList
          style="height: 450px"
          :loading="selectListLoading"
          :disabled="selectedDisabled"
          :keyField="isUserTab ? 'username' : 'id'"
          :labelField="isUserTab ? 'aliasName' : 'name'"
          :data-source="selectListDataSource"
          v-model:value="selected">
          <template #tabs>
            <a-tabs class="select-tabs" size="small" v-model:activeKey="selectTabKey">
              <a-tab-pane key="USER" tab="用户"></a-tab-pane>
              <a-tab-pane key="GROUP" tab="用户组"></a-tab-pane>
            </a-tabs>
          </template>
        </SelectList>

        <p v-show="userSelected.length > 0">
          已选中的用户：
          <a v-if="!selectedDisabled" @click="userSelected = []">清空</a>
        </p>
        <div class="select-preview flex-1">
          <a-tag
            class="tag"
            color="blue"
            v-for="(username, i) in userSelected"
            :key="username"
            :closable="!selectedDisabled"
            @close.prevent="handleSelectClose(i, 'USER')">
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
            @close.prevent="handleSelectClose(i, 'GROUP')">
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
      <a-form class="flex-1 hidden" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
        <a-form-item label="粒度" v-bind="validateInfos.privilegeType">
          <a-select :value="formState.privilegeType" @change="onPrivilegeChange">
            <a-select-option v-for="(val, key) in levels" :key="key" :value="key">
              {{ val }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item
          class="form-item-column"
          v-if="
            formState.privilegeType === 'COLUMN' ||
            formState.privilegeType === 'COLUMN_ROW'
          "
          label="字段权限"
          v-bind="validateInfos.columns">
          <SelectList
            style="height: 420px"
            keyField="name"
            labelField="displayName"
            :data-source="dataset.fields"
            v-model:value="formState.columns" />
        </a-form-item>

        <a-form-item
          class="form-item-row"
          v-if="
            formState.privilegeType === 'ROW' || formState.privilegeType === 'COLUMN_ROW'
          "
          label="行权限"
          v-bind="validateInfos.rows">
          <Row v-model:row="formState.rows" :dataset="dataset" :max="ROW_MAX" />
        </a-form-item>

        <a-form-item label="有效期" v-bind="validateInfos.expireDuration">
          <a-radio-group
            :options="expireDays"
            v-model:value="formState.expireDuration"></a-radio-group>
        </a-form-item>
      </a-form>
    </div>

    <template #footer>
      <a-space style="float: right">
        <a-button @click="close">取消</a-button>
        <a-tooltip
          placement="topRight"
          :title="confirmDisabled ? '请选择用户或用户组' : ''">
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
import SelectList from 'common/components/ExtendSelect'
import Row from './components/Row.vue'
import { RELATION } from '@/CONST.dict'
import { levels, expireDays, dayToSec } from './config'
import { getDetailById as getDatasetDetail } from '@/apis/dataset'
import {
  getDetailById,
  getDetailByDatasetIdAndUsername,
  postAuthorize,
  putAuthorize,
  renewAuthorize,
} from '@/apis/dataset/authorize'
import { getUserByWorkspaceId } from '@/apis/user'
import { getRoleByWorkspaceId } from '@/apis/role'

const emits = defineEmits(['update:open', 'success'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
  selectable: {
    type: Boolean,
    default: true,
  },
  beforeSubmit: {
    type: Function,
  },
})
// 不可选择用户或用户组
const selectedDisabled = computed(() => !!props.initData.id || !props.selectable)

const selectTabKey = ref('USER')
const isUserTab = computed(() => selectTabKey.value === 'USER')

// 可选的用户和用户组
const selectListLoading = ref(false)
const selectListUsers = shallowRef([])
const selectListGroups = shallowRef([])
const fetchUsers = async () => {
  try {
    selectListLoading.value = true
    const { data = [] } = await getUserByWorkspaceId({
      workspaceId: dataset.value.workspaceId,
      pageSize: 10000,
    })

    selectListUsers.value = data
  } catch (error) {
    console.error('获取用户错误', error)
  } finally {
    selectListLoading.value = false
  }
}
const fetchGroups = async () => {
  try {
    selectListLoading.value = true
    const { data = [] } = await getRoleByWorkspaceId({
      workspaceId: dataset.value.workspaceId,
      pageSize: 10000,
    })

    selectListGroups.value = data
  } catch (error) {
    console.error('获取用户组错误', error)
  } finally {
    selectListLoading.value = false
  }
}
const selectListDataSource = computed(() =>
  isUserTab.value ? selectListUsers.value : selectListGroups.value
)
// 获取可选的用户和用户组
const fetchSelectListDatasource = async () => {
  try {
    selectListLoading.value = true

    await Promise.all([fetchUsers(), fetchGroups()])
  } catch (error) {
    console.error('获取可选的用户和用户组错误', error)
  } finally {
    selectListLoading.value = false
  }
}

// 数据集
const dataset = ref({})
const fetchDataset = async () => {
  try {
    const res = await getDatasetDetail(props.initData.datasetId)

    dataset.value = res
  } catch (error) {
    console.error('获取数据集错误', error)
  }
}

// 初始化行数据
const _transformRowsInit = (res, level = 1) => {
  if (typeof res !== 'object') return

  return {
    relation: res.relation,
    children: res.children.map(child => {
      if (child.children && child.children.length === 1) {
        return {
          ...child.children[0],
        }
      } else {
        return level === 1 ? _transformRowsInit(child, ++level) : child
      }
    }),
  }
}

const loading = ref(false)
// const detail = ref({})
const fetchDetail = async () => {
  try {
    loading.value = true

    const { authorized, id, datasetId, username } = props.initData
    // 从数据集角度，有过授权，从数据集id和申请人获取最新一条记录
    // 从授权记录角度，获取授权记录详情
    const fn =
      typeof authorized === 'boolean' && authorized
        ? () => getDetailByDatasetIdAndUsername({ datasetId, username })
        : () => getDetailById(id)

    const {
      username: un,
      roleId,
      privilegeType,
      expireDuration,
      rows,
      columnPrivilege: columns,
    } = await fn()

    userSelected.value = un ? [un] : []
    groupSelected.value = roleId ? [roleId] : []

    initFormState({
      privilegeType,
      expireDuration,
      rows: _transformRowsInit(rows),
      columns,
    })
  } catch (error) {
    console.error('授权详情获取错误', error)
  } finally {
    loading.value = false
  }
}

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
const reset = () => {
  userSelected.value = []
  groupSelected.value = []
  resetFields()
}

watch(
  () => props.open,
  async open => {
    if (open) {
      const { authorized, id, datasetId, username, _type } = props.initData

      if (dataset.value.id !== datasetId) {
        await fetchDataset()
      }

      selectTabKey.value = _type === 'GROUP' ? 'GROUP' : 'USER'

      // 有授权过或有授权记录
      if (authorized || id) {
        await fetchDetail()
      } else {
        reset()

        if (username) userSelected.value = [username]
      }

      fetchSelectListDatasource()
    }
  },
  { immediate: true }
)

const close = () => {
  emits('update:open', false)
  resetFields()
}

const onPrivilegeChange = e => {
  if (!formState.privilegeType.includes('ROW') || !e.includes('ROW')) {
    formState.rows = {}
  }

  formState.privilegeType = e
  clearValidate(['columns', 'rows'])
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
  },
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

const useForm = Form.useForm
const formState = reactive({
  privilegeType: 'TABLE',
  columns: [],
  rows: {},
  expireDuration: dayToSec(7),
})

const getChildLength = (list = []) =>
  list.reduce((acc, pre) => {
    return (acc += pre.children ? getChildLength(pre.children) : 1)
  }, 0)

const ROW_MAX = 10
const canRowAddable = computed(() => {
  return getChildLength(formState.rows.children) < ROW_MAX
})
provide('canaddable', canRowAddable)

// 自定义校验行
const rowValidator = (rule, value, cb) => {
  if (unValidator(value)) {
    return Promise.reject(rule.message)
  } else {
    return Promise.resolve()
  }
}
// 校验行权限数据
const unValidator = ({ children = [] } = {}) => {
  if (!children.length) return true

  if (
    children.some(v => {
      if (v.children?.length) {
        return unValidator(v)
      } else {
        return !v.field || !v.value?.length
      }
    })
  ) {
    return true
  }

  return false
}

const formRuls = reactive({
  columns: [{ required: true, message: '字段不能为空' }],
  rows: [{ required: true, validator: rowValidator, message: '必填项不能为空' }],
})
const { validate, resetFields, clearValidate, validateInfos } = useForm(
  formState,
  formRuls
)

// 处理行权限数据
const _transformRows = (rows, level = 1) => {
  if (typeof rows !== 'object') return

  const { relation: rel = RELATION.OR, children: list = [] } = rows

  return {
    relation: rel,
    children: list.map(item => {
      const { relation, children = [], value } = item

      if (children.length && children.length > 1) {
        return _transformRows({ relation, children }, ++level)
      } else {
        return Object.assign(
          {
            relation: rel,
          },
          level === 1
            ? {
                children: [
                  {
                    ...item,
                    value: Array.isArray(value) ? value : [value],
                  },
                ],
              }
            : { ...item, value: Array.isArray(value) ? value : [value] }
        )
      }
    }),
  }
}

const confirmLoading = ref(false)
const confirmDisabled = computed(() => {
  return !userSelected.value.length && !groupSelected.value.length
})
const ok = () => {
  const namesMap = {
    TABLE: [],
    COLUMN: ['columns'],
    ROW: ['rows'],
    COLUMN_ROW: ['columns', 'rows'],
  }
  validate(
    ['privilegeType', 'expireDuration'].concat(namesMap[formState.privilegeType])
  ).then(res => {
    submit({
      ...res,
      columns: undefined,
      columnPrivilege: res.columns?.join(','),
      rows: _transformRows(res.rows),
      datasetId: props.initData.datasetId,
      usernames: userSelected.value,
      roleIds: groupSelected.value,
    })
  })
}

const submit = async payload => {
  try {
    if (!dataset.value.id && !props.initData.datasetId) {
      return message.error('数据集不存在')
    }

    confirmLoading.value = true

    if (typeof props.beforeSubmit === 'function') {
      await props.beforeSubmit()
    }

    const fn = !props.initData.id
      ? async () => postAuthorize(payload)
      : props.initData._mode === 'RENEW'
      ? async () => renewAuthorize(props.initData.id, payload)
      : async () => putAuthorize(props.initData.id, payload)

    await fn()

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
