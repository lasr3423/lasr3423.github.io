<template>
  <div class="admin-qna-detail">

    <!-- 로딩 -->
    <div class="loading-wrap" v-if="loading">
      <span class="spinner" />
    </div>

    <template v-else-if="qna">

      <!-- 상단 네비 -->
      <div class="breadcrumb">
        <router-link to="/admin/qna/list" class="back-link">← QnA 목록</router-link>
      </div>

      <!-- 질문 정보 헤더 -->
      <div class="detail-header">
        <div class="header-left">
          <span class="category-badge">{{ qna.category }}</span>
          <span class="status-badge" :class="statusClass(qna.qnaStatus)">
            {{ statusLabel(qna.qnaStatus) }}
          </span>
          <span class="secret-badge" v-if="qna.isSecret">🔒 비밀글</span>
        </div>
        <div class="header-right">
          <!-- 상태 변경 드롭다운 (FA-034) -->
          <label class="inline-label">상태 변경</label>
          <select class="status-select" v-model="editStatus" @change="changeQnaStatus">
            <option value="WAITING">답변대기</option>
            <option value="PROCESSING">처리중</option>
            <option value="COMPLETE">답변완료</option>
          </select>
        </div>
      </div>

      <!-- 질문 카드 -->
      <article class="qna-card question-card">
        <h2 class="qna-title">{{ qna.title }}</h2>
        <div class="meta-row">
          <span class="meta-item">작성자: <strong>{{ qna.memberName }}</strong></span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ formatDateTime(qna.createdAt) }}</span>
          <span class="meta-divider" v-if="qna.answeredAt">·</span>
          <span class="meta-item answered" v-if="qna.answeredAt">
            답변일: {{ formatDateTime(qna.answeredAt) }}
          </span>
        </div>
        <div class="content-body">{{ qna.content }}</div>
      </article>

      <!-- 기존 답변/재문의 계층 목록 -->
      <div
        v-for="reply in replies"
        :key="reply.id"
        class="qna-card reply-card"
        :style="{ marginLeft: `${reply.depth * 24}px` }"
      >
        <div class="reply-meta">
          <span class="reply-icon">↳</span>
          <span class="reply-label" :class="reply.isAdmin ? 'admin-reply' : 'user-reply'">
            {{ reply.isAdmin ? '관리자 답변' : '추가 문의' }}
          </span>
          <span class="meta-item small">{{ formatDateTime(reply.createdAt) }}</span>

          <!-- 관리자 답변만 수정/삭제 가능 (FA-036, FA-037) -->
          <template v-if="reply.isAdmin">
            <button class="btn-icon edit" @click="startEdit(reply)">수정</button>
            <button class="btn-icon delete" @click="deleteReply(reply.id)">삭제</button>
          </template>
        </div>

        <!-- 수정 중 -->
        <template v-if="editingId === reply.id">
          <textarea
            v-model="editContent"
            class="edit-textarea"
            rows="4"
          />
          <div class="edit-footer">
            <button class="btn-sm cancel" @click="cancelEdit">취소</button>
            <button class="btn-sm save" @click="saveEdit(reply.id)" :disabled="saving">
              <span v-if="saving" class="spinner-xs" />
              <span v-else>저장</span>
            </button>
          </div>
        </template>

        <!-- 일반 표시 -->
        <div class="content-body" v-else>{{ reply.content }}</div>
      </div>

      <!-- 답변 등록 폼 (FA-035) -->
      <div class="answer-form-wrap" v-if="canAnswer">
        <h3 class="form-title">
          <span class="form-title-badge">관리자 답변 등록</span>
        </h3>
        <div class="answer-form">
          <textarea
            v-model="answerContent"
            class="answer-textarea"
            placeholder="답변 내용을 입력해주세요..."
            rows="7"
            maxlength="3000"
          />
          <div class="form-footer">
            <span class="char-count">{{ answerContent.length }}/3000</span>
            <button
              class="btn-answer"
              @click="submitAnswer"
              :disabled="submitting || !answerContent.trim()"
            >
              <span v-if="submitting" class="spinner-sm" />
              <span v-else>답변 등록</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 이미 답변 완료 -->
      <div class="already-answered" v-else>
        <p>✅ 답변이 완료된 문의입니다.</p>
      </div>

    </template>

    <div class="not-found" v-else>
      <p>해당 문의를 찾을 수 없습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  adminGetQnaDetail,
  adminUpdateQnaStatus,
  adminAnswerQna,
  adminUpdateAnswer,
  adminDeleteQna,
} from '@/api/qna.js'

const route = useRoute()
const id = route.params.id

const loading = ref(true)
const qna = ref(null)
const replies = ref([])

const editStatus = ref('WAITING')
const answerContent = ref('')
const submitting = ref(false)

const editingId = ref(null)
const editContent = ref('')
const saving = ref(false)

// 답변 가능: WAITING 또는 PROCESSING 상태
const canAnswer = computed(() =>
  qna.value && (qna.value.qnaStatus === 'WAITING' || qna.value.qnaStatus === 'PROCESSING')
)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await adminGetQnaDetail(id)
    const data = res.data
    qna.value = data.question ?? data
    replies.value = data.replies ?? []
    editStatus.value = qna.value.qnaStatus
  } catch (e) {
    qna.value = null
  } finally {
    loading.value = false
  }
}

const changeQnaStatus = async () => {
  try {
    await adminUpdateQnaStatus(id, editStatus.value)
    qna.value.qnaStatus = editStatus.value
  } catch (e) {
    alert(e?.response?.data?.message ?? '상태 변경 실패')
    editStatus.value = qna.value.qnaStatus
  }
}

const submitAnswer = async () => {
  if (!answerContent.value.trim()) return
  submitting.value = true
  try {
    await adminAnswerQna(id, answerContent.value.trim())
    answerContent.value = ''
    await fetchDetail()
  } catch (e) {
    alert(e?.response?.data?.message ?? '답변 등록 실패')
  } finally {
    submitting.value = false
  }
}

const startEdit = (reply) => {
  editingId.value = reply.id
  editContent.value = reply.content
}

const cancelEdit = () => {
  editingId.value = null
  editContent.value = ''
}

const saveEdit = async (replyId) => {
  saving.value = true
  try {
    await adminUpdateAnswer(replyId, editContent.value.trim())
    cancelEdit()
    await fetchDetail()
  } catch (e) {
    alert(e?.response?.data?.message ?? '수정 실패')
  } finally {
    saving.value = false
  }
}

const deleteReply = async (replyId) => {
  if (!confirm('해당 답변을 삭제하시겠습니까?')) return
  try {
    await adminDeleteQna(replyId)
    await fetchDetail()
  } catch (e) {
    alert(e?.response?.data?.message ?? '삭제 실패')
  }
}

const formatDateTime = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

const statusLabel = (s) => ({ WAITING: '답변대기', PROCESSING: '처리중', COMPLETE: '답변완료' }[s] ?? s)
const statusClass = (s) => ({ WAITING: 'waiting', PROCESSING: 'processing', COMPLETE: 'complete' }[s] ?? '')

onMounted(fetchDetail)
</script>

<style scoped>
.admin-qna-detail { padding: 28px 0 80px; }

/* ── 네비 ──────────────────────────────── */
.breadcrumb { margin-bottom: 20px; }
.back-link {
  font-size: 14px;
  color: #666;
  text-decoration: none;
  transition: color 0.15s;
}
.back-link:hover { color: #111; }

/* ── 헤더 ─────────────────────────────── */
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.inline-label { font-size: 13px; color: #555; font-weight: 600; }

.category-badge {
  padding: 3px 10px;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 12px;
  color: #555;
}
.status-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
.status-badge.waiting    { background: #fff3cd; color: #856404; }
.status-badge.processing { background: #cfe2ff; color: #084298; }
.status-badge.complete   { background: #d1e7dd; color: #0a3622; }
.secret-badge { font-size: 12px; color: #888; }

.status-select {
  padding: 6px 10px;
  border: 1.5px solid #ddd;
  border-radius: 5px;
  font-size: 13px;
  background: #fff;
  cursor: pointer;
  color: #333;
}
.status-select:focus { outline: none; border-color: #111; }

/* ── 카드 ──────────────────────────────── */
.qna-card {
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  padding: 24px 28px;
  margin-bottom: 14px;
  background: #fff;
}
.question-card { border-top: 3px solid #111; }
.reply-card { background: #fafafa; }

.qna-title {
  font-size: 19px;
  font-weight: 700;
  color: #111;
  margin: 0 0 12px;
  letter-spacing: -0.4px;
  line-height: 1.4;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}
.meta-item     { font-size: 13px; color: #888; }
.meta-item.answered { color: #0066cc; }
.meta-divider  { color: #ccc; }
.content-body {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

/* ── 답변 메타 ─────────────────────────── */
.reply-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.reply-icon { color: #aaa; }
.reply-label {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 12px;
}
.admin-reply { background: #e8f4fd; color: #0066cc; }
.user-reply  { background: #f0f0f0; color: #555; }
.meta-item.small { font-size: 12px; color: #aaa; }

/* ── 수정/삭제 버튼 ───────────────────── */
.btn-icon {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: 1.5px solid transparent;
  transition: all 0.15s;
}
.btn-icon.edit   { border-color: #ddd; background: #fff; color: #444; }
.btn-icon.edit:hover { border-color: #111; color: #111; }
.btn-icon.delete { border-color: #fee2e2; background: #fff5f5; color: #c53030; }
.btn-icon.delete:hover { background: #fed7d7; }

/* ── 인라인 수정 ───────────────────────── */
.edit-textarea {
  width: 100%;
  padding: 12px;
  border: 1.5px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
  margin-top: 8px;
}
.edit-textarea:focus { outline: none; border-color: #111; }

.edit-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.btn-sm {
  padding: 7px 18px;
  border-radius: 5px;
  font-size: 13px;
  cursor: pointer;
  border: 1.5px solid #ddd;
  background: #fff;
  color: #444;
  transition: all 0.15s;
}
.btn-sm.save   { background: #111; color: #fff; border-color: #111; }
.btn-sm.save:hover:not(:disabled) { background: #333; }
.btn-sm.save:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-sm.cancel:hover { border-color: #999; color: #111; }

/* ── 답변 등록 폼 ──────────────────────── */
.answer-form-wrap {
  margin-top: 8px;
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  overflow: hidden;
}
.form-title {
  padding: 14px 20px;
  background: #f4f8ff;
  border-bottom: 1px solid #e5e5e5;
  margin: 0;
}
.form-title-badge {
  font-size: 14px;
  font-weight: 700;
  color: #0066cc;
}
.answer-form { padding: 16px; }
.answer-textarea {
  width: 100%;
  padding: 12px;
  border: 1.5px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
  min-height: 140px;
}
.answer-textarea:focus { outline: none; border-color: #111; }
.form-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}
.char-count { font-size: 12px; color: #aaa; }
.btn-answer {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 10px 28px;
  background: #0066cc;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-answer:hover:not(:disabled) { background: #0052a3; }
.btn-answer:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── 완료 안내 ─────────────────────────── */
.already-answered {
  margin-top: 8px;
  padding: 16px 20px;
  background: #d1e7dd;
  border-radius: 8px;
  font-size: 14px;
  color: #0a3622;
  font-weight: 600;
}

/* ── 로딩 ──────────────────────────────── */
.loading-wrap { display: flex; justify-content: center; padding: 60px 0; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid #eee;
  border-top-color: #111;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: block;
}
.spinner-sm {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}
.spinner-xs {
  width: 12px; height: 12px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

.not-found { text-align: center; padding: 60px 0; color: #aaa; font-size: 14px; }
</style>