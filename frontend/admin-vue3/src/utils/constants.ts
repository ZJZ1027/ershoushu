/**
 * 与后端一致的枚举常量。
 *
 * 只放「代码里要用来做判断」的枚举；纯展示用的枚举请走数据字典（DICT_TYPE），
 * 避免同一份取值在前端硬编码、后台字典里又维护一遍。
 */

// 全局通用状态：0 开启 / 1 停用，对应字典 common_status
export const CommonStatusEnum = {
  ENABLE: 0,
  DISABLE: 1
}

// 菜单类型：对应字典 system_menu_type
export const SystemMenuTypeEnum = {
  DIR: 1, // 目录
  MENU: 2, // 菜单
  BUTTON: 3 // 按钮
}

// 角色类型：对应字典 system_role_type
export const SystemRoleTypeEnum = {
  SYSTEM: 1, // 内置角色，不允许删除
  CUSTOM: 2 // 自定义角色
}
