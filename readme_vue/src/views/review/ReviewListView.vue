<template>
  <div class="review-list-container">
    <section v-if="summary" class="review-summary">
      <div class="rating-avg-box">
        <span class="avg-score">{{ summary.averageRating.toFixed(1) }}</span>
        <div class="stars">
          <span
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ filled: n <= Math.round(summary.averageRating) }"
          >★</span>
        </div>
        <p class="total-count">전체 리뷰 {{ summary.totalCount }}건</p>
      </div>
    </section>

    <div class="list-header">
      <h3 class="title">상품 리뷰</h3>
      <div class="sort-options">
        <button :class="{ active: sortBy === 'latest' }" @click="changeSort('latest')">최신순</button>
        <button :class="{ active: sortBy === 'rating' }" @click="changeSort('rating')">평점순</button>
      </div>
    </div>

    <div v-if="reviews.length > 0" class="review-list">
      <article v-for="review in reviews" :key="review.id" class="review-item">
        <div class="item-header">
          <div class="reviewer-info">
            <span class="reviewer-name">{{ maskName(review.memberName) }}</span>
            <div class="item-stars">
              <span v-for="n in 5" :key="n" :class="{ filled: n <= review.rating }">★</span>
            </div>
          </div>
          <span class="review-date">{{ formatDate(review.createdAt) }}</span>
        </div>

        <div class="item-body" @click="goDetail(review.id)">
          <p class="review-content">{{ review.content }}</p>
          
          <div v-if="review.imageUrls?.length" class="review-images">
            <img 
              v-for="(url, i) in review.imageUrls.slice(0, 3)" 
              :key="i" 
              :src="url" 
              class="thumb-img" 
            />
            <span v-if="review.imageUrls.length > 3" class="more-count">+{{ review.imageUrls.length - 3 }}</span>
          </div>
        </div>

        <div class="item-footer">
          <span class="hits">조회 {{ review.hits }}</span>
          <span class="likes">👍 도움돼요 {{ review.likeCount }}</span>
        </div>
      </article>
    </div>

    <div v-else class="empty-list">
      등록된 리뷰가 없습니다. 첫 리뷰를 작성해 보세요!
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()

// Props로 productId를 받거나 쿼리에서 추출 (상품 상세페이지 포함용)
const productId = route.params.productId || route.query.productId
const reviews = ref([])
const summary = ref(null)
const sortBy = ref('latest')

// ── REQ-R-001: 리뷰 목록 및 요약 데이터 로드 ──
const fetchReviews = async () => {
  if (!productId) return
  try {
    // 상품 상세 조회 API(FP-002) 흐름에 포함된 리뷰 목록 호출 [cite: 252, 253]
    const { data } = await axios.get(`/api/review/product/${productId}`, {
      params: { sort: sortBy.value }
    })
    reviews.value = data.reviews
    summary.value = {
      averageRating: data.averageRating,
      totalCount: data.totalCount
    }
  } catch (e) {
    console.error('리뷰 목록 로드 실패', e)
  }
}

const changeSort = (type) => {
  sortBy.value = type
  fetchReviews()
}

const goDetail = (id) => {
  router.push(`/review/detail/${id}`) // REQ-R-005 연결
}

const maskName = (name) => {
  if (!name) return '비회원'
  return name[0] + '*'.repeat(name.length - 1)
}

const formatDate = (date) => {
  return date ? date.split('T')[0] : ''
}

onMounted(fetchReviews)
</script>

<style scoped>
.review-list-container { padding: 20px 0; border-top: 1px solid #eee; }

/* 요약 박스 */
.review-summary {
  background: #fcfcfc;
  padding: 30px;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 30px;
}
.avg-score { font-size: 2.5rem; font-weight: 800; color: #1a1a2e; display: block; }
.stars { color: #ddd; font-size: 1.2rem; margin: 8px 0; }
.star.filled { color: #f5a623; }
.total-count { color: #888; font-size: 0.9rem; }

/* 헤더 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #333;
}
.title { font-size: 1.1rem; font-weight: 700; }
.sort-options button {
  background: none; border: none; color: #aaa; cursor: pointer; font-size: 0.85rem;
}
.sort-options button.active { color: #1a1a2e; font-weight: 700; }

/* 개별 아이템 */
.review-item { padding: 20px 0; border-bottom: 1px solid #eee; }
.item-header { display: flex; justify-content: space-between; margin-bottom: 10px; }
.reviewer-info { display: flex; align-items: center; gap: 10px; }
.reviewer-name { font-weight: 600; font-size: 0.9rem; }
.item-stars { color: #ddd; font-size: 0.8rem; }
.item-stars span.filled { color: #f5a623; }
.review-date { color: #bbb; font-size: 0.8rem; }

.item-body { cursor: pointer; }
.review-content { 
  font-size: 0.95rem; line-height: 1.6; color: #444;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
}

.review-images { display: flex; gap: 5px; margin-top: 10px; align-items: center; }
.thumb-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.more-count { font-size: 0.8rem; color: #aaa; background: #eee; padding: 4px 8px; border-radius: 4px; }

.item-footer { margin-top: 12px; font-size: 0.8rem; color: #aaa; display: flex; gap: 15px; }
.empty-list { text-align: center; padding: 50px 0; color: #bbb; }
</style>