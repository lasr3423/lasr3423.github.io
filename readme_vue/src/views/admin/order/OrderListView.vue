<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">주문 관리</h1>
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
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">현재 상태</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문일</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">상태 변경</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="order in orders" :key="order.id" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 font-mono text-xs text-slate-600">{{ order.number }}</td>
                <td class="px-6 py-4 text-right font-semibold text-slate-900">{{ order.finalPrice.toLocaleString() }}원</td>
                <td class="px-6 py-4 text-center">
                  <span :class="statusClass(order.orderStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ statusLabel(order.orderStatus) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-slate-500">{{ formatDate(order.orderAt) }}</td>
                <td class="px-6 py-4 text-right">
                  <select :value="order.orderStatus"
                    class="rounded-xl border border-slate-200 bg-white px-2 py-1.5 text-xs"
                    @change="handleStatusChange(order.id, $event.target.value)">
                    <option value="PENDING">결제대기</option>
                    <option value="PAYED">결제완료</option>
                    <option value="APPROVAL">배송중</option>
                    <option value="CANCELED">취소</option>
                  </select>
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

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">주문이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { adminApi } from '@/api/admin';

const orders = ref([]);
const loading = ref(true);
const page = ref(0);
const totalPages = ref(1);

const statusLabel = (s) => ({ PENDING: '결제대기', PAYED: '결제완료', APPROVAL: '배송중', CANCELED: '취소' }[s] ?? s);
const statusClass = (s) => ({
  PENDING:  'bg-yellow-50 text-yellow-700',
  PAYED:    'bg-green-50 text-green-700',
  APPROVAL: 'bg-brand-50 text-brand-700',
  CANCELED: 'bg-rose-50 text-rose-700',
}[s] ?? 'bg-slate-100 text-slate-600');
const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR') : '-';

async function fetchOrders() {
  try {
    loading.value = true;
    const { data } = await adminApi.getOrders({ page: page.value, size: 20 });
    orders.value = data.content;
    totalPages.value = data.totalPages || 1;
  } catch (e) {
    console.error('주문 목록 로드 실패:', e);
  } finally {
    loading.value = false;
  }
}

async function handleStatusChange(id, status) {
  try {
    await adminApi.updateOrderStatus(id, status);
    fetchOrders();
  } catch (e) {
    alert(e.response?.data?.message || '상태 변경 실패');
  }
}

onMounted(fetchOrders);
</script>
