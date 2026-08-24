<template>
  <div class="lf-title">
    <h2 class="lf-title-main">{{ t('sys.login.signInFormTitle') }}</h2>
    <p class="lf-title-sub">请使用所属租户下的账号登录</p>
  </div>
</template>
<script lang="ts" setup>
defineOptions({ name: 'LoginFormTitle' })

const { t } = useI18n()
</script>

<style lang="scss" scoped>
.lf-title {
  margin-bottom: 6px;
}

.lf-title-main {
  margin: 0;
  font-size: 23px;
  font-weight: 600;
  line-height: 1.3;
  letter-spacing: -0.2px;
  color: var(--bm-text-1);
}

.lf-title-sub {
  margin: 7px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--bm-text-3);
}
</style>
