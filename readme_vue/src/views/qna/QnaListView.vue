<template>
  <div class="qna-list-page">
    <!-- 헤더 -->
    <section class="page-header">
      <div class="header-inner">
        <h1 class="page-title">1:1 문의</h1>
        <p class="page-sub">궁금하신 사항을 문의해 주세요. 빠르게 답변드리겠습니다.</p>
      </div>
    </section>

    <!-- 탭 + 작성 버튼 -->
    <div class="toolbar">
      <div class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: selectedStatus === tab.value }"
          @click="changeStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
      <router-link to="/qna/write" class="write-btn" v-if="isLoggedIn">
        <span class="icon">✏️</span> 문의하기
      </router-link>
    </div>

    <!-- 로딩 -->
    <div class="loading-wrap" v-if="loading">
      <span class="spinner" />
    </div>

    <!-- 목록 없음 -->
    <div class="empty-state" v-else-if="!qnaList.length">
      <p class="empty-icon">💬</p>
      <p class="empty-text">등록된 문의가 없습니다.</p>
      <router-link to="/qna/write" class="write-btn-sm" v-if="isLoggedIn">첫 문의 남기기</router-link>
    </div>

    <!-- 목록 테이블 -->
    <div class="table-wrap" v-else>
      <table class="qna-table">
        <thead>
          <tr>
            <th class="col-num">번호</th>
            <th class="col-cat">카테고리</th>
            <th class="col-title">제목</th>
            <th class="col-writer">작성자</th>
            <th class="col-date">작성일</th>
            <th class="col-status">상태</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in qnaList"
            :key="item.id"
            class="qna-row"
            @click="goDetail(item)"
          >
            <td class="col-num">{{ item.id }}</td>
            <td class="col-cat">
              <span class="category-badge">{{ item.category }}</span>
            </td>
            <td class="col-title">
              <span class="lock-icon" v-if="item.isSecret">🔒</span>
              <span class="title-text">
                {{ isHidden(item) ? '비밀글입니다.' : item.title }}
              </span>
            </td>
            <td class="col-writer">
              {{ maskName(item.memberName) }}
            </td>
            <td class="col-date">{{ formatDate(item.createdAt) }}</td>
            <td class="col-status">
              <span class="status-badge" :class="statusClass(item.qnaStatus)">
                {{ statusLabel(item.qnaStatus) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination" v-if="totalPages > 1">
      <button
        class="page-btn"
        :disabled="currentPage === 0"
        @click="changePage(currentPage - 1)"
      >‹ 이전</button>

      <button
        v-for="p in pageNumbers"
        :key="p"
        class="page-btn"
        :class="{ active: p === currentPage }"
        @click="changePage(p)"
      >{{ p + 1 }}</button>

      <button
        class="page-btn"
        :disabled="currentPage === totalPages - 1"
        @click="changePage(currentPage + 1)"
      >다음 ›</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { getQnaList } from '@/api/qna.js'

const router = useRouter()
const authStore = useAuthStore()

const isLoggedIn = computed(() => !!authStore.accessToken)
const currentMemberId = computed(() => authStore.memberId)

const qnaList = ref([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(1)
const selectedStatus = ref('')

const statusTabs = [
  { value: '', label: '전체' },
  { value: 'WAITING', label: '답변대기' },
  { value: 'PROCESSING', label: '처리중' },
  { value: 'COMPLETE', label: '답변완료' },
]

const pageNumbers = computed(() => {
  const pages = []
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value - 1, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getQnaList(currentPage.value, selectedStatus.value)
    const data = res.data
    qnaList.value = data.content ?? []
    totalPages.value = data.totalPages ?? 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const changeStatus = (status) => {
  selectedStatus.value = status
  currentPage.value = 0
  fetchList()
}

const changePage = (p) => {
  currentPage.value = p
  fetchList()
}

const goDetail = (item) => {
  if (item.isSecret && !canViewSecret(item)) return
  router.push(`/qna/detail/${item.id}`)
}

const canViewSecret = (item) => {
  if (!isLoggedIn.value) return false
  return item.memberId === currentMemberId.value || authStore.isAdmin
}

const isHidden = (item) => item.isSecret && !canViewSecret(item)

const maskName = (name = '') => {
  if (!name || name.length <= 1) return name
  return name[0] + '*'.repeat(name.length - 1)
}

const formatDate = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
  })
}

const statusLabel = (s) => ({ WAITING: '답변대기', PROCESSING: '처리중', COMPLETE: '답변완료' }[s] ?? s)
const statusClass = (s) => ({ WAITING: 'waiting', PROCESSING: 'processing', COMPLETE: 'complete' }[s] ?? '')

onMounted(fetchList)
</script>

<style scoped>
/* ── 레이아웃 ─────────────────────────── */
.qna-list-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px 60px;
}

/* ── 헤더 ─────────────────────────────── */
.page-header {
  padding: 48px 0 32px;
  border-bottom: 2px solid #111;
  margin-bottom: 28px;
}
.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #111;
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}
.page-sub {
  font-size: 14px;
  color: #888;
  margin: 0;
}

/* ── 툴바 ─────────────────────────────── */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 10px;
}
.status-tabs {
  display: flex;
  gap: 6px;
}
.tab-btn {
  padding: 7px 16px;
  border: 1.5px solid #ddd;
  background: #fff;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  color: #555;
  transition: all 0.18s;
}
.tab-btn.active,
.tab-btn:hover {
  border-color: #111;
  background: #111;
  color: #fff;
}
.write-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 9px 20px;
  background: #111;
  color: #fff;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.18s;
}
.write-btn:hover { background: #333; }

/* ── 테이블 ────────────────────────────── */
.table-wrap { overflow-x: auto; }
.qna-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.qna-table th {
  padding: 12px 10px;
  border-top: 1px solid #111;
  border-bottom: 1px solid #ddd;
  font-weight: 600;
  color: #333;
  background: #fafafa;
  white-space: nowrap;
  text-align: center;
}
.qna-table td {
  padding: 14px 10px;
  border-bottom: 1px solid #eee;
  vertical-align: middle;
  text-align: center;
}
.qna-row {
  cursor: pointer;
  transition: background 0.12s;
}
.qna-row:hover { background: #f7f7f7; }

.col-num  { width: 70px; }
.col-cat  { width: 110px; }
.col-title{ text-align: left; }
.col-writer { width: 100px; }
.col-date { width: 110px; }
.col-status { width: 90px; }

.title-text { vertical-align: middle; }
.lock-icon { margin-right: 4px; font-size: 13px; }

/* ── 배지 ──────────────────────────────── */
.category-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  background: #f0f0f0;
  font-size: 12px;
  color: #555;
}
.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
.status-badge.waiting    { background: #fff3cd; color: #856404; }
.status-badge.processing { background: #cfe2ff; color: #084298; }
.status-badge.complete   { background: #d1e7dd; color: #0a3622; }

/* ── 빈 상태 ───────────────────────────── */
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #aaa;
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { font-size: 15px; margin-bottom: 20px; }
.write-btn-sm {
  display: inline-block;
  padding: 9px 24px;
  background: #111;
  color: #fff;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
}

/* ── 로딩 ──────────────────────────────── */
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 36px; height: 36px;
  border: 3px solid #eee;
  border-top-color: #111;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: block;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 페이지네이션 ──────────────────────── */
.pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 36px;
}
.page-btn {
  padding: 7px 14px;
  border: 1.5px solid #ddd;
  background: #fff;
  border-radius: 5px;
  font-size: 13px;
  cursor: pointer;
  color: #444;
  transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: #111; color: #111; }
.page-btn.active { background: #111; color: #fff; border-color: #111; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── 반응형 ────────────────────────────── */
@media (max-width: 640px) {
  .col-writer, .col-num { display: none; }
  .page-title { font-size: 20px; }
}
</style>