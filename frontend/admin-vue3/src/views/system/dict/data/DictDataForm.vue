<template>
  <a-modal
    v-model:visible="dialogVisible"
    :title="dialogTitle"
    :width="600"
    :mask-closable="false"
    :ok-text="formType === 'create' ? '确定创建' : '保存'"
    cancel-text="取消"
    :ok-loading="formLoading"
    :on-before-ok="handleBeforeOk"
    @cancel="dialogVisible = false"
  >
    <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
      <a-form-item field="dictType" label="字典类型">
        <a-input
          v-model="formData.dictType"
          :disabled="typeof formData.id !== 'undefined'"
          placeholder="请输入参数名称"
          allow-clear
        />
      </a-form-item>
      <a-form-item field="label" label="数据标签" required>
        <a-input v-model="formData.label" placeholder="请输入数据标签" allow-clear />
      </a-form-item>
      <a-form-item field="value" label="数据键值" required>
        <a-input v-model="formData.value" placeholder="请输入数据键值" allow-clear />
      </a-form-item>
      <a-form-item field="sort" label="显示排序" required>
        <a-input-number v-model="formData.sort" :min="0" style="width: 100%" />
      </a-form-item>
      <a-form-item field="status" label="状态" required>
        <a-radio-group v-model="formData.status">
          <a-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item field="colorType" label="颜色类型">
        <a-select v-model="formData.colorType">
          <a-option
            v-for="item in colorTypeOptions"
            :key="item.value"
            :value="item.value"
            :label="item.label + '(' + item.value + ')'"
          />
        </a-select>
      </a-form-item>
      <a-form-item field="cssClass" label="CSS Class">
        <a-input v-model="formData.cssClass" placeholder="请输入 CSS Class" allow-clear />
      </a-form-item>
      <a-form-item field="remark" label="备注">
        <a-textarea
          v-model="formData.remark"
          placeholder="请输入内容"
          :auto-size="{ minRows: 3, maxRows: 5 }"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as DictDataApi from '@/api/system/dict/dict.data'
import { CommonStatusEnum } from '@/utils/constants'

defineOptions({ name: 'SystemDictDataForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  sort: undefined,
  label: '',
  value: '',
  dictType: '',
  status: CommonStatusEnum.ENABLE,
  colorType: '',
  cssClass: '',
  remark: ''
})
const formRules = {
  label: [{ required: true, message: '数据标签不能为空' }],
  value: [{ required: true, message: '数据键值不能为空' }],
  sort: [{ required: true, message: '数据顺序不能为空' }],
  status: [{ required: true, message: '状态不能为空' }]
}
const formRef = ref() // 表单 Ref

// 数据标签回显样式
const colorTypeOptions = readonly([
  {
    value: 'default',
    label: '默认'
  },
  {
    value: 'primary',
    label: '主要'
  },
  {
    value: 'success',
    label: '成功'
  },
  {
    value: 'info',
    label: '信息'
  },
  {
    value: 'warning',
    label: '警告'
  },
  {
    value: 'danger',
    label: '危险'
  }
])

/** 打开弹窗 */
const open = async (type: string, id?: number, dictType?: string) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增字典数据' : '编辑字典数据'
  formType.value = type
  resetForm()
  if (dictType) {
    formData.value.dictType = dictType
  }
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await DictDataApi.getDictData(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const handleBeforeOk = async () => {
  const errors = await formRef.value?.validate()
  if (errors) return false
  // 提交请求
  formLoading.value = true
  try {
    // 新增时 id/sort 为空、createTime 由后端生成，与 DictDataVO 只是部分重叠，需先转 unknown
    const data = formData.value as unknown as DictDataApi.DictDataVO
    if (formType.value === 'create') {
      await DictDataApi.createDictData(data)
      message.success(t('common.createSuccess'))
    } else {
      await DictDataApi.updateDictData(data)
      message.success(t('common.updateSuccess'))
    }
    // 发送操作成功的事件
    emit('success')
    return true
  } catch {
    return false
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    sort: undefined,
    label: '',
    value: '',
    dictType: '',
    status: CommonStatusEnum.ENABLE,
    colorType: '',
    cssClass: '',
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
