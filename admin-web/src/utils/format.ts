/**
 * 格式化日期时间
 */
export const formatDateTime = (dateStr?: string | Date): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/**
 * 格式化金额
 */
export const formatMoney = (amount?: number): string => {
  if (amount === undefined || amount === null || isNaN(amount)) return '¥0.00'
  return `¥${Number(amount).toFixed(2)}`
}

