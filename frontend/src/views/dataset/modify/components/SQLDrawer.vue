<template>
  <a :disabled="disabled" @click="!disabled && showSql()"> 查看SQL </a>

  <a-drawer title="SQL查看" :width="700" v-model:open="open" @cancel="sqlCode = ''">
    <div style="margin-bottom: 12px">
      此处仅模拟关联后的查询SQL示例，可检查关联配置是否满足自己诉求
      <div class="font-help">
        分析时，拖入各种分组和条件的查询SQL，请在分析查询中查看
      </div>
    </div>

    <div
      class="sql-content"
      style="display: flex; flex-direction: column; height: calc(100% - 52px)">
      <a-spin :spinning="loading">
        <pre ref="preRef" class="sql-area" v-html="sqlCode"></pre>
      </a-spin>
    </div>
  </a-drawer>
</template>

<script setup>
import { ref, inject } from 'vue'
import { getPreviewSQL } from '@/apis/dataset'
import { sqlFormat } from 'common/utils/format'
import hljs from 'highlight.js/lib/core'
import sql from 'highlight.js/lib/languages/sql'
import 'highlight.js/styles/atom-one-dark.min.css'
hljs.registerLanguage('sql', sql)

const { getDatasetConfig, getDatasetFields } = inject('index')

const props = defineProps({
  dataset: {
    type: Object,
    default: () => ({}),
  },
  disabled: Boolean,
})

const loading = ref(false)
const preRef = ref(null)
const sqlCode = ref('')
const fetchSql = async () => {
  try {
    loading.value = true

    const res = await getPreviewSQL({
      ...props.dataset,
      config: getDatasetConfig(),
      fields: getDatasetFields(),
    })
    sqlCode.value = hljs.highlight(sqlFormat(res), { language: 'sql' }).value
    preRef.value.scrollTo(0, 0)
  } catch (error) {
    console.error('获取SQL失败', error)
  } finally {
    loading.value = false
  }
}

const open = ref(false)
const showSql = () => {
  open.value = true
  fetchSql()
}
</script>

<style lang="scss" scoped>
.sql-content {
  :deep(.ant-spin-nested-loading) {
    height: 100%;
    .ant-spin-container {
      height: inherit;
    }
  }
}
</style>
