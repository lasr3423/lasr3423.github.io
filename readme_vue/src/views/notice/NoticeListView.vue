<template>
  <div class="notice-list-page">
    <!-- 헤더 배너 -->
    <section class="page-banner">
      <div class="banner-inner">
        <p class="banner-eyebrow">ANNOUNCEMENT</p>
        <h1 class="banner-title">공지사항</h1>
        <p class="banner-sub">README의 새로운 소식과 안내를 확인하세요</p>
      </div>
    </section>
 
    <!-- 본문 -->
    <section class="notice-content">
      <div class="container">
        <!-- 로딩 -->
        <div v-if="loading" class="state-box">
          <span class="spinner" />
          <p>불러오는 중…</p>
        </div>
 
        <!-- 에러 -->
        <div v-else-if="error" class="state-box state-error">
          <p>{{ error }}</p>
          <button class="btn-retry" @click="fetchList">다시 시도</button>
        </div>
 
        <!-- 목록 -->
        <template v-else>
          <ul class="notice-table">
            <li class="notice-table__head">
              <span class="col-pin" />
              <span class="col-title">제목</span>
              <span class="col-date">작성일</span>
              <span class="col-view">조회</span>
            </li>
 
            <li
              v-for="item in list"
              :key="item.id"
              class="notice-table__row"
              :class="{ 'is-fixed': item.isFixed }"
              @click="goDetail(item.id)"
            >
              <span class="col-pin">
                <span v-if="item.isFixed" class="pin-badge">고정</span>
              </span>
              <span class="col-title">
                <span class="row-title">{{ item.title }}</span>
                <span v-if="isNew(item.createdAt)" class="new-badge">N</span>
              </span>
              <span class="col-date">{{ formatDate(item.createdAt) }}</span>
              <span class="col-view">{{ item.viewCount.toLocaleString() }}</span>
            </li>
          </ul>
 
          <!-- 빈 상태 -->
          <div v-if="!list.length" class="state-box">
            <p>등록된 공지사항이 없습니다.</p>
          </div>
 
          <!-- 페이지네이션 -->
          <div v-if="totalPages > 1" class="pagination">
            <button
              class="page-btn"
              :disabled="currentPage === 0"
              @click="changePage(currentPage - 1)"
            >‹</button>
 
            <button
              v-for="p in pageRange"
              :key="p"
              class="page-btn"
              :class="{ active: p === currentPage }"
              @click="changePage(p)"
            >{{ p + 1 }}</button>
 
            <button
              class="page-btn"
              :disabled="currentPage === totalPages - 1"
              @click="changePage(currentPage + 1)"
            >›</button>
          </div>
        </template>
      </div>
    </section>
  </div>
</template>
 
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNoticeList } from '@/api/notice.js'
 
const router = useRouter()
 
const list        = ref([])
const loading     = ref(false)
const error       = ref(null)
const currentPage = ref(0)
const totalPages  = ref(0)
 
const pageRange = computed(() => {
  const start = Math.max(0, currentPage.value - 2)
  const end   = Math.min(totalPages.value - 1, currentPage.value + 2)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})
 
async function fetchList() {
  loading.value = true
  error.value   = null
  try {
    const { data } = await getNoticeList(currentPage.value)
    list.value       = data.content   ?? data
    totalPages.value = data.totalPages ?? 1
  } catch (e) {
    error.value = '공지사항을 불러오지 못했습니다.'
    console.error(e)
  } finally {
    loading.value = false
  }
}
 
function changePage(p) {
  currentPage.value = p
  fetchList()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
 
function goDetail(id) {
  router.push(`/notice/detail/${id}`)
}
 
function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
  })
}
 
function isNew(dateStr) {
  if (!dateStr) return false
  return (Date.now() - new Date(dateStr).getTime()) < 7 * 24 * 60 * 60 * 1000
}
 
onMounted(fetchList)
</script>
 
<style scoped>
/* ── 변수 ── */
:root {
  --cream:   #faf8f4;
  --ink:     #1a1a2e;
  --bark:    #5c4a32;
  --gold:    #c9a84c;
  --pine:    #2d6a4f;
  --mist:    #e8e4dc;
  --radius:  6px;
}
 
/* ── 배너 ── */
.page-banner {
  background: var(--ink, #1a1a2e);
  color: #fff;
  padding: 64px 24px 52px;
  text-align: center;
  position: relative;
  overflow: hidden;
}
.page-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    -45deg,
    transparent,
    transparent 40px,
    rgba(201, 168, 76, 0.04) 40px,
    rgba(201, 168, 76, 0.04) 41px
  );
}
.banner-inner { position: relative; z-index: 1; }
.banner-eyebrow {
  font-family: 'Courier New', monospace;
  font-size: 11px;
  letter-spacing: 4px;
  color: var(--gold, #c9a84c);
  margin: 0 0 12px;
}
.banner-title {
  font-size: clamp(28px, 5vw, 48px);
  font-weight: 800;
  letter-spacing: -1px;
  margin: 0 0 12px;
}
.banner-sub {
  font-size: 14px;
  color: rgba(255,255,255,0.6);
  margin: 0;
}
 
/* ── 레이아웃 ── */
.notice-content { padding: 48px 16px 80px; background: var(--cream, #faf8f4); }
.container { max-width: 860px; margin: 0 auto; }
 
/* ── 테이블 ── */
.notice-table { list-style: none; margin: 0; padding: 0; border-top: 2px solid var(--ink, #1a1a2e); }
 
.notice-table__head,
.notice-table__row {
  display: grid;
  grid-template-columns: 56px 1fr 110px 64px;
  align-items: center;
  gap: 8px;
  padding: 0 8px;
}
 
.notice-table__head {
  padding-top: 12px;
  padding-bottom: 12px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: rgba(26,26,46,0.5);
  border-bottom: 1px solid var(--mist, #e8e4dc);
}
 
.notice-table__row {
  padding-top: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--mist, #e8e4dc);
  cursor: pointer;
  transition: background 0.15s;
}
.notice-table__row:hover { background: rgba(201, 168, 76, 0.06); }
.notice-table__row.is-fixed { background: rgba(45, 106, 79, 0.05); }
.notice-table__row.is-fixed:hover { background: rgba(45, 106, 79, 0.1); }
 
.col-pin  { display: flex; justify-content: center; }
.col-date,
.col-view { text-align: center; font-size: 13px; color: rgba(26,26,46,0.55); }
 
.col-title {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}
.row-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--ink, #1a1a2e);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
 
/* 뱃지 */
.pin-badge {
  display: inline-block;
  padding: 2px 7px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: var(--pine, #2d6a4f);
  color: #fff;
}
.new-badge {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--gold, #c9a84c);
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
 
/* ── 페이지네이션 ── */
.pagination {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  gap: 6px;
}
.page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid var(--mist, #e8e4dc);
  border-radius: var(--radius, 6px);
  background: #fff;
  color: var(--ink, #1a1a2e);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: var(--gold, #c9a84c); color: var(--gold, #c9a84c); }
.page-btn.active { background: var(--ink, #1a1a2e); border-color: var(--ink, #1a1a2e); color: #fff; font-weight: 700; }
.page-btn:disabled { opacity: 0.35; cursor: not-allowed; }
 
/* ── 상태 박스 ── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 80px 0;
  color: rgba(26,26,46,0.5);
  font-size: 14px;
}
.state-error { color: #c0392b; }
.btn-retry {
  padding: 8px 20px;
  border: 1px solid currentColor;
  border-radius: var(--radius, 6px);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
}
 
/* 스피너 */
.spinner {
  display: block;
  width: 32px;
  height: 32px;
  border: 3px solid var(--mist, #e8e4dc);
  border-top-color: var(--gold, #c9a84c);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
 
/* ── 반응형 ── */
@media (max-width: 600px) {
  .notice-table__head,
  .notice-table__row { grid-template-columns: 48px 1fr 84px 0; }
  .col-view { display: none; }
  .banner-title { font-size: 26px; }
}
</style>