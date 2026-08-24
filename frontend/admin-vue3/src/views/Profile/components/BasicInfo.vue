<template>
  <a-form ref="formRef" :model="formModel" :rules="rules" auto-label-width class="profile-form">
    <a-form-item field="nickname" :label="t('profile.user.nickname')">
      <a-input v-model="formModel.nickname" allow-clear :placeholder="t('profile.user.nickname')" />
    </a-form-item>
    <a-form-item field="mobile" :label="t('profile.user.mobile')">
      <a-input v-model="formModel.mobile" allow-clear :placeholder="t('profile.user.mobile')" />
    </a-form-item>
    <a-form-item field="email" :label="t('profile.user.email')">
      <a-input v-model="formModel.email" allow-clear :placeholder="t('profile.user.email')" />
    </a-form-item>
    <a-form-item field="sex" :label="t('profile.user.sex')">
      <a-radio-group v-model="formModel.sex">
        <a-radio :value="1">{{ t('profile.user.man') }}</a-radio>
        <a-radio :value="2">{{ t('profile.user.woman') }}</a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item>
      <a-space :size="12">
        <XButton :title="t('common.save')" type="primary" @click="submit()" />
        <XButton :title="t('common.reset')" @click="init()" />
      </a-space>
    </a-form-item>
  </a-form>
</template>
<script lang="ts" setup>
import {
  getUserProfile,
  updateUserProfile,
  UserProfileUpdateReqVO
} from '@/api/system/user/profile'
import { useUserStore } from '@/store/modules/user'

defineOptions({ name: 'BasicInfo' })

const { t } = useI18n()
const message = useMessage() // 消息弹窗
const userStore = useUserStore()

// 定义事件
const emit = defineEmits<{
  (e: 'success'): void
}>()

// 表单校验（Arco 规则：正则用 match，trigger 交由 form-item/validate-trigger）
const rules = reactive<Recordable>({
  nickname: [{ required: true, message: t('profile.rules.nickname') }],
  email: [
    { required: true, message: t('profile.rules.mail') },
    { type: 'email', message: t('profile.rules.truemail') }
  ],
  mobile: [
    { required: true, message: t('profile.rules.phone') },
    { match: /^1[3-9]\d{9}$/, message: t('profile.rules.truephone') }
  ]
})

// 表单数据模型
const formModel = ref<Recordable>({ sex: 0 })

const formRef = ref() // 表单 Ref（Arco FormInstance）

// 监听 userStore 中头像的变化，同步更新表单数据（头像不在表单字段中，但随提交一起上送）
watch(
  () => userStore.getUser.avatar,
  (newAvatar) => {
    if (newAvatar && formModel.value) {
      formModel.value.avatar = newAvatar
    }
  }
)

const submit = async () => {
  const form = unref(formRef)
  if (!form) return
  // Arco validate()：返回 undefined 表示校验通过，否则为错误对象
  const errors = await form.validate()
  if (errors) return
  const data = unref(formModel) as UserProfileUpdateReqVO
  await updateUserProfile(data)
  message.success(t('common.updateSuccess'))
  const profile = await init()
  await userStore.setUserNicknameAction(profile.nickname)
  // 发送成功事件
  emit('success')
}

const init = async () => {
  const res = await getUserProfile()
  formModel.value = res || {}
  return res
}

onMounted(async () => {
  await init()
})
</script>
<style scoped>
/* 约束表单宽度，避免输入框在 2/3 宽卡片里被拉得过长 */
.profile-form {
  max-width: 520px;
}
</style>
