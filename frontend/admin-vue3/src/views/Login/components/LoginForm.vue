<template>
  <a-form
    ref="formLogin"
    :model="loginData.loginForm"
    :rules="LoginRules"
    class="login-form"
    layout="vertical"
    size="large"
  >
    <!-- 别再套 a-row + 单列 a-col：每个表单项都成了自己那列的 :last-child，
         「最后一项不留底边距」的规则会命中全部项，间距被吃干净、标题压到输入框上。
         layout="vertical" 的表单项本来就自带纵向节奏，直接平铺即可。 -->
    <a-form-item class="lf-title-item" hide-label>
      <LoginFormTitle class="w-full" />
    </a-form-item>
    <a-form-item v-if="loginData.tenantEnable === 'true'" field="tenantName" hide-label>
      <a-input
        v-model="loginData.loginForm.tenantName"
        :placeholder="t('login.tenantNamePlaceholder')"
      >
        <template #prefix><Icon icon="ep:house" /></template>
      </a-input>
    </a-form-item>
    <a-form-item field="username" hide-label>
      <a-input v-model="loginData.loginForm.username" :placeholder="t('login.usernamePlaceholder')">
        <template #prefix><Icon icon="ep:avatar" /></template>
      </a-input>
    </a-form-item>
    <a-form-item field="password" hide-label>
      <a-input-password
        v-model="loginData.loginForm.password"
        :placeholder="t('login.passwordPlaceholder')"
        @keyup.enter="handleLogin()"
      >
        <template #prefix><Icon icon="ep:lock" /></template>
      </a-input-password>
    </a-form-item>
    <a-form-item class="lf-remember-item" hide-label>
      <a-checkbox v-model="loginData.loginForm.rememberMe">
        {{ t('login.remember') }}
      </a-checkbox>
    </a-form-item>
    <a-form-item class="lf-submit-item" hide-label>
      <XButton
        :loading="loginLoading"
        :title="t('login.login')"
        class="w-full"
        type="primary"
        @click="handleLogin()"
      />
    </a-form-item>
  </a-form>
</template>
<script lang="ts" setup>
import { showFullLoading } from '@/utils/loading'
import LoginFormTitle from './LoginFormTitle.vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'

import * as authUtil from '@/utils/auth'
import * as LoginApi from '@/api/login'
import { useFormValid } from './useLogin'

defineOptions({ name: 'LoginForm' })

const { t } = useI18n()
const formLogin = ref()
const { validForm } = useFormValid(formLogin)
const { currentRoute, push } = useRouter()
const redirect = ref<string>('')
const loginLoading = ref(false)

const LoginRules = {
  tenantName: [required],
  username: [required],
  password: [required]
}
const loginData = reactive({
  tenantEnable: import.meta.env.VITE_APP_TENANT_ENABLE,
  loginForm: {
    tenantName: import.meta.env.VITE_APP_DEFAULT_LOGIN_TENANT || '',
    username: import.meta.env.VITE_APP_DEFAULT_LOGIN_USERNAME || '',
    password: import.meta.env.VITE_APP_DEFAULT_LOGIN_PASSWORD || '',
    rememberMe: true
  }
})

// 记住我：只回填租户与账号，密码每次都要重新输
const getLoginFormCache = () => {
  const loginForm = authUtil.getLoginForm()
  if (loginForm) {
    loginData.loginForm = {
      ...loginData.loginForm,
      username: loginForm.username ? loginForm.username : loginData.loginForm.username,
      rememberMe: loginForm.rememberMe,
      tenantName: loginForm.tenantName ? loginForm.tenantName : loginData.loginForm.tenantName
    }
  }
}
const loading = ref() // 全屏加载实例（showFullLoading）
// 登录
const handleLogin = async () => {
  loginLoading.value = true
  try {
    const data = await validForm()
    if (!data) {
      return
    }
    const form = { ...loginData.loginForm }
    // 安全：不再前端换取租户编号，tenantName 随登录请求体交由后端内部解析租户
    const res = await LoginApi.login({
      tenantName: form.tenantName,
      username: form.username,
      password: form.password
    })
    if (!res) {
      return
    }
    loading.value = showFullLoading({ text: '正在加载系统中...' })
    if (form.rememberMe) {
      authUtil.setLoginForm({
        tenantName: form.tenantName,
        username: form.username,
        rememberMe: true
      })
    } else {
      authUtil.removeLoginForm()
    }
    authUtil.setToken(res)
    // 动态路由要等 permission 守卫里 generateRoutes 之后才有；
    // 退出登录用了 location.replace，内存里的 addRouters 是空的，
    // 不能再读 addRouters[0].path，否则会抛错卡在登录页。
    // 落到 '/'，由守卫拉取用户信息、注入路由后再进首页。
    const target = redirect.value && redirect.value !== '/login' ? redirect.value : '/'
    await push(target)
  } finally {
    loginLoading.value = false
    loading.value?.close()
  }
}

watch(
  () => currentRoute.value,
  (route: RouteLocationNormalizedLoaded) => {
    redirect.value = route?.query?.redirect as string
  },
  {
    immediate: true
  }
)
onMounted(() => {
  getLoginFormCache()
})
</script>

<style lang="scss" scoped>
:deep(.anticon) {
  &:hover {
    color: var(--color-primary-6) !important;
  }
}
</style>
