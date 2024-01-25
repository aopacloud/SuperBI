<template>
  <section class="section" style="min-width: 1200px">
    <header class="header">
      <img class="logo" src="@/assets/images/logo0.png" alt="" />
    </header>

    <main class="main">
      <section class="section" style="flex-direction: row">
        <aside class="left">
          <div class="desc">
            <h1 class="title">SuperBI多维可视化分析平台</h1>
            <div class="sub-title">交互简单、分析高效、操作灵活、查询快速</div>
            <dl class="desc-list">
              <dt>自助分析</dt>
              <dd class="desc-item" style="--n: 0">业务人员自由拖拽数据自助分析</dd>
              <dt>可视化报表</dt>
              <dd class="desc-item" style="--n: 100px">
                基于丰富的图表类型及组件快速搭建可视化报表
              </dd>
              <dt>秒级查询</dt>
              <dd class="desc-item" style="--n: 200px">
                支持对海量数据多种数据源进行秒级查询
              </dd>
            </dl>
          </div>
          <div class="bg">
            <img class="bg-img" src="@/assets/svg/login_bg.svg" alt="" />
          </div>
        </aside>
        <main class="right">
          <div class="login-form">
            <h2 class="title">SUPER BI</h2>
            <a-form
              :model="formState"
              :rules="formRules"
              :label-col="{ span: 0 }"
              :wrapper-col="{ span: 24 }"
              autocomplete="off"
              @finish="submit">
              <a-form-item name="username">
                <a-input
                  size="large"
                  placeholder="账号"
                  class="form-input"
                  v-model:value.trim="formState.username">
                  <template #prefix><UserOutlined /></template>
                </a-input>
              </a-form-item>

              <a-form-item name="password">
                <a-input
                  size="large"
                  autocomplete="new-password"
                  placeholder="密码"
                  class="form-input"
                  :type="passwordType"
                  v-model:value.trim="formState.password">
                  <template #prefix><LockOutlined /></template>
                  <template #suffix v-if="formState.password.length">
                    <keep-alive>
                      <EyeOutlined
                        class="pwd-eye-icon"
                        v-if="passwordType === 'password'"
                        @click="passwordType = 'text'" />
                      <EyeInvisibleOutlined
                        class="pwd-eye-icon"
                        v-else
                        @click="passwordType = 'password'" />
                    </keep-alive>
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item name="code" style="margin-bottom: -24px">
                <a-input-group compact>
                  <a-input
                    size="large"
                    style="width: 65%"
                    placeholder="验证码"
                    class="form-input"
                    v-model:value="formState.code">
                    <template #prefix>
                      <SafetyOutlined />
                    </template>
                  </a-input>
                  <div class="code">
                    <img :src="codeUrl" @click="getCode" class="code-img" />
                  </div>
                </a-input-group>
              </a-form-item>

              <a-form-item>
                <a-checkbox v-model:checked="formState.remember">记住密码</a-checkbox>
              </a-form-item>

              <a-form-item>
                <a-button
                  size="large"
                  type="primary"
                  htmlType="submit"
                  block
                  :loading="loading">
                  登 录{{ loading ? '中...' : '' }}
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </main>
      </section>
    </main>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import {
  UserOutlined,
  LockOutlined,
  SafetyOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
} from '@ant-design/icons-vue'
import Cookies from 'js-cookie'
import { getCodeImg } from '@/apis/login'
import { useRouter, useRoute } from 'vue-router'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import useUserStore from '@/store/modules/user'
import { storagePrefix } from '@/settings'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const formState = reactive({
  username: '',
  password: '',
  code: '',
  uuid: '',
  remember: false,
})

const formRules = {
  username: [{ required: true, message: '请输入您的账号' }],
  password: [{ required: true, message: '请输入您的密码' }],
  code: [{ required: true, message: '请输入验证码' }],
}

const passwordType = ref('password')
// 重定向地址
const redirect = ref(route.query.redirect)
// 验证码开关
const codeUrl = ref('')
const loading = ref(false)
// 登录提交
const submit = async () => {
  try {
    loading.value = true

    const { username, password, code, remember, uuid } = formState
    const params = { username, password, code, uuid }

    await userStore.login(params).then(() => {
      if (formState.remember) {
        Cookies.set(
          storagePrefix + 'login',
          JSON.stringify({
            username,
            password: encrypt(password),
            remember,
          })
        )
      } else {
        Cookies.remove(storagePrefix + 'login')
      }

      router.push({ path: redirect.value || '/' })
    })
  } catch (error) {
    console.error('登录失败', error)
    getCode()
  } finally {
    loading.value = false
  }
}

/**
 * 获取验证码
 */
const getCode = async () => {
  try {
    const { img, uuid } = await getCodeImg()
    codeUrl.value = 'data:image/gif;base64,' + img
    formState.uuid = uuid
  } catch (error) {
    console.error('验证码获取失败', error)
  }
}

// 从cookie初始化
const initFromCookie = () => {
  const storageInfo = Cookies.get(storagePrefix + 'login')

  if (!storageInfo) return

  const { username, password, remember } = JSON.parse(storageInfo)

  formState.username = username || ''
  formState.password = password ? decrypt(password) : ''
  formState.remember = remember || false
}

initFromCookie()
getCode()
</script>

<style lang="scss" scoped>
.section {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background-color: #f3f6fd;
}

.header {
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;

  .logo {
    max-height: 100%;
  }
}

.main {
  flex: 1;
  overflow: hidden;
}

.bg {
  margin-top: -60px;
  overflow: hidden;
  .bg-img {
    width: 600px;
    vertical-align: bottom;
    user-select: none;
  }
}

.left {
  position: relative;
  flex: 1.5;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-end;
  height: 100%;
  padding-right: 50px;
  .title {
    font-size: 2em;
  }
  .sub-title {
    font-size: 1.3em;
    text-align: right;
    color: #92adcb;
  }
  .desc {
    margin: 100px 220px 0 0;
  }
  .desc-list {
    margin-top: 30px;
    line-height: 2;
    font-size: 20px;
    color: #000000e0;
    dt {
      display: none;
      font-weight: 600;
      color: rgba(21, 76, 255, 0.5);
    }
    dd {
      margin-left: var(--n);
    }
  }
}

.right {
  flex: 1;
  display: flex;
  padding-left: 20px;
  overflow: hidden;
}
.login-form {
  align-self: center;
  width: 460px;
  padding: 25px 25px 5px 25px;
  border-radius: 6px;
  background-color: #fff;

  .title {
    margin: 0 0 24px;
    text-align: center;
  }

  .ant-form-item {
    margin-bottom: 30px;
  }

  .form-input {
    border-radius: 2px !important;
    .ant-input-prefix {
      margin-right: 8px;
      color: #666;
    }
  }
}

.code {
  width: 33%;
  height: 40px;
  float: right;
  &-img {
    width: 100%;
    height: 40px;
    padding-left: 12px;
    cursor: pointer;
    vertical-align: middle;
  }
}
.pwd-eye-icon {
  user-select: none;
}
</style>
