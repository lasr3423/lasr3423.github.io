<template>
  <section class="mx-auto max-w-xl rounded-[2rem] border border-slate-200 bg-white p-8 text-center shadow-sm">
    <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 text-2xl text-emerald-600">
      ✓
    </div>
    <p class="mt-6 text-sm font-semibold uppercase tracking-[0.2em] text-emerald-600">결제 완료</p>
    <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">주문을 확인하고 있습니다</h1>
    <p class="mt-3 text-sm leading-6 text-slate-500">
      결제 결과를 확인한 뒤 주문 내역으로 이동합니다. 잠시만 기다려주세요.
    </p>
  </section>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '@/api/axios';

const route  = useRoute();
const router = useRouter();

onMounted(async () => {
  const { paymentKey, orderId, amount } = route.query;

  try {
    await axios.post('/api/order/payment/confirm', {
      paymentKey,
      orderId: Number(orderId),
      amount:  Number(amount),
    });
    router.push('/mypage/order');
  } catch (e) {
    console.error('결제 확인 실패', e);
    router.push('/payment/fail');
  }
});
</script>
