export interface PersonalItem {
  id: number
  userId: number
  itemName: string
  category?: string
  quantity: number
  unit?: string
  storageLocation?: string
  purchaseDate?: string
  status: 'ACTIVE' | 'ARCHIVED'
  statusDesc?: string
  remark?: string
  isLent: boolean
  currentLendingId?: number
  currentBorrower?: string
  currentLentAt?: string
  currentExpectedReturnAt?: string
  isOverdue: boolean
  createdAt: string
  updatedAt: string
}

export interface ItemLending {
  id: number
  userId: number
  itemId: number
  itemName?: string
  borrowerName: string
  lentAt: string
  expectedReturnAt?: string
  returnedAt?: string
  status: 'LENT' | 'RETURNED' | 'OVERDUE'
  statusDesc?: string
  isOverdue: boolean
  remark?: string
  createdAt: string
}

export interface ItemDetail {
  item: PersonalItem
  activeLending?: ItemLending
  lendingHistory: ItemLending[]
}

export interface Memo {
  id: number
  userId: number
  title: string
  content?: string
  tags?: string
  tagList?: string[]
  isPinned: number
  status: 'ACTIVE' | 'ARCHIVED'
  statusDesc?: string
  createdAt: string
  updatedAt: string
}

export interface Diary {
  id: number
  userId: number
  diaryDate: string
  title?: string
  content: string
  mood?: string
  moodDesc?: string
  weather?: string
  weatherDesc?: string
  coverUrl?: string
  createdAt: string
  updatedAt: string
}

export interface LifeRecordSummary {
  totalItemCount: number
  lentItemCount: number
  overdueLendingCount: number
  totalMemoCount: number
  pinnedMemoCount: number
  totalDiaryCount: number
  thisMonthDiaryCount: number
}

