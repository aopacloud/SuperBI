<template>
  <section class="dataset-modify">
    <a-spin :spinning="loading">
      <header class="header flex justify-between align-center">
        <div class="header-left flex-inline align-center">
          <a-button type="text" @click="toBack"><LeftOutlined /></a-button>
          <span>
            {{ detail.name }}
            <a-tooltip v-if="detail.description" :title="detail.description">
              <InfoCircleOutlined />
            </a-tooltip>
          </span>
          <a-button
            size="small"
            type="text"
            style="margin-left: 6px"
            :icon="h(EditOutlined)"
            @click="edit"
          ></a-button>
        </div>
        <a-space class="header-right">
          <a-button @click="toBack">返回</a-button>

          <a-button
            v-if="hasWritePermission"
            :disabled="publishLoading"
            :loading="saveLoading"
            @click="save()"
          >
            保存
          </a-button>
          <a-button
            v-if="hasManagePermission"
            type="primary"
            :disabled="!canPublish || saveLoading"
            :loading="publishLoading"
            @click="saveToPublish"
          >
            提交并发布
          </a-button>
        </a-space>
      </header>

      <main class="body">
        <Resize
          class="resize-theme-1"
          :directions="['right']"
          :style="{ border: 'none', width: asideWidth + 'px' }"
          :autoChange="false"
          @resized="onAsideResized"
        >
          <Aside
            ref="asideRef"
            :selectedContent="selectedContent"
            :shouldMount="detailSuccess"
            :detail="detail"
          />
        </Resize>

        <section class="content">
          <Resize
            class="resize-theme-1"
            :directions="['bottom']"
            :style="{ border: 'none', height: graphHeight + 'px' }"
            :autoChange="false"
            @resized="onContentResized"
          >
            <SelectedArea
              style="height: 100%"
              :detail="detail"
              :selectedContent="selectedContent"
              @change="reloadTableFields"
            />
          </Resize>
          <!-- TODO: 性能瓶颈 -->
          <a-tabs
            :animated="false"
            v-model:activeKey="tabKey"
            @tabClick="onTabChange"
          >
            <a-tab-pane key="fields" tab="字段配置">
              <FieldTable
                ref="tableRef"
                :loading="tableFieldsLoading"
                :dataset="detail"
                :selectedContent="selectedContent"
              />

              <!-- :originFields="tableFields" -->
            </a-tab-pane>

            <a-tab-pane key="partition" tab="分区及更新">
              <PartitionTab
                ref="partitionTabRef"
                :fields="allFields"
                v-model:dataset="detail"
              />
            </a-tab-pane>

            <a-tab-pane key="query" tab="申请与查询">
              <QueryTab
                ref="queryTabRef"
                :fields="allFields"
                v-model:dataset="detail"
              />
            </a-tab-pane>

            <a-tab-pane key="preview" tab="数据预览">
              <FieldPreview
                :dataset="{ workspaceId, ...detail }"
                :selectedContent="selectedContent"
                :fields="allFields"
              />
            </a-tab-pane>
          </a-tabs>
        </section>
      </main>
    </a-spin>

    <ModifyModal
      v-model:open="modifyModalOpen"
      :init-data="detail"
      :init-params="{ position: 'DATASET', type: 'ALL', workspaceId }"
      @ok="onEditOk"
    />
  </section>
</template>

<script setup>
import {
  computed,
  h,
  nextTick,
  onBeforeMount,
  ref,
  shallowRef,
  provide,
  toRaw
} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  LeftOutlined,
  EditOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import Aside from './components/Aside.vue'
import FieldTable from './components/FieldTable.vue'
import FieldPreview from './components/Preview.vue'
import ModifyModal from './components/ModifyModal.vue'
import { getFieldsByTableItem } from '@/apis/metaData'
import PartitionTab from './components/PartitionTab.vue'
import QueryTab from './components/QueryTab.vue'
import SelectedArea from './SelectedArea.vue'
import {
  getLastVersionById,
  postDataset,
  putDataset,
  postPublishById,
  getPreviewFields
} from '@/apis/dataset'
import { moveDirectory } from '@/apis/directory'
import { versionJs } from '@/versions'
import Resize from 'common/components/Layout/Resize/index.vue'
import { flat } from 'common/utils/array'

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

const workspaceId = computed(() => detail.value.workspaceId)

const toBack = () => {
  router.replace({ name: 'DatasetList' })
}

const toBackFrom = datasetId => {
  const { from, reportId } = route.query

  if (!from) {
    toBack()
  } else {
    const id = reportId || datasetId
    if (id) {
      router.replace({ name: from, params: { id } })
    } else {
      toBack()
    }
  }
}

const loading = ref(false)
const detailSuccess = ref(false)
// 数据集详情
const detail = ref({ workspaceId: appStore.workspaceId })

const fetchDetail = async id => {
  try {
    loading.value = true

    const res = await getLastVersionById(id)

    if (res.workspaceId && res.workspaceId !== workspaceId.value) {
      await appStore.setWorkspaceId(res.workspaceId, true)
    }

    const { content, layout } = res.config

    if (typeof content === 'undefined') {
      transformCompatibleConfig(res)
    } else {
      const { tables = [], joinDescriptors = [] } = content
      selectedContent.tables = tables
      selectedContent.joinDescriptors = joinDescriptors
    }

    if (layout) {
      // 兼容历史数据的兼容
      let layoutObj = JSON.parse(layout)
      try {
        layoutObj.cells.forEach(item => {
          if (item.shape === 'shape-node') {
            if (item.ports?.groups) {
              Object.keys(item.ports.groups).forEach(g => {
                item.ports.groups[g].attrs.circle.r = 0
              })
            }
          }
        })
      } catch (error) {
        console.error('兼容布局错误', error)
      }

      graph.value.fromJSON(layoutObj)
    }

    detail.value = {
      ...res,
      _extraConfig:
        typeof res.extraConfig === 'string'
          ? JSON.parse(res.extraConfig)
          : undefined
    }

    transformSelectedContent()
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
    nextTick(() => {
      detailSuccess.value = true
    })
    modifyModalOpen.value = true
  }
})

// 兼容历史数据
const transformCompatibleConfig = ({ config, ...res }) => {
  const { datasourceName, dbName, engine, tableName } = config

  const tableItem = {
    alias: 't1',
    columns: res.fields.map(t => t.name),
    datasourceName,
    dbName,
    engine,
    tableName
  }

  selectedContent.tables[0] = tableItem
  graph.value.__initNode__(tableItem).then(({ node }) => {
    node.position(30, 50)
  })
}

const transformSelectedContent = () => {
  const { tables = [], joinDescriptors = [] } = selectedContent
  const pros = Array(tables.length)

  for (let i = 0; i < tables.length; i++) {
    const table = tables[i]
    const p = { ...table }
    delete p.columns
    delete p.filters

    pros[i] = getFieldsByTableItem(p).then(r => {
      const originFields = r.fields
      const columns = table.columns.filter(c =>
        originFields.some(t => t.name === c)
      )
      selectedContent.tables[i] = {
        ...table,
        originFields,
        columns
      }
      delete selectedContent.tables[i]['fields']
    })
  }

  Promise.all(pros).then(() => {
    // 从底层表更新关联字段
    selectedContent.joinDescriptors = joinDescriptors.map(des => {
      const { leftTableAlias, rightTableAlias, joinType, joinFields = [] } = des

      const source = selectedContent.tables.find(
        t => t.alias === leftTableAlias
      )
      const target = selectedContent.tables.find(
        t => t.alias === rightTableAlias
      )

      return {
        sourceAlias: leftTableAlias,
        targetAlias: rightTableAlias,
        joinType: joinType,
        joinFields: joinFields.map(({ leftFieldName, rightFieldName }) => {
          const sOField = source.originFields.find(
            t => t.name === leftFieldName
          )
          const tOField = target.originFields.find(
            t => t.name === rightFieldName
          )

          return {
            sourceField: sOField?.name,
            targetField: tOField?.name
          }
        })
      }
    })

    detailSuccess.value = true
    detail.value.config = getDatasetConfig()
    reloadTableFields(true)
  })
}

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
        : dataType
    }
  })
}

const partitionTabRef = ref(null)
const queryTabRef = ref(null)

const move = dId => {
  try {
    const payload = {
      position: 'DATASET',
      type: 'ALL',
      workspaceId: workspaceId.value,
      targetId: dId,
      folderId: detail.value.folderId
    }

    return moveDirectory(payload)
  } catch (error) {
    console.error('更新文件夹位置失败', error)
  }
}

// 字段校验
const validateFieldsError = (fields = []) => {
  return fields.some(field => {
    const { status, displayName, name } = field

    // HIDE VIEW
    // 显示字段的展示名称必填
    if (status === 'VIEW' && !displayName) {
      message.warn(`${name}字段展示名称不能为空`)

      return true
    }
  })
}

const validateJoinDescriptors = () => {
  const { tables = [], joinDescriptors = [] } = toRaw(selectedContent)
  if (tables.length > 1 && !joinDescriptors.length) {
    message.warn('关联表未配置关联字段，请修改后再提交')
    return false
  }

  if (
    joinDescriptors.some(({ sourceAlias, targetAlias, joinFields = [] }) => {
      if (
        !joinFields.length ||
        joinFields.some(
          ({ sourceField, targetField }) => !sourceField || !targetField
        )
      ) {
        message.warn('关联表未配置关联字段，请修改后再提交')
        return true
      } else {
        const sourceTable = tables.find(t => t.alias === sourceAlias)
        const targetTable = tables.find(t => t.alias === targetAlias)
        const sourceHasDt = sourceTable.originFields.some(
          versionJs.ViewsDatasetModify.isDt
        )
        const targetHasDt = targetTable.originFields.some(
          versionJs.ViewsDatasetModify.isDt
        )

        if (sourceHasDt && targetHasDt) {
          const sTFHasDt = flat(sourceTable?.filters?.children || []).some(
            t => t.name === versionJs.ViewsDatasetModify.dtFieldName
          )
          const tTFHasDt = flat(targetTable?.filters?.children || []).some(
            t => t.name === versionJs.ViewsDatasetModify.dtFieldName
          )

          // 关联字段都没有dt
          const joinWithoutDt = joinFields.every(
            ({ sourceField, targetField }) =>
              sourceField !== versionJs.ViewsDatasetModify.dtFieldName ||
              targetField !== versionJs.ViewsDatasetModify.dtFieldName
          )

          // 任一表有dt作为过滤条件时，不校验dt作为关联字段
          if (!sTFHasDt && !tTFHasDt && joinWithoutDt) {
            message.warn('dt必须作为关联字段，请修改后再提交')
            return true
          }
        } else {
          if (
            joinFields.some(({ sourceField: sF, targetField: tF }) => {
              const sOField = sourceTable.originFields.find(t => t.name === sF)
              const tOField = targetTable.originFields.find(t => t.name === tF)

              return sOField.dataType !== tOField.dataType
            })
          ) {
            message.warn('表关联字段类型不一致，请修改后再提交')
            return true
          }
        }
      }
    })
  ) {
    return false
  }

  return true
}

const getContentConfig = () => {
  const { tables: ts = [], joinDescriptors: ds = [] } = toRaw(selectedContent)
  const tables = ts.map(t => {
    const r = { ...t }
    delete r.fields
    delete r.originFields
    return r
  })
  const joinDescriptors = ds.map(d => {
    const { sourceAlias, targetAlias, joinType, joinFields = [] } = d
    return {
      leftTableAlias: sourceAlias,
      rightTableAlias: targetAlias,
      joinType,
      joinFields: joinFields.map(({ sourceField, targetField }) => {
        return {
          leftFieldName: sourceField,
          joinType,
          rightFieldName: targetField
        }
      })
    }
  })

  return {
    tables,
    joinDescriptors
  }
}

/**
 * 保存数据集
 * @returns 返回保存后的数据集ID，如果发生错误则返回Promise.reject()
 */
const save = async () => {
  try {
    if (!detail.value.name?.trim().length) {
      message.warn('数据集名称不能为空')

      return
    }

    saveLoading.value = true

    const fields = tableRef.value.getTableData()
    if (validateFieldsError(fields)) return

    const partitionData = await partitionTabRef.value?.validate()
    const queryData = await queryTabRef.value?.validate()

    const extraConfig = {
      ...detail.value._extraConfig,
      ...partitionData,
      ...queryData,
      enableApply: queryData?.enableApply ?? detail.value.enableApply
    }

    const payload = {
      ...detail.value,
      config: getDatasetConfig(true),
      workspaceId: workspaceId.value,
      fields: _transformFieldsPayload(fields),
      extraConfig: JSON.stringify(extraConfig)
    }

    const fn = !!payload.id
      ? () => putDataset(payload.id, payload)
      : () => postDataset(payload)
    const res = await fn()

    if (detail.value.folderId !== res.folder.id) {
      move(res.id)
    }

    detail.value = {
      ...res,
      _extraConfig:
        typeof res.extraConfig === 'string'
          ? JSON.parse(res.extraConfig)
          : undefined
    }
    message.success('保存成功')

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

    if (!validateJoinDescriptors()) {
      return
    }

    const id = await save(true)
    await postPublishById(id)

    message.success('发布成功')
    toBackFrom(id)
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
// const tableFields = ref([])

const tabKey = ref('fields')

/**
 * 重新加载表格字段
 * @param isInit 是否为初始化状态，默认为false
 * @returns 无返回值，但会更新表格字段数据
 */
const reloadTableFields = async (isInit = false) => {
  try {
    tableFieldsLoading.value = true

    const selectedTables = selectedContent.tables
    // 获取前一装填的数据集字段
    const prevFields = isInit
      ? detail.value.fields
      : tableRef.value?.getTableData()
    // 新增字段
    const addFields = prevFields.filter(field => field.type === 'ADD')
    // 合并后的字段
    let newFields = []

    // 单表 直接从源表更新字段
    if (isSingleTable.value) {
      const originFields = selectedTables[0].originFields || []
      const originDiff = originFields.filter(f =>
        prevFields.every(t => t.sourceFieldName !== f.name)
      )
      selectedTables[0].columns = originFields.map(t => t.name)

      newFields = prevFields.concat(
        originDiff.map(t => ({
          ...t,
          _fromOrigin: true,
          sourceFieldName: t.name,
          tableAlias: selectedTables[0].alias
        }))
      )
    } else {
      if (isInit) {
        newFields = prevFields.filter(field => {
          // 新增字段
          if (field.type === 'ADD') return true

          const { tableAlias, sourceFieldName } = field

          // 无表别名（历史数据）
          const table = !tableAlias
            ? selectedTables[0]
            : selectedTables.find(t => t.alias === tableAlias)

          // 从源表中删除字段
          if (!table.columns?.includes(sourceFieldName)) return false

          return true
        })
      } else {
        newFields = selectedTables.reduce((acc, table) => {
          const { alias, columns = [], originFields = [] } = table
          const fs = columns.map(t => {
            const field = originFields.find(f => f.name === t)
            if (field) {
              field._fromOrigin = true
              field.sourceFieldName = field.name
            }
            const oldField = prevFields.find(
              f => f.tableAlias === alias && f.name === t
            )
            return {
              ...field,
              ...oldField,
              tableAlias: alias
            }
          })

          return acc.concat(fs)
        }, [])

        // 将新增字段顺序放到最后
        // newFields = newFields.concat(addFields)
      }
    }

    const payload = {
      datasetId: detail.value.id,
      tables: selectedTables.map(table => {
        const { alias, dbName, tableName, columns = [], filters } = table
        return { alias, dbName, tableName, columns, filters }
      }),
      fields: prevFields
    }
    const res = await getPreviewFields(payload)

    newFields = Object.keys(res).map(aliasName => {
      const [alias, name] = aliasName.split('.')
      const prev = newFields.find(
        t => t.tableAlias === alias && t.sourceFieldName === name
      )

      if (prev) {
        return {
          ...prev,
          name: selectedTables.length > 1 ? res[aliasName] : name,
          sourceFieldName: name
        }
      }
    })

    detail.value.fields = newFields.concat(addFields)

    tableRef.value?.init().then(() => {
      setTimeout(() => {
        tableFieldsLoading.value = false
      }, 10)
    })
  } catch (error) {
    console.error('获取预览字段失败', error)
    tableFieldsLoading.value = false
  }
}

const getDatasetConfig = (needLayout = false) => {
  return {
    ...detail.value.config,
    engine:
      detail.value?.config?.engine ?? selectedContent.tables[0]?.['engine'],
    content: getContentConfig(),
    layout: needLayout ? JSON.stringify(graph.value.toJSON()) : undefined
  }
}

const selectedContent = reactive({
  tables: [],
  joinDescriptors: []
})

// 单表
const isSingleTable = computed(() => selectedContent.tables.length === 1)

const canPublish = computed(() => selectedContent.tables.length > 0)

const asideRef = ref(null)

const graph = ref(null)
provide('index', {
  getDatasetConfig,
  getDatasetFields: () => tableRef.value?.getTableData(),
  selectedContent: {
    getConfig: getContentConfig,
    get(t) {
      return t ? selectedContent[t] : selectedContent
    },
    updateTable(table) {
      if (!table) return
      const index = selectedContent.tables.findIndex(
        t => t.alias === table.alias
      )
      selectedContent.tables[index] = table
    },
    updateJoinDescriptor(descriptor) {
      const { sourceAlias, targetAlias, joinType, joinFields } = descriptor
      const item = selectedContent.joinDescriptors.find(
        d => d.sourceAlias === sourceAlias && d.targetAlias === targetAlias
      )
      if (!item) {
        selectedContent.joinDescriptors.push(descriptor)
        graph.value.__updateEdgeLabel__(targetAlias, joinType)
      } else {
        if (item.joinType !== joinType) {
          graph.value.__updateEdgeLabel__(targetAlias, joinType)
        }
        item.joinType = joinType
        item.joinFields = joinFields
      }
    },
    addTable(p, payload) {
      const selectTable = selectedContent.tables
      const toNames = l => l.map(t => t.name)

      tableFieldsLoading.value = true

      getFieldsByTableItem(p)
        .then(r => {
          const item = {
            ...r,
            originFields: r.fields,
            alias: p.alias,
            columns: toNames(r.fields)
          }
          delete item.fields

          if (!selectTable.length) {
            selectedContent.tables.push(item)
          } else {
            const { source } = payload
            const sourceTable = selectTable.find(
              t => t.tableName === source.tableName
            )
            const hasDt = sourceTable.columns.some(
              t => t === versionJs.ViewsDatasetModify.dtFieldName
            )
            // 源表有dt，目标表不勾选dt
            const fields = r.fields.filter(t =>
              hasDt ? !versionJs.ViewsDatasetModify.isDt(t) : true
            )
            selectedContent.tables.push({ ...item, columns: toNames(fields) })

            const { alias: tAlias } = p,
              { alias: sAlias } = source
            selectedContent.joinDescriptors.push({
              sourceAlias: sAlias,
              targetAlias: tAlias,
              joinType: 'LEFT',
              joinFields: []
            })
          }

          reloadTableFields()
        })
        .catch(e => {
          console.error('添加表失败', e)
          graph.value.__removeCell__(p.alias, () => {
            asideRef.value?.reloadSelected()
            setTimeout(() => {
              tableFieldsLoading.value = false
            }, 10)
          })
        })
    },
    reloadTableFields
  },
  graph: {
    get() {
      return graph.value
    },
    set(g) {
      graph.value = g
    },
    // 删除节点
    onNodeRemoved: ({ alias }) => {
      const { tables = [], joinDescriptors = [] } = selectedContent
      const index = tables.findIndex(t => t.alias === alias)
      selectedContent.tables.splice(index, 1)
      selectedContent.joinDescriptors = joinDescriptors.filter(
        t => t.sourceAlias !== alias && t.targetAlias !== alias
      )

      reloadTableFields()
    },
    // 移动节点
    onNodeMoved({ oldSource, newSource, target }) {
      // 无源节点 - 新的边连线
      if (!newSource || !oldSource) return

      // 相同的源节点
      if (newSource.alias === oldSource.alias) return

      const index = selectedContent.joinDescriptors.findIndex(
        ({ sourceAlias, targetAlias }) => {
          return oldSource.alias === sourceAlias && target.alias === targetAlias
        }
      )

      selectedContent.joinDescriptors.splice(index, 1)

      // 非游离的节点
      if (newSource.alias !== target.alias) {
        graph.value.__updateEdgeLabel__(target.alias, 'LEFT')
      }
    }
  }
})

const asideWidth = computed(() => {
  const width = appStore.appLayout?.datasetModify?.aside?.width

  if (typeof width === 'undefined') {
    return 300
  }
  return width
})

const graphHeight = computed(() => {
  const height = appStore.appLayout?.datasetModify?.graph?.height

  if (typeof height === 'undefined') {
    return 180
  }
  return height
})

const onAsideResized = e => {
  const payload = appStore.appLayout?.datasetModify
  appStore.setLayout('datasetModify', { ...payload, aside: { width: e.width } })
}
const onContentResized = e => {
  const payload = appStore.appLayout?.datasetModify
  appStore.setLayout('datasetModify', {
    ...payload,
    graph: { height: e.height }
  })

  tableRef.value.resize()
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

.content {
  flex: 1;
  display: flex;
  flex-direction: column;

  overflow: auto;

  & > .ant-tabs {
    flex: 1;
    overflow: hidden;
    :deep(.ant-tabs-nav) {
      margin-bottom: 0;
      margin-left: 12px;
    }
    :deep(.ant-tabs-content),
    :deep(.ant-tabs-tabpane) {
      height: 100%;
    }
  }
}
</style>
<style lang="scss">
.table-alias {
  display: inline-flex;
  vertical-align: middle;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  background-color: #f2f2f2;
  margin: 2px 4px;
  border-radius: 2px;
  &.empty {
    background-color: transparent;
  }
}
</style>
