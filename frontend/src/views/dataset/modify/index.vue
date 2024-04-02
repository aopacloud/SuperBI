<template>
  <section class="dataset-modify">
    <a-spin :spinning="loading">
      <header class="header flex justify-between align-center">
        <div class="header-left flex-inline align-center">
          <a-button type="text" @click="toBack"><LeftOutlined /></a-button>
          {{ detail.name }}
          <a-button
            size="small"
            type="text"
            style="margin-left: 6px"
            :icon="h(EditOutlined)"
            @click="edit"></a-button>
        </div>
        <a-space class="header-right">
          <a-button @click="toBack">返回</a-button>

          <a-button
            v-if="hasWritePermission"
            :disabled="publishLoading"
            :loading="saveLoading"
            @click="save">
            保存
          </a-button>
          <a-button
            v-if="hasManagePermission"
            type="primary"
            :disabled="!detail.config?.tableName || saveLoading"
            :loading="publishLoading"
            @click="saveToPublish">
            提交并发布
          </a-button>
        </a-space>
      </header>

      <main class="body">
        <Aside
          class="aside"
          style="width: 300px"
          :shouldMount="detailSuccess"
          :detail="detail"
          @on-table-change="fetchFields" />

        <section class="content">
          <header class="title">
            <span
              v-if="!detail.config?.tableName"
              style="line-height: 26px; color: #666">
              暂未选中任何表
            </span>
            <a-tag v-else class="title-tag" color="#1677ff">
              {{ detail.config?.dbName + '.' + detail.config?.tableName }}
            </a-tag>
          </header>

          <!-- TODO: 性能瓶颈 -->
          <a-tabs
            :animated="false"
            v-model:activeKey="tabKey"
            @tabClick="onTabChange">
            <a-tab-pane key="fields" tab="字段配置">
              <FieldTable
                ref="tableRef"
                :loading="tableFieldsLoading"
                :dataset="detail"
                :originFields="tableFields" />
            </a-tab-pane>

            <a-tab-pane key="partition" tab="分区及更新">
              <PartitionTab
                ref="partitionTabRef"
                :fields="allFields"
                v-model:dataset="detail" />
            </a-tab-pane>

            <a-tab-pane key="query" tab="申请与查询">
              <QueryTab
                ref="queryTabRef"
                :fields="allFields"
                v-model:dataset="detail" />
            </a-tab-pane>

            <a-tab-pane key="preview" tab="数据预览">
              <FieldPreview
                :dataset="{ workspaceId, ...detail }"
                :fields="allFields" />
            </a-tab-pane>
          </a-tabs>
        </section>
      </main>
    </a-spin>

    <ModifyModal
      v-model:open="modifyModalOpen"
      :init-data="detail"
      :init-params="{ position: 'DATASET', type: 'ALL', workspaceId }"
      @ok="onEditOk" />
  </section>
</template>

<script setup>
import { computed, h, nextTick, onBeforeMount, ref, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { LeftOutlined, EditOutlined } from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import Aside from './components/Aside.vue'
import FieldTable from './components/FieldTable.vue'
import FieldPreview from './components/Preview.vue'
import ModifyModal from './components/ModifyModal.vue'
import { getFieldsByTableItem } from '@/apis/metaData'
import PartitionTab from './components/PartitionTab.vue'
import QueryTab from './components/QueryTab.vue'
import {
  getLastVersionById,
  postDataset,
  putDataset,
  postPublishById,
} from '@/apis/dataset'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

// 编辑权限
const hasWritePermission = computed(() => {
  if (userStore.hasPermission('DATASET:WRITE:ALL:WORKSPACE')) {
    return true
  } else if (!detail.value.id) {
    return userStore.hasPermission('DATASET:VIEW:CREATE')
  } else if (userStore.hasPermission('DATASET:WRITE:CREATE')) {
    return detail.value.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DATASET:WRITE:HAS:PRIVILEGE')) {
    return detail.value.permission === 'WRITE'
  } else {
    return false
  }
})

// 管理权限（发布，移动，下线，删除，共享，推送）
const hasManagePermission = computed(() => {
  if (userStore.hasPermission('DATASET:MANAGE:ALL:WORKSPACE')) {
    return true
  } else if (!detail.value.id) {
    return userStore.hasPermission('DATASET:VIEW:CREATE')
  } else if (userStore.hasPermission('DATASET:MANAGE:CREATE')) {
    return detail.value.creator === userStore.userInfo.username
  } else if (userStore.hasPermission('DATASET:MANAGE:HAS:PRIVILEGE')) {
    return detail.value.permission === 'WRITE'
  } else {
    return false
  }
})

const workspaceId = computed(() => appStore.workspaceId)

const toBack = () => {
  router.replace({ name: 'DatasetList' })
}

const toBackFrom = () => {
  const { from, reportId } = route.query

  if (!from) {
    toBack()
  } else {
    router.replace({ name: from, params: { id: reportId } })
  }
}

const loading = ref(false)
const detailSuccess = ref(false)
// 数据集详情
const detail = ref({ workspaceId })
const fetchDetail = async id => {
  try {
    loading.value = true

    const res = await getLastVersionById(id)

    detailSuccess.value = true

    detail.value = {
      ...res,
      _extraConfig:
        typeof res.extraConfig === 'string'
          ? JSON.parse(res.extraConfig)
          : undefined,
    }

    nextTick(() => {
      tableRef.value?.init()
    })
  } catch (error) {
    console.error('获取数据集详情错误', error)
  } finally {
    loading.value = false
  }
}
onBeforeMount(() => {
  const { id } = route.params

  if (id) {
    fetchDetail(id)
  } else {
    detailSuccess.value = true

    modifyModalOpen.value = true
  }
})

// 编辑弹框
const modifyModalOpen = ref(false)
const edit = () => {
  modifyModalOpen.value = true
}
const onEditOk = e => {
  for (const key in e) {
    detail.value[key] = e[key]
  }
}

// 字段表格引用
const tableRef = ref(null)
// const btnDisabled = ref(false)
const saveLoading = ref(false)
const publishLoading = ref(false)

// 转换提交的参数
const _transformFieldsPayload = (fields = []) => {
  return fields.map(item => {
    const { dataType } = item

    return {
      ...item,
      dataType: Array.isArray(dataType)
        ? dataType.filter(Boolean).join('_')
        : dataType,
    }
  })
}

const partitionTabRef = ref(null)
const queryTabRef = ref(null)

// 保存
const save = async () => {
  try {
    if (!detail.value.name?.trim().length) {
      message.warn('数据集名称不能为空')

      return
    }

    saveLoading.value = true

    const fields = tableRef.value.getTableData()

    const partitionData = await partitionTabRef.value?.validate()
    const queryData = await queryTabRef.value?.validate()

    const extraConfig = {
      ...detail.value._extraConfig,
      ...partitionData,
      ...queryData,
      enableApply: queryData?.enableApply ?? detail.value.enableApply,
    }

    const payload = {
      ...detail.value,
      workspaceId: workspaceId.value,
      fields: _transformFieldsPayload(fields),
      extraConfig: JSON.stringify(extraConfig),
    }

    const fn = !!payload.id
      ? () => putDataset(payload.id, payload)
      : () => postDataset(payload)
    const res = await fn()

    message.success('保存成功')

    detail.value = {
      ...res,
      _extraConfig:
        typeof res.extraConfig === 'string'
          ? JSON.parse(res.extraConfig)
          : undefined,
    }

    return res.id
  } catch (error) {
    console.error('数据集保存错误', error)

    return Promise.reject()
  } finally {
    saveLoading.value = false
  }
}

// 保存并发布
const saveToPublish = async () => {
  try {
    publishLoading.value = true

    const id = await save()
    await postPublishById(id)

    message.success('发布成功')
    toBackFrom()
  } catch (error) {
    console.error('数据集发布错误', error)
  } finally {
    publishLoading.value = false
  }
}

const allFields = shallowRef([])
const onTabChange = key => {
  allFields.value = tableRef.value?.getTableData?.()
}

const tableFieldsLoading = ref(false)
const tableFields = ref([])

const tabKey = ref('fields')

/**
 * 获取数据表字段
 * @param {any} payload 参数
 * @param {boolean} type 0 重置，1 不重置
 */
const fetchFields = async (payload, reset = 0) => {
  try {
    // tabKey.value = 'fields'
    tableFieldsLoading.value = true

    const { fields = [], ...res } = await getFieldsByTableItem(payload)

    if (reset === 1) {
      detail.value.fields = []
      detail.value.config = res
    }
    tableFields.value = fields

    nextTick(() => {
      tableRef.value?.init()
    })
  } catch (error) {
    console.error('获取表字段错误', error)
  } finally {
    tableFieldsLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.dataset-modify {
  display: flex;
  flex-direction: column;
  height: 100%;
  & > :deep(.ant-spin-nested-loading) {
    height: inherit;

    & > .ant-spin-container {
      height: inherit;
      display: flex;
      flex-direction: column;
    }
  }
}
.header {
  padding: 6px 8px 6px 0;
}
.body {
  flex: 1;
  display: flex;
  overflow: auto;
}

.aside {
  padding: 12px 16px;
}
.content {
  flex: 1;
  display: flex;
  flex-direction: column;

  overflow: auto;
  .title {
    min-height: 46px;
    padding: 10px 12px;
    background-color: #eff1f3;
  }
  .title-tag {
    padding: 2px 6px;
    font-size: 14px;
  }
  & > .ant-tabs {
    flex: 1;
    overflow: hidden;
    :deep(.ant-tabs-nav) {
      margin-bottom: 0;
    }
    :deep(.ant-tabs-content),
    :deep(.ant-tabs-tabpane) {
      height: 100%;
    }
  }
}
</style>
