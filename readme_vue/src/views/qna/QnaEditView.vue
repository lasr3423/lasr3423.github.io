<template>
  <div class="page-wrap">
    <h2 class="page-title">문의 내용 수정</h2>
    
    <form v-if="qna" @submit.prevent="handleUpdate">
      <div class="form-group">
        <label class="form-label">문의 카테고리</label>
        <select v-model="qna.category" class="form-select">
          <option value="DELIVERY">배송</option>
          <option value="PRODUCT">상품</option>
          <option value="CANCEL">반품/취소</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">제목</label>
        <input v-model="qna.title" class="form-input" type="text" />
      </div>

      <div class="form-group">
        <label class="form-label">내용</label>
        <textarea v-model="qna.content" class="form-textarea" rows="8"></textarea>
      </div>

      <div class="form-actions">
        <button type="button" @click="$router.back()">취소</button>
        <button type="submit" class="btn-submit">수정하기</button>
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
const qna = ref(null)

onMounted(async () => {
  try {
    const { data } = await axios.get(`/api/mypage/qna/detail/${route.params.id}`)
    
    // 답변 완료 상태 체크 (REQ-M-023: 답변 완료 후 수정 불가) 
    if (data.qnaStatus === 'COMPLETE') {
      alert('답변이 완료된 문의는 수정할 수 없습니다.')
      return router.back()
    }
    qna.value = data
  } catch (e) {
    alert('문의 글을 찾을 수 없습니다.')
    router.back()
  }
})

const handleUpdate = async () => {
  try {
    // PATCH /mypage/qna/update/{id} 호출 [cite: 149]
    await axios.patch(`/api/mypage/qna/update/${route.params.id}`, {
      title: qna.value.title,
      content: qna.value.content,
      category: qna.value.category
    })
    alert('문의가 성공적으로 수정되었습니다.')
    router.push('/mypage/qna/list') // [cite: 145]
  } catch (e) {
    alert('수정 처리 중 오류가 발생했습니다.')
  }
}
</script>