import { Ref } from 'vue'

export function useFormValid(formRef: Ref<any>) {
  // Arco a-form.validate() 校验通过返回 undefined，失败返回错误对象（不会 reject）
  // 故取反：通过 => true，失败 => false，沿用调用方 `if (!data) return` 的语义
  async function validForm() {
    const form = unref(formRef)
    if (!form) return false
    const errors = await form.validate()
    return !errors
  }

  return {
    validForm
  }
}
