/**
 * 格式化日期时间
 */
export const formatDateTime = (dateStr?: string | Date): string => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return '-'
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * 仅格式化日期 (YYYY-MM-DD)
 */
export const formatDate = (dateStr?: string | Date): string => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return '-'
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/**
 * 格式化金额
 */
export const formatMoney = (amount?: number): string => {
  if (amount === undefined || amount === null || isNaN(amount)) return '¥0.00'
  return `¥${Number(amount).toFixed(2)}`
}

/**
 * 格式化秒数为 mm:ss
 */
export const formatDuration = (seconds: number): string => {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

