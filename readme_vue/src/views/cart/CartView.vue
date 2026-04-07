<template>
  <section class="space-y-6">
    <div class="flex flex-col gap-2">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Cart</p>
      <h1 class="text-3xl font-bold tracking-tight text-slate-900">Your shopping bag</h1>
    </div>

    <div v-if="cartStore.loading" class="rounded-[2rem] border border-dashed border-slate-300 bg-white px-6 py-16 text-center text-sm text-slate-500">
      Loading cart...
    </div>
    <div v-else-if="cartStore.error" class="rounded-[2rem] border border-rose-200 bg-rose-50 px-6 py-16 text-center text-sm text-rose-700">
      {{ cartStore.error }}
    </div>
    <div v-else-if="cartStore.items.length === 0" class="rounded-[2rem] border border-dashed border-slate-300 bg-white px-6 py-16 text-center">
      <p class="text-sm text-slate-500">Your cart is empty.</p>
      <button class="mt-4 rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700" @click="router.push('/product')">
        Continue shopping
      </button>
    </div>

    <div v-else class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
      <div class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-6 py-4">
          <label class="flex items-center gap-3 text-sm font-medium text-slate-700">
            <input type="checkbox" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" :checked="isAllChecked" @change="toggleAll">
            Select all ({{ cartStore.items.length }})
          </label>
          <button class="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-500 transition hover:border-rose-200 hover:bg-rose-50 hover:text-rose-700" @click="removeCheckedItems">
            Remove selected
          </button>
        </div>

        <div class="divide-y divide-slate-200">
          <article v-for="item in cartStore.items" :key="item.cartItemId" class="grid gap-4 px-6 py-5 md:grid-cols-[auto_minmax(0,1fr)_auto_auto]">
            <label class="flex items-start pt-2">
              <input type="checkbox" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" :checked="item.isChecked" @change="cartStore.toggleCheck(item.cartItemId)">
            </label>

            <div class="flex min-w-0 gap-4">
              <img :src="item.thumbnail || '/img/no-image.png'" :alt="item.title" class="h-28 w-20 cursor-pointer rounded-2xl object-cover" @click="router.push(`/product/${item.productId}`)">
              <div class="min-w-0 space-y-1">
                <p class="cursor-pointer truncate text-base font-semibold text-slate-900 hover:text-brand-700" @click="router.push(`/product/${item.productId}`)">
                  {{ item.title }}
                </p>
                <p class="text-sm text-slate-500">{{ item.author }}</p>
                <p class="text-sm font-medium text-brand-700">Fast delivery</p>
              </div>
            </div>

            <div class="flex items-center text-sm font-semibold text-slate-700 md:justify-center">
              {{ item.salePrice.toLocaleString() }} KRW
            </div>

            <div class="flex flex-col items-start gap-3 md:items-end">
              <div class="inline-flex items-center overflow-hidden rounded-2xl border border-slate-200">
                <button class="px-3 py-2 text-lg text-slate-500 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-300" @click="handleUpdateQty(item, item.quantity - 1)" :disabled="item.quantity <= 1">
                  -
                </button>
                <span class="min-w-12 border-x border-slate-200 px-3 py-2 text-center text-sm font-semibold text-slate-800">
                  {{ item.quantity }}
                </span>
                <button class="px-3 py-2 text-lg text-slate-500 transition hover:bg-slate-100" @click="handleUpdateQty(item, item.quantity + 1)">
                  +
                </button>
              </div>
              <p class="text-sm font-bold text-slate-900">
                {{ (item.salePrice * item.quantity).toLocaleString() }} KRW
              </p>
            </div>
          </article>
        </div>
      </div>

      <aside class="h-fit rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm xl:sticky xl:top-24">
        <h2 class="text-lg font-bold text-slate-900">Order summary</h2>

        <div class="mt-6 space-y-4 text-sm text-slate-600">
          <div class="flex items-center justify-between">
            <span>Original price</span>
            <span>{{ cartStore.checkedOriginalTotal.toLocaleString() }} KRW</span>
          </div>
          <div class="flex items-center justify-between">
            <span>Discount</span>
            <span class="text-brand-700">0 KRW</span>
          </div>
          <div class="flex items-center justify-between">
            <span>Delivery</span>
            <span class="text-emerald-600">Free</span>
          </div>
        </div>

        <div class="mt-6 border-t border-slate-200 pt-6">
          <div class="flex items-center justify-between">
            <span class="text-sm font-semibold text-slate-700">Total</span>
            <span class="text-2xl font-bold text-brand-800">{{ cartStore.checkedTotal.toLocaleString() }} KRW</span>
          </div>
          <p class="mt-2 text-xs text-slate-400">{{ checkedCount }} item(s) selected</p>
        </div>

        <button class="mt-6 w-full rounded-2xl bg-brand-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400" :disabled="checkedCount === 0" @click="goToOrder">
          Continue to order
        </button>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/store/cart';
import { useOrderStore } from '@/store/order';

const router = useRouter();
const cartStore = useCartStore();
const orderStore = useOrderStore();

const isAllChecked = computed(() =>
  cartStore.items.length > 0 && cartStore.items.every(i => i.isChecked)
);

const checkedCount = computed(() =>
  cartStore.items.filter(i => i.isChecked).length
);

function toggleAll(e) {
  cartStore.toggleAll(e.target.checked);
}

async function handleUpdateQty(item, newQty) {
  if (newQty < 1) {
    await cartStore.removeItem(item.cartItemId);
    return;
  }
  try {
    await cartStore.updateItem(item.cartItemId, newQty);
  } catch (e) {
    console.error(e);
  }
}

async function removeCheckedItems() {
  const checked = cartStore.items.filter(i => i.isChecked);
  for (const item of checked) {
    await cartStore.removeItem(item.cartItemId);
  }
}

function goToOrder() {
  if (checkedCount.value === 0) return;
  const checkedItems = cartStore.items.filter(i => i.isChecked);
  const itemName = checkedItems.length === 1
    ? checkedItems[0].title
    : `${checkedItems[0].title} and ${checkedItems.length - 1} more`;

  orderStore.setOrder(null, cartStore.checkedTotal, itemName);

  const ids = checkedItems.map(i => i.cartItemId).join(',');
  router.push({ path: '/order', query: { cartItemIds: ids } });
}

onMounted(() => {
  cartStore.fetchCart();
});
</script>
