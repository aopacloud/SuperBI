<template>
  <a-alert
    style="padding: 4px 10px"
    v-if="queryParams.dashboard?.id && queryParams.filters.length">
    <template #message>
      <div style="color: rgba(0, 0, 0, 0.65)">
        来源于看板:
        <b>{{ queryParams.dashboard.name || '-' }}</b>
        <a-divider type="vertical"></a-divider>
        以下为看板全局筛选过滤后的结果，可点击
        <a @click="reset">去掉看板全局筛选</a>
        条件，只查看该图表原始条件下的结果
      </div>
    </template>
  </a-alert>
</template>

<script setup>
import { computed, inject } from 'vue'

const { dashboardFilters } = inject('index')

const queryParams = computed(() => dashboardFilters.get())

const reset = () => {
  dashboardFilters.reset()
}
</script>

<style lang="scss" scoped></style>
