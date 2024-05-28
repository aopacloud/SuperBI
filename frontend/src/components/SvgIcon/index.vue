<template>
  <Icon>
    <template #component>
      <svg aria-hidden="true" width="1em" height="1em">
        <use :href="iconName" />
      </svg>
    </template>
  </Icon>
</template>

<script setup>
import { computed } from 'vue'
import Icon from '@ant-design/icons-vue'

defineOptions({
  name: 'SvgIcon',
})

const props = defineProps({
  name: { type: String, required: true },
  color: String,
})

const iconName = computed(() => '#svgIcon-' + props.name)
</script>
