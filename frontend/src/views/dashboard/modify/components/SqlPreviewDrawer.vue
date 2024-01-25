<template>
  <a-drawer
    width="600"
    :open="open"
    :closable="false"
    :headerStyle="{ padding: '8px 10px 8px 6px' }"
    :body-style="{ padding: '10px' }">
    <template #title>
      <a-button type="text" style="padding: 0 6px" :icon="h(LeftOutlined)" @click="close">
        返回
      </a-button>
    </template>
    <template #extra>
      <a-button style="margin-right: 8px" v-copy="sql" v-copy:success="onCopySuccess">
        复制
      </a-button>
      <a-button type="primary" @click="onExport">导出</a-button>
    </template>

    <pre ref="preRef" class="sql-content">{{ displaySql }}</pre>
  </a-drawer>
</template>

<script setup>
import { h, ref, watch, nextTick } from 'vue'
import { LeftOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { exportFile } from 'common/utils/file'
import { sqlFormat } from 'common/utils/format'

const emits = defineEmits(['update:open'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  sql: {
    type: String,
    default: '',
  },
})

watch(
  () => props.open,
  open => {
    if (open) {
      nextTick(() => {
        init()
      })
    }
  }
)
// 显示值
const displaySql = ref()
const preRef = ref(null)
const init = () => {
  displaySql.value = sqlFormat(props.sql)
  // 重置滚动条位置
  preRef.value.scrollTo(0, 0)
}

const close = () => {
  emits('update:open', false)
}
const onCopySuccess = () => {
  message.success('SQL 复制成功')
}
const onExport = () => {
  exportFile(props.sql, { suffix: '.sql', type: '' })
}
</script>

<style lang="scss" scoped>
.sql-content {
  display: block;
  height: 100%;
  margin: 0;
  padding: 16px;
  overflow: auto;
  background-color: #000;
  color: #fff;
  font-weight: bold;
  font-family: 'microsoft yahei';
  font-size: 16px;
  line-height: 28px;
}
</style>
