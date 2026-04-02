<template>
  <section class="review-section">
    <!-- 평점 요약 -->
    <div class="review-summary">
      <div class="rating-avg">
        <span class="avg-score">{{ avgRating }}</span>
        <div class="stars">
          <span
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ filled: n <= Math.round(avgRating) }"
          >★</span>
        </div>
        <span class="review-count">리뷰 {{ totalCount }}개</span>
      </div>
    </div>

    <!-- 리뷰 작성 버튼 (구매자이고 미작성인 경우) -->
    <div v-if="canWrite" class="write-btn-wrap">
      <button class="btn-write" @click="goWrite">리뷰 작성하기</button>
    </div>

    <!-- 리뷰 목록 -->
    <ul v-if="reviews.length > 0" class="review-list">
      <li
        v-for="review in reviews"
        :key="review.id"
        class="review-item"
        @click="goDetail(review.id)"
      >
        <div class="review-header">
          <div class="reviewer-info">
            <span class="reviewer-name">{{ maskName(review.memberName) }}</span>
            <div class="stars small">
              <span
                v-for="n in 5"
                :key="n"
                class="star"
                :class="{ filled: n <= review.rating }"
              >★</span>
            </div>
          </div>
          <span class="review-date">{{ formatDate(review.createdAt) }}</span>
        </div>

        <p class="review-content">{{ review.content }}</p>

        <!-- 이미지 섬네일 -->
        <div v-if="review.imageUrls?.length" class="review-images">
          <img
            v-for="(url, i) in review.imageUrls.slice(0, 3)"
            :key="i"
            :src="url"
            alt="리뷰 이미지"
            class="review-thumb"
          />
          <span v-if="review.imageUrls.length > 3" class="more-images">
            +{{ review.imageUrls.length - 3 }}
          </span>
        </div>

        <!-- 좋아요/싫어요 (FR-004) -->
        <div class="reaction-wrap" @click.stop>
          <button
            class="btn-reaction"
            :class="{ active: review.myReaction === 'LIKE' }"
            @click="handleReaction(review, 'LIKE')"
          >
            👍 {{ review.likeCount }}
          </button>
          <button
            class="btn-reaction"
            :class="{ active: review.myReaction === 'DISLIKE' }"
            @click="handleReaction(review, 'DISLIKE')"
          >
            👎 {{ review.dislikeCount }}
          </button>
        </div>
      </li>
    </ul>

    <p v-else class="no-review">아직 등록된 리뷰가 없습니다.</p>

    <!-- 페이징 -->
    <div v-if="totalPages > 1" class="pagination">
      <button
        v-for="p in totalPages"
        :key="p"
        class="page-btn"
        :class="{ active: p - 1 === currentPage }"
        @click="changePage(p - 1)"
      >{{ p }}</button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { getReviewList, reactReview } from '@/api/review.js'

const props = defineProps({
  productId: { type: [Number, String], required: true },
  canWrite:  { type: Boolean, default: false }   // 부모(ProductDetail)에서 구매확정 여부 전달
})

const router    = useRouter()
const authStore = useAuthStore()

const reviews     = ref([])
const avgRating   = ref(0)
const totalCount  = ref(0)
const currentPage = ref(0)
const totalPages  = ref(0)

// ── 데이터 조회 ──────────────────────────────
const fetchReviews = async (page = 0) => {
  try {
    const { data } = await getReviewList(props.productId, page)
    reviews.value     = data.reviews
    avgRating.value   = data.avgRating?.toFixed(1) ?? '0.0'
    totalCount.value  = data.totalCount
    currentPage.value = data.currentPage
    totalPages.value  = data.totalPages
  } catch (e) {
    console.error('리뷰 목록 조회 실패', e)
  }
}

onMounted(() => fetchReviews())
watch(() => props.productId, () => fetchReviews())

// ── 페이지 변경 ──────────────────────────────
const changePage = (page) => fetchReviews(page)

// ── 라우팅 ──────────────────────────────────
const goWrite  = () => router.push({ path: '/review/write', query: { productId: props.productId } })
const goDetail = (reviewId) => router.push(`/review/detail/${reviewId}`)

// ── FR-004  리뷰 반응 ─────────────────────────
const handleReaction = async (review, type) => {
  if (!authStore.isLoggedIn) {
    alert('로그인이 필요합니다.')
    return router.push('/signin')
  }
  try {
    await reactReview(review.id, type)
    // 낙관적 업데이트
    if (review.myReaction === type) {
      // 동일 반응 → 취소
      type === 'LIKE' ? review.likeCount-- : review.dislikeCount--
      review.myReaction = null
    } else {
      if (review.myReaction === 'LIKE')    review.likeCount--
      if (review.myReaction === 'DISLIKE') review.dislikeCount--
      type === 'LIKE' ? review.likeCount++ : review.dislikeCount++
      review.myReaction = type
    }
  } catch (e) {
    console.error('반응 처리 실패', e)
  }
}

// ── 유틸 ────────────────────────────────────
const maskName   = (name) => name ? name[0] + '*'.repeat(name.length - 1) : ''
const formatDate = (dt)   => dt ? dt.slice(0, 10) : ''
</script>

<style scoped>
.review-section { padding: 24px 0; }

/* 평점 요약 */
.review-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}
.rating-avg { display: flex; align-items: center; gap: 12px; }
.avg-score  { font-size: 2.4rem; font-weight: 700; color: #222; }
.review-count { color: #888; font-size: 0.9rem; }

/* 별점 */
.stars       { display: flex; gap: 2px; }
.star        { color: #ddd; font-size: 1.1rem; }
.star.filled { color: #f5a623; }
.stars.small .star { font-size: 0.9rem; }

/* 작성 버튼 */
.write-btn-wrap { text-align: right; margin-bottom: 16px; }
.btn-write {
  padding: 8px 20px;
  background: #1a1a2e;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-write:hover { background: #16213e; }

/* 리뷰 목록 */
.review-list { list-style: none; padding: 0; margin: 0; }
.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.15s;
}
.review-item:hover { background: #fafafa; }

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.reviewer-info { display: flex; align-items: center; gap: 10px; }
.reviewer-name { font-weight: 600; font-size: 0.95rem; }
.review-date   { color: #aaa; font-size: 0.8rem; }

.review-content {
  color: #333;
  line-height: 1.6;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 이미지 */
.review-images { display: flex; gap: 8px; margin-bottom: 12px; align-items: center; }
.review-thumb  {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
.more-images { color: #888; font-size: 0.85rem; }

/* 반응 */
.reaction-wrap { display: flex; gap: 10px; }
.btn-reaction {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: #fff;
  color: #555;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.15s;
}
.btn-reaction.active { border-color: #1a1a2e; color: #1a1a2e; font-weight: 600; }
.btn-reaction:hover  { background: #f5f5f5; }

/* 페이징 */
.pagination { display: flex; justify-content: center; gap: 6px; margin-top: 24px; }
.page-btn {
  width: 32px; height: 32px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 0.9rem;
}
.page-btn.active { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }

.no-review { text-align: center; color: #aaa; padding: 40px 0; }
</style>