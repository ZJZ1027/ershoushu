<template>
  <div class="change-avatar">
    <CropperAvatar
      ref="cropperRef"
      :btnProps="{ preIcon: 'ant-design:cloud-upload-outlined' }"
      :showBtn="false"
      :value="img"
      width="120px"
      @change="handelUpload"
    />
  </div>
</template>
<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import { updateUserProfile } from '@/api/system/user/profile'
import { CropperAvatar } from '@/components/Cropper'
import { useUserStore } from '@/store/modules/user'
import { uploadFile } from '@/api/infra/file'

defineOptions({ name: 'UserAvatar' })

defineProps({
  img: propTypes.string.def('')
})

const userStore = useUserStore()

const cropperRef = ref()
const handelUpload = async ({ data, filename }: { data: Blob; filename?: string }) => {
  // 裁剪结果是 Blob，包成 File 才能带上文件名，否则后端只能拿到 "blob" 这个名字
  const file = new File([data], filename || 'avatar.png', { type: data.type || 'image/png' })
  const res = await uploadFile({ file })
  const avatar = res.data as string
  await updateUserProfile({ avatar })

  // 关闭弹窗，并更新 userStore
  cropperRef.value.close()
  await userStore.setUserAvatarAction(avatar)
}
</script>

<style lang="scss" scoped>
.change-avatar {
  img {
    display: block;
    margin-bottom: 15px;
    border-radius: 50%;
  }
}
</style>
