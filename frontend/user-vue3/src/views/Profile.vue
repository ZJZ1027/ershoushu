<template>
  <div class="page" style="max-width:520px">
    <h1 class="page-title">个人资料</h1>
    <p class="page-sub">手机和微信仅在卖家同意预约后对对方可见。</p>
    <div class="panel">
    <a-form :model="form" layout="vertical" @submit-success="save">
      <a-form-item label="昵称"><a-input v-model="form.nickname" /></a-form-item>
      <a-form-item label="手机"><a-input v-model="form.mobile" /></a-form-item>
      <a-form-item label="微信"><a-input v-model="form.wechat" /></a-form-item>
      <a-form-item label="校区"><a-input v-model="form.campus" /></a-form-item>
      <a-button html-type="submit" type="primary">保存</a-button>
    </a-form>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { Message } from '@arco-design/web-vue'
import { updateProfile } from '@/api'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const form = reactive({ nickname: '', mobile: '', wechat: '', campus: '' })

onMounted(async () => {
  if (!user.profile) await user.loadProfile()
  Object.assign(form, user.profile || {})
})

const save = async () => {
  await updateProfile(form)
  await user.loadProfile()
  Message.success('已保存')
}
</script>
