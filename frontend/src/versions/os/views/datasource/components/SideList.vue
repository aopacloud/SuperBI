<template>
  <section class="section">
    <header class="header">
      <a-input-search
        style="flex: 1"
        allow-clear
        placeholder="请输入搜索"
        v-model:value="keyword"
        @search="onKeywordSeach" />
      <a-button
        type="primary"
        style="margin-left: 8px"
        :icon="h(PlusOutlined)"
        @click="emits('create')">
        数据源
      </a-button>
    </header>

    <main class="main" ref="mainRef">
      <a-spin :spinning="loading">
        <a-empty v-if="!list.length" style="margin-top: 50px" />

        <ul v-else class="list">
          <li class="item" v-if="pagerDisabled.prevShow">
            <a
              class="pager-prev-btn"
              :disabled="pagerDisabled.prevDisabled"
              @click="pagerCurrentChange(-1)">
              上一页
            </a>
          </li>

          <li
            class="item"
            v-for="(item, index) in list"
            :key="item.id"
            :class="{ active: activeIndex === index }"
            @click="select(index)">
            <span class="type">
              <img
                class="type-icon"
                :src="getEngineIcon(item.engine)"
                :alt="item.engine" />
            </span>
            <div class="content">
              <div class="title">{{ item.name }}</div>
              <div class="desc">
                <div class="desc-text">所有者: {{ item.creatorAlias }}</div>
                <a-dropdown>
                  <a-button
                    class="more-tools"
                    size="small"
                    type="text"
                    :icon="h(MoreOutlined)"
                    @click.stop />
                  <template #overlay>
                    <a-menu @click="e => onMenuClick(e, item)">
                      <a-menu-item
                        v-if="isAdmin || userStore.username === item.creator"
                        key="edit">
                        编辑
                      </a-menu-item>
                      <a-menu-item key="connect">连接测试</a-menu-item>
                      <a-menu-item key="copy">复制</a-menu-item>
                      <a-menu-item
                        v-if="isAdmin || userStore.username === item.creator"
                        key="del"
                        style="color: red">
                        删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
            </div>
          </li>

          <li class="item" v-if="pagerDisabled.nextShow">
            <a
              class="pager-next-btn"
              :disabled="pagerDisabled.nextDisabled"
              @click="pagerCurrentChange(1)">
              下一页
            </a>
          </li>
        </ul>
      </a-spin>
    </main>
  </section>
</template>

<script setup>
import { h, ref, computed, onMounted, reactive, nextTick } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, MoreOutlined } from '@ant-design/icons-vue'
import {
  getDatasourceList,
  deleteDatasourceById,
  postTestConnect,
} from '@/apis/datasource'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import { engines } from '../modify/config'

const emits = defineEmits(['create', 'edit', 'copy', 'select'])

const appStore = useAppStore()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.isSuperAdmin)
const workspaceId = computed(() => appStore.workspaceId)

const keyword = ref('')
const onKeywordSeach = () => {
  pager.current = 1

  fetchData()
}

const pager = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
})
const pagerDisabled = computed(() => {
  const { current, pageSize, total } = pager

  const prevShow = current > 1
  const nextShow = current * pageSize < total

  const prevDisabled = current === 1
  const nextDisabled = current * pageSize >= total

  return {
    prevShow,
    nextShow,
    prevDisabled,
    nextDisabled,
  }
})

const mainRef = ref()

const pagerCurrentChange = async current => {
  if (current < 0 && pagerDisabled.value.prevDisabled) return
  if (current > 0 && pagerDisabled.value.nextDisabled) return

  await fetchData(current)

  nextTick(() => {
    mainRef.value.scrollTo(0, pager.current > 1 ? 54 : 0)
  })
}

const queryParams = computed(() => {
  const kw = keyword.value.trim()
  const { current, pageSize } = pager

  return {
    workspaceId: workspaceId.value,
    keyword: kw,
    pageNum: current,
    pageSize,
  }
})

const loading = ref(false)
const list = ref([])

const fetchData = async (current = 0) => {
  try {
    loading.value = true

    const { total = 0, data = [] } = await getDatasourceList({
      ...queryParams.value,
      pageNum: queryParams.value.pageNum + current,
      reasonable: false,
    })

    pager.total = total
    pager.current += current
    list.value = data

    if (!data.length) {
      activeIndex.value = -1

      emits('select', {})

      return
    }

    if (activeIndex.value < 0) {
      activeIndex.value = 0
    } else if (activeIndex.value > data.length - 1) {
      activeIndex.value = data.length - 1
    }

    emits('select', data[activeIndex.value])
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const getEngineIcon = engine => {
  const item = engines.find(t => t.engine === engine)

  if (!item) return

  return item.icon
}

const activeIndex = ref(-1)
const select = index => {
  activeIndex.value = index

  const item = list.value[index]
  emits('select', item)
}

defineExpose({
  reload: fetchData,
})

onMounted(fetchData)

const onMenuClick = ({ key }, row) => {
  switch (key) {
    case 'edit':
      emits('edit', row)
      break
    case 'connect':
      connect(row)
      break
    case 'copy':
      emits('copy', row)
      break
    case 'del':
      delRecord(row)
      break
    default:
      break
  }
}

// 测试连接
const connect = async row => {
  if (row._connectting) return

  try {
    row._connectting = true

    const { pass } = await postTestConnect(row)

    const statusText = pass ? '连接成功' : '连接失败，请检查数据源配置或网络情况'
    if (!pass) {
      message.error(statusText)
    } else {
      message.success(statusText)
    }

    row._status = pass ? 'success' : 'error'
    row._statusText = statusText
  } catch (error) {
    console.error('测试连接失败', error)
  } finally {
    row._connectting = false
  }
}

const delRecord = async row => {
  Modal.confirm({
    title: '提示',
    content: `正在操作删除数据源【${row.name}】，该操作不可撤回，确定继续删除操作?`,
    okType: 'danger',
    okText: '确定',
    onOk() {
      return submitDel(row)
    },
  })
}

const submitDel = async row => {
  try {
    await deleteDatasourceById(row.id)
    message.success('删除成功')

    fetchData()
  } catch (error) {
    console.error('删除失败', error)
  }
}
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.header {
  display: flex;
  align-items: center;
}
.main {
  flex: 1;
  margin-top: 24px;
  overflow: auto;
}

.item {
  display: flex;
  padding: 4px 6px;
  margin-bottom: 6px;
  transition: all 0.2s;
  cursor: pointer;
  &:hover {
    background-color: #f0f2f5;

    .more-tools {
      opacity: 1;
    }
  }
  &.active {
    background-color: #f0f2f5;
    .title {
      color: #1677ff;
      font-weight: 600;
    }
  }

  .type {
    margin-right: 6px;
    align-self: center;
  }

  .type-icon {
    display: inline-block;
    width: 24px;
    height: 24px;
  }
  .content {
    flex: 1;
  }
  .title {
    line-height: 26px;
    font-size: 14px;
    // font-weight: 600;
  }
  .desc {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .desc-text {
      color: #999;
    }
  }
  .more-tools {
    opacity: 0;
  }
}

.pager-prev-btn,
.pager-next-btn {
  flex: 1;
  display: block;
  line-height: 24px;
  text-align: center;
  &:hover {
    background-color: #f0f2f5;
  }
}
</style>
