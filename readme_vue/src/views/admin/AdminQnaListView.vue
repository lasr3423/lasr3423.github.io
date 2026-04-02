<template>
  <div class="admin-qna-list">
    <div class="page-title-row">
      <h1 class="page-title">QnA 관리</h1>
      <p class="page-sub">전체 문의를 조회하고 답변을 관리합니다.</p>
    </div>

    <!-- 필터 바 -->
    <div class="filter-bar">
      <div class="status-tabs">
        <button
          v-for="tab in statusTabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: selectedStatus === tab.value }"
          @click="changeStatus(tab.value)"
        >
          {{ tab.label }}
          <span class="tab-count" v-if="tab.value === 'WAITING' && waitingCount > 0">
            {{ waitingCount }}
          </span>
        </button>
      </div>
    </div>

    <!-- 로딩 -->
    <div class="loading-wrap" v-if="loading">
      <span class="spinner" />
    </div>

    <!-- 빈 목록 -->
    <div class="empty-state" v-else-if="!qnaList.length">
      <p>해당하는 문의가 없습니다.</p>
    </div>

    <!-- 테이블 -->
    <div class="table-wrap" v-else>
      <table class="qna-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-cat">카테고리</th>
            <th class="col-title">제목</th>
            <th class="col-member">작성자</th>
            <th class="col-date">작성일</th>
            <th class="col-status">상태</th>
            <th class="col-action">관리</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in qnaList"
            :key="item.id"
            class="qna-row"
          >
            <td class="col-id">{{ item.id }}</td>
            <td class="col-cat">
              <span class="category-badge">{{ item.category }}</span>
            </td>
            <td class="col-title title-cell">
              <span class="lock-icon" v-if="item.isSecret">🔒</span>
              <span>{{ item.title }}</span>
            </td>
            <td class="col-member">{{ item.memberName }}</td>
            <td class="col-date">{{ formatDate(item.createdAt) }}</td>
            <td class="col-status">
              <select
                class="status-select"
                :value="item.qnaStatus"
                @change="onStatusChange(item.id, $event.target.value)"
              >
                <option value="WAITING">답변대기</option>
                <option value="PROCESSING">처리중</option>
                <option value="COMPLETE">답변완료</option>
              </select>
            </td>
            <td class="col-action">
              <router-link
                :to="`/admin/qna/detail/${item.id}`"
                class="action-btn"
              >
                상세·답변
              </router-link>
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
import { adminGetQnaList, adminUpdateQnaStatus } from '@/api/qna.js'

const qnaList = ref([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(1)
const selectedStatus = ref('')
const waitingCount = ref(0)

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
    const res = await adminGetQnaList(currentPage.value, selectedStatus.value)
    const data = res.data
    qnaList.value = data.content ?? []
    totalPages.value = data.totalPages ?? 1
    // 미답변 카운트 추출
    if (!selectedStatus.value) {
      waitingCount.value = data.waitingCount ?? qnaList.value.filter(q => q.qnaStatus === 'WAITING').length
    }
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

const onStatusChange = async (id, qnaStatus) => {
  try {
    await adminUpdateQnaStatus(id, qnaStatus)
    const target = qnaList.value.find(q => q.id === id)
    if (target) target.qnaStatus = qnaStatus
  } catch (e) {
    alert(e?.response?.data?.message ?? '상태 변경에 실패했습니다.')
  }
}

const formatDate = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
  })
}

onMounted(fetchList)
</script>

<style scoped>
.admin-qna-list {
  padding: 32px 0;
}

/* ── 헤더 ─────────────────────────────── */
.page-title-row { margin-bottom: 28px; }
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #111;
  margin: 0 0 4px;
}
.page-sub { font-size: 13px; color: #888; margin: 0; }

/* ── 필터 ─────────────────────────────── */
.filter-bar { margin-bottom: 20px; }
.status-tabs { display: flex; gap: 6px; flex-wrap: wrap; }
.tab-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 7px 16px;
  border: 1.5px solid #ddd;
  background: #fff;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  color: #555;
  transition: all 0.15s;
}
.tab-btn.active,
.tab-btn:hover { border-color: #111; background: #111; color: #fff; }
.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #e53e3e;
  color: #fff;
  border-radius: 9px;
  font-size: 11px;
  font-weight: 700;
}
.tab-btn.active .tab-count { background: #fff; color: #e53e3e; }

/* ── 테이블 ────────────────────────────── */
.table-wrap { overflow-x: auto; }
.qna-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.qna-table th {
  padding: 11px 10px;
  border-top: 1px solid #111;
  border-bottom: 1px solid #ddd;
  font-weight: 600;
  color: #333;
  background: #fafafa;
  white-space: nowrap;
  text-align: center;
}
.qna-table td {
  padding: 13px 10px;
  border-bottom: 1px solid #eee;
  vertical-align: middle;
  text-align: center;
}
.qna-row:hover { background: #f7f7f7; }

.col-id     { width: 60px; }
.col-cat    { width: 100px; }
.col-title  { text-align: left; }
.col-member { width: 100px; }
.col-date   { width: 110px; }
.col-status { width: 130px; }
.col-action { width: 90px; }

.title-cell { display: table-cell; }
.lock-icon  { margin-right: 4px; font-size: 12px; }

.category-badge {
  display: inline-block;
  padding: 3px 10px;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 12px;
  color: #555;
}

.status-select {
  padding: 5px 8px;
  border: 1.5px solid #ddd;
  border-radius: 5px;
  font-size: 12px;
  background: #fff;
  cursor: pointer;
  color: #333;
}
.status-select:focus { outline: none; border-color: #111; }

.action-btn {
  display: inline-block;
  padding: 5px 12px;
  background: #111;
  color: #fff;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.15s;
  white-space: nowrap;
}
.action-btn:hover { background: #333; }

/* ── 빈 상태 ───────────────────────────── */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #aaa;
  font-size: 14px;
}

/* ── 로딩 ──────────────────────────────── */
.loading-wrap { display: flex; justify-content: center; padding: 60px 0; }
.spinner {
  width: 32px; height: 32px;
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
  margin-top: 32px;
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
</style>