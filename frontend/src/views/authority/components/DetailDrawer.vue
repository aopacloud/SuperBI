<template>
  <a-drawer width="500" :open="open" @close="close">
    <template #title>
      <span style="font-size: 16px; font-weight: bold">审核详情</span>
      <a-badge
        style="margin-left: 10px"
        v-bind="displayApproveStatus(initData.approveStatus)" />
    </template>

    <ul class="info-list">
      <li class="info-item">
        <span class="info-item--name">数据集</span>
        <div class="info-item--text">{{ initData.datasetName }}</div>
      </li>

      <template v-if="from !== 'apply'">
        <li class="info-item">
          <span class="info-item--name">数据集归属空间</span>
          <div class="info-item--text">{{ initData.workspaceName || '-' }}</div>
        </li>
        <li class="info-item">
          <span class="info-item--name">数据集创建人</span>
          <div class="info-item--text">{{ initData.datasetCreatorAliasName || '-' }}</div>
        </li>
        <li class="info-item">
          <span class="info-item--name">申请人</span>
          <div class="info-item--text">{{ initData.aliasName || '-' }}</div>
        </li>
        <li class="info-item">
          <span class="info-item--name">申请理由</span>
          <div class="info-item--text">{{ initData.reason || '-' }}</div>
        </li>
      </template>

      <li class="info-item">
        <span class="info-item--name">有效期</span>
        <div class="info-item--text">
          {{ computeExpireDuration(initData.expireDuration) }}
        </div>
      </li>
      <li class="info-item">
        <span class="info-item--name">发起时间</span>
        <div class="info-item--text">{{ initData.createTime }}</div>
      </li>
      <li class="info-item">
        <span class="info-item--name">{{ from === 'manage' ? '状态' : '' }}更新时间</span>
        <div class="info-item--text">{{ initData.updateTime }}</div>
      </li>
    </ul>

    <a-spin style="margin: 20px 0" :spinning="loading">
      <a-empty v-if="!steps.length" description="暂无审核进展" />
      <a-steps v-else direction="vertical" size="small" :current="stepValue">
        <a-step v-for="(step, index) in steps" :key="index" :title="step.reviewName">
          <template v-if="step.handler" #description>
            <div class="step-child" v-for="(item, i) in step.handler" :key="i">
              <span>当前处理人：{{ item.name }}；</span>
              <span>消息状态：{{ status === 'READ' ? '已读' : '未读' }}；</span>
              <span>处理状态：{{ item.status }}；</span>
              <span>处理时间：{{ item.handleTime }}；</span>
            </div>
          </template>
        </a-step>
      </a-steps>
    </a-spin>

    <template #footer>
      <slot name="footer"></slot>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, shallowRef, watch } from 'vue'
import { CheckCircleOutlined, CloseOutlined } from '@ant-design/icons-vue'
import { computeExpireDuration, displayApproveStatus } from '../config'
import { getOne } from '@/apis/apply'

const emits = defineEmits(['update:open'])
const props = defineProps({
  open: {
    type: Boolean,
    default: true,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
  from: {
    type: String,
    default: 'apply',
  },
})

// vue3 - 注册一个监听器
watch(
  () => props.open,
  op => {
    if (op && detail.value.id !== props.initData.id) {
      fetchDetail()
    }
  }
)

const loading = ref(false)
const detail = shallowRef({})
const steps = ref([])
const stepValue = ref()
const fetchDetail = async () => {
  try {
    loading.value = true

    const res = await getOne(props.initData.id)

    detail.value = res
    steps.value = JSON.parse(res.reviewStateJson || '[]')
    stepValue.value = steps.value.findLastIndex(step => step.status === 'DONE')
  } catch (error) {
    console.error('获取申请详情错误', error)
  } finally {
    loading.value = false
  }
}

const close = () => {
  emits('update:open', false)
}
</script>

<style lang="scss" scoped>
.info-list {
  .info-item {
    display: flex;
    margin-bottom: 5px;
    &--name:after {
      content: ':';
    }
    &--text {
      flex: 1;
      margin-left: 10px;
    }
  }
}
.step-child {
  padding: 6px 10px;
  margin-top: 4px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  & + & {
    margin-top: 10px;
  }
}
</style>
