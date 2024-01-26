<template>
  <div class="info">
    <ul class="list">
      <li class="item">
        <span class="item-label">数据库</span>
        <div class="item-content">
          {{ info.database }}

          <a-badge
            v-if="info._status"
            style="margin-left: 20px"
            class="status-badge"
            v-bind="infoStatusProp" />
        </div>
      </li>

      <li class="item">
        <span class="item-label">显示名称</span>
        <div class="item-content">{{ info.name }}</div>
      </li>

      <li class="item">
        <span class="item-label">主机</span>
        <div class="item-content">{{ info.host }}</div>
      </li>

      <li class="item">
        <span class="item-label">端口</span>
        <div class="item-content">{{ info.port }}</div>
      </li>
      <li class="item">
        <span class="item-label">数据库地址</span>
        <div class="item-content">{{ info.url }}</div>
      </li>
      <li class="item">
        <span class="item-label">用户名</span>
        <div class="item-content">{{ info.user }}</div>
      </li>
      <li class="item">
        <span class="item-label">密码</span>
        <div class="item-content">
          {{ info.password }}

          <!-- <a-button
            style="padding: 0 4px"
            size="small"
            type="text"
            :title="pwdVisible ? '显示密码' : '隐藏密码'"
            @click="togglePwdVisible">
            <EyeOutlined v-if="!pwdVisible" />
            <EyeInvisibleOutlined v-else />
          </a-button> -->
        </div>
      </li>
      <li class="item">
        <span class="item-label">所有者</span>
        <div class="item-content">{{ info.creatorAlias }}</div>
      </li>
      <li class="item">
        <span class="item-label">SSL</span>
        <div class="item-content">
          <a-checkbox disabled :checked="info.sslEnable" />
        </div>
      </li>
      <li class="item" v-if="info.initSql">
        <span class="item-label">初始化SQL</span>
        <div class="item-content">
          <textarea
            class="sql-input"
            style="width: 600px"
            readonly
            :value="info.initSql" />
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons-vue'

const props = defineProps({
  info: {
    type: Object,
    default: () => ({
      password: '123456',
    }),
  },
})

const infoStatusProp = computed(() => {
  const { _status, _statusText } = props.info

  return { status: _status, text: _statusText }
})

// 密码显示模式
const pwdVisible = ref(false)
const togglePwdVisible = () => {
  pwdVisible.value = !pwdVisible.value
}
const resetPwdVisible = () => {
  pwdVisible.value = false
}

const displayPwd = computed(() => {
  return pwdVisible.value ? props.info.password : props.info.password?.replace(/./g, '*')
})

watch(() => props.info.id, resetPwdVisible)
</script>

<style lang="scss" scoped>
.info {
  height: 100%;
  margin-left: 24px;
  overflow: auto;
}
.item {
  display: flex;
  margin-bottom: 16px;
  &-label {
    position: relative;
    margin-right: 12px;
    width: 90px;
    text-align: right;
    &:after {
      content: ':';
      margin-left: 2px;
    }
  }
  &-content {
    flex: 1;
  }
}

.status-badge {
  :deep(.ant-badge-status-dot) {
    width: 8px !important;
    height: 8px !important;
    &.ant-badge-status-error + .ant-badge-status-text {
      color: #ff4d4f;
    }
  }
}

.sql-input {
  display: inline-block;
  width: 100%;
  min-height: 100px;
  margin: 0;
  padding: 6px 12px;
  background-color: #f3f3f3;
  color: rgba(0, 0, 0, 0.25);
  line-height: inherit;
  font-size: inherit;
  border: none;
  outline: none;
  resize: none;
}
</style>
