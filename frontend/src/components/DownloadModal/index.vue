﻿<template>
  <a-modal
    title="下载设置"
    :open="open"
    :confirm-loading="loading"
    @cancel="cancel"
    @ok="ok">
    <div style="margin: 30px 0 20px 20px">
      <div>
        <span>数据范围</span>

        <a-radio-group style="margin-left: 16px" v-model:value="modelValue">
          <a-radio :value="false">展示数据</a-radio>
          <a-radio :value="true">全量数据</a-radio>
        </a-radio-group>
      </div>

      <!-- <a-progress :percent="30" /> -->
    </div>
  </a-modal>
</template>

<script setup>
import { ref } from 'vue'
import { downloadQueryResult } from '@/apis/analysis'
import { downloadWithBlob } from 'common/utils/file'

defineOptions({
  name: 'DownloadModal',
})

const emits = defineEmits(['update:open', 'download'])
const props = defineProps({
  open: {
    type: Boolean,
    default: true,
  },
  filename: {
    type: String,
    default: '查询结果',
  },
  initParams: {
    type: Object,
    default: () => ({}),
  },
})

const loading = ref(false)
const modelValue = ref(false)

const cancel = () => {
  emits('update:open', false)

  modelValue.value = false
  loading.value = false
}

const ok = () => {
  if (modelValue.value) {
    downloadFromOrigin()
  } else {
    emits('download')
    cancel()
  }
}

/**
 * 从原始数据源下载文件
 * @returns 无返回值
 */
const downloadFromOrigin = async () => {
  try {
    loading.value = true

    const res = await downloadQueryResult(
      { all: modelValue.value },
      props.initParams
    )

    await downloadWithBlob(res, `${props.filename}-${Date.now()}.xlsx`)

    cancel()
  } catch (err) {
    console.error('看板下载错误', err)
  } finally {
    loading.value = false
  }
}
</script>
