<template>
  <div class="auth-page">
    <section class="auth-hero" aria-hidden="true">
      <div class="auth-hero-media"></div>
      <div class="auth-hero-veil"></div>
      <div class="auth-hero-copy">
        <p class="auth-kicker">Campus Book Stack</p>
        <h1 class="auth-brand">校园二手书</h1>
        <p class="auth-lead">把闲置教材留给下一位同学，面交更安心。</p>
      </div>
    </section>

    <section class="auth-panel">
      <div class="auth-card">
        <p class="auth-card-kicker">欢迎回来</p>
        <h2 class="auth-card-title">登录</h2>
        <p class="auth-card-desc">使用学号账号进入校园书栈</p>

        <a-form class="auth-form" :model="form" layout="vertical" @submit-success="submit">
          <a-form-item field="username" label="账号" required>
            <a-input v-model="form.username" placeholder="请输入账号" size="large" allow-clear />
          </a-form-item>
          <a-form-item field="password" label="密码" required>
            <a-input-password v-model="form.password" placeholder="请输入密码" size="large" />
          </a-form-item>
          <a-button html-type="submit" type="primary" long size="large" :loading="loading">
            进入书栈
          </a-button>
        </a-form>

        <p class="auth-foot">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
        <p class="auth-hint">演示账号 student1 / admin123</p>
      </div>
    </section>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getLoginForm, setLoginForm } from '@/utils/auth'

const user = useUserStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)

const saved = getLoginForm()
const form = reactive({
  username: saved?.username || '',
  password: saved?.password || ''
})

const submit = async () => {
  loading.value = true
  try {
    await user.login(form.username, form.password)
    setLoginForm({ username: form.username, password: form.password })
    router.push((route.query.redirect as string) || '/')
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
}

.auth-hero {
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  color: #f4f8f6;
}

.auth-hero-media {
  position: absolute;
  inset: -4%;
  background:
    linear-gradient(145deg, rgba(10, 40, 34, 0.2), rgba(10, 40, 34, 0.55)),
    url('https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=1600&q=80')
      center / cover no-repeat;
  animation: drift 22s ease-in-out infinite alternate;
}

.auth-hero-veil {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(160deg, rgba(13, 107, 88, 0.55), rgba(20, 35, 28, 0.72) 58%, rgba(20, 35, 28, 0.85)),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.14), transparent 40%);
}

.auth-hero-copy {
  position: relative;
  z-index: 1;
  max-width: 480px;
  padding: clamp(40px, 8vw, 88px);
  animation: auth-rise 0.7s ease both;
}

.auth-kicker {
  margin: 0 0 14px;
  font-size: 0.78rem;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  opacity: 0.78;
}

.auth-brand {
  margin: 0;
  font-family: var(--font-brand);
  font-size: clamp(2.6rem, 5vw, 4rem);
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1.05;
}

.auth-lead {
  margin: 18px 0 0;
  max-width: 22em;
  font-size: 1.05rem;
  line-height: 1.65;
  opacity: 0.9;
}

.auth-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background:
    radial-gradient(500px 280px at 80% 0%, rgba(13, 107, 88, 0.08), transparent 60%),
    linear-gradient(180deg, #f7faf8, #eef3f0);
}

.auth-card {
  width: min(420px, 100%);
  animation: auth-rise 0.55s ease 0.08s both;
}

.auth-card-kicker {
  margin: 0 0 8px;
  color: var(--teal);
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.auth-card-title {
  margin: 0;
  font-family: var(--font-brand);
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.03em;
}

.auth-card-desc {
  margin: 8px 0 28px;
  color: var(--muted);
}

.auth-form :deep(.arco-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.arco-btn) {
  margin-top: 6px;
  height: 44px;
  font-weight: 650;
}

.auth-foot {
  margin: 22px 0 0;
  color: var(--ink-soft);
  font-size: 0.92rem;
}

.auth-foot a {
  color: var(--teal-deep);
  font-weight: 700;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.auth-foot a:hover {
  border-bottom-color: var(--teal);
}

.auth-hint {
  margin: 10px 0 0;
  color: var(--muted);
  font-size: 0.8rem;
}

@media (max-width: 900px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .auth-hero {
    min-height: 38vh;
  }

  .auth-hero-copy {
    padding: 36px 24px 28px;
  }

  .auth-brand {
    font-size: clamp(2.2rem, 9vw, 3rem);
  }

  .auth-panel {
    padding: 28px 20px 48px;
    align-items: flex-start;
  }
}
</style>
