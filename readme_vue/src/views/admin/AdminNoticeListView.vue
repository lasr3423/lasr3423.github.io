<template>
  <div class="admin-notice-list">
    <!-- 페이지 헤더 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">공지사항 관리</h2>
        <p class="page-sub">전체 공지사항을 조회하고 관리합니다</p>
      </div>
      <button class="btn-primary" @click="goInsert">+ 공지 등록</button>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="state-box">
      <span class="spinner" />
    </div>

    <!-- 에러 -->
    <div v-else-if="error" class="state-box state-error">
      <p>{{ error }}</p>
      <button class="btn-retry" @click="fetchList">다시 시도</button>
    </div>

    <template v-else>
      <!-- 요약 카드 -->
      <div class="summary-row">
        <div class="summary-card">
          <span class="summary-label">전체</span>
          <strong class="summary-num">{{ totalElements }}</strong>
        </div>
        <div class="summary-card summary-card--fixed">
          <span class="summary-label">📌 고정</span>
          <strong class="summary-num">{{ fixedCount }}</strong>
        </div>
      </div>

      <!-- 테이블 -->
      <div class="table-wrap">
        <table class="admin-table">
          <thead>
            <tr>
              <th class="th-no">번호</th>
              <th class="th-pin">고정</th>
              <th class="th-title">제목</th>
              <th class="th-date">작성일</th>
              <th class="th-view">조회수</th>
              <th class="th-action">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in list"
              :key="item.id"
              :class="{ 'row-fixed': item.isFixed }"
            >
              <td class="td-center text-muted">{{ item.id }}</td>
              <td class="td-center">
                <span v-if="item.isFixed" class="pin-dot" title="고정">📌</span>
              </td>
              <td class="td-title">
                <router-link :to="`/admin/notice/detail/${item.id}`" class="title-link">
                  {{ item.title }}
                </router-link>
              </td>
              <td class="td-center text-muted">{{ formatDate(item.createdAt) }}</td>
              <td class="td-center text-muted">{{ item.viewCount.toLocaleString() }}</td>
              <td class="td-center">
                <div class="action-btns">
                  <button class="btn-action btn-edit" @click="goUpdate(item.id)">수정</button>
                  <button class="btn-action btn-delete" @click="confirmDelete(item)">삭제</button>
                </div>
              </td>
            </tr>
            <tr v-if="!list.length">
              <td colspan="6" class="td-empty">등록된 공지사항이 없습니다.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">‹</button>
        <button
          v-for="p in pageRange"
          :key="p"
          class="page-btn"
          :class="{ active: p === currentPage }"
          @click="changePage(p)"
        >{{ p + 1 }}</button>
        <button class="page-btn" :disabled="currentPage === totalPages - 1" @click="changePage(currentPage + 1)">›</button>
      </div>
    </template>

    <!-- 삭제 확인 모달 -->
    <Teleport to="body">
      <div v-if="deleteTarget" class="modal-overlay" @click.self="deleteTarget = null">
        <div class="modal">
          <h3 class="modal-title">공지사항 삭제</h3>
          <p class="modal-body">
            <strong>「{{ deleteTarget.title }}」</strong>을(를) 삭제하시겠습니까?<br>
            삭제된 공지사항은 복구할 수 없습니다.
          </p>
          <div class="modal-footer">
            <button class="btn-cancel" @click="deleteTarget = null">취소</button>
            <button class="btn-confirm-delete" :disabled="deleteLoading" @click="doDelete">
              {{ deleteLoading ? '삭제 중…' : '삭제' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminNoticeList, deleteNotice } from '@/api/notice.js'

const router = useRouter()

const list          = ref([])
const loading       = ref(false)
const error         = ref(null)
const currentPage   = ref(0)
const totalPages    = ref(0)
const totalElements = ref(0)

const fixedCount = computed(() => list.value.filter(n => n.isFixed).length)

const pageRange = computed(() => {
  const start = Math.max(0, currentPage.value - 2)
  const end   = Math.min(totalPages.value - 1, currentPage.value + 2)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// 삭제 모달
const deleteTarget  = ref(null)
const deleteLoading = ref(false)

async function fetchList() {
  loading.value = true
  error.value   = null
  try {
    const { data } = await getAdminNoticeList(currentPage.value)
    list.value          = data.content       ?? data
    totalPages.value    = data.totalPages    ?? 1
    totalElements.value = data.totalElements ?? list.value.length
  } catch (e) {
    error.value = '목록을 불러오지 못했습니다.'
    console.error(e)
  } finally {
    loading.value = false
  }
}

function changePage(p) {
  currentPage.value = p
  fetchList()
}

function goInsert()    { router.push('/admin/notice/insert') }
function goUpdate(id)  { router.push(`/admin/notice/update/${id}`) }

function confirmDelete(item) { deleteTarget.value = item }

async function doDelete() {
  if (!deleteTarget.value) return
  deleteLoading.value = true
  try {
    await deleteNotice(deleteTarget.value.id)
    deleteTarget.value = null
    await fetchList()
  } catch (e) {
    alert('삭제에 실패했습니다.')
    console.error(e)
  } finally {
    deleteLoading.value = false
  }
}

function formatDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
  })
}

onMounted(fetchList)
</script>

<style scoped>
.admin-notice-list { padding: 32px; }

/* ── 헤더 ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 28px;
  flex-wrap: wrap;
  gap: 16px;
}
.page-title { font-size: 22px; font-weight: 800; color: #1a1a2e; margin: 0 0 4px; }
.page-sub   { font-size: 13px; color: rgba(26,26,46,0.5); margin: 0; }
.btn-primary {
  padding: 10px 20px;
  background: #1a1a2e;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-primary:hover { background: #2d6a4f; }

/* ── 요약 카드 ── */
.summary-row {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.summary-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: #fff;
  border: 1px solid #e8e4dc;
  border-radius: 8px;
}
.summary-card--fixed { border-color: #2d6a4f; }
.summary-label { font-size: 12px; color: rgba(26,26,46,0.5); }
.summary-num   { font-size: 20px; font-weight: 800; color: #1a1a2e; }

/* ── 테이블 ── */
.table-wrap {
  background: #fff;
  border: 1px solid #e8e4dc;
  border-radius: 8px;
  overflow: hidden;
}
.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.admin-table thead tr {
  background: #f5f3ef;
  border-bottom: 2px solid #e8e4dc;
}
.admin-table th {
  padding: 12px 14px;
  font-weight: 700;
  color: rgba(26,26,46,0.6);
  text-align: center;
  white-space: nowrap;
}
.th-title { text-align: left; }
.admin-table td {
  padding: 14px;
  border-bottom: 1px solid #f0ede7;
  vertical-align: middle;
}
.admin-table tr:last-child td { border-bottom: none; }
.admin-table tr.row-fixed { background: rgba(45,106,79,0.04); }

.td-center { text-align: center; }
.td-title  { text-align: left; }
.text-muted { color: rgba(26,26,46,0.5); }

.title-link {
  color: #1a1a2e;
  text-decoration: none;
  font-weight: 500;
}
.title-link:hover { color: #c9a84c; text-decoration: underline; }

.td-empty {
  text-align: center;
  padding: 48px;
  color: rgba(26,26,46,0.4);
}

/* ── 액션 버튼 ── */
.action-btns { display: flex; gap: 6px; justify-content: center; }
.btn-action {
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.15s;
}
.btn-edit   { background: #e8f4f0; color: #2d6a4f; }
.btn-edit:hover { background: #2d6a4f; color: #fff; }
.btn-delete { background: #fdecea; color: #c0392b; }
.btn-delete:hover { background: #c0392b; color: #fff; }

/* ── 페이지네이션 ── */
.pagination {
  margin-top: 28px;
  display: flex;
  justify-content: center;
  gap: 6px;
}
.page-btn {
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  border: 1px solid #e8e4dc;
  border-radius: 5px;
  background: #fff;
  color: #1a1a2e;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: #c9a84c; }
.page-btn.active { background: #1a1a2e; border-color: #1a1a2e; color: #fff; font-weight: 700; }
.page-btn:disabled { opacity: 0.35; cursor: not-allowed; }

/* ── 상태 박스 ── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px 0;
  color: rgba(26,26,46,0.45);
  font-size: 14px;
}
.state-error { color: #c0392b; }
.btn-retry {
  padding: 8px 18px;
  border: 1px solid currentColor;
  border-radius: 5px;
  background: transparent;
  cursor: pointer;
}
.spinner {
  display: block;
  width: 32px;
  height: 32px;
  border: 3px solid #e8e4dc;
  border-top-color: #c9a84c;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 삭제 모달 ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}
.modal {
  background: #fff;
  border-radius: 10px;
  padding: 32px 28px 24px;
  width: min(420px, 90vw);
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
}
.modal-title { font-size: 17px; font-weight: 800; margin: 0 0 16px; color: #1a1a2e; }
.modal-body  { font-size: 14px; line-height: 1.7; color: rgba(26,26,46,0.75); margin: 0 0 24px; }
.modal-footer { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel {
  padding: 9px 20px;
  border: 1px solid #e8e4dc;
  border-radius: 6px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
}
.btn-confirm-delete {
  padding: 9px 20px;
  border: none;
  border-radius: 6px;
  background: #c0392b;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.btn-confirm-delete:disabled { opacity: 0.6; cursor: not-allowed; }
</style>