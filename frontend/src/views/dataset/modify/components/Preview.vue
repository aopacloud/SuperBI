<template>
  <section class="preview-section flex-column">
    <header class="flex align-center" style="height: 52px; padding: 10px 12px">
      呈现 100 条样例数据
      <a-button
        style="margin-left: 8px"
        size="small"
        type="text"
        :loading="loading"
        :icon="h(ReloadOutlined)"
        @click="fetchData" />
    </header>

    <main class="main flex-1 scroll">
      <vxe-grid
        size="small"
        height="auto"
        :scroll-x="{ enabled: true, gt: 20 }"
        :scroll-y="{ enabled: true, gt: 20 }"
        :columns="columns"
        :data="list"
        :loading="loading"></vxe-grid>
    </main>
  </section>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { ReloadOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { getWordWidth } from 'common/utils/help'
import { postAnalysisQuery } from '@/apis/analysis'
import { AGGREGATOR_SPLIT } from '@/views/analysis/utils'
import { message } from 'ant-design-vue'

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
  fields: {
    type: Array,
    default: () => [],
  },
})

const loading = ref(false)
const canRetry = ref(false)
const columns = ref([])
const list = ref([])

const WIDTH_BUFFER = 20

const fetchData = async () => {
  try {
    loading.value = true

    if (!props.dataset.config?.tableName) {
      message.warn('暂未选中任何表')

      return
    }

    const { rows, fieldNames } = await postAnalysisQuery({
      dataset: {
        ...props.dataset,
        fields: props.fields.filter(t => t.status !== 'HIDE'),
      },
      type: 'PREVIEW',
    })

    columns.value = fieldNames.map((fieldName, i) => {
      const name = fieldName.split(AGGREGATOR_SPLIT)[0]
      const field = props.fields.find(t => t.name === name)

      const contentMaxWidth = rows.map(t => getWordWidth(t[i], 10, 16))
      const titleWidth = getWordWidth(field.displayName, 10, 16)
      const maxWidth = Math.max(...contentMaxWidth, titleWidth) + WIDTH_BUFFER

      return {
        field: field.name,
        title: field.displayName,
        width: maxWidth,
      }
    })

    list.value = rows.map(row =>
      row.reduce((acc, pre, i) => {
        acc[columns.value[i].field] = pre

        return acc
      }, {})
    )
  } catch (error) {
    console.error('获取数据预览错误', error)
  } finally {
    loading.value = false
    canRetry.value = true
  }
}

onMounted(() => {
  fetchData()
})
</script>
