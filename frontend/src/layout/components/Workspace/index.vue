<template>
  <div>
    <Compact style="align-items: center">
      <a-select
        class="ghost-select"
        style="width: 120px"
        placeholder="选择空间"
        v-model:value="value"
        @change="onChange">
        <a-select-option v-for="item in list" :key="item.id" :title="item.name">
          {{ item.name }}
        </a-select-option>
      </a-select>
      <a-tooltip v-if="hasReadPermission" title="空间管理">
        <router-link :to="{ name: 'SystemWorkspace' }">
          <a-button type="primary" :icon="h(AppstoreOutlined)"></a-button>
        </router-link>
      </a-tooltip>
    </Compact>
  </div>
</template>

<script setup>
import { h, ref, computed } from 'vue'
import { Compact } from 'ant-design-vue'
import { AppstoreOutlined } from '@ant-design/icons-vue'
import useAppStore from '@/store/modules/app'
import useResourceStore from '@/store/modules/resource'

const appStore = useAppStore()
const list = computed(() => {
  return appStore.workspaceList
})

const resourceStore = useResourceStore()
const hasReadPermission = computed(() => {
  return resourceStore.routes.some(t => t.name === 'SystemWorkspace')
})

const value = ref(appStore.workspaceId)
const onChange = e => {
  appStore.setWorkspaceId(e)

  location.reload()
}
</script>

<style lang="scss" scoped></style>
