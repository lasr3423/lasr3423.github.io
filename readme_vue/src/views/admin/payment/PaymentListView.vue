<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">결제 내역 관리</h1>
      <p class="mt-1 text-sm text-slate-400">결제 수단과 처리 상태를 확인하고 이상 거래 여부를 빠르게 점검합니다.</p>
    </section>

    <!-- 필터 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <select v-model="statusFilter"
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400">
          <option value="">전체 상태</option>
          <option value="READY">결제준비</option>
          <option value="PAID">결제완료</option>
          <option value="CANCELLED">취소</option>
          <option value="FAILED">실패</option>
        </select>
        <button @click="search"
          class="rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700">
          조회
        </button>
      </div>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="payments.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">결제 ID</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문번호</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">결제수단</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">금액</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">결제일시</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="p in payments" :key="p.paymentId" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 text-xs text-slate-400">{{ p.paymentId }}</td>
                <td class="px-6 py-4 font-mono text-xs text-slate-600">{{ p.orderNumber }}</td>
                <td class="px-6 py-4 text-center text-xs text-slate-500">{{ p.paymentProvider ?? '-' }}</td>
                <td class="px-6 py-4 text-right font-semibold text-slate-900">{{ p.amount?.toLocaleString() }}원</td>
                <td class="px-6 py-4 text-center">
                  <span :class="statusClass(p.paymentStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ statusLabel(p.paymentStatus) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-xs text-slate-500">{{ formatDate(p.paidAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchPayments()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchPayments()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">결제 내역이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const payments     = ref([])
const loading      = ref(true)
const statusFilter = ref('')
const page         = ref(0)
const totalPages   = ref(1)

const statusLabel = (s) => ({ READY: '준비', PAID: '완료', CANCELLED: '취소', FAILED: '실패' }[s] ?? s)
const statusClass = (s) => ({
  READY:     'bg-amber-50 text-amber-700',
  PAID:      'bg-emerald-50 text-emerald-700',
  CANCELLED: 'bg-slate-100 text-slate-500',
  FAILED:    'bg-rose-50 text-rose-700',
}[s] ?? 'bg-slate-100 text-slate-500')
const formatDate = (d) => d ? new Date(d).toLocaleString('ko-KR') : '-'

async function fetchPayments() {
  loading.value = true
  try {
    const { data } = await adminApi.getPayments({ page: page.value, size: 20, status: statusFilter.value || undefined })
    payments.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('결제 내역 로드 실패', e)
  } finally {
    loading.value = false
  }
}

function search() { page.value = 0; fetchPayments() }
onMounted(fetchPayments)
</script>
