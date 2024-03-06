<template>
  <a-modal
    wrapClassName="fullscreen-modal"
    :open="open"
    :closable="false"
    :footer="null"
    @cancel="exit">
    <template #title>
      <header class="header">
        <div>
          <a-button type="text" :icon="h(LeftOutlined)" @click="toBack" />
          <span class="title">{{ title }}</span>
          <span v-if="detail.engine" class="sub-title">{{ detail.engine }}</span>
        </div>

        <a-space class="action" v-if="detail.engine">
          <a-button @click="exit">退出</a-button>
          <a-button ghost type="primary" :loading="connectloading" @click="connect">
            测试连接
          </a-button>
          <a-button type="primary" :loading="saveLoading" @click="save">保存</a-button>
        </a-space>
      </header>
    </template>

    <keep-alive>
      <List v-if="!detail.engine && !id" @click="toCreate" />

      <ClickhouseCreate
        v-else
        ref="createRef"
        :initData="detail"
        :connectting="connectloading"
        @connect="connect" />
    </keep-alive>
  </a-modal>
</template>

<script setup>
import { h, ref, computed, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { LeftOutlined } from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import List from './components/List.vue'
import ClickhouseCreate from './components/Clickhouse.vue'
import {
  postDatasource,
  getDatasourceDetailById,
  putDatasource,
  postTestConnect,
} from '@/apis/datasource'

const appStore = useAppStore()

const emits = defineEmits('update:open', 'ok')
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
  // 模式 0 新增 1 编辑 2 复制
  mode: {
    type: Number,
    default: 0,
    validator: val => [0, 1, 2].includes(val),
  },
})

const workspaceId = computed(() => appStore.workspaceId)
const title = computed(() => (!!detail.value.id ? '编辑数据源' : '新建数据源'))

watch(
  () => props.open,
  op => {
    if (op) {
      detail.value.engine = ''

      if (props.mode === 0) return

      if (props.mode === 1) {
        fetchDetail(props.initData.id)
      } else {
        const { id, engine } = props.initData

        detail.value.engine = engine

        fetchDetail(id).then(() => {
          detail.value.id = undefined
        })
      }
    }
  }
)

const toBack = () => {
  if (detail.value.engine) {
    detail.value = {}
  } else {
    emits('update:open', false)
  }
}

const exit = () => {
  message.info('内容未保存')

  emits('update:open', false)
}

const loading = ref(false)
const detail = ref({})
// 获取详情
const fetchDetail = async id => {
  try {
    loading.value = true

    const res = await getDatasourceDetailById(id)

    detail.value = res
  } catch (error) {
    console.error('获取数据源详情失败', error)
  } finally {
    loading.value = false
  }
}

const toCreate = engine => {
  detail.value = { engine }
  nextTick(() => {
    createRef.value.reset?.()
  })
}

const connectloading = ref(false)
const connect = async () => {
  if (connectloading.value) return

  try {
    connectloading.value = true

    const createRes = await createRef.value.validate()

    const { pass } = await postTestConnect({
      id: detail.value.id,
      engine: detail.value.engine,
      workspaceId: workspaceId.value,
      ...createRes,
    })

    if (!pass) {
      message.error('连接失败，请检查数据源配置或网络情况')
    } else {
      message.success('连接成功')
    }
  } catch (error) {
    console.error('测试连接失败', error)
  } finally {
    connectloading.value = false
  }
}

const createRef = ref()
const saveLoading = ref(false)
const save = async () => {
  try {
    saveLoading.value = false

    const createRes = await createRef.value.validate()

    const fn = detail.value.id
      ? () =>
          putDatasource(detail.value.id, {
            engine: detail.value.engine,
            ...createRes,
          })
      : () =>
          postDatasource({
            engine: detail.value.engine,
            workspaceId: workspaceId.value,
            ...createRes,
          })

    const res = await fn()

    message.success('保存成功')
    emits('ok', res)
    emits('update:open', false)
  } catch (error) {
    console.error('创建失败', error)
  } finally {
    saveLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  padding: 12px;
}
.header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}
.title {
  font-weight: 400;
}
.sub-title {
  margin-left: 6px;
  color: #999;
  &::before {
    content: '/';
    margin-right: 6px;
  }
}
</style>
