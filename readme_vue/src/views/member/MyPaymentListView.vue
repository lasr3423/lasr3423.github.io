<template>
  <div class="space-y-6">
    <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <h1 class="text-xl font-bold text-slate-800">결제 내역</h1>
      <p class="mt-1 text-sm text-slate-400">내가 결제한 내역을 확인할 수 있습니다.</p>
    </section>

    <div v-if="loading" class="flex justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

    <template v-else-if="payments.length > 0">
      <article
        v-for="p in payments"
        :key="p.paymentId"
        class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-xs text-slate-400">{{ formatDate(p.paidAt) }}</p>
            <p class="mt-1 text-sm font-medium text-slate-700">
              주문 <span class="font-mono text-slate-900">{{ p.orderNumber }}</span>
            </p>
            <p class="mt-0.5 text-xs text-slate-500">결제수단: {{ p.method ?? '-' }}</p>
          </div>
          <div class="text-right">
            <span
              class="inline-block rounded-full px-3 py-1 text-xs font-semibold"
              :class="statusClass(p.paymentStatus)"
            >
              {{ statusLabel(p.paymentStatus) }}
            </span>
            <p class="mt-2 text-lg font-bold text-brand-800">{{ p.amount?.toLocaleString() }}원</p>
          </div>
        </div>
        <div v-if="p.failureReason" class="mt-3 rounded-xl bg-rose-50 px-4 py-2 text-xs text-rose-600">
          실패 사유: {{ p.failureReason }}
        </div>
      </article>

      <div class="flex justify-center gap-2">
        <button :disabled="page === 0" @click="page--; fetchPayments()"
          class="rounded-xl border border-slate-200 px-4 py-2 text-sm disabled:opacity-40">이전</button>
        <span class="self-center text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="page++; fetchPayments()"
          class="rounded-xl border border-slate-200 px-4 py-2 text-sm disabled:opacity-40">다음</button>
      </div>
    </template>

    <div v-else class="rounded-2xl border border-slate-200 bg-white py-16 text-center text-sm text-slate-400">
      결제 내역이 없습니다.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { memberApi } from '@/api/member'

const payments   = ref([])
const loading    = ref(true)
const page       = ref(0)
const totalPages = ref(1)

const statusLabel = (s) => ({ READY: '결제준비', PAID: '결제완료', CANCELLED: '취소됨', FAILED: '실패' }[s] ?? s)
const statusClass = (s) => ({
  READY:     'bg-amber-50 text-amber-600',
  PAID:      'bg-emerald-50 text-emerald-600',
  CANCELLED: 'bg-slate-100 text-slate-500',
  FAILED:    'bg-rose-50 text-rose-600',
}[s] ?? 'bg-slate-100 text-slate-500')
const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' }) : '-'

async function fetchPayments() {
  loading.value = true
  try {
    const { data } = await memberApi.getPayments({ page: page.value, size: 10 })
    payments.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('결제 내역 로드 실패', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchPayments)
</script>
