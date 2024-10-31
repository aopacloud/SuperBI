<template>
  <SettingSectionLayout title="指标卡设置">
    <a-collapse expand-icon-position="end">
      <a-collapse-panel key="labelShow" class="collapse-panel-onlyHeader">
        <template #header>
          <span>指标名</span>
          <a-checkbox
            style="float: right"
            v-model:checked="modelValue.labelShow"
            @click.stop
            >显示
          </a-checkbox>
        </template>
      </a-collapse-panel>

      <a-collapse-panel
        key="compare"
        header="同环比"
        :collapsible="compareDisabled ? 'disabled' : ''"
      >
        <BasisRatio v-model:value="contrastModelValue" />
      </a-collapse-panel>
    </a-collapse>
  </SettingSectionLayout>
</template>

<script setup>
import { watch, inject } from 'vue'
import SettingSectionLayout from './SectionLayout.vue'
import BasisRatio from './BasisRatio.vue'
import { useVModel } from 'common/hooks/useVModel'

const emits = defineEmits(['update:options', 'update:compare'])

const props = defineProps({
  options: {
    type: Object,
    default: () => ({})
  },
  compare: {
    type: Object,
    default: () => ({})
  }
})

const modelValue = useVModel(props, 'options', emits)

watch(
  modelValue,
  val => {
    emits('update:options', val)
  },
  { deep: true }
)
const contrastModelValue = useVModel(props, 'compare', emits)
watch(
  contrastModelValue,
  val => {
    emits('update:compare', val)
  },
  { deep: true }
)

const indexInject = inject('index')

const compareDisabled = computed(() => {
  const compare = indexInject.compare.get() || {}

  return compare.measures?.length ? false : true
})
</script>

<style lang="scss" scoped>
.collapse-panel-onlyHeader {
  :deep(.ant-collapse-expand-icon) {
    visibility: hidden;
  }
  :deep(.ant-collapse-content) {
    display: none;
  }
}
</style>
