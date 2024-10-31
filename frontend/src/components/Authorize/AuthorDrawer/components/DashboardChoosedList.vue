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
import { ref, shallowRef, onMounted } from 'vue'
import SelectList from '@/common/components/ExtendSelect'
import { getDashboardSharableList } from '@/apis/authorize'

const props = defineProps({
  workspaceId: {
    type: Number,
  },
  disabled: {
    type: Boolean,
  },
})

const loading = ref(false)
const list = shallowRef([])
const fetchList = async () => {
  try {
    loading.value = true

    const res = await getDashboardSharableList({ workspaceId: props.workspaceId })
    list.value = res
  } catch (error) {
    console.error('获取看板列表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>

<style lang="scss" scoped></style>
