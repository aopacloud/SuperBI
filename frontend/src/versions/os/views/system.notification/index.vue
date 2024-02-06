<template>
  <section class="page-section">
    <header class="header">
      <a-tabs v-model:activeKey="activeKey" @change="onTabChange">
        <a-tab-pane
          class="custom-tab"
          key="PERMISSION"
          tab="权限相关"
          @click="loadData('PERMISSION')" />
        <a-tab-pane
          class="custom-tab"
          key="APPROVAL"
          tab="审批相关"
          @click="loadData('APPROVAL')" />
        <a-tab-pane
          class="custom-tab"
          key="ACCOUNT"
          tab="账号相关"
          @click="loadData('ACCOUNT')" />

        <template #rightExtra>
          <a-input-search
            placeholder="请输入搜索"
            allow-clear
            style="width: 260px"
            v-model:value="keyword"
            @search="onKeywordSearch" />
        </template>
      </a-tabs>
    </header>

    <a-spin :spinning="loading">
      <main class="main" ref="mainListRef">
        <a-empty style="margin-top: 100px" v-if="!list.length" />

        <ul class="list" v-else>
          <ListItem
            v-for="item in list"
            :key="item.id"
            :activeKey="activeKey"
            :item="item"
            :observe="ob"
            @reject="onReject"
            @resolve="onResolve"
            @reApply="onReApply" />
        </ul>
      </main>
    </a-spin>

    <footer class="footer text-right" style="margin-right: 14px">
      <a-pagination
        v-model:current="pager.current"
        v-model:pageSize="pager.pageSize"
        :total="pager.total"
        @change="fetchData()" />
    </footer>
  </section>

  <!-- 驳回 -->
  <RejectModal v-model:open="rejectModalOpen" :initData="rowInfo" />

  <!-- 重新申请 -->
  <ApplyModal v-model:open="reApplyModalOpen" :initData="rowInfo" @ok="onApplyOk" />

  <!-- 通过并授权 -->
  <AuthorDrawer
    v-model:open="authorDrawerOpen"
    :selectable="false"
    :initData="rowInfo"
    :beforeSubmit="pass"
    @success="fetchData()" />
</template>

<script setup lang="jsx">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import ListItem from './components/Item.vue'
import RejectModal from '@/components/RejectModal/index.vue'
import ApplyModal from '@/components/ApplyModal/index.vue'
import AuthorDrawer from '@/components/Authorize/AuthorDrawer.vue'
import { getNotificationList, postReaded } from '@/apis/system/notification'
import { postPassApply } from '@/apis/dataset/apply'

const activeKey = ref('PERMISSION')
const onTabChange = () => {
  pager.current = 1
  list.value = []

  fetchData()
}

const keyword = ref('')
const onKeywordSearch = () => {
  pager.current = 1
  fetchData()
}

const loading = ref(false)
const list = ref([])
const pager = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
})

const queryParams = computed(() => {
  const kw = keyword.value.trim()
  const { current: pageNum, pageSize } = pager

  return { type: activeKey.value, pageNum, pageSize, keyword: kw }
})

const fetchData = async payload => {
  try {
    loading.value = true

    const { data = [], total = 0 } = await getNotificationList({
      ...queryParams.value,
      ...payload,
    })

    list.value = data.map(item => {
      return { ...item, content: JSON.parse(item.content) }
    })
    pager.total = total
    mainListRef.value.scrollTo(0, 0)
  } catch (error) {
    console.error('通知消息获取失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const rejectModalOpen = ref(false)
const rowInfo = ref({})

// 驳回
const onReject = async e => {
  rowInfo.value = {
    ...e,
    id: e.content.applyId,
    datasetName: e.resourceName,
    aliasName: e.content.aliasName,
  }
  rejectModalOpen.value = true
}

// 重新申请
const reApplyModalOpen = ref(false)
const onReApply = async e => {
  rowInfo.value = { ...e, name: e.resourceName }
  reApplyModalOpen.value = true
}

const onApplyOk = e => {
  message.success(
    <span>
      申请已提交，可提醒直属上级在云之家进行审批，前往
      <a href='#/authority/apply' target='_blank'>
        授权中心
      </a>
      查看进展
    </span>
  )
}

const pass = () => postPassApply(rowInfo.value.content.applyId)
// 通过并授权
const authorDrawerOpen = ref(false)
const onResolve = async e => {
  rowInfo.value = {
    ...e,
    _id: e.id,
    id: undefined,
    name: e.resourceName,
    datasetId: e.resourceId,
    username: e.content.proposer,
  }
  authorDrawerOpen.value = true
}

// 提交已读
const submitReaded = async () => {
  try {
    const ids = shouldReadIds.value

    await postReaded(ids)

    list.value.forEach(item => {
      if (ids.includes(item.id)) {
        item.readed = true
      }
    })
  } catch (error) {
    const ids = shouldReadIds.value
    list.value.forEach(item => {
      if (ids.includes(item.id)) {
        item.readed = false
      }
    })
  }
}

// 需要已读id
const shouldReadIds = ref([])
watch(
  shouldReadIds,
  l => {
    if (!l.length) return

    submitReaded()
  },
  { deep: true }
)

const observeCb = entries => {
  const result = []
  for (const entry of entries) {
    const { target, isIntersecting } = entry

    const targetId = target.getAttribute('data-notificationId')
    const item = list.value.find(t => t.id === +targetId)

    if (item && !item.readed && isIntersecting) {
      result.push(item.id)
    }
  }

  shouldReadIds.value = result
}
const mainListRef = ref()
const ob = new IntersectionObserver(observeCb, {
  root: mainListRef.value,
  rootMargin: '6px 12px 6px 12px',
  threshold: 0.8,
  delay: 1000,
})

onUnmounted(() => {
  ob.disconnect()
})
</script>

<style lang="scss" scoped>
.page-section {
  display: flex;
  flex-direction: column;
  height: 100%;

  .ant-spin-nested-loading {
    flex: 1;
    margin-top: 6px;
    overflow: hidden;
    :deep(.ant-spin-container) {
      height: 100%;
    }
  }
}
.header {
  padding: 0 16px;
  :deep(.ant-tabs-nav) {
    margin-bottom: 0;
  }
}
.main {
  height: 100%;
  padding: 6px 12px;
  overflow-y: scroll;
  .ant-spin-nested-loading {
    height: 100%;
  }
}
.footer {
  padding: 12px;
  border-top: 1px solid #e8e8e8;
}
.list {
  min-height: 240px;
  margin: 0;
  .item {
    &:not(:last-child) {
      border-bottom: 1px solid #e8e8e8;
    }
  }
}

.pager-btn {
  line-height: 24px;
  margin: 0;
}
.pager-btn-prev,
.pager-btn-next {
  display: block;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
  &:hover {
    background-color: rgba(0, 0, 0, 0.06);
  }
}
</style>
