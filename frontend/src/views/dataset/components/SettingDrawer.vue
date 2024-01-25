<template>
  <a-drawer title="数据集设置" width="500" :open="open" @close="close">
    <section>
      <div class="item">
        <div class="item-label" style="align-self: flex-start">数据集:</div>
        <div class="item-content">
          <h3 style="margin: 0">{{ initData.name || '-' }}</h3>
        </div>
      </div>

      <div class="item">
        <div class="item-label">是否开放申请:</div>
        <div class="item-content">
          <a-radio-group v-model:value="modelValue">
            <a-radio :value="1">全员可申请</a-radio>
            <a-radio :value="0">不可申请</a-radio>
          </a-radio-group>

          <div v-if="modelValue === 0" style="margin-top: 5px; color: #aaa">
            当数据集不可申请时，无权限的用户在全部列表下看不见该数据集
          </div>
        </div>
      </div>
    </section>

    <template #footer>
      <a-space style="float: right">
        <a-button @click="close">取消</a-button>
        <a-button type="primary" :loading="confirmLoading" @click="ok">确定</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { postSet } from '@/apis/dataset'

const emits = defineEmits(['update:open', 'ok'])
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  initData: {
    type: Object,
    default: () => ({}),
  },
})

const modelValue = ref(0)
watch(
  () => props.open,
  open => {
    if (open) {
      modelValue.value = props.initData.enableApply ?? 0
    } else {
      confirmLoading.value = false
    }
  }
)

const close = () => {
  emits('update:open', false)
}
const confirmLoading = ref(false)
const ok = async () => {
  try {
    confirmLoading.value = true
    const res = await postSet(props.initData.id, {
      enableApply: modelValue.value,
    })

    message.success('设置成功')
    emits('success', { id: props.initData.id, ...res })
    close()
  } catch (error) {
    console.error('数据集设置错误', error)
  } finally {
    confirmLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.item {
  display: flex;
  margin-bottom: 16px;
  &-label {
    width: 120px;
    text-align: right;
  }
  &-content {
    flex: 1;
    margin-left: 12px;
  }
}
</style>
