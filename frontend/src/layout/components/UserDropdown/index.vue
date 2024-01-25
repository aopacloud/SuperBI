<template>
  <div>
    <a-dropdown trigger="click">
      <div class="avatar-img">
        <img :src="userStore.avatar" class="user-avatar" />
        <CaretDownOutlined style="margin-left: 4px" />
      </div>
      <template #overlay>
        <a-menu @click="dropdownClick">
          <LayoutComponentsUserDropdownProfile />

          <template v-if="userStore.isSuperAdmin">
            <LayoutComponentsUserDropdownUser />

            <router-link to="/system/role">
              <a-menu-item>角色管理</a-menu-item>
            </router-link>
          </template>

          <a-menu-divider></a-menu-divider>
          <a-menu-item key="logout"> 退出登录 </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup>
import { Modal } from 'ant-design-vue'
import { CaretDownOutlined } from '@ant-design/icons-vue'
import useUserStore from '@/store/modules/user'
import { versionVue } from '@/versions'

const { LayoutComponentsUserDropdownProfile, LayoutComponentsUserDropdownUser } =
  versionVue

const userStore = useUserStore()

const dropdownClick = ({ key }) => {
  switch (key) {
    case 'logout':
      logout()
      break
    default:
      break
  }
}

const logout = () => {
  userStore.logout().then(() => {
    location.reload()
  })
}
</script>

<style lang="scss" scoped>
.avatar-img {
  display: flex;
  align-items: center;
  cursor: pointer;
  height: 100%;
  .user-avatar {
    cursor: pointer;
    width: 40px;
    height: 40px;
    border-radius: 10px;
  }
}
</style>
