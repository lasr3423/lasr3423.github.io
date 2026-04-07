<template>
  <section class="space-y-6">
    <div v-if="loading" class="rounded-[2rem] border border-dashed border-slate-300 bg-white px-6 py-16 text-center text-sm text-slate-500">
      Loading product...
    </div>
    <div v-else-if="error" class="rounded-[2rem] border border-rose-200 bg-rose-50 px-6 py-16 text-center text-sm text-rose-700">
      {{ error }}
    </div>

    <template v-else-if="product">
      <div class="grid gap-8 rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm lg:grid-cols-[minmax(280px,360px)_1fr]">
        <div class="overflow-hidden rounded-[1.75rem] bg-slate-100">
          <img :src="product.thumbnail || '/img/no-image.png'" :alt="product.title" class="aspect-[3/4] w-full object-cover">
        </div>

        <div class="space-y-6">
          <div class="space-y-2">
            <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Book detail</p>
            <h1 class="text-3xl font-bold tracking-tight text-slate-900">{{ product.title }}</h1>
            <p class="text-base text-slate-500">{{ product.author }}</p>
          </div>

          <div class="rounded-[1.5rem] bg-slate-50 p-5">
            <div class="flex flex-wrap items-center gap-3">
              <span class="text-sm text-slate-400 line-through">{{ product.price.toLocaleString() }} KRW</span>
              <span class="text-2xl font-bold text-accent-600">{{ product.discountRate }}%</span>
              <span class="text-3xl font-bold text-slate-900">{{ product.salePrice.toLocaleString() }} KRW</span>
            </div>
            <p class="mt-3 text-sm text-slate-500">
              Earn {{ Math.floor(product.salePrice * 0.1).toLocaleString() }} points on purchase.
            </p>
          </div>

          <div class="grid gap-3 rounded-[1.5rem] border border-slate-200 p-5 sm:grid-cols-2">
            <div class="rounded-2xl bg-slate-50 p-4">
              <p class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">Stock</p>
              <p class="mt-2 text-sm font-semibold text-slate-800">
                <span v-if="product.stock === 0" class="text-rose-600">Out of stock</span>
                <span v-else-if="product.stock <= 5" class="text-amber-600">Only {{ product.stock }} left</span>
                <span v-else>{{ product.stock }} available</span>
              </p>
            </div>
            <div class="rounded-2xl bg-slate-50 p-4">
              <p class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">Views</p>
              <p class="mt-2 text-sm font-semibold text-slate-800">{{ product.viewCount ?? '-' }}</p>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-4">
            <div class="inline-flex items-center overflow-hidden rounded-2xl border border-slate-200">
              <button class="px-4 py-3 text-lg text-slate-500 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-300" @click="decreaseQty" :disabled="quantity <= 1">
                -
              </button>
              <span class="min-w-14 border-x border-slate-200 px-4 py-3 text-center text-sm font-semibold text-slate-800">
                {{ quantity }}
              </span>
              <button class="px-4 py-3 text-lg text-slate-500 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-300" @click="increaseQty" :disabled="product.stock === 0">
                +
              </button>
            </div>

            <button class="rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400" @click="addToCart" :disabled="product.stock === 0">
              Add to cart
            </button>
          </div>
        </div>
      </div>

      <div class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex flex-wrap gap-2 border-b border-slate-200 pb-4">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="[
              'rounded-full px-4 py-2 text-sm font-semibold transition',
              activeTab === tab.key
                ? 'bg-brand-800 text-white'
                : 'bg-slate-100 text-slate-500 hover:text-brand-800'
            ]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="pt-6">
          <div v-if="activeTab === 'info'" class="text-sm leading-7 text-slate-600">
            <p>{{ product.description || 'No description is available yet.' }}</p>
          </div>
          <div v-else-if="activeTab === 'review'" class="rounded-2xl bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
            Review feature is not implemented yet.
          </div>
          <div v-else-if="activeTab === 'qna'" class="rounded-2xl bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
            QnA feature is not implemented yet.
          </div>
          <div v-else class="rounded-2xl bg-slate-50 px-4 py-8 text-sm leading-7 text-slate-600">
            <p>Fast delivery is available for qualified orders.</p>
            <p>Return and exchange costs depend on the final order status.</p>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { productApi } from '@/api/product';
import { useCartStore } from '@/store/cart';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();

const product = ref(null);
const loading = ref(false);
const error = ref(null);
const quantity = ref(1);

const tabs = [
  { key: 'info', label: 'Info' },
  { key: 'review', label: 'Review' },
  { key: 'qna', label: 'QnA' },
  { key: 'delivery', label: 'Delivery' },
];
const activeTab = ref('info');

function increaseQty() {
  if (product.value && quantity.value < product.value.stock) {
    quantity.value++;
  }
}

function decreaseQty() {
  if (quantity.value > 1) quantity.value--;
}

async function addToCart() {
  if (!authStore.isLoggedIn) {
    router.push('/signin');
    return;
  }
  try {
    await cartStore.addItem(product.value.id, quantity.value);
  } catch (e) {
    console.error(e);
  }
}

async function fetchProduct() {
  loading.value = true;
  error.value = null;
  try {
    const { data } = await productApi.getDetail(route.params.productId);
    product.value = data;
  } catch (e) {
    error.value = e.response?.status === 404
      ? 'This product could not be found.'
      : 'Failed to load the product.';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchProduct();
});
</script>
