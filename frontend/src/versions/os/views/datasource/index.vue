<template>
  <section class="page-section">
    <aside class="page-aside">
      <SideList
        ref="sideListRef"
        @create="toCreate"
        @edit="onEdit"
        @copy="onCopy"
        @select="onSideSelected" />
    </aside>
    <main class="page-main">
      <a-tabs :disabled="isEmpty" class="main-tabs">
        <a-tab-pane :disabled="isEmpty" key="info" tab="基础信息">
          <a-empty
            v-if="isEmpty"
            style="margin-top: 200px"
            description="暂未选择数据源" />

          <DetailInfo v-else :info="detail" />
        </a-tab-pane>
        <a-tab-pane :disabled="isEmpty" key="list" tab="数据列表">
          <TableList v-if="!isEmpty" :id="detail.id" />
        </a-tab-pane>
      </a-tabs>
    </main>

    <!-- 数据源新增编辑 -->
    <ModifyModal
      v-model:open="modifyModalOpen"
      :mode="modifyMode"
      :initData="modifyInfo"
      @ok="onModifyOk" />
  </section>
</template>

<script setup>
import { ref } from 'vue'
import SideList from './components/SideList.vue'
import DetailInfo from './components/DetailInfo.vue'
import ModifyModal from './modify/index.vue'
import TableList from './TableList.vue'

const sideListRef = ref()

const modifyMode = ref(0)
const modifyModalOpen = ref(false)
const modifyInfo = ref({})
const onModifyOk = () => {
  sideListRef.value?.reload()
}

// 新增
const toCreate = () => {
  modifyMode.value = 0
  modifyInfo.value = {}
  modifyModalOpen.value = true
}
// 编辑
const onEdit = row => {
  modifyMode.value = 1
  modifyInfo.value = { ...row }
  modifyModalOpen.value = true
}
// 复制
const onCopy = row => {
  modifyMode.value = 2
  modifyInfo.value = { ...row }
  modifyModalOpen.value = true
}

const onSideSelected = item => {
  detail.value = item
}

const detail = ref({})
const isEmpty = computed(() => !detail.value.id)
// 获取数据源详情
</script>

<style lang="scss" scoped>
.page-section {
  display: flex;
  height: 100%;
  padding: 12px;
}
.page-aside {
  width: 360px;
  margin-top: 16px;
  padding-right: 20px;
  border-right: 1px solid #f3f3f3;
}
.page-main {
  flex: 1;
  margin-left: 20px;
}

.main-tabs {
  height: 100%;
  :deep(.ant-tabs-content),
  :deep(.ant-tabs-tabpane) {
    height: 100%;
  }
}
</style>
