<template>
  <section class="mx-auto max-w-xl rounded-[2rem] border border-rose-200 bg-white p-8 text-center shadow-sm">
    <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-rose-100 text-2xl text-rose-600">
      !
    </div>
    <p class="mt-6 text-sm font-semibold uppercase tracking-[0.2em] text-rose-600">Payment failed</p>
    <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">Returning to your cart</h1>
    <p class="mt-3 text-sm leading-6 text-slate-500">
      We are storing the failure reason and sending you back so you can retry your order.
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
  const { orderId, code, message } = route.query;

  await axios.post('/api/order/payment/fail', {
    orderId: Number(orderId),
    code,
    message,
  });

  router.push('/cart');
});
</script>
