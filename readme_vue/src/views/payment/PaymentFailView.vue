<template>
  <section class="mx-auto max-w-xl rounded-[2rem] border border-rose-200 bg-white p-8 text-center shadow-sm">
    <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-rose-100 text-2xl text-rose-600">
      !
    </div>
    <p class="mt-6 text-sm font-semibold uppercase tracking-[0.2em] text-rose-600">결제 실패</p>
    <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">장바구니로 돌아갑니다</h1>
    <p class="mt-3 text-sm leading-6 text-slate-500">
      결제 실패 정보를 저장하고 다시 시도할 수 있도록 장바구니로 이동합니다.
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
  const { orderId, code, message } = route.query;

  try {
    await axios.post('/api/order/payment/fail', {
      orderId: Number(orderId),
      code,
      message,
    });
  } catch (e) {
    console.error('결제 실패 처리 오류', e);
  } finally {
    router.push('/cart');
  }
});
</script>
