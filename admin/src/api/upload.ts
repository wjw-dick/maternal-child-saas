import http from './http'

export interface UploadResult {
  id: number
  url: string
  name: string
  size: number
}

const IMAGE_TYPES = ['jpg', 'jpeg', 'png', 'gif', 'webp']

/** 上传文件到租户存储，返回访问地址。 */
export async function uploadFile(
  file: File,
  bizType = 'common',
  onProgress?: (percent: number) => void
): Promise<UploadResult> {
  const form = new FormData()
  form.append('file', file)
  form.append('bizType', bizType)
  return http.post('/tenant/files', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000,
    onUploadProgress: (event) => {
      if (event.total && onProgress) {
        onProgress(Math.min(99, Math.round((event.loaded * 100) / event.total)))
      }
    }
  })
}

export function isImage(url: string) {
  const ext = (url || '').split('?')[0].split('.').pop()?.toLowerCase() || ''
  return IMAGE_TYPES.includes(ext)
}
