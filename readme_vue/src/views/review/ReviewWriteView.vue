<template>
  <div class="page-wrap">
    <h2 class="page-title">리뷰 작성</h2>

    <!-- 상품 정보 요약 -->
    <div v-if="product" class="product-info">
      <img :src="product.thumbnail" alt="상품 이미지" class="product-thumb" />
      <div class="product-meta">
        <p class="product-title">{{ product.title }}</p>
        <p class="product-author">{{ product.author }}</p>
      </div>
    </div>

    <form class="review-form" @submit.prevent="submitReview">
      <!-- 평점 (1~5) -->
      <div class="form-group">
        <label class="form-label">평점 <span class="required">*</span></label>
        <div class="star-select">
          <span
            v-for="n in 5"
            :key="n"
            class="star-pick"
            :class="{ filled: n <= form.rating }"
            @click="form.rating = n"
            @mouseover="hoverRating = n"
            @mouseleave="hoverRating = 0"
          >{{ n <= (hoverRating || form.rating) ? '★' : '☆' }}</span>
          <span class="rating-label">{{ ratingLabel }}</span>
        </div>
        <p v-if="errors.rating" class="err-msg">{{ errors.rating }}</p>
      </div>

      <!-- 내용 -->
      <div class="form-group">
        <label class="form-label">리뷰 내용 <span class="required">*</span></label>
        <textarea
          v-model="form.content"
          class="form-textarea"
          :class="{ error: errors.content }"
          placeholder="구매하신 상품에 대한 솔직한 리뷰를 남겨주세요. (최소 10자)"
          rows="6"
          maxlength="1000"
        />
        <div class="textarea-footer">
          <p v-if="errors.content" class="err-msg">{{ errors.content }}</p>
          <span class="char-count">{{ form.content.length }} / 1000</span>
        </div>
      </div>

      <!-- 이미지 첨부 (FR-003, 최대 5장) -->
      <div class="form-group">
        <label class="form-label">이미지 첨부 <span class="optional">(선택, 최대 5장)</span></label>
        <div class="image-upload-area">
          <!-- 미리보기 -->
          <div
            v-for="(preview, i) in imagePreviews"
            :key="i"
            class="image-preview-wrap"
          >
            <img :src="preview" alt="미리보기" class="image-preview" />
            <button type="button" class="btn-remove-image" @click="removeImage(i)">×</button>
          </div>

          <!-- 업로드 버튼 (5장 미만일 때만 표시) -->
          <label v-if="imagePreviews.length < 5" class="upload-btn" for="imageInput">
            <span class="upload-icon">+</span>
            <span class="upload-text">사진 추가</span>
          </label>
          <input
            id="imageInput"
            type="file"
            accept="image/*"
            multiple
            class="hidden-input"
            @change="handleImageChange"
          />
        </div>
        <p v-if="errors.images" class="err-msg">{{ errors.images }}</p>
        <p class="hint">이미지 한 장당 최대 10MB / 요청당 최대 20MB</p>
      </div>

      <!-- 버튼 -->
      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="$router.back()">취소</button>
        <button type="submit" class="btn-submit" :disabled="isSubmitting">
          {{ isSubmitting ? '등록 중...' : '리뷰 등록' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { writeReview } from '@/api/review.js'
import { getProductDetail } from '@/api/product.js'

const route  = useRoute()
const router = useRouter()

const productId = route.query.productId
const product   = ref(null)

const form = ref({ rating: 0, content: '' })
const errors = ref({})
const hoverRating  = ref(0)
const imageFiles   = ref([])    // File 객체 배열
const imagePreviews = ref([])   // base64 미리보기
const isSubmitting  = ref(false)

const ratingLabel = computed(() => {
  const labels = { 1: '별로예요', 2: '그저 그래요', 3: '보통이에요', 4: '좋아요', 5: '최고예요' }
  return labels[hoverRating.value || form.value.rating] ?? '별점을 선택해 주세요'
})

onMounted(async () => {
  if (!productId) return router.back()
  try {
    const { data } = await getProductDetail(productId)
    product.value = data
  } catch (e) {
    console.error('상품 정보 로드 실패', e)
  }
})

// ── 이미지 처리 (FR-003) ─────────────────────
const handleImageChange = (e) => {
  const files = Array.from(e.target.files)
  const remaining = 5 - imageFiles.value.length

  if (files.length > remaining) {
    errors.value.images = `최대 5장까지 첨부 가능합니다. (${remaining}장 더 추가 가능)`
    files.splice(remaining)
  } else {
    errors.value.images = ''
  }

  files.forEach(file => {
    if (file.size > 10 * 1024 * 1024) {
      errors.value.images = '이미지 한 장의 크기는 최대 10MB입니다.'
      return
    }
    imageFiles.value.push(file)
    const reader = new FileReader()
    reader.onload = (ev) => imagePreviews.value.push(ev.target.result)
    reader.readAsDataURL(file)
  })

  // input 초기화 (같은 파일 재선택 허용)
  e.target.value = ''
}

const removeImage = (index) => {
  imageFiles.value.splice(index, 1)
  imagePreviews.value.splice(index, 1)
  errors.value.images = ''
}

// ── 유효성 검사 ──────────────────────────────
const validate = () => {
  errors.value = {}
  if (!form.value.rating)           errors.value.rating  = '평점을 선택해 주세요.'
  if (form.value.content.length < 10) errors.value.content = '리뷰 내용을 10자 이상 입력해 주세요.'
  return Object.keys(errors.value).length === 0
}

// ── 제출 (FR-002 + FR-003) ───────────────────
const submitReview = async () => {
  if (!validate()) return

  isSubmitting.value = true
  try {
    const formData = new FormData()
    formData.append('productId', productId)
    formData.append('rating',    form.value.rating)
    formData.append('content',   form.value.content)
    imageFiles.value.forEach(file => formData.append('images', file))

    await writeReview(formData)
    alert('리뷰가 등록되었습니다.')
    router.back()
  } catch (e) {
    if (e.response?.status === 403) {
      alert('구매 확정 후 리뷰를 작성할 수 있습니다.')
    } else if (e.response?.status === 409) {
      alert('이미 작성한 리뷰가 있습니다.')
    } else {
      alert('리뷰 등록에 실패했습니다. 다시 시도해 주세요.')
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.page-wrap  { max-width: 720px; margin: 0 auto; padding: 32px 20px; }
.page-title { font-size: 1.5rem; font-weight: 700; margin-bottom: 24px; color: #1a1a2e; }

/* 상품 정보 */
.product-info {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 28px;
}
.product-thumb  { width: 72px; height: 90px; object-fit: cover; border-radius: 4px; }
.product-meta   { display: flex; flex-direction: column; justify-content: center; gap: 4px; }
.product-title  { font-weight: 600; font-size: 1rem; color: #222; }
.product-author { color: #888; font-size: 0.85rem; }

/* 폼 */
.review-form  { display: flex; flex-direction: column; gap: 24px; }
.form-group   { display: flex; flex-direction: column; gap: 8px; }
.form-label   { font-weight: 600; font-size: 0.95rem; color: #333; }
.required     { color: #e53e3e; }
.optional     { color: #aaa; font-size: 0.8rem; font-weight: 400; }

/* 별점 선택 */
.star-select { display: flex; align-items: center; gap: 4px; }
.star-pick {
  font-size: 2rem;
  cursor: pointer;
  color: #ddd;
  transition: color 0.1s;
  user-select: none;
}
.star-pick.filled { color: #f5a623; }
.rating-label { margin-left: 10px; color: #555; font-size: 0.9rem; }

/* 텍스트에어리어 */
.form-textarea {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 12px;
  font-size: 0.95rem;
  resize: vertical;
  transition: border-color 0.2s;
  font-family: inherit;
  line-height: 1.6;
}
.form-textarea:focus   { outline: none; border-color: #1a1a2e; }
.form-textarea.error   { border-color: #e53e3e; }
.textarea-footer       { display: flex; justify-content: space-between; align-items: flex-start; }
.char-count            { color: #aaa; font-size: 0.8rem; white-space: nowrap; }

/* 이미지 업로드 */
.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  border: 1px dashed #ccc;
  border-radius: 6px;
  min-height: 100px;
  align-items: flex-start;
}
.image-preview-wrap { position: relative; }
.image-preview {
  width: 90px; height: 90px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
  display: block;
}
.btn-remove-image {
  position: absolute;
  top: -6px; right: -6px;
  width: 20px; height: 20px;
  border-radius: 50%;
  background: #e53e3e;
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-btn {
  width: 90px; height: 90px;
  border: 1px dashed #aaa;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  gap: 4px;
  transition: background 0.15s;
}
.upload-btn:hover { background: #f5f5f5; }
.upload-icon { font-size: 1.6rem; color: #aaa; }
.upload-text { font-size: 0.75rem; color: #aaa; }
.hidden-input { display: none; }
.hint { color: #aaa; font-size: 0.8rem; }

/* 에러 메시지 */
.err-msg { color: #e53e3e; font-size: 0.82rem; margin: 0; }

/* 버튼 */
.form-actions { display: flex; justify-content: flex-end; gap: 10px; padding-top: 8px; }
.btn-cancel {
  padding: 10px 24px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: #fff;
  color: #555;
  cursor: pointer;
  font-size: 0.95rem;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit {
  padding: 10px 28px;
  border: none;
  border-radius: 4px;
  background: #1a1a2e;
  color: #fff;
  cursor: pointer;
  font-size: 0.95rem;
  transition: background 0.15s;
}
.btn-submit:hover:not(:disabled) { background: #16213e; }
.btn-submit:disabled { background: #aaa; cursor: not-allowed; }
</style>