<template>
  <SelectList
    v-bind="$attrs"
    keyField="id"
    labelField="name"
    :loading="loading"
    :disabled="disabled"
    :dataSource="list">
    <template #itemLabel="{ item }">
      {{ item.name }}
      <span class="font-help2">{{ item.creatorAlias }} </span>
    </template>
  </SelectList>
</template>

<script setup>
import { ref, shallowRef, watch, onMounted } from 'vue'
import SelectList from '@/common/components/ExtendSelect'
import { getDatasetAuthorizableList } from '@/apis/authorize'

const props = defineProps({
  resource: {
    type: String,
    default: 'DATASET',
    validator: s => ['DATASET', 'DASHBOARD'].includes(s),
  },
  workspaceId: {
    type: Number,
  },
  disabled: {
    type: Boolean,
  },
  dashboardIds: {
    type: Array,
    default: () => [],
  },
})

const loading = ref(false)
const list = shallowRef([])
const fetchList = async () => {
  try {
    loading.value = true

    const res = await getDatasetAuthorizableList({
      workspaceId: props.workspaceId,
      dashboardIds: props.dashboardIds.join(','),
    })

    list.value = res
  } catch (error) {
    console.error('获取所有数据集失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)

watch(() => props.dashboardIds, fetchList)
</script>

<style lang="scss" scoped></style>
