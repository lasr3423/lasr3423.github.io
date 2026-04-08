<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">리뷰 관리</h1>
      <p class="mt-1 text-sm text-slate-400">부적절한 리뷰를 삭제할 수 있습니다.</p>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="reviews.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">별점</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">내용</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">상품 ID</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">작성일</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="r in reviews" :key="r.reviewId" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 text-center">
                  <span class="text-amber-400">{{ '★'.repeat(r.rating) }}</span>
                  <span class="text-slate-200">{{ '★'.repeat(5 - r.rating) }}</span>
                </td>
                <td class="px-6 py-4 max-w-xs">
                  <p class="line-clamp-2 text-slate-700">{{ r.content }}</p>
                </td>
                <td class="px-6 py-4 text-xs text-slate-400">{{ r.productId }}</td>
                <td class="px-6 py-4 text-xs text-slate-400">{{ formatDate(r.createdAt) }}</td>
                <td class="px-6 py-4 text-right">
                  <button
                    class="rounded-xl bg-rose-500 px-3 py-1.5 text-xs font-semibold text-white hover:bg-rose-600"
                    @click="deleteReview(r.reviewId)"
                  >
                    삭제
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchReviews()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchReviews()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">리뷰가 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const reviews    = ref([])
const loading    = ref(true)
const page       = ref(0)
const totalPages = ref(1)

const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR') : '-'

async function fetchReviews() {
  loading.value = true
  try {
    const { data } = await adminApi.getAdminReviews({ page: page.value, size: 20 })
    reviews.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('리뷰 로드 실패', e)
  } finally {
    loading.value = false
  }
}

async function deleteReview(id) {
  if (!confirm('이 리뷰를 삭제하시겠습니까?')) return
  try {
    await adminApi.deleteReview(id)
    fetchReviews()
  } catch (e) {
    alert(e.response?.data?.message || '삭제 실패')
  }
}

onMounted(fetchReviews)
</script>
