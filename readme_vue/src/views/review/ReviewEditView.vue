<template>
  <div class="page-wrap">
    <h2 class="page-title">리뷰 수정</h2>
    
    <form v-if="form" @submit.prevent="handleUpdate">
      <div class="form-group">
        <label class="form-label">평점</label>
        <div class="star-select">
          <span v-for="n in 5" :key="n" 
                class="star-pick" :class="{ filled: n <= form.rating }"
                @click="form.rating = n">★</span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">리뷰 내용</label>
        <textarea v-model="form.content" class="form-textarea" rows="6"></textarea>
      </div>

      <div class="form-actions">
        <button type="button" @click="$router.back()">취소</button>
        <button type="submit" class="btn-submit">수정 완료</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const router = useRouter()
const form = ref({ rating: 0, content: '' })

// 기존 리뷰 데이터 로드 (REQ-M-017 연관) [cite: 136, 137]
onMounted(async () => {
  try {
    const { data } = await axios.get(`/api/mypage/review/detail/${route.params.id}`)
    form.value = { rating: data.rating, content: data.content }
  } catch (e) {
    alert('데이터를 불러올 수 없습니다.')
    router.back()
  }
})

// 수정 요청 전송 (REQ-M-018) [cite: 138, 139]
const handleUpdate = async () => {
  try {
    await axios.patch(`/api/mypage/review/update/${route.params.id}`, form.value)
    alert('리뷰가 수정되었습니다.')
    router.push('/mypage/review/list')
  } catch (e) {
    alert('수정에 실패했습니다.')
  }
}
</script>