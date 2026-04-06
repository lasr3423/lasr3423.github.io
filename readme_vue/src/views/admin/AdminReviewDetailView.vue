<template>
  <div class="admin-page">
    <h2>관리자 리뷰 상세 제어</h2>
    
    <div v-if="review" class="detail-card">
      <div class="info-row">
        <span><strong>작성자:</strong> {{ review.memberName }}</span>
        <span><strong>작성일:</strong> {{ review.createdAt }}</span>
      </div>
      <div class="info-row">
        <span><strong>평점:</strong> {{ review.rating }} / 5</span>
        <span><strong>조회수:</strong> {{ review.hits }}</span>
      </div>
      
      <div class="content-box">
        {{ review.content }}
      </div>

      <div v-if="review.imageUrls?.length" class="admin-images">
        <img v-for="url in review.imageUrls" :key="url" :src="url" alt="리뷰 이미지" />
      </div>

      <div class="admin-actions">
        <button class="btn-list" @click="$router.push('/admin/review')">목록으로</button>
        <button class="btn-admin-delete" @click="handleAdminDelete">강제 삭제 (Soft Delete)</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()
const review = ref(null)

onMounted(async () => {
  try {
    // FA-030: 리뷰 상세 및 이미지 조회 [cite: 74]
    const { data } = await axios.get(`/api/admin/review/detail/${route.params.id}`)
    review.value = data
  } catch (e) {
    alert('데이터를 불러올 수 없습니다.')
  }
})

const handleAdminDelete = async () => {
  if (!confirm('정책 위반 등의 사유로 이 리뷰를 강제 삭제하시겠습니까?')) return

  try {
    // FA-031: 관리자 리뷰 삭제 (deleted_at 기록) [cite: 76]
    await axios.delete(`/api/admin/review/${review.value.id}`)
    alert('삭제 처리가 완료되었습니다.')
    router.push('/admin/review')
  } catch (e) {
    alert('삭제 권한이 없거나 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.detail-card { background: #fff; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
.info-row { display: flex; justify-content: space-between; margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px dashed #eee; }
.content-box { min-height: 150px; padding: 15px; background: #f9f9f9; margin-bottom: 20px; }
.admin-images img { width: 100px; height: 100px; object-fit: cover; margin-right: 10px; }
.admin-actions { display: flex; gap: 10px; margin-top: 20px; }
.btn-admin-delete { background: #d9534f; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
</style>