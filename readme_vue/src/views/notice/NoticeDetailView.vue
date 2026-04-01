<template>
  <div v-if="notice" class="notice-detail-container">
    <div class="notice-header">
      <span v-if="notice.is_fixed" class="badge">중요</span>
      <h2 class="notice-title">{{ notice.title }}</h2>
      <div class="notice-info">
        <span>작성일: {{ formatDate(notice.created_at) }}</span>
        <span>조회수: {{ notice.view_count }}</span>
      </div>
    </div>
    
    <hr />

    <div class="notice-content">
      <p>{{ notice.content }}</p>
    </div>

    <div class="button-group">
      <button @click="goToList" class="list-btn">목록으로</button>
    </div>
  </div>
  
  <div v-else class="loading">
    로딩 중...
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { noticeApi } from '@/api/notice';

const route = useRoute();
const router = useRouter();
const notice = ref(null);

const fetchNoticeDetail = async () => {
  const id = route.params.id;
  try {
    const response = await noticeApi.getNoticeDetail(id);
    // 서버 응답 구조가 { data: { ... } } 형태인 경우를 대비
    notice.value = response.data; 
  } catch (error) {
    console.error("공지사항 상세 로드 실패:", error);
    alert("존재하지 않거나 삭제된 공지사항입니다.");
    router.push('/notice');
  }
};

const goToList = () => {
  router.push('/notice');
};

const formatDate = (dateString) => {
  if (!dateString) return "-";
  return new Date(dateString).toLocaleString();
};

onMounted(() => {
  fetchNoticeDetail();
});
</script>

<style scoped>
.notice-detail-container { max-width: 800px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; }
.notice-header { margin-bottom: 20px; }
.notice-title { display: inline; margin-left: 10px; }
.notice-info { margin-top: 10px; color: #666; font-size: 0.9rem; display: flex; gap: 20px; }
.notice-content { min-height: 300px; padding: 20px 0; line-height: 1.6; white-space: pre-wrap; }
.badge { background: #ff4d4f; color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; vertical-align: middle; }
.button-group { text-align: center; margin-top: 30px; }
.list-btn { padding: 10px 20px; cursor: pointer; background-color: #f0f0f0; border: 1px solid #ccc; }
</style>