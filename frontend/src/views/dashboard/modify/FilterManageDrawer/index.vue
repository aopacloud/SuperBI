<template>
  <a-drawer title="全局筛选配置" width="700" :open="open" @close="cancel">
    <section class="page flex-column">
      <main class="flex-1 flex scroll">
        <FilterList
          ref="filterListRef"
          class="left-side"
          :data-source="list"
          @select="onFilterItemSelect" />

        <RelatedCharts
          ref="relatedChartsRef"
          class="right-side"
          :charts="reports"
          :item="filterItemInfo"
          v-model:value="filterItemInfo.charts" />
      </main>

      <footer class="footer">
        <FilterBottom ref="filterBottomRef" :item="filterItemInfo" />
      </footer>
    </section>

    <template #footer>
      <a-space style="float: right">
        <a-button @click="cancel">取消</a-button>
        <a-button type="primary" @click="ok">确认</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import FilterList from './components/FilterList.vue'
import RelatedCharts from './components/RelatedCharts.vue'
import FilterBottom from './components/FilterBottom.vue'
import { deepClone } from 'common/utils/help'

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  dataSource: {
    type: Array,
    default: () => [],
  },
  reports: {
    type: Array,
    default: () => [],
  },
})

const list = ref([])
watch(
  () => props.open,
  open => {
    open ? init() : reset()
  }
)

const init = () => {
  list.value = deepClone(props.dataSource)

  if (!list.value.length) {
    filterItemInfo.value = {}
  } else {
    filterItemInfo.value = { ...list.value[0] }
  }
}

const reset = () => {
  list.value = []
}

const filterItemInfo = ref({})
const onFilterItemSelect = e => {
  if (typeof e.id === 'undefined') {
    filterItemInfo.value = {}
  } else {
    filterItemInfo.value = deepClone(e)
  }
}

const filterListRef = ref(null)
watch(
  filterItemInfo,
  val => {
    filterListRef.value?.update(val)
  },
  { deep: true }
)

const relatedChartsRef = ref(null)
const filterBottomRef = ref(null)
const cancel = () => {
  emits('update:open', false)
}
const ok = async () => {
  const listValidateRes = filterListRef.value.validate()
  const bottomValidateRes = await filterBottomRef.value.validate()

  if (!listValidateRes || !bottomValidateRes) {
    return false
  } else {
    const payload = filterListRef.value.getData()

    emits('ok', payload)
  }
}
</script>

<style lang="scss" scoped>
.left-side {
  width: 250px;
}
.right-side {
  flex: 1;
  border-left: 1px solid #d9d9d9;
  padding-left: 16px;
}
.footer {
  height: 240px;
  padding-top: 16px;
  border-top: 1px solid #d9d9d9;
}
</style>
