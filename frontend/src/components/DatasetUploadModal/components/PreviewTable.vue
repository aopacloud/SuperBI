<template>
  <a-table
    size="small"
    :pagination="false"
    :scroll="{ x: 'max-content' }"
    :columns="columns"
    :dataSource="data"
  />
</template>

<script setup>
const props = defineProps({
  columns: {
    type: Array,
    default: () => []
  },
  data: {
    type: Array,
    default: () => []
  }
})
</script>
