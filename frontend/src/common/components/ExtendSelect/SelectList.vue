<template>
  <SelectLayout ref="layoutRef" v-bind="$attrs">
    <template #pane="{ dataSource, value, updateValue }">
      <slot name="prefix" />

      <PaneList
        v-bind="$attrs"
        :value="value"
        @update:value="updateValue"
        :data-source="dataSource">
        <template #item="scopedData" v-if="$slots.item">
          <slot name="item" v-bind="scopedData"> </slot>
        </template>
      </PaneList>

      <slot name="suffix" />
    </template>

    <template #tabs v-if="$slots.tabs">
      <slot name="tabs" />
    </template>

    <template #footer v-if="$slots.footer">
      <slot name="footer" />
    </template>
  </SelectLayout>
</template>
<script setup>
import { ref } from 'vue'
import SelectLayout from './layout.vue'
import PaneList from './panes/list.vue'

const layoutRef = ref(null)
const clearSearch = () => {
  layoutRef.value.clearSearch?.()
}

defineExpose({
  clearSearch,
})
</script>
