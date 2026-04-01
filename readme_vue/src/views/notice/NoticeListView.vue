<template>
  <div class="notice-container">
    <h2>공지사항</h2>
    <table class="notice-table">
      <thead>
        <tr>
          <th>번호</th>
          <th>제목</th>
          <th>작성일</th>
          <th>조회수</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="notice in noticeList" :key="notice.id" :class="{ 'fixed-row': notice.isFixed }">
          <td>{{ notice.isFixed ? '📌' : notice.id }}</td>
          <td @click="goToDetail(notice.id)" class="title-cell">
            <span v-if="notice.isFixed" class="badge">중요</span>
            {{ notice.title }}
          </td>
          <td>{{ formatDate(notice.createdAt) }}</td>
          <td>{{ notice.viewCount }}</td>
        </tr>
      </tbody>
    </table>
    
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { noticeApi } from '@/api/notice';

const router = useRouter();
const noticeList = ref([]);

const fetchNotices = async () => {
  try {
    const response = await noticeApi.getNoticeList(0);
    noticeList.value = response.data.content; // Spring Data Page 객체 구조 기준
  } catch (error) {
    console.error("공지사항 로드 실패:", error);
  }
};

const goToDetail = (id) => {
  router.push(`/notice/${id}`);
};

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString();
};

onMounted(fetchNotices);
</script>

<style scoped>
.fixed-row { background-color: #f9f9f9; font-weight: bold; }
.badge { background: #ff4d4f; color: white; padding: 2px 6px; border-radius: 4px; margin-right: 5px; font-size: 12px; }
.title-cell { cursor: pointer; text-align: left; }
</style>