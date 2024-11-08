<template>
  <a-modal
    :title="title"
    :open="open"
    :maskClosable="false"
    :confirm-loading="submitLoading"
    @cancel="cancel"
    @ok="save"
  >
    <a-alert type="warning" show-icon style="margin-bottom: 16px">
      <template #message>
        移交后，原所有者将不再拥有 <b>编辑</b>、<b>删除</b> 等管理权限
      </template>
    </a-alert>

    <a-form :label-col="{ span: 6 }">
      <template v-if="isMultiple">
        <a-form-item label="" style="margin-left: 50px">
          您正在批量移交
          <h3 style="display: inline; margin: 0 6px">{{ ids.length }}个</h3>
          {{ resourceTypeMap[resourceType] }}
        </a-form-item>
      </template>

      <template v-else>
        <a-form-item label="名称">{{ initData.name || '-' }}</a-form-item>

        <a-form-item label="描述">
          {{ initData.description || '-' }}
        </a-form-item>

        <a-form-item label="原负责人">
          {{ initData.creatorAlias || initData.creator || '-' }}
        </a-form-item>
      </template>

      <a-form-item label="被移交人" v-bind="validateInfos.toUsername">
        <a-select
          placeholder="请输入或下拉选择"
          show-search
          allow-clear
          :filterOption="filterOption"
          :options="userOptions"
          :field-names="{ label: 'aliasName', value: 'username' }"
          v-model:value="formState.toUsername"
        />
      </a-form-item>

      <a-form-item
        label=""
        v-if="resourceType === 'DASHBOARD' || resourceType === 'DATASET'"
        v-bind="validateInfos.relReport"
      >
        <div class="font-help" style="margin-left: 50px; line-height: 28px">
          同时移交原负责人在{{ resourceTypeMap[resourceType] }}上的所有图表
          <a-switch
            style="margin-left: 6px; vertical-align: middle"
            v-model:checked="formState.relReport"
          />
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { computed, watch } from 'vue'
import { Form, message } from 'ant-design-vue'
import useAppStore from '@/store/modules/app'
import { getUserByWorkspaceId } from '@/apis/user'
import { resourceTypeMap } from './config'
import { postResourceTransfer } from '@/apis/transfer'
import { flat, intersection } from '@/common/utils/array'

defineOptions({
  name: 'TransferDrawer'
})

const appStore = useAppStore()

const emits = defineEmits(['close', 'ok'])

const open = defineModel('open', { type: Boolean, default: false })

const { resourceType, initData, ids, workspaceId } = defineProps({
  resourceType: {
    type: String,
    validator: s => ['DASHBOARD', 'DATASET', 'REPORT'].includes(s)
  },
  initData: {
    type: Object,
    default() {
      return {}
    }
  },
  workspaceId: {
    type: [Number, Array]
  },
  ids: {
    type: Array,
    default: () => []
  }
})

const workspaceIds = computed(() => {
  if (typeof workspaceId === 'undefined') return [appStore.workspaceId]

  return Array.isArray(workspaceId) ? [...new Set(workspaceId)] : [workspaceId]
})

// 多个资源
const isMultiple = computed(() => ids.length)

const title = computed(() => {
  return (resourceTypeMap[resourceType] || '资源') + '移交'
})

// 成员交集
const getIntersectionList = list => {
  const ids = list.map(t => t.map(a => a.username))
  const flatted = flat(list)
  const intersectionIds = intersection(...ids)

  return intersectionIds.map(id => flatted.find(t => t.username === id))
}

// 成员列表
const userLoading = ref(false)
const users = shallowRef([])
const fetchAllUsers = async () => {
  const fn = id => {
    return getUserByWorkspaceId({
      workspaceId: id,
      pageSize: 10000
    })
  }

  try {
    userLoading.value = true

    Promise.all(workspaceIds.value.map(fn)).then(res => {
      users.value = getIntersectionList(res.map(t => t.data))
    })
  } catch (error) {
    console.error('获取所有系统权限成员失败', error)
  } finally {
    userLoading.value = false
  }
}
const filterOption = (input, option) => {
  return (
    (option.aliasName + option.username)
      .toLowerCase()
      .indexOf(input.toLowerCase()) > -1
  )
}

const userOptions = computed(() => {
  if (!initData.creator) return users.value

  return users.value.map(t => {
    return {
      ...t,
      // 不可选原负责人
      disabled: t.username === initData.creator
    }
  })
})

const formState = reactive({
  toUsername: undefined,
  relReport: undefined
})

const formRules = reactive({
  toUsername: [{ required: true, message: '移交人不能为空' }]
})

const { validate, validateInfos, resetFields } = Form.useForm(
  formState,
  formRules
)

const watcherUp = watch(open, op => {
  if (op) {
    fetchAllUsers()
  }
})

const save = () => {
  validate().then(r => {
    submit({ ...r, autoTrans: formState.relReport })
  })
}

const submitLoading = ref(false)
const submit = async payload => {
  try {
    submitLoading.value = true

    const params = {
      position: resourceType,
      id: initData.id,
      idList: isMultiple.value ? ids : undefined,
      fromUsername: initData.creator,
      fromUsernameList: isMultiple.value ? initData.creators : undefined,
      ...payload
    }

    await postResourceTransfer(params)
    message.success('移交成功')
    cancel()
    emits('ok')
  } catch (error) {
    console.error('移交失败', error)
  } finally {
    submitLoading.value = false
  }
}

const cancel = () => {
  open.value = false
  resetFields()
  emits('close')
}
</script>

<style lang="scss" scoped>
.alert {
  margin-bottom: 20px;
  padding: 6px 10px;
  background-color: #f2f2f2;
  border-radius: 2px;
}
</style>
