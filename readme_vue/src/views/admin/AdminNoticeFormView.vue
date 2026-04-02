<template>
  <div class="admin-notice-form">
    <!-- 헤더 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ isEdit ? '공지사항 수정' : '공지사항 등록' }}</h2>
        <p class="page-sub">{{ isEdit ? '공지 내용을 수정합니다' : '새 공지사항을 작성합니다' }}</p>
      </div>
      <button class="btn-back-arrow" @click="goList">← 목록</button>
    </div>

    <!-- 로딩 (수정 시 기존 데이터 로드) -->
    <div v-if="fetchLoading" class="state-box"><span class="spinner" /></div>

    <form v-else class="form-card" @submit.prevent="handleSubmit">
      <!-- 고정 여부 -->
      <div class="form-row form-row--toggle">
        <label class="form-label">고정 공지</label>
        <label class="toggle-switch">
          <input type="checkbox" v-model="form.isFixed" />
          <span class="toggle-track">
            <span class="toggle-thumb" />
          </span>
          <span class="toggle-label">{{ form.isFixed ? '고정 ON' : '고정 OFF' }}</span>
        </label>
        <p class="form-hint">고정 공지는 목록 최상단에 항상 표시됩니다</p>
      </div>

      <!-- 제목 -->
      <div class="form-row">
        <label class="form-label" for="notice-title">
          제목 <span class="required">*</span>
        </label>
        <input
          id="notice-title"
          v-model.trim="form.title"
          type="text"
          class="form-input"
          :class="{ 'is-error': errors.title }"
          placeholder="공지사항 제목을 입력하세요"
          maxlength="255"
        />
        <div class="input-footer">
          <span v-if="errors.title" class="err-msg">{{ errors.title }}</span>
          <span v-else class="form-hint" />
          <span class="char-count">{{ form.title.length }} / 255</span>
        </div>
      </div>

      <!-- 내용 -->
      <div class="form-row">
        <label class="form-label" for="notice-content">
          내용 <span class="required">*</span>
        </label>
        <textarea
          id="notice-content"
          v-model="form.content"
          class="form-textarea"
          :class="{ 'is-error': errors.content }"
          placeholder="공지사항 내용을 입력하세요"
          rows="16"
        />
        <div class="input-footer">
          <span v-if="errors.content" class="err-msg">{{ errors.content }}</span>
          <span v-else class="form-hint">줄바꿈은 그대로 표시됩니다</span>
          <span class="char-count">{{ form.content.length }}자</span>
        </div>
      </div>

      <!-- 미리보기 패널 -->
      <div class="preview-panel">
        <button type="button" class="preview-toggle" @click="showPreview = !showPreview">
          {{ showPreview ? '▲ 미리보기 닫기' : '▼ 미리보기 열기' }}
        </button>
        <transition name="slide-fade">
          <div v-if="showPreview" class="preview-body">
            <p class="preview-title">{{ form.title || '(제목 없음)' }}</p>
            <div class="preview-content" v-html="previewHtml" />
          </div>
        </transition>
      </div>

      <!-- 버튼 -->
      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="goList">취소</button>
        <button type="submit" class="btn-submit" :disabled="submitLoading">
          {{ submitLoading ? (isEdit ? '수정 중…' : '등록 중…') : (isEdit ? '수정 완료' : '등록') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminNoticeDetail, createNotice, updateNotice } from '@/api/notice.js'

const route  = useRoute()
const router = useRouter()

// 수정 모드 여부
const isEdit = computed(() => !!route.params.id)

const form = ref({
  title:   '',
  content: '',
  isFixed: false,
})
const errors       = ref({})
const submitLoading = ref(false)
const fetchLoading  = ref(false)
const showPreview   = ref(false)

const previewHtml = computed(() =>
  form.value.content.replace(/\n/g, '<br>')
)

// 수정 시 기존 데이터 로드
async function loadNotice() {
  fetchLoading.value = true
  try {
    const { data } = await getAdminNoticeDetail(route.params.id)
    const n = data.notice ?? data
    form.value.title   = n.title   ?? ''
    form.value.content = n.content ?? ''
    form.value.isFixed = n.isFixed ?? false
  } catch (e) {
    alert('공지사항 정보를 불러오지 못했습니다.')
    console.error(e)
    goList()
  } finally {
    fetchLoading.value = false
  }
}

// 유효성 검사
function validate() {
  errors.value = {}
  if (!form.value.title)   errors.value.title   = '제목을 입력해주세요.'
  if (!form.value.content) errors.value.content = '내용을 입력해주세요.'
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateNotice(route.params.id, form.value)
      router.push(`/admin/notice/detail/${route.params.id}`)
    } else {
      await createNotice(form.value)
      router.push('/admin/notice/list')
    }
  } catch (e) {
    alert(isEdit.value ? '수정에 실패했습니다.' : '등록에 실패했습니다.')
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

function goList() { router.push('/admin/notice/list') }

onMounted(() => {
  if (isEdit.value) loadNotice()
})
</script>

<style scoped>
.admin-notice-form { padding: 32px; max-width: 880px; }

/* ── 헤더 ── */
.page-header {
  display: flex; justify-content: space-between;
  align-items: flex-end; margin-bottom: 28px; flex-wrap: wrap; gap: 16px;
}
.page-title { font-size: 22px; font-weight: 800; color: #1a1a2e; margin: 0 0 4px; }
.page-sub   { font-size: 13px; color: rgba(26,26,46,0.5); margin: 0; }
.btn-back-arrow {
  background: none; border: none;
  font-size: 14px; color: rgba(26,26,46,0.55); cursor: pointer; padding: 6px 0;
}
.btn-back-arrow:hover { color: #1a1a2e; }

/* ── 폼 카드 ── */
.form-card {
  background: #fff; border: 1px solid #e8e4dc;
  border-radius: 10px; padding: 36px 40px; display: flex; flex-direction: column; gap: 28px;
}

/* ── 폼 행 ── */
.form-row { display: flex; flex-direction: column; gap: 8px; }
.form-label {
  font-size: 13px; font-weight: 700; color: #1a1a2e; letter-spacing: 0.2px;
}
.required { color: #c0392b; margin-left: 2px; }

.form-input,
.form-textarea {
  width: 100%; padding: 12px 14px;
  border: 1.5px solid #e8e4dc; border-radius: 6px;
  font-size: 14px; color: #1a1a2e; background: #fdfcfa;
  transition: border-color 0.15s, box-shadow 0.15s;
  outline: none; box-sizing: border-box;
}
.form-input:focus,
.form-textarea:focus {
  border-color: #c9a84c;
  box-shadow: 0 0 0 3px rgba(201,168,76,0.12);
}
.form-input.is-error,
.form-textarea.is-error { border-color: #c0392b; }

.form-textarea { resize: vertical; min-height: 280px; font-family: inherit; line-height: 1.7; }

.input-footer {
  display: flex; justify-content: space-between; align-items: center; gap: 8px;
}
.form-hint { font-size: 11px; color: rgba(26,26,46,0.4); }
.char-count { font-size: 11px; color: rgba(26,26,46,0.4); white-space: nowrap; }
.err-msg    { font-size: 11px; color: #c0392b; }

/* ── 토글 스위치 ── */
.form-row--toggle { flex-direction: row; align-items: center; flex-wrap: wrap; gap: 12px; }
.form-row--toggle .form-hint { flex-basis: 100%; margin-top: -4px; }

.toggle-switch {
  display: flex; align-items: center; gap: 10px; cursor: pointer;
}
.toggle-switch input { display: none; }

.toggle-track {
  position: relative; width: 44px; height: 24px;
  background: #e8e4dc; border-radius: 12px; transition: background 0.2s;
}
.toggle-switch input:checked + .toggle-track { background: #2d6a4f; }

.toggle-thumb {
  position: absolute; top: 3px; left: 3px;
  width: 18px; height: 18px; background: #fff;
  border-radius: 50%; transition: transform 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.2);
}
.toggle-switch input:checked + .toggle-track .toggle-thumb { transform: translateX(20px); }
.toggle-label { font-size: 13px; font-weight: 600; color: #1a1a2e; user-select: none; }

/* ── 미리보기 ── */
.preview-panel {
  border: 1px solid #e8e4dc; border-radius: 8px; overflow: hidden;
}
.preview-toggle {
  width: 100%; padding: 12px 16px; background: #f5f3ef;
  border: none; text-align: left; font-size: 12px;
  color: rgba(26,26,46,0.55); cursor: pointer; letter-spacing: 0.5px;
  transition: background 0.15s;
}
.preview-toggle:hover { background: #ede9e1; }
.preview-body { padding: 24px 28px; background: #fff; border-top: 1px solid #e8e4dc; }
.preview-title {
  font-size: 18px; font-weight: 800; color: #1a1a2e;
  margin: 0 0 16px; padding-bottom: 14px; border-bottom: 1px solid #e8e4dc;
}
.preview-content { font-size: 14px; line-height: 1.85; color: #2c2c3e; }

.slide-fade-enter-active,
.slide-fade-leave-active { transition: all 0.2s ease; }
.slide-fade-enter-from,
.slide-fade-leave-to    { opacity: 0; transform: translateY(-6px); }

/* ── 버튼 ── */
.form-actions {
  display: flex; justify-content: flex-end; gap: 10px; padding-top: 8px;
}
.btn-cancel {
  padding: 11px 24px; border: 1px solid #e8e4dc;
  border-radius: 6px; background: #fff; color: rgba(26,26,46,0.7);
  font-size: 14px; cursor: pointer; transition: all 0.15s;
}
.btn-cancel:hover { border-color: #1a1a2e; color: #1a1a2e; }
.btn-submit {
  padding: 11px 32px; border: none; border-radius: 6px;
  background: #1a1a2e; color: #fff; font-size: 14px; font-weight: 700;
  cursor: pointer; transition: background 0.15s;
}
.btn-submit:hover:not(:disabled) { background: #2d6a4f; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── 상태 ── */
.state-box {
  display: flex; align-items: center; justify-content: center; padding: 80px;
}
.spinner {
  display: block; width: 32px; height: 32px;
  border: 3px solid #e8e4dc; border-top-color: #c9a84c;
  border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 640px) {
  .form-card { padding: 24px 18px; }
  .admin-notice-form { padding: 20px 16px; }
  .form-row--toggle { flex-direction: column; align-items: flex-start; }
}
</style>