/** 从 URL 中提取文件名 */
export const getFileNameFromUrl = (url: string): string => {
  try {
    const urlObj = new URL(url)
    const pathname = urlObj.pathname
    const fileName = pathname.split('/').pop() || 'unknown'
    return decodeURIComponent(fileName)
  } catch {
    // 如果 URL 解析失败，尝试从字符串中提取
    const parts = url.split('/')
    return parts[parts.length - 1] || 'unknown'
  }
}

/** 判断是否为图片 */
export const isImage = (filename: string): boolean => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg'].includes(ext)
}

/** 格式化文件大小 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/** 获取文件图标 */
export const getFileIcon = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  if (isImage(ext)) {
    return 'ep:picture'
  }
  return 'ep:document'
}

/**
 * 文件类型徽章底色（沿用 Office / Adobe 的识别色，白字压在上面）。
 *
 * 这张表原先在 ProjectTbfile / ProjectInviteUpload / ProjectTbEval / ProjectFileForm
 * 各存一份，取值还互相漂移（txt 有 #86909c 也有 #909399），这里合成一份。
 *
 * 徽章是 11~13px 的白字，要 4.5:1。原表里 zip #fa8c16 只有 2.38、PDF #f40f02 4.28、
 * PPT #d24726 4.49、txt #86909c 3.24 都不达标，各自压深到刚好过线又不失识别度的一档
 * （实测：Word 7.16 · Excel 5.83 · PPT 5.26 · PDF 5.14 · 压缩包 6.05 · 其它 5.65）。
 * 这几个是文件类型的身份色、不属于主题色，所以留字面值、不随明暗翻转（白字两态都成立）。
 */
export const getFileTypeColor = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  const map: Record<string, string> = {
    doc: '#2b579a',
    docx: '#2b579a',
    xls: '#217346',
    xlsx: '#217346',
    ppt: '#c0401d',
    pptx: '#c0401d',
    pdf: '#d81b0d',
    zip: '#a64500',
    rar: '#a64500',
    '7z': '#a64500',
    txt: '#5f6874'
  }
  return map[ext] || '#5f6874'
}
