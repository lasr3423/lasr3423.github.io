<template>
  <div class="admin-page">
    <h2 class="page-title">리뷰 관리</h2>

    <!-- ── 목록 뷰 ── -->
    <template v-if="!selectedReview">
      <div class="toolbar">
        <span class="total-info">전체 {{ totalCount }}건</span>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>상품명</th>
            <th>작성자</th>
            <th>평점</th>
            <th>작성일</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="review in reviews" :key="review.id">
            <td>{{ review.id }}</td>
            <td class="product-name" @click="openDetail(review.id)">
              {{ review.productTitle }}
            </td>
            <td>{{ maskName(review.memberName) }}</td>
            <td>
              <span class="stars-small">
                <span v-for="n in 5" :key="n" :class="{ filled: n <= review.rating }">★</span>
              </span>
            </td>
            <td>{{ formatDate(review.createdAt) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-detail" @click="openDetail(review.id)">상세</button>
                <button class="btn-delete" @click="confirmDelete(review)">삭제</button>
              </div>
            </td>
          </tr>
          <tr v-if="reviews.length === 0">
            <td colspan="6" class="empty">등록된 리뷰가 없습니다.</td>
          </tr>
        </tbody>
      </table>

      <!-- 페이징 -->
      <div v-if="totalPages > 1" class="pagination">
        <button
          v-for="p in totalPages"
          :key="p"
          class="page-btn"
          :class="{ active: p - 1 === currentPage }"
          @click="fetchReviews(p - 1)"
        >{{ p }}</button>
      </div>
    </template>

    <!-- ── 상세 뷰 (FA-030) ── -->
    <template v-else>
      <button class="btn-back" @click="selectedReview = null">← 목록으로</button>

      <div class="detail-card" v-if="!isDetailLoading">
        <div class="detail-header">
          <div>
            <p class="detail-product">{{ selectedReview.productTitle }}</p>
            <p class="detail-author-label">작성자: {{ maskName(selectedReview.memberName) }}</p>
          </div>
          <div class="detail-meta">
            <div class="stars">
              <span v-for="n in 5" :key="n" :class="['star', { filled: n <= selectedReview.rating }]">★</span>
            </div>
            <span class="detail-date">{{ formatDate(selectedReview.createdAt) }}</span>
          </div>
        </div>

        <p class="detail-content">{{ selectedReview.content }}</p>

        <!-- 리뷰 이미지 (FA-030 부적절 이미지 확인용) -->
        <div v-if="selectedReview.imageUrls?.length" class="image-gallery">
          <img
            v-for="(url, i) in selectedReview.imageUrls"
            :key="i"
            :src="url"
            alt="리뷰 이미지"
            class="gallery-img"
            @click="lightboxUrl = url"
          />
        </div>

        <div class="detail-stats">
          <span>조회수 {{ selectedReview.hits }}</span>
          <span>👍 {{ selectedReview.likeCount }}</span>
          <span>👎 {{ selectedReview.dislikeCount }}</span>
        </div>

        <div class="detail-actions">
          <button class="btn-delete-lg" @click="confirmDelete(selectedReview)">이 리뷰 삭제</button>
        </div>
      </div>
      <div v-else class="loading">불러오는 중...</div>
    </template>

    <!-- 라이트박스 -->
    <div v-if="lightboxUrl" class="lightbox" @click="lightboxUrl = null">
      <img :src="lightboxUrl" alt="확대" class="lightbox-img" />
    </div>

    <!-- 삭제 확인 모달 (FA-031) -->
    <div v-if="deleteTarget" class="modal-overlay" @click.self="deleteTarget = null">
      <div class="modal">
        <p class="modal-msg">
          <strong>리뷰 #{{ deleteTarget.id }}</strong>을 삭제하시겠습니까?<br />
          <small>삭제된 리뷰는 복구할 수 없습니다.</small>
        </p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="deleteTarget = null">취소</button>
          <button class="btn-confirm-delete" :disabled="isDeleting" @click="deleteReview">
            {{ isDeleting ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios.js'

const reviews        = ref([])
const totalCount     = ref(0)
const currentPage    = ref(0)
const totalPages     = ref(0)
const selectedReview = ref(null)
const isDetailLoading = ref(false)
const deleteTarget   = ref(null)
const isDeleting     = ref(false)
const lightboxUrl    = ref(null)

// ── FA-029  리뷰 목록 조회 ────────────────────
const fetchReviews = async (page = 0) => {
  try {
    const { data } = await api.get('/admin/review/list', { params: { page } })
    reviews.value     = data.reviews
    totalCount.value  = data.totalCount
    currentPage.value = data.currentPage
    totalPages.value  = data.totalPages
  } catch (e) {
    console.error('리뷰 목록 조회 실패', e)
  }
}

onMounted(() => fetchReviews())

// ── FA-030  리뷰 상세 조회 ────────────────────
const openDetail = async (reviewId) => {
  isDetailLoading.value = true
  selectedReview.value  = {}   // 상세 뷰로 전환
  try {
    const { data } = await api.get(`/admin/review/detail/${reviewId}`)
    selectedReview.value = data
  } catch (e) {
    console.error('리뷰 상세 조회 실패', e)
    selectedReview.value = null
  } finally {
    isDetailLoading.value = false
  }
}

// ── FA-031  리뷰 삭제 (soft delete) ──────────
const confirmDelete = (review) => (deleteTarget.value = review)

const deleteReview = async () => {
  isDeleting.value = true
  try {
    await api.delete(`/admin/review/${deleteTarget.value.id}`)
    alert('리뷰가 삭제되었습니다.')
    deleteTarget.value   = null
    selectedReview.value = null
    fetchReviews(currentPage.value)
  } catch (e) {
    alert('삭제에 실패했습니다.')
    console.error(e)
  } finally {
    isDeleting.value = false
  }
}

// ── 유틸 ────────────────────────────────────
const maskName   = (name) => name ? name[0] + '*'.repeat(name.length - 1) : ''
const formatDate = (dt)   => dt ? dt.slice(0, 10) : ''
</script>

<style scoped>
.admin-page { padding: 24px; }
.page-title { font-size: 1.4rem; font-weight: 700; margin-bottom: 20px; color: #1a1a2e; }

/* 툴바 */
.toolbar     { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.total-info  { color: #888; font-size: 0.88rem; }

/* 테이블 */
.data-table  { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.data-table th,
.data-table td { padding: 12px 10px; border-bottom: 1px solid #eee; text-align: left; }
.data-table th  { background: #f8f8f8; font-weight: 600; color: #444; }
.product-name   { color: #1a1a2e; cursor: pointer; font-weight: 500; }
.product-name:hover { text-decoration: underline; }
.empty          { text-align: center; color: #aaa; padding: 32px; }

/* 별점 */
.stars-small span       { color: #ddd; font-size: 0.85rem; }
.stars-small span.filled { color: #f5a623; }

/* 액션 버튼 */
.action-btns { display: flex; gap: 6px; }
.btn-detail  { padding: 4px 12px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; font-size: 0.82rem; }
.btn-delete  { padding: 4px 12px; border: 1px solid #e53e3e; background: #fff; color: #e53e3e; border-radius: 4px; cursor: pointer; font-size: 0.82rem; }
.btn-detail:hover { background: #f5f5f5; }
.btn-delete:hover { background: #fff5f5; }

/* 페이징 */
.pagination  { display: flex; justify-content: center; gap: 6px; margin-top: 20px; }
.page-btn    { width: 32px; height: 32px; border: 1px solid #ddd; border-radius: 4px; background: #fff; cursor: pointer; font-size: 0.88rem; }
.page-btn.active { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }

/* 뒤로가기 */
.btn-back { background: none; border: none; color: #555; cursor: pointer; font-size: 0.9rem; margin-bottom: 20px; padding: 0; }
.btn-back:hover { color: #1a1a2e; }

/* 상세 카드 */
.detail-card { background: #fff; border: 1px solid #eee; border-radius: 10px; padding: 28px; }
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.detail-product     { font-weight: 700; font-size: 1.05rem; color: #222; margin-bottom: 4px; }
.detail-author-label { color: #888; font-size: 0.85rem; }
.detail-meta        { text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.stars              { display: flex; gap: 2px; }
.star               { color: #ddd; font-size: 1rem; }
.star.filled        { color: #f5a623; }
.detail-date        { color: #aaa; font-size: 0.82rem; }

.detail-content { color: #333; line-height: 1.8; white-space: pre-wrap; margin-bottom: 20px; }

.image-gallery { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px; }
.gallery-img   { width: 110px; height: 110px; object-fit: cover; border-radius: 6px; border: 1px solid #eee; cursor: pointer; }
.gallery-img:hover { opacity: 0.85; }

.detail-stats { display: flex; gap: 16px; color: #888; font-size: 0.85rem; margin-bottom: 20px; }
.detail-actions { text-align: right; }
.btn-delete-lg {
  padding: 10px 24px;
  background: #e53e3e;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-delete-lg:hover { background: #c53030; }
.loading { text-align: center; padding: 48px; color: #aaa; }

/* 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: #fff;
  border-radius: 10px;
  padding: 28px 32px;
  min-width: 320px;
  text-align: center;
}
.modal-msg     { margin-bottom: 20px; line-height: 1.8; color: #333; }
.modal-actions { display: flex; justify-content: center; gap: 12px; }
.btn-cancel    { padding: 8px 20px; border: 1px solid #ccc; border-radius: 4px; background: #fff; cursor: pointer; }
.btn-confirm-delete { padding: 8px 20px; background: #e53e3e; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
.btn-confirm-delete:disabled { background: #aaa; cursor: not-allowed; }

/* 라이트박스 */
.lightbox     { position: fixed; inset: 0; background: rgba(0,0,0,0.82); display: flex; align-items: center; justify-content: center; z-index: 9999; cursor: pointer; }
.lightbox-img { max-width: 90vw; max-height: 90vh; object-fit: contain; border-radius: 6px; }
</style>