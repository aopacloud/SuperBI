<template>
  <li class="item" :class="{ readed: item.readed }" :data-notificationId="item.id">
    <div class="header">
      <h3 class="title">
        {{ getResource(item.resourceType) }}
        {{ getPermissionMap(item.content.status)['text'] }}
      </h3>
      <span class="create-time"> {{ item.createTime }}</span>
    </div>
    <div class="main flex justify-between">
      <div class="info flex-1">
        <p class="p">
          您的 【{{ getResource(item.resourceType) }}】

          {{ getPermissionMap(item.content.status)['tip'] }}

          <span style="margin-left: 12px">
            {{ getPermissionMap(item.content.status)['status'] }}
          </span>
          <!-- <a-tag style="margin-left: 12px; vertical-align: top"></a-tag> -->
        </p>
        <p class="p">
          <span>
            {{ item.content.status === 'UNDER_REVIEW' ? '申请人' : '操作人' }}:
          </span>
          <span>
            {{ item.content.aliasName || '-' }}
          </span>
          <span
            style="margin-left: 5px; color: #aaa"
            v-if="['EXPIRED', 'EXPIRING', 'REJECTED'].includes(item.content.status)">
            {{ tip() }}
          </span>
        </p>

        <p class="p" v-if="activeKey === 'APPROVAL'">
          <span>
            {{
              item.content.status === 'UNDER_REVIEW'
                ? '申请理由'
                : item.content.status === 'REJECTED'
                ? '驳回理由'
                : '备注'
            }}:
          </span>
          {{ item.content.reason || '-' }}
        </p>
      </div>
      <div class="action" style="align-self: flex-end">
        <a-space>
          <a
            v-if="activeKey === 'PERMISSION' && item.content.status === 'EXPIRED'"
            @click="reApply">
            重新申请
          </a>

          <template
            v-if="activeKey === 'APPROVAL' && item.content.status === 'UNDER_REVIEW'">
            <a @click="resolve">通过并授权</a>
            <a style="color: red" @click="reject">驳回 </a>
          </template>
        </a-space>
      </div>
    </div>
    <div class="footer">
      {{ getResource(item.resourceType) }}：
      <a @click="toDetail(item)">{{ item.resourceName }}</a>
    </div>
  </li>
</template>

<script setup>
import { onMounted, getCurrentInstance, onUnmounted, computed } from 'vue'
import { permissionMap, approveMap, resourceMap, tipMap } from '../config'
import { useRouter } from 'vue-router'

const router = useRouter()

const emits = defineEmits(['reject', 'resolve', 'reApply'])
const props = defineProps({
  activeKey: String,
  item: {
    type: Object,
    default: () => ({}),
  },
  observe: {
    type: Object,
  },
})

const tip = () => {
  const fn = tipMap[props.activeKey]

  if (typeof fn === 'undefined') return ''

  return fn(props.item.content.datasetCreatorAlias)
}

const statusMap = computed(() => {
  if (props.activeKey === 'PERMISSION') {
    return permissionMap
  } else if (props.activeKey === 'APPROVAL') {
    return approveMap
  } else {
    return {}
  }
})

const getResource = type => {
  return resourceMap[type]
}

const getPermissionMap = status => {
  return statusMap.value[status] || {}
}

// 驳回
const reject = () => {
  if (props.item.loading) return

  emits('reject', props.item)
}
// 重新申请
const reApply = () => {
  if (props.item.loading) return

  emits('reApply', props.item)
}
// 拒绝
const resolve = () => {
  if (props.item.loading) return

  emits('resolve', props.item)
}

const toDetail = row => {
  const { resourceType, resourceId } = row

  let name = ''
  if (resourceType === 'DASHBOARD') {
    name = 'DashboardPreview'
  } else if (resourceType === 'DATASET') {
    name = 'DatasetAnalysis'
  } else if (resourceType === 'REPORT') {
    name = 'ReportDetail'
  } else {
    name = 'DatasourceDetail'
  }

  const routeRes = router.resolve({
    name,
    params: { id: resourceId },
  })

  if (!routeRes) {
  } else {
    window.open(routeRes.href, '_blank')
  }
}

onMounted(() => {
  if (props.observe) {
    props.observe.observe(getCurrentInstance().ctx.$el)
  }
})
onUnmounted(() => {
  if (props.observe) {
    props.observe.unobserve(getCurrentInstance().ctx.$el)
  }
})
</script>

<style lang="scss" scoped>
.item {
  padding: 10px;
  &.readed {
    .title {
      color: #777;
    }
    & > :not(.header) {
      color: #bbb;
    }
  }
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  margin-bottom: 10px;
  .readed {
    position: absolute;
  }
}
.p {
  margin: 6px 0 0;
  line-height: 24px;
}
.footer {
  margin-top: 6px;
}

.title {
  margin: 0;
  font-weight: 600;
}
.create-time {
  color: #999;
}
</style>
