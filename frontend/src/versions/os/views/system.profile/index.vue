<template>
  <section class="section" style="padding-left: 10%">
    <a-descriptions
      style="margin-top: 60px"
      title="基础信息"
      :column="1"
      :labelStyle="{ width: '100px', justifyContent: 'flex-end' }">
      <a-descriptions-item label="账户名">{{ userInfo.username }}</a-descriptions-item>
      <a-descriptions-item label="显示名">{{ userInfo.aliasName }}</a-descriptions-item>
      <a-descriptions-item
        label="头像"
        :labelStyle="{ alignItems: 'center' }"
        :contentStyle="{ alignItems: 'center' }">
        <a-avatar :size="64">
          <template #icon>
            <keep-alive>
              <LoadingOutlined v-if="avatorUploading" />
              <img v-else :src="avatar" alt="" />
            </keep-alive>
          </template>
        </a-avatar>

        <a-button type="link" :disabled="avatorUploading" @click="openFileChange">
          上传头像
        </a-button>

        <input
          ref="fileInputRef"
          hidden
          type="file"
          accept=".jpg,.jepg,.png"
          @change="onFileChange" />
      </a-descriptions-item>
    </a-descriptions>

    <a-descriptions
      style="margin-top: 60px"
      title="隐私信息"
      :column="1"
      :labelStyle="{ width: '100px', justifyContent: 'flex-end' }">
      <a-descriptions-item label="邮箱">
        {{ userInfo.email }}
        <a style="margin-left: 12px" @click="openEmialModal">绑定邮箱</a>
      </a-descriptions-item>
      <a-descriptions-item label="手机号"
        >{{ userInfo.mobile }}
        <a style="margin-left: 12px" @click="openMobileModal">绑定手机号</a>
      </a-descriptions-item>
      <a-descriptions-item label="密码">
        ******
        <a style="margin-left: 12px" @click="openPwdModal">修改密码</a>
      </a-descriptions-item>
    </a-descriptions>
  </section>

  <!-- 邮箱 -->
  <ModifyEmailModal
    :initData="userInfo"
    v-model:open="emailModifyModalOpen"
    @ok="onEmailModifyOk" />

  <!-- 手机号 -->
  <ModifyMobileModal
    :initData="userInfo"
    v-model:open="mobileModifyModalOpen"
    @ok="onMobileModifyOk" />

  <!-- 密码 -->
  <ModifyPasswordModal :initData="userInfo" v-model:open="pwdModifyModalOpen" />
</template>

<script setup>
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { LoadingOutlined } from '@ant-design/icons-vue'
import ModifyEmailModal from './components/ModifyEmailModal.vue'
import ModifyMobileModal from './components/ModifyMobileModal.vue'
import ModifyPasswordModal from './components/ModifyPasswordModal.vue'
import { postAvatar } from '@/apis/system/profile'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const userInfo = ref({ ...userStore.userInfo })

// 头像
const avatar = computed(() => userStore.avatar)

// 选择文件
const fileInputRef = ref()
const openFileChange = () => {
  fileInputRef.value.click()
}
const onFileChange = e => {
  const file = e.target.files[0]
  if (!file) return

  if (!validateFile(file)) return

  uploadAvatar(file)
}

// 校验
const validateFile = async file => {
  const size = file.size / 1024 / 1024
  if (size > 2) {
    message.error('图片大小不能超过 2MB!')

    return false
  }
  return true
}

// 上传头像
const avatorUploading = ref(false)
const uploadAvatar = async file => {
  try {
    avatorUploading.value = true

    const { avatar } = await postAvatar({ avatarFile: file })

    userInfo.value.avatar = avatar
    userStore.setUserinfo({ ...userStore.userInfo, avatar })
  } catch (error) {
    console.error('头像上传失败', error)
  } finally {
    avatorUploading.value = false
  }
}

// 修改邮箱
const emailModifyModalOpen = ref(false)
const openEmialModal = () => {
  emailModifyModalOpen.value = true
}
const onEmailModifyOk = e => {
  userInfo.value.email = e
}

// 修改手机号
const mobileModifyModalOpen = ref(false)
const openMobileModal = () => {
  mobileModifyModalOpen.value = true
}
const onMobileModifyOk = e => {
  userInfo.value.mobile = e
}

// 修改密码
const pwdModifyModalOpen = ref(false)
const openPwdModal = () => {
  pwdModifyModalOpen.value = true
}
</script>

<style lang="scss" scoped>
.section {
  :deep(.ant-descriptions-item) {
    padding-bottom: 24px;
  }
}
</style>
