<template>
  <div class="page" style="max-width:520px">
    <h1 class="page-title">个人资料</h1>
    <p class="page-sub">手机和微信仅在卖家同意预约后对对方可见。更换头像需管理端审核通过后才会对外展示。</p>
    <div class="panel">
      <a-form :model="form" layout="vertical" @submit-success="save">
        <a-form-item label="头像">
          <div class="avatar-row">
            <button type="button" class="avatar-btn" :disabled="uploading || auditStatus === 1" @click="pickAvatar">
              <img v-if="avatarPreview" :src="avatarPreview" alt="头像" />
              <span v-else class="avatar-fallback">{{ avatarLetter }}</span>
              <span class="avatar-mask">{{ avatarMaskText }}</span>
            </button>
            <div class="avatar-hint">
              <p>当前展示为已通过审核的头像</p>
              <p v-if="auditStatus === 1" class="status pending">新头像审核中，通过前仍显示旧头像</p>
              <p v-else-if="auditStatus === 2" class="status rejected">
                上次头像未通过审核{{ rejectReason ? '：' + rejectReason : '' }}
              </p>
              <p class="muted">支持 JPG / PNG，建议正方形，不超过 5MB</p>
              <div v-if="pendingPreview" class="pending-box">
                <span class="muted">待审预览</span>
                <img :src="pendingPreview" alt="待审头像" />
              </div>
              <a-button size="small" :loading="uploading" :disabled="auditStatus === 1" @click="pickAvatar">
                {{ auditStatus === 1 ? '审核中' : '选择图片' }}
              </a-button>
            </div>
            <input
              ref="fileInput"
              class="file-hidden"
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              @change="onAvatarChange"
            />
          </div>
        </a-form-item>
        <a-form-item label="昵称"><a-input v-model="form.nickname" placeholder="展示给其他同学的名字" /></a-form-item>
        <a-form-item label="个性签名">
          <a-textarea
            v-model="form.signature"
            placeholder="一句话介绍自己，如：计算机大二，教材随缘出"
            :max-length="100"
            show-word-limit
            :auto-size="{ minRows: 2, maxRows: 4 }"
          />
        </a-form-item>
        <a-form-item label="手机"><a-input v-model="form.mobile" placeholder="面交联系用" /></a-form-item>
        <a-form-item label="微信"><a-input v-model="form.wechat" placeholder="选填" /></a-form-item>
        <a-form-item label="校区"><a-input v-model="form.campus" placeholder="如：本部" /></a-form-item>
        <a-button html-type="submit" type="primary" :loading="saving">保存</a-button>
      </a-form>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { updateProfile, uploadFile } from '@/api'
import { fileUrl } from '@/api/http'
import { useUserStore } from '@/stores/user'

const user = useUserStore()
const fileInput = ref<HTMLInputElement>()
const uploading = ref(false)
const saving = ref(false)
const auditStatus = ref(0)
const rejectReason = ref('')
const pendingAvatar = ref('')
const draftAvatar = ref('')
const form = reactive({
  nickname: '',
  signature: '',
  mobile: '',
  wechat: '',
  campus: '',
  avatar: ''
})

const avatarPreview = computed(() => fileUrl(form.avatar))
const pendingPreview = computed(() => fileUrl(draftAvatar.value || pendingAvatar.value))
const avatarLetter = computed(() => {
  const name = form.nickname || user.profile?.username || '我'
  return String(name).trim().charAt(0).toUpperCase() || '我'
})
const avatarMaskText = computed(() => {
  if (uploading.value) return '上传中'
  if (auditStatus.value === 1) return '审核中'
  return '更换'
})

const fillFromProfile = () => {
  const p = user.profile || {}
  form.nickname = p.nickname || ''
  form.signature = p.signature || ''
  form.mobile = p.mobile || ''
  form.wechat = p.wechat || ''
  form.campus = p.campus || ''
  form.avatar = p.avatar || ''
  pendingAvatar.value = p.avatarPending || ''
  auditStatus.value = Number(p.avatarAuditStatus) || 0
  rejectReason.value = p.avatarRejectReason || ''
  draftAvatar.value = ''
}

onMounted(async () => {
  if (!user.profile) await user.loadProfile()
  fillFromProfile()
})

const pickAvatar = () => {
  if (auditStatus.value === 1) {
    Message.warning('已有头像在审核中，请等待结果')
    return
  }
  fileInput.value?.click()
}

const onAvatarChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    Message.warning('请选择图片文件')
    input.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    Message.warning('图片请控制在 5MB 以内')
    input.value = ''
    return
  }
  uploading.value = true
  try {
    const url = await uploadFile(file)
    draftAvatar.value = String(url || '')
    Message.success('图片已选好，点击保存提交审核')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

const save = async () => {
  saving.value = true
  try {
    const payload: any = {
      nickname: form.nickname,
      signature: form.signature,
      mobile: form.mobile,
      wechat: form.wechat,
      campus: form.campus
    }
    if (draftAvatar.value) {
      payload.avatar = draftAvatar.value
    }
    await updateProfile(payload)
    await user.loadProfile()
    fillFromProfile()
    Message.success(payload.avatar ? '资料已保存，头像已提交审核' : '已保存')
  } finally {
    saving.value = false
  }
}
</script>
<style scoped>
.avatar-row {
  display: flex;
  align-items: center;
  gap: 18px;
}

.avatar-btn {
  position: relative;
  flex: none;
  width: 88px;
  height: 88px;
  padding: 0;
  border: 2px solid var(--line);
  border-radius: 50%;
  overflow: hidden;
  background: var(--teal-soft);
  cursor: pointer;
  color: var(--teal-deep);
  font-family: var(--font-brand);
  font-size: 1.8rem;
  font-weight: 700;
}

.avatar-btn:disabled {
  cursor: not-allowed;
  opacity: 0.85;
}

.avatar-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.avatar-mask {
  position: absolute;
  inset: auto 0 0;
  padding: 4px 0;
  background: rgba(20, 35, 28, 0.62);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font-body);
  line-height: 1.2;
}

.avatar-hint p {
  margin: 0 0 4px;
  font-size: 0.92rem;
}

.avatar-hint .muted {
  margin-bottom: 10px;
}

.status.pending {
  color: #c46a00;
}

.status.rejected {
  color: var(--danger);
}

.pending-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 8px 0 12px;
}

.pending-box img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--line);
}

.file-hidden {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

@media (max-width: 480px) {
  .avatar-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
