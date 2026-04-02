<template>
  <div class="qna-write-page">
    <section class="page-header">
      <div class="header-inner">
        <h1 class="page-title">문의 작성</h1>
        <p class="page-sub">1:1 문의 작성 후 답변을 받아보세요.</p>
      </div>
    </section>

    <form class="write-form" @submit.prevent="submitQna">

      <!-- 카테고리 -->
      <div class="form-row">
        <label class="form-label required">카테고리</label>
        <div class="form-field">
          <select v-model="form.category" class="form-select" required>
            <option value="" disabled>카테고리 선택</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
          <span class="error" v-if="errors.category">{{ errors.category }}</span>
        </div>
      </div>

      <!-- 제목 -->
      <div class="form-row">
        <label class="form-label required">제목</label>
        <div class="form-field">
          <input
            v-model="form.title"
            type="text"
            class="form-input"
            placeholder="제목을 입력해주세요. (최대 100자)"
            maxlength="100"
            required
          />
          <span class="char-count">{{ form.title.length }}/100</span>
          <span class="error" v-if="errors.title">{{ errors.title }}</span>
        </div>
      </div>

      <!-- 내용 -->
      <div class="form-row">
        <label class="form-label required">내용</label>
        <div class="form-field">
          <textarea
            v-model="form.content"
            class="form-textarea"
            placeholder="문의 내용을 자세히 입력해주세요. (최대 2000자)"
            maxlength="2000"
            rows="10"
            required
          />
          <span class="char-count">{{ form.content.length }}/2000</span>
          <span class="error" v-if="errors.content">{{ errors.content }}</span>
        </div>
      </div>

      <!-- 비밀글 여부 (FQ-003) -->
      <div class="form-row">
        <label class="form-label">비밀글</label>
        <div class="form-field check-wrap">
          <label class="check-label">
            <input type="checkbox" v-model="form.isSecret" class="check-input" />
            <span class="check-text">비밀글로 등록하기 (작성자·관리자만 열람 가능)</span>
          </label>
        </div>
      </div>

      <!-- 상품 연관 (선택) -->
      <div class="form-row">
        <label class="form-label">관련 상품 ID</label>
        <div class="form-field">
          <input
            v-model.number="form.productId"
            type="number"
            class="form-input narrow"
            placeholder="상품 ID 입력 (선택사항)"
            min="1"
          />
        </div>
      </div>

      <!-- 버튼 -->
      <div class="btn-row">
        <button type="button" class="btn-cancel" @click="$router.back()">취소</button>
        <button type="submit" class="btn-submit" :disabled="submitting">
          <span v-if="submitting" class="spinner-sm" />
          <span v-else>등록하기</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { writeQna } from '@/api/qna.js'

const router = useRouter()

const categories = [
  '배송문의',
  '교환/반품',
  '결제문의',
  '상품문의',
  '회원/계정',
  '기타',
]

const form = reactive({
  category: '',
  title: '',
  content: '',
  isSecret: false,
  productId: null,
})

const errors = reactive({ category: '', title: '', content: '' })
const submitting = ref(false)

const validate = () => {
  errors.category = form.category ? '' : '카테고리를 선택해주세요.'
  errors.title = form.title.trim() ? '' : '제목을 입력해주세요.'
  errors.content = form.content.trim() ? '' : '내용을 입력해주세요.'
  return !errors.category && !errors.title && !errors.content
}

const submitQna = async () => {
  if (!validate()) return
  submitting.value = true
  try {
    const payload = {
      category: form.category,
      title: form.title.trim(),
      content: form.content.trim(),
      isSecret: form.isSecret,
      ...(form.productId && { productId: form.productId }),
    }
    await writeQna(payload)
    alert('문의가 등록되었습니다.')
    router.push('/qna/list')
  } catch (e) {
    alert(e?.response?.data?.message ?? '등록에 실패했습니다.')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.qna-write-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 0 20px 80px;
}

/* ── 헤더 ─────────────────────────────── */
.page-header {
  padding: 48px 0 32px;
  border-bottom: 2px solid #111;
  margin-bottom: 36px;
}
.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #111;
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}
.page-sub { font-size: 14px; color: #888; margin: 0; }

/* ── 폼 ──────────────────────────────── */
.write-form { display: flex; flex-direction: column; gap: 24px; }

.form-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 10px;
  align-items: start;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding-top: 10px;
}
.form-label.required::after {
  content: ' *';
  color: #e53e3e;
}

.form-field { display: flex; flex-direction: column; gap: 4px; position: relative; }

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  color: #111;
  background: #fff;
  transition: border-color 0.18s;
  box-sizing: border-box;
  font-family: inherit;
}
.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #111;
}
.form-input.narrow { max-width: 200px; }
.form-textarea { resize: vertical; min-height: 200px; }

.char-count {
  font-size: 12px;
  color: #aaa;
  text-align: right;
}
.error { font-size: 12px; color: #e53e3e; }

/* ── 체크박스 ──────────────────────────── */
.check-wrap { padding-top: 10px; }
.check-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.check-input { width: 16px; height: 16px; cursor: pointer; accent-color: #111; }
.check-text { font-size: 14px; color: #444; }

/* ── 버튼 ──────────────────────────────── */
.btn-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.btn-cancel {
  padding: 10px 28px;
  border: 1.5px solid #ddd;
  background: #fff;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  color: #555;
  transition: all 0.15s;
}
.btn-cancel:hover { border-color: #999; color: #111; }
.btn-submit {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 32px;
  background: #111;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s;
}
.btn-submit:hover:not(:disabled) { background: #333; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.spinner-sm {
  width: 16px; height: 16px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 반응형 ────────────────────────────── */
@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .form-label { padding-top: 0; }
}
</style>