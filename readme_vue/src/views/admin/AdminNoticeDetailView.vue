<template>
  <div class="admin-notice-detail">
    <!-- 로딩 -->
    <div v-if="loading" class="state-box"><span class="spinner" /></div>

    <!-- 에러 -->
    <div v-else-if="error" class="state-box state-error">
      <p>{{ error }}</p>
      <button class="btn-back" @click="goList">목록으로</button>
    </div>

    <template v-else-if="notice">
      <!-- 페이지 헤더 -->
      <div class="page-header">
        <button class="btn-back-arrow" @click="goList">← 목록</button>
        <div class="header-actions">
          <button class="btn-edit" @click="goUpdate">수정</button>
          <button class="btn-delete" @click="showDeleteModal = true">삭제</button>
        </div>
      </div>

      <!-- 공지 카드 -->
      <div class="notice-card">
        <!-- 메타 정보 -->
        <div class="notice-meta">
          <span v-if="notice.isFixed" class="pin-badge">📌 고정 공지</span>
          <span class="meta-id">No.{{ notice.id }}</span>
          <span class="meta-date">{{ formatDate(notice.createdAt) }}</span>
          <span v-if="notice.updatedAt" class="meta-date">(수정: {{ formatDate(notice.updatedAt) }})</span>
          <span class="meta-view">조회 {{ notice.viewCount.toLocaleString() }}</span>
        </div>

        <!-- 제목 -->
        <h1 class="notice-title">{{ notice.title }}</h1>

        <hr class="divider" />

        <!-- 작성자 -->
        <div class="author-row">
          <span class="author-label">작성자</span>
          <span class="author-name">{{ notice.memberName ?? '관리자' }}</span>
        </div>

        <!-- 본문 -->
        <div class="notice-content" v-html="renderedContent" />
      </div>
    </template>

    <!-- 삭제 확인 모달 -->
    <Teleport to="body">
      <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
        <div class="modal">
          <h3 class="modal-title">공지사항 삭제</h3>
          <p class="modal-body">
            이 공지사항을 삭제하시겠습니까?<br>삭제 후에는 복구할 수 없습니다.
          </p>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showDeleteModal = false">취소</button>
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
import { useRoute, useRouter } from 'vue-router'
import { getAdminNoticeDetail, deleteNotice } from '@/api/notice.js'

const route  = useRoute()
const router = useRouter()

const notice          = ref(null)
const loading         = ref(false)
const error           = ref(null)
const showDeleteModal = ref(false)
const deleteLoading   = ref(false)

const renderedContent = computed(() =>
  notice.value ? notice.value.content.replace(/\n/g, '<br>') : ''
)

async function fetchDetail() {
  loading.value = true
  error.value   = null
  try {
    const { data } = await getAdminNoticeDetail(route.params.id)
    notice.value = data.notice ?? data
  } catch (e) {
    error.value = '공지사항을 불러오지 못했습니다.'
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function doDelete() {
  deleteLoading.value = true
  try {
    await deleteNotice(route.params.id)
    router.push('/admin/notice/list')
  } catch (e) {
    alert('삭제에 실패했습니다.')
    console.error(e)
  } finally {
    deleteLoading.value = false
  }
}

function goList()   { router.push('/admin/notice/list') }
function goUpdate() { router.push(`/admin/notice/update/${route.params.id}`) }
function formatDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
  })
}

onMounted(fetchDetail)
</script>

<style scoped>
.admin-notice-detail { padding: 32px; max-width: 900px; }

/* ── 헤더 ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.btn-back-arrow {
  background: none;
  border: none;
  font-size: 14px;
  color: rgba(26,26,46,0.6);
  cursor: pointer;
  padding: 6px 0;
}
.btn-back-arrow:hover { color: #1a1a2e; }

.header-actions { display: flex; gap: 8px; }
.btn-edit {
  padding: 9px 20px;
  border: 1px solid #2d6a4f;
  border-radius: 6px;
  background: #fff;
  color: #2d6a4f;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-edit:hover { background: #2d6a4f; color: #fff; }
.btn-delete {
  padding: 9px 20px;
  border: 1px solid #c0392b;
  border-radius: 6px;
  background: #fff;
  color: #c0392b;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-delete:hover { background: #c0392b; color: #fff; }

/* ── 공지 카드 ── */
.notice-card {
  background: #fff;
  border: 1px solid #e8e4dc;
  border-radius: 10px;
  padding: 36px 40px;
}

.notice-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 12px;
  color: rgba(26,26,46,0.5);
}
.pin-badge {
  padding: 3px 10px;
  background: #2d6a4f;
  color: #fff;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
}
.meta-id { font-weight: 700; color: #c9a84c; }

.notice-title {
  font-size: clamp(18px, 2.5vw, 26px);
  font-weight: 800;
  color: #1a1a2e;
  margin: 0 0 24px;
  line-height: 1.4;
  letter-spacing: -0.3px;
}

.divider {
  border: none;
  border-top: 1px solid #e8e4dc;
  margin: 0 0 20px;
}

.author-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 28px;
  font-size: 13px;
}
.author-label { color: rgba(26,26,46,0.45); }
.author-name  { font-weight: 600; color: #1a1a2e; }

.notice-content {
  font-size: 14px;
  line-height: 1.9;
  color: #2c2c3e;
  word-break: keep-all;
  min-height: 120px;
}

/* ── 상태 ── */
.state-box {
  display: flex; flex-direction: column; align-items: center;
  gap: 16px; padding: 80px 0;
  color: rgba(26,26,46,0.45); font-size: 14px;
}
.state-error { color: #c0392b; }
.btn-back {
  padding: 9px 20px; border: 1px solid currentColor;
  border-radius: 6px; background: transparent; cursor: pointer; font-size: 13px;
}
.spinner {
  display: block; width: 32px; height: 32px;
  border: 3px solid #e8e4dc; border-top-color: #c9a84c;
  border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 모달 ── */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 9999; backdrop-filter: blur(2px);
}
.modal {
  background: #fff; border-radius: 10px;
  padding: 32px 28px 24px; width: min(420px, 90vw);
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
}
.modal-title { font-size: 17px; font-weight: 800; margin: 0 0 16px; color: #1a1a2e; }
.modal-body  { font-size: 14px; line-height: 1.7; color: rgba(26,26,46,0.75); margin: 0 0 24px; }
.modal-footer { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel {
  padding: 9px 20px; border: 1px solid #e8e4dc;
  border-radius: 6px; background: #fff; font-size: 13px; cursor: pointer;
}
.btn-confirm-delete {
  padding: 9px 20px; border: none; border-radius: 6px;
  background: #c0392b; color: #fff; font-size: 13px; font-weight: 700; cursor: pointer;
}
.btn-confirm-delete:disabled { opacity: 0.6; cursor: not-allowed; }

@media (max-width: 600px) {
  .notice-card { padding: 24px 18px; }
  .admin-notice-detail { padding: 20px 16px; }
}
</style>