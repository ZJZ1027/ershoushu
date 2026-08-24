<template>
  <a-form ref="formRef" :model="password" :rules="rules" auto-label-width class="profile-form">
    <a-form-item :label="t('profile.password.oldPassword')" field="oldPassword">
      <InputPassword v-model="password.oldPassword" />
    </a-form-item>
    <a-form-item :label="t('profile.password.newPassword')" field="newPassword">
      <InputPassword v-model="password.newPassword" strength />
    </a-form-item>
    <a-form-item :label="t('profile.password.confirmPassword')" field="confirmPassword">
      <InputPassword v-model="password.confirmPassword" strength />
    </a-form-item>
    <a-form-item>
      <a-space :size="12">
        <XButton :title="t('common.save')" type="primary" @click="submit()" />
        <XButton :title="t('common.reset')" @click="reset()" />
      </a-space>
    </a-form-item>
  </a-form>
</template>
<script lang="ts" setup>
import { InputPassword } from '@/components/InputPassword'
import { updateUserPassword } from '@/api/system/user/profile'

defineOptions({ name: 'ResetPwd' })

const { t } = useI18n()
const message = useMessage()
const formRef = ref()
const password = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单校验：Arco 自定义校验签名为 (value, callback)，callback 传错误文案表示失败
const equalToPassword = (value: string, callback: (error?: string) => void) => {
  if (password.newPassword !== value) {
    callback(t('profile.password.diffPwd'))
  } else {
    callback()
  }
}

const rules = reactive<Recordable>({
  oldPassword: [
    { required: true, message: t('profile.password.oldPwdMsg') },
    { minLength: 4, maxLength: 16, message: t('profile.password.pwdRules') }
  ],
  newPassword: [
    { required: true, message: t('profile.password.newPwdMsg') },
    { minLength: 4, maxLength: 16, message: t('profile.password.pwdRules') }
  ],
  confirmPassword: [
    { required: true, message: t('profile.password.cfPwdMsg') },
    { validator: equalToPassword }
  ]
})

const submit = async () => {
  const form = unref(formRef)
  if (!form) return
  const errors = await form.validate()
  if (errors) return
  await updateUserPassword(password.oldPassword, password.newPassword)
  message.success(t('common.updateSuccess'))
}

const reset = () => {
  unref(formRef)?.resetFields()
}
</script>
<style scoped>
.profile-form {
  max-width: 520px;
}
</style>
