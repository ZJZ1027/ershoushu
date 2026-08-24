/**
 * 审计日志里「客户端信息」的展示化处理。
 *
 * 日志表存的是原始 User-Agent 和 remoteAddr，直接铺进表格有两个问题：
 *   1. UA 串又长又同质（同一批人清一色 Mozilla/5.0 (Windows NT 10.0…）），占掉三成列宽却读不出信息；
 *   2. 本机访问会记成 IPv6 回环 0:0:0:0:0:0:0:1，运维一眼看不出这就是 127.0.0.1。
 * 这里只做展示层归一，原始值仍在 tooltip / 详情里可查。
 */

const BROWSERS: Array<{ test: RegExp; name: string }> = [
  // 顺序有讲究：Edge/Opera 的 UA 里也带 Chrome，必须先匹配它们
  { test: /Edg[eA]?\/([\d.]+)/, name: 'Edge' },
  { test: /OPR\/([\d.]+)/, name: 'Opera' },
  { test: /(?:Chrome|CriOS)\/([\d.]+)/, name: 'Chrome' },
  { test: /Firefox\/([\d.]+)/, name: 'Firefox' },
  { test: /Version\/([\d.]+).*Safari/, name: 'Safari' }
]

const OS_LIST: Array<{ test: RegExp; name: string }> = [
  { test: /Windows NT 10/, name: 'Windows' },
  { test: /Windows/, name: 'Windows' },
  { test: /Mac OS X/, name: 'macOS' },
  { test: /Android/, name: 'Android' },
  { test: /(iPhone|iPad|iOS)/, name: 'iOS' },
  { test: /Linux/, name: 'Linux' }
]

/** UA → 「Edge 148 · Windows」；识别不出时退回原串前 40 字 */
export const formatUserAgent = (ua?: string): string => {
  const s = String(ua || '')
  if (!s) return '—'
  const b = BROWSERS.find((x) => x.test.test(s))
  const os = OS_LIST.find((x) => x.test.test(s))
  const parts: string[] = []
  if (b) {
    const major = (b.test.exec(s)?.[1] || '').split('.')[0]
    parts.push(major ? `${b.name} ${major}` : b.name)
  }
  if (os) parts.push(os.name)
  return parts.length ? parts.join(' · ') : s.slice(0, 40)
}

/** IPv6 回环归一成 127.0.0.1，并剥掉 IPv4-mapped 前缀 */
export const formatIp = (ip?: string): string => {
  const s = String(ip || '').trim()
  if (!s) return '—'
  if (s === '::1' || s === '0:0:0:0:0:0:0:1') return '127.0.0.1'
  return s.replace(/^::ffff:/i, '')
}
