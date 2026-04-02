<template>
  <div class="qna-detail-page">
    <!-- 로딩 -->
    <div class="loading-wrap" v-if="loading">
      <span class="spinner" />
    </div>

    <template v-else-if="qna">
      <!-- 상단 네비 -->
      <div class="breadcrumb">
        <router-link to="/qna/list" class="back-link">← 목록으로</router-link>
      </div>

      <!-- 질문 카드 -->
      <article class="qna-card question-card">
        <header class="card-header">
          <span class="category-badge">{{ qna.category }}</span>
          <span class="status-badge" :class="statusClass(qna.qnaStatus)">
            {{ statusLabel(qna.qnaStatus) }}
          </span>
          <span class="secret-badge" v-if="qna.isSecret">🔒 비밀글</span>
        </header>

        <h2 class="qna-title">{{ qna.title }}</h2>

        <div class="meta-row">
          <span class="meta-item">{{ maskName(qna.memberName) }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ formatDateTime(qna.createdAt) }}</span>
        </div>

        <div class="content-body">{{ qna.content }}</div>
      </article>

      <!-- 답변 목록 (depth 1+) -->
      <div
        v-for="reply in replies"
        :key="reply.id"
        class="qna-card reply-card"
        :style="{ marginLeft: `${(reply.depth) * 28}px` }"
      >
        <header class="card-header reply-header">
          <span class="reply-icon">↳</span>
          <span class="reply-label" :class="reply.isAdmin ? 'admin-reply' : 'user-reply'">
            {{ reply.isAdmin ? '관리자 답변' : '추가 문의' }}
          </span>
        </header>

        <div class="content-body">{{ reply.content }}</div>

        <div class="meta-row">
          <span class="meta-item">{{ formatDateTime(reply.createdAt) }}</span>
        </div>
      </div>

      <!-- n차 재문의 폼 (FQ-004 — depth 최대 4) -->
      <div class="re-question-wrap" v-if="canReply">
        <button
          class="toggle-reply-btn"
          @click="showReplyForm = !showReplyForm"
        >
          {{ showReplyForm ? '취소' : '💬 추가 문의하기' }}
        </button>

        <div class="reply-form" v-if="showReplyForm">
          <textarea
            v-model="replyContent"
            class="reply-textarea"
            placeholder="추가 문의 내용을 입력해주세요. (최대 2000자)"
            rows="5"
            maxlength="2000"
          />
          <div class="reply-form-footer">
            <span class="char-count">{{ replyContent.length }}/2000</span>
            <button
              class="btn-submit"
              @click="submitReply"
              :disabled="submitting || !replyContent.trim()"
            >
              <span v-if="submitting" class="spinner-sm" />
              <span v-else>등록</span>
            </button>
          </div>
        </div>
      </div>

      <!-- depth 초과 안내 -->
      <div class="depth-limit-notice" v-else-if="isLoggedIn && isMyQna && maxDepthReached">
        <p>최대 문의 단계(4단계)에 도달하여 추가 문의가 불가합니다.</p>
      </div>
    </template>

    <div class="not-found" v-else>
      <p>해당 문의를 찾을 수 없습니다.</p>
      <router-link to="/qna/list" class="back-btn">목록으로</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { getQnaDetail, writeReply } from '@/api/qna.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const id = route.params.id

const loading = ref(true)
const qna = ref(null)
const replies = ref([])

const isLoggedIn = computed(() => !!authStore.accessToken)
const isMyQna = computed(() => qna.value?.memberId === authStore.memberId)

// depth 최대값 계산
const maxDepth = computed(() => Math.max(0, ...replies.value.map((r) => r.depth)))
const maxDepthReached = computed(() => maxDepth.value >= 4)

// 재문의 가능: 로그인 + 내 글 + depth < 4 + COMPLETE 상태 아님
const canReply = computed(() =>
  isLoggedIn.value &&
  isMyQna.value &&
  !maxDepthReached.value &&
  qna.value?.qnaStatus !== 'COMPLETE'
)

const showReplyForm = ref(false)
const replyContent = ref('')
const submitting = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getQnaDetail(id)
    const data = res.data
    qna.value = data.question ?? data
    replies.value = data.replies ?? []
  } catch (e) {
    qna.value = null
  } finally {
    loading.value = false
  }
}

const submitReply = async () => {
  if (!replyContent.value.trim()) return
  submitting.value = true
  try {
    await writeReply(id, {
      content: replyContent.value.trim(),
      parentId: Number(id),
    })
    replyContent.value = ''
    showReplyForm.value = false
    await fetchDetail()
  } catch (e) {
    alert(e?.response?.data?.message ?? '등록에 실패했습니다.')
  } finally {
    submitting.value = false
  }
}

const maskName = (name = '') => {
  if (!name || name.length <= 1) return name
  return name[0] + '*'.repeat(name.length - 1)
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
.qna-detail-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 40px 20px 80px;
}

/* ── 네비 ──────────────────────────────── */
.breadcrumb { margin-bottom: 24px; }
.back-link {
  font-size: 14px;
  color: #666;
  text-decoration: none;
  transition: color 0.15s;
}
.back-link:hover { color: #111; }

/* ── 카드 ──────────────────────────────── */
.qna-card {
  border: 1.5px solid #e5e5e5;
  border-radius: 10px;
  padding: 28px 32px;
  margin-bottom: 16px;
  background: #fff;
}

.question-card {
  border-top: 3px solid #111;
}

.reply-card {
  background: #fafafa;
  border-color: #ebebeb;
}

/* ── 카드 헤더 ─────────────────────────── */
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

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

.secret-badge {
  font-size: 12px;
  color: #888;
}

.reply-header { margin-bottom: 10px; }
.reply-icon { font-size: 16px; color: #aaa; }
.reply-label {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 12px;
}
.admin-reply  { background: #e8f4fd; color: #0066cc; }
.user-reply   { background: #f0f0f0; color: #555; }

/* ── 제목 & 내용 ───────────────────────── */
.qna-title {
  font-size: 20px;
  font-weight: 700;
  color: #111;
  margin: 0 0 10px;
  line-height: 1.4;
  letter-spacing: -0.4px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;
}
.meta-item   { font-size: 13px; color: #888; }
.meta-divider { color: #ccc; }

.content-body {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

/* ── 재문의 ────────────────────────────── */
.re-question-wrap { margin-top: 8px; }

.toggle-reply-btn {
  padding: 9px 20px;
  border: 1.5px solid #111;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  color: #111;
  transition: all 0.15s;
}
.toggle-reply-btn:hover { background: #111; color: #fff; }

.reply-form {
  margin-top: 12px;
  border: 1.5px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.reply-textarea {
  width: 100%;
  padding: 16px;
  border: none;
  font-size: 14px;
  font-family: inherit;
  color: #111;
  resize: vertical;
  box-sizing: border-box;
  min-height: 110px;
}
.reply-textarea:focus { outline: none; }

.reply-form-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 10px 16px;
  background: #fafafa;
  border-top: 1px solid #eee;
}

.char-count { font-size: 12px; color: #aaa; }

.btn-submit {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 22px;
  background: #111;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-submit:hover:not(:disabled) { background: #333; }
.btn-submit:disabled { opacity: 0.4; cursor: not-allowed; }

/* ── 안내 ──────────────────────────────── */
.depth-limit-notice {
  margin-top: 12px;
  padding: 12px 16px;
  background: #fff8e1;
  border-radius: 6px;
  font-size: 13px;
  color: #856404;
}

.not-found { text-align: center; padding: 80px 0; color: #aaa; }
.back-btn {
  display: inline-block;
  margin-top: 16px;
  padding: 9px 24px;
  background: #111;
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
  font-size: 13px;
}

/* ── 로딩 ──────────────────────────────── */
.loading-wrap { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 36px; height: 36px;
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
@keyframes spin { to { transform: rotate(360deg); } }
</style>