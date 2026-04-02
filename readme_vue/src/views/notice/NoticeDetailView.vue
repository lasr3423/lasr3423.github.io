<template>
  <div class="notice-detail-page">
    <!-- 로딩 -->
    <div v-if="loading" class="state-box">
      <span class="spinner" />
    </div>
 
    <!-- 에러 -->
    <div v-else-if="error" class="state-box state-error">
      <p>{{ error }}</p>
      <button class="btn-back" @click="goList">목록으로</button>
    </div>
 
    <!-- 상세 내용 -->
    <template v-else-if="notice">
      <!-- 헤더 -->
      <header class="detail-header">
        <div class="detail-header__inner">
          <div class="header-meta">
            <span v-if="notice.isFixed" class="pin-badge">📌 고정 공지</span>
            <span class="header-date">{{ formatDate(notice.createdAt) }}</span>
            <span class="header-view">조회 {{ notice.viewCount.toLocaleString() }}</span>
          </div>
          <h1 class="detail-title">{{ notice.title }}</h1>
        </div>
      </header>
 
      <!-- 본문 -->
      <section class="detail-body">
        <div class="container">
          <div class="detail-content" v-html="renderedContent" />
 
          <!-- 하단 네비게이션 -->
          <nav class="detail-nav">
            <button
              class="nav-item nav-item--prev"
              :disabled="!prev"
              @click="prev && goDetail(prev.id)"
            >
              <span class="nav-arrow">‹</span>
              <span class="nav-info">
                <em>이전 글</em>
                <span>{{ prev ? prev.title : '이전 공지가 없습니다' }}</span>
              </span>
            </button>
 
            <button class="nav-item nav-item--list" @click="goList">
              목록
            </button>
 
            <button
              class="nav-item nav-item--next"
              :disabled="!next"
              @click="next && goDetail(next.id)"
            >
              <span class="nav-info nav-info--right">
                <em>다음 글</em>
                <span>{{ next ? next.title : '다음 공지가 없습니다' }}</span>
              </span>
              <span class="nav-arrow">›</span>
            </button>
          </nav>
        </div>
      </section>
    </template>
  </div>
</template>
 
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoticeDetail } from '@/api/notice.js'
 
const route  = useRoute()
const router = useRouter()
 
const notice  = ref(null)
const prev    = ref(null)
const next    = ref(null)
const loading = ref(false)
const error   = ref(null)
 
// 줄바꿈 → <br> 렌더
const renderedContent = computed(() =>
  notice.value
    ? notice.value.content.replace(/\n/g, '<br>')
    : ''
)
 
async function fetchDetail(id) {
  loading.value = true
  error.value   = null
  notice.value  = null
  try {
    const { data } = await getNoticeDetail(id)
    notice.value = data.notice ?? data
    prev.value   = data.prev   ?? null
    next.value   = data.next   ?? null
  } catch (e) {
    error.value = '공지사항을 불러오지 못했습니다.'
    console.error(e)
  } finally {
    loading.value = false
  }
}
 
function goList()      { router.push('/notice/list') }
function goDetail(id)  { router.push(`/notice/detail/${id}`) }
function formatDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('ko-KR', {
    year: 'numeric', month: 'long', day: 'numeric',
  })
}
 
onMounted(() => fetchDetail(route.params.id))
watch(() => route.params.id, (id) => id && fetchDetail(id))
</script>
 
<style scoped>
/* ── 상세 헤더 ── */
.notice-detail-page { min-height: 100vh; background: #faf8f4; }
 
.detail-header {
  background: #1a1a2e;
  color: #fff;
  padding: 56px 24px 48px;
}
.detail-header__inner { max-width: 800px; margin: 0 auto; }
 
.header-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  font-size: 13px;
  color: rgba(255,255,255,0.55);
}
.pin-badge {
  padding: 3px 10px;
  background: #2d6a4f;
  color: #fff;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
}
.detail-title {
  font-size: clamp(20px, 3.5vw, 32px);
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.5px;
  margin: 0;
}
 
/* ── 본문 영역 ── */
.detail-body { padding: 48px 16px 80px; }
.container   { max-width: 800px; margin: 0 auto; }
 
.detail-content {
  font-size: 15px;
  line-height: 1.85;
  color: #1a1a2e;
  background: #fff;
  border: 1px solid #e8e4dc;
  border-radius: 8px;
  padding: 40px 40px;
  min-height: 200px;
  word-break: keep-all;
}
 
/* ── 하단 네비 ── */
.detail-nav {
  margin-top: 32px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 8px;
  border-top: 1px solid #e8e4dc;
  padding-top: 20px;
}
 
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 1px solid #e8e4dc;
  border-radius: 6px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
}
.nav-item:hover:not(:disabled) { border-color: #c9a84c; background: rgba(201,168,76,0.04); }
.nav-item:disabled { opacity: 0.4; cursor: not-allowed; }
 
.nav-item--prev { justify-content: flex-start; }
.nav-item--next { justify-content: flex-end; }
.nav-item--list {
  padding: 14px 24px;
  font-weight: 700;
  font-size: 13px;
  color: #1a1a2e;
  white-space: nowrap;
  justify-content: center;
}
 
.nav-arrow { font-size: 20px; color: #c9a84c; flex-shrink: 0; }
.nav-info  { display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.nav-info--right { text-align: right; }
.nav-info em  { font-style: normal; font-size: 10px; letter-spacing: 0.5px; color: rgba(26,26,46,0.4); }
.nav-info span {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
 
/* ── 상태 박스 ── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  min-height: 400px;
  font-size: 14px;
  color: rgba(26,26,46,0.5);
}
.state-error { color: #c0392b; }
.btn-back {
  padding: 10px 24px;
  border: 1px solid currentColor;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
}
.spinner {
  display: block;
  width: 36px;
  height: 36px;
  border: 3px solid #e8e4dc;
  border-top-color: #c9a84c;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
 
/* ── 반응형 ── */
@media (max-width: 600px) {
  .detail-content { padding: 24px 20px; }
  .detail-nav { grid-template-columns: 1fr 1fr; }
  .nav-item--list { grid-column: span 2; }
  .nav-info span  { max-width: 120px; }
}
</style>