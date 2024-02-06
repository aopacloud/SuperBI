<template>
  <section class="page flex">
    <aside class="aside" style="width: 300px">
      <AsideSection
        @updateAll="setAll"
        ref="asideRef"
        @select="onAsideClick"
        @create="onCreate" />
    </aside>

    <main class="content flex-1 scroll">
      <MainSection ref="mainRef" :info="selectInfo" @edit="onEdit" @del="onDel" />
    </main>
  </section>

  <!-- 空间新增、编辑 -->
  <ModifyModal
    v-model:open="modifyModalOpen"
    :allList="allAsideList"
    :initData="modifyInfo"
    @ok="onModifyModalOk" />
</template>

<script setup>
import { ref, shallowRef } from 'vue'
import { Modal, message } from 'ant-design-vue'
import AsideSection from './components/Aside.vue'
import MainSection from './components/Main.vue'
import ModifyModal from './components/ModifyModal.vue'
import { deleteWorkspace } from '@/apis/workspace'

// 所有可管理的空间列表
const allAsideList = shallowRef([])
const setAll = e => {
  allAsideList.value = e
}

const selectInfo = ref({})
const onAsideClick = e => {
  selectInfo.value = e
}

const modifyModalOpen = ref(false)
const modifyInfo = ref({})
// 新增
const onCreate = () => {
  modifyInfo.value = {}
  modifyModalOpen.value = true
}
// 编辑
const onEdit = () => {
  modifyInfo.value = { ...selectInfo.value }
  modifyModalOpen.value = true
}
// 删除
const onDel = () => {
  Modal.confirm({
    title: '提示',
    content: '空间删除后无法恢复，确认删除？',
    onOk: () =>
      deleteWorkspace(selectInfo.value.id).then(r => {
        message.success('空间删除成功')
        asideRef.value.reload()
      }),
  })
}

const asideRef = ref()
const mainRef = ref()
const onModifyModalOk = e => {
  asideRef.value.reload()
  mainRef.value.update(e)

  if (selectInfo.value.id === e.id) {
    selectInfo.value = { ...selectInfo.value, ...e }
  }
}
</script>

<style lang="scss" scoped></style>
