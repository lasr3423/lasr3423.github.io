<template>
  <div class="space-y-6">
    <div class="flex items-center gap-3">
      <router-link to="/mypage/order" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-50">← 목록으로</router-link>
      <section class="rounded-[2rem] border border-slate-200 bg-white px-6 py-4 shadow-sm flex-1">
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Order Detail</p>
      </section>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

    <template v-else-if="order">
      <!-- 주문 상태 -->
      <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="font-mono text-xs text-slate-400">{{ order.number }}</p>
            <p class="mt-1 text-xl font-bold text-slate-900">주문 상세</p>
          </div>
          <span :class="statusClass(order.orderStatus)" class="rounded-full px-4 py-1.5 text-sm font-semibold">
            {{ statusLabel(order.orderStatus) }}
          </span>
        </div>
        <p class="mt-2 text-sm text-slate-500">주문일: {{ formatDate(order.orderAt) }}</p>
      </section>

      <!-- 주문 상품 -->
      <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
        <h2 class="mb-4 text-base font-bold text-slate-900">주문 상품</h2>
        <div class="divide-y divide-slate-100">
          <div v-for="item in order.items" :key="item.id" class="flex items-start justify-between py-4">
            <div>
              <p class="font-semibold text-slate-900">{{ item.productTitle }}</p>
              <p class="mt-0.5 text-sm text-slate-500">{{ item.productAuthor }}</p>
              <p class="mt-1 text-xs text-slate-400">수량: {{ item.quantity }}권</p>
            </div>
            <p class="text-sm font-semibold text-slate-900">{{ item.itemTotal.toLocaleString() }}원</p>
          </div>
        </div>
      </section>

      <!-- 결제 정보 -->
      <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
        <h2 class="mb-4 text-base font-bold text-slate-900">결제 정보</h2>
        <div class="space-y-2 text-sm">
          <div class="flex justify-between text-slate-600">
            <span>상품 합계</span><span>{{ order.totalPrice.toLocaleString() }}원</span>
          </div>
          <div class="flex justify-between text-accent-600">
            <span>할인 금액</span><span>- {{ order.discountAmount.toLocaleString() }}원</span>
          </div>
          <div class="flex justify-between border-t border-slate-200 pt-2 text-base font-bold text-slate-900">
            <span>최종 결제</span><span>{{ order.finalPrice.toLocaleString() }}원</span>
          </div>
        </div>
      </section>

      <!-- 배송 정보 -->
      <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
        <h2 class="mb-4 text-base font-bold text-slate-900">배송 정보</h2>
        <div class="grid grid-cols-1 gap-3 text-sm sm:grid-cols-2">
          <div>
            <p class="text-xs font-medium uppercase tracking-widest text-slate-400">받는 분</p>
            <p class="mt-1 text-slate-800">{{ order.receiverName }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase tracking-widest text-slate-400">연락처</p>
            <p class="mt-1 text-slate-800">{{ order.receiverPhone }}</p>
          </div>
          <div class="sm:col-span-2">
            <p class="text-xs font-medium uppercase tracking-widest text-slate-400">주소</p>
            <p class="mt-1 text-slate-800">
              [{{ order.deliveryZipCode }}] {{ order.deliveryAddress }} {{ order.deliveryAddressDetail }}
            </p>
          </div>
          <div v-if="order.deliveryMemo" class="sm:col-span-2">
            <p class="text-xs font-medium uppercase tracking-widest text-slate-400">배송 메모</p>
            <p class="mt-1 text-slate-800">{{ order.deliveryMemo }}</p>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { memberApi } from '@/api/member';

const route = useRoute();
const order = ref(null);
const loading = ref(true);

const statusLabel = (s) => ({ PENDING: '결제대기', PAYED: '결제완료', APPROVAL: '배송중', CANCELED: '취소' }[s] ?? s);
const statusClass = (s) => ({
  PENDING:  'bg-yellow-50 text-yellow-700',
  PAYED:    'bg-green-50 text-green-700',
  APPROVAL: 'bg-brand-50 text-brand-700',
  CANCELED: 'bg-rose-50 text-rose-700',
}[s] ?? 'bg-slate-100 text-slate-600');
const formatDate = (d) => d ? new Date(d).toLocaleString('ko-KR') : '-';

onMounted(async () => {
  try {
    const { data } = await memberApi.getOrder(route.params.orderId);
    order.value = data;
  } catch (e) {
    console.error('주문 상세 로드 실패:', e);
  } finally {
    loading.value = false;
  }
});
</script>
