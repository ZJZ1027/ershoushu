/** 组件尺寸（对应 Arco 的 size：mini/small/medium/large，这里沿用后台设置里的三档） */
export type ComponentSize = 'default' | 'small' | 'large'

/**
 * 字典数据的颜色档位。取值由后端字典表的 color_type 列决定，
 * 前端 DictTag 再把它映射成 Arco 的 a-tag color。
 */
export type DictColorType = 'success' | 'info' | 'warning' | 'danger'
