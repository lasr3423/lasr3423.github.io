<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">주문 승인 처리</h1>
      <p class="mt-1 text-sm text-slate-400">결제 완료(PAYED) 상태의 주문을 배송 준비(APPROVAL)로 승인합니다.</p>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="orders.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문번호</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">결제금액</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문일</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">승인</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="order in orders" :key="order.orderId" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 font-mono text-xs text-slate-600">{{ order.number }}</td>
                <td class="px-6 py-4 text-right font-semibold text-slate-900">{{ order.finalPrice?.toLocaleString() }}원</td>
                <td class="px-6 py-4 text-center">
                  <span class="rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">결제완료</span>
                </td>
                <td class="px-6 py-4 text-slate-500">{{ formatDate(order.orderAt) }}</td>
                <td class="px-6 py-4 text-center">
                  <button
                    class="rounded-xl bg-brand-800 px-4 py-1.5 text-xs font-semibold text-white transition hover:bg-brand-700"
                    @click="approve(order.orderId)"
                  >
                    배송 준비 승인
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchOrders()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchOrders()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">승인 대기 주문이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const orders     = ref([])
const loading    = ref(true)
const page       = ref(0)
const totalPages = ref(1)

const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR') : '-'

async function fetchOrders() {
  loading.value = true
  try {
    const { data } = await adminApi.getPendingApprovalOrders({ page: page.value, size: 20 })
    orders.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('승인 대기 주문 로드 실패', e)
  } finally {
    loading.value = false
  }
}

async function approve(orderId) {
  if (!confirm('이 주문을 배송 준비 상태로 승인하시겠습니까?')) return
  try {
    await adminApi.updateOrderStatus(orderId, 'APPROVAL')
    fetchOrders()
  } catch (e) {
    alert(e.response?.data?.message || '승인 처리 실패')
  }
}

onMounted(fetchOrders)
</script>
