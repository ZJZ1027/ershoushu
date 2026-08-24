/**
 * 纯文本展示清洗工具。
 *
 * 背景：外部导入的文本里常混入字面的 HTML 实体（如 `&#xa0;`、`&amp;`、`&lt;`），
 * 直接用 `{{ }}` 文本插值会把这些实体「原样」显示出来。
 * 这里在展示层解码，既修掉观感问题，也不改动库里的存量数据。
 */

/** 仅解码 HTML 实体（利用 textarea 的 RCDATA 解析，安全、不执行任何标签）。 */
export function decodeHtmlEntities(input?: string | null): string {
  if (!input) return ''
  if (input.indexOf('&') === -1) return input
  const el = document.createElement('textarea')
  el.innerHTML = input
  return el.value
}

/**
 * 解码实体 + 把不换行空格（nbsp / \u00a0）归一为普通空格 + 去首尾空白。
 * 用于标题、摘要等「单行/多行截断」的纯文本展示。
 */
export function cleanText(input?: string | null): string {
  return decodeHtmlEntities(input).replace(/\u00a0/g, ' ').trim()
}
