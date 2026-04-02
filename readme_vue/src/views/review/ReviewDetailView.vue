<template>
  <div class="page-wrap">
    <!-- 로딩 -->
    <div v-if="isLoading" class="loading">불러오는 중...</div>

    <template v-else-if="review">
      <!-- 뒤로가기 -->
      <button class="btn-back" @click="$router.back()">← 목록으로</button>

      <!-- 상품 링크 -->
      <div class="product-link" @click="goProduct">
        <img :src="review.productThumbnail" alt="" class="product-thumb" />
        <span class="product-title">{{ review.productTitle }}</span>
        <span class="link-arrow">›</span>
      </div>

      <!-- 리뷰 본문 -->
      <article class="review-card">
        <!-- 헤더: 작성자 / 평점 / 날짜 -->
        <div class="review-header">
          <div class="reviewer-info">
            <span class="reviewer-name">{{ maskName(review.memberName) }}</span>
            <div class="stars">
              <span
                v-for="n in 5"
                :key="n"
                class="star"
                :class="{ filled: n <= review.rating }"
              >★</span>
            </div>
            <span class="rating-num">{{ review.rating }}.0</span>
          </div>
          <span class="review-date">{{ formatDate(review.createdAt) }}</span>
        </div>

        <!-- 본문 -->
        <p class="review-content">{{ review.content }}</p>

        <!-- 이미지 (FR-003 업로드된 이미지) -->
        <div v-if="review.imageUrls?.length" class="image-gallery">
          <img
            v-for="(url, i) in review.imageUrls"
            :key="i"
            :src="url"
            alt="리뷰 이미지"
            class="gallery-img"
            @click="openImage(url)"
          />
        </div>

        <!-- 조회수 -->
        <p class="view-count">조회수 {{ review.hits }}</p>

        <!-- 좋아요 / 싫어요 (FR-004) -->
        <div class="reaction-area">
          <button
            class="btn-reaction"
            :class="{ active: review.myReaction === 'LIKE' }"
            @click="handleReaction('LIKE')"
          >
            👍 도움됐어요 &nbsp;<strong>{{ review.likeCount }}</strong>
          </button>
          <button
            class="btn-reaction dislike"
            :class="{ active: review.myReaction === 'DISLIKE' }"
            @click="handleReaction('DISLIKE')"
          >
            👎 별로예요 &nbsp;<strong>{{ review.dislikeCount }}</strong>
          </button>
        </div>
      </article>
    </template>

    <div v-else class="no-data">리뷰를 찾을 수 없습니다.</div>

    <!-- 이미지 라이트박스 -->
    <div v-if="lightboxUrl" class="lightbox" @click="lightboxUrl = null">
      <img :src="lightboxUrl" alt="이미지 확대" class="lightbox-img" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { getReviewDetail, reactReview } from '@/api/review.js'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

const review      = ref(null)
const isLoading   = ref(true)
const lightboxUrl = ref(null)

// ── FR-005  리뷰 상세 조회 (view_count +1) ───
onMounted(async () => {
  try {
    const { data } = await getReviewDetail(route.params.id)
    review.value = data
  } catch (e) {
    console.error('리뷰 상세 조회 실패', e)
  } finally {
    isLoading.value = false
  }
})

// ── FR-004  리뷰 반응 ─────────────────────────
const handleReaction = async (type) => {
  if (!authStore.isLoggedIn) {
    alert('로그인이 필요합니다.')
    return router.push('/signin')
  }
  try {
    await reactReview(review.value.id, type)
    const r = review.value
    if (r.myReaction === type) {
      type === 'LIKE' ? r.likeCount-- : r.dislikeCount--
      r.myReaction = null
    } else {
      if (r.myReaction === 'LIKE')    r.likeCount--
      if (r.myReaction === 'DISLIKE') r.dislikeCount--
      type === 'LIKE' ? r.likeCount++ : r.dislikeCount++
      r.myReaction = type
    }
  } catch (e) {
    console.error('반응 처리 실패', e)
  }
}

// ── 상품 상세 이동 ───────────────────────────
const goProduct = () => {
  if (review.value?.productId) {
    router.push(`/product/category/detail/${review.value.productId}`)
  }
}

// ── 유틸 ────────────────────────────────────
const openImage  = (url) => (lightboxUrl.value = url)
const maskName   = (name) => name ? name[0] + '*'.repeat(name.length - 1) : ''
const formatDate = (dt)   => dt ? dt.slice(0, 10) : ''
</script>

<style scoped>
.page-wrap { max-width: 720px; margin: 0 auto; padding: 32px 20px; }
.loading   { text-align: center; padding: 60px 0; color: #aaa; }
.no-data   { text-align: center; padding: 60px 0; color: #aaa; }

/* 뒤로가기 */
.btn-back {
  background: none;
  border: none;
  color: #555;
  cursor: pointer;
  font-size: 0.9rem;
  padding: 0;
  margin-bottom: 20px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.btn-back:hover { color: #1a1a2e; }

/* 상품 링크 */
.product-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 24px;
  transition: background 0.15s;
}
.product-link:hover { background: #f0f0f0; }
.product-thumb  { width: 48px; height: 60px; object-fit: cover; border-radius: 4px; }
.product-title  { flex: 1; font-size: 0.95rem; font-weight: 600; color: #333; }
.link-arrow     { color: #aaa; font-size: 1.2rem; }

/* 리뷰 카드 */
.review-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 24px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.reviewer-info { display: flex; align-items: center; gap: 10px; }
.reviewer-name { font-weight: 700; font-size: 1rem; }
.stars         { display: flex; gap: 2px; }
.star          { color: #ddd; font-size: 1rem; }
.star.filled   { color: #f5a623; }
.rating-num    { color: #f5a623; font-weight: 700; font-size: 0.95rem; }
.review-date   { color: #aaa; font-size: 0.82rem; }

.review-content {
  color: #333;
  line-height: 1.8;
  font-size: 0.97rem;
  margin: 0 0 20px;
  white-space: pre-wrap;
}

/* 이미지 갤러리 */
.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.gallery-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: opacity 0.15s;
}
.gallery-img:hover { opacity: 0.85; }

.view-count {
  color: #bbb;
  font-size: 0.8rem;
  text-align: right;
  margin: 0 0 16px;
}

/* 반응 버튼 */
.reaction-area { display: flex; gap: 12px; padding-top: 16px; border-top: 1px solid #f0f0f0; }
.btn-reaction {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  font-size: 0.9rem;
  color: #555;
  transition: all 0.15s;
}
.btn-reaction:hover        { background: #f9f9f9; }
.btn-reaction.active       { border-color: #1a1a2e; color: #1a1a2e; background: #f0f2ff; }
.btn-reaction.dislike.active { border-color: #e53e3e; color: #e53e3e; background: #fff5f5; }

/* 라이트박스 */
.lightbox {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  cursor: pointer;
}
.lightbox-img {
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 6px;
}
</style>