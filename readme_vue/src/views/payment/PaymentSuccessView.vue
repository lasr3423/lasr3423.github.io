<template>
  <section class="mx-auto max-w-xl rounded-[2rem] border border-slate-200 bg-white p-8 text-center shadow-sm">
    <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 text-2xl text-emerald-600">
      ✓
    </div>
    <p class="mt-6 text-sm font-semibold uppercase tracking-[0.2em] text-emerald-600">Payment completed</p>
    <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">Confirming your order</h1>
    <p class="mt-3 text-sm leading-6 text-slate-500">
      We are finalizing the payment result and moving you to your order history.
    </p>
  </section>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '@/api/axios';

const route = useRoute();
const router = useRouter();

onMounted(async () => {
  const { paymentKey, orderId, amount } = route.query;

  try {
    await axios.post('/api/order/payment/confirm', {
      paymentKey,
      orderId: Number(orderId),
      amount: Number(amount),
    });
    router.push('/mypage/order');
  } catch (e) {
    router.push('/cart');
  }
});
</script>
