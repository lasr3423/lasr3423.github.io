<template>
  <section class="space-y-6">
    <div class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Catalog</p>
          <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">Book list</h1>
        </div>

        <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tab in categoryTabs"
              :key="tab.value"
              :class="[
                'rounded-full px-4 py-2 text-sm font-medium transition',
                selectedCategory === tab.value
                  ? 'bg-brand-800 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-brand-50 hover:text-brand-800'
              ]"
              @click="selectCategory(tab.value)"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="flex flex-col gap-2 sm:flex-row">
            <select
              v-model="searchType"
              class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600 outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            >
              <option value="title">Title</option>
              <option value="author">Author</option>
            </select>
            <input
              v-model="searchKeyword"
              class="min-w-[220px] rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
              type="text"
              placeholder="Search for a book"
              @keyup.enter="handleSearch"
            >
            <button
              class="rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
              @click="handleSearch"
            >
              Search
            </button>
          </div>
        </div>
      </div>

      <div class="mt-5 flex flex-wrap gap-2">
        <button
          v-for="sub in subCategoryTabs"
          :key="sub.value"
          :class="[
            'rounded-full border px-4 py-2 text-xs font-semibold uppercase tracking-[0.16em] transition',
            selectedSubCategory === sub.value
              ? 'border-brand-800 bg-brand-50 text-brand-800'
              : 'border-slate-200 bg-white text-slate-500 hover:border-brand-200 hover:text-brand-700'
          ]"
          @click="selectSubCategory(sub.value)"
        >
          {{ sub.label }}
        </button>
      </div>
    </div>

    <div v-if="productStore.loading" class="rounded-[2rem] border border-dashed border-slate-300 bg-white px-6 py-16 text-center text-sm text-slate-500">
      Loading books...
    </div>

    <div v-else-if="productStore.error" class="rounded-[2rem] border border-rose-200 bg-rose-50 px-6 py-16 text-center text-sm text-rose-700">
      {{ productStore.error }}
    </div>

    <div v-else-if="productStore.products.length === 0" class="rounded-[2rem] border border-dashed border-slate-300 bg-white px-6 py-16 text-center text-sm text-slate-500">
      No products found.
    </div>

    <div v-else class="grid gap-5 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
      <article
        v-for="product in productStore.products"
        :key="product.id"
        class="group overflow-hidden rounded-[1.75rem] border border-slate-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-xl"
      >
        <div class="relative">
          <span
            v-if="product.salesCount > 10000"
            class="absolute left-4 top-4 rounded-full bg-brand-800 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.16em] text-white"
          >
            Best
          </span>
          <span
            v-else-if="Number(product.discountRate) >= 40"
            class="absolute left-4 top-4 rounded-full bg-accent-500 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.16em] text-white"
          >
            Sale
          </span>

          <img
            :src="product.thumbnail || '/img/no-image.png'"
            :alt="product.title"
            class="aspect-[3/4] w-full cursor-pointer object-cover transition duration-300 group-hover:scale-[1.03]"
            @click="goToDetail(product.id)"
          >
        </div>

        <div class="space-y-3 p-5">
          <div class="space-y-1">
            <p class="line-clamp-2 cursor-pointer text-base font-semibold text-slate-900 transition hover:text-brand-700" @click="goToDetail(product.id)">
              {{ product.title }}
            </p>
            <p class="text-sm text-slate-500">{{ product.author }}</p>
          </div>

          <div class="flex items-end gap-2">
            <span class="text-lg font-bold text-accent-600">{{ product.discountRate }}%</span>
            <span class="text-xl font-bold text-slate-900">{{ product.salePrice.toLocaleString() }} KRW</span>
          </div>
          <p class="text-sm text-slate-400 line-through">{{ product.price.toLocaleString() }} KRW</p>

          <button
            class="w-full rounded-2xl bg-brand-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
            @click="addToCart(product.id)"
          >
            Add to cart
          </button>
        </div>
      </article>
    </div>

    <div v-if="productStore.totalPages > 1" class="flex flex-wrap items-center justify-center gap-2">
      <button class="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-500 transition hover:border-brand-200 hover:text-brand-800 disabled:cursor-not-allowed disabled:opacity-40" @click="changePage(0)" :disabled="productStore.currentPage === 0">
        First
      </button>
      <button class="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-500 transition hover:border-brand-200 hover:text-brand-800 disabled:cursor-not-allowed disabled:opacity-40" @click="changePage(productStore.currentPage - 1)" :disabled="productStore.currentPage === 0">
        Prev
      </button>

      <button
        v-for="p in pageRange"
        :key="p"
        :class="[
          'rounded-full px-4 py-2 text-sm font-semibold transition',
          p === productStore.currentPage
            ? 'bg-brand-800 text-white'
            : 'border border-slate-200 bg-white text-slate-500 hover:border-brand-200 hover:text-brand-800'
        ]"
        @click="changePage(p)"
      >
        {{ p + 1 }}
      </button>

      <button class="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-500 transition hover:border-brand-200 hover:text-brand-800 disabled:cursor-not-allowed disabled:opacity-40" @click="changePage(productStore.currentPage + 1)" :disabled="productStore.currentPage === productStore.totalPages - 1">
        Next
      </button>
      <button class="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm text-slate-500 transition hover:border-brand-200 hover:text-brand-800 disabled:cursor-not-allowed disabled:opacity-40" @click="changePage(productStore.totalPages - 1)" :disabled="productStore.currentPage === productStore.totalPages - 1">
        Last
      </button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useProductStore } from '@/store/product';
import { useCartStore } from '@/store/cart';
import { useAuthStore } from '@/store/auth';

const router = useRouter();
const route = useRoute();
const productStore = useProductStore();
const cartStore = useCartStore();
const authStore = useAuthStore();

const selectedCategory = ref('ALL');
const selectedSubCategory = ref('ALL');
const searchKeyword = ref('');
const searchType = ref('title');

const categoryTabs = [
  { label: 'All', value: 'ALL' },
  { label: 'Domestic', value: 'DOMESTIC' },
  { label: 'Foreign', value: 'FOREIGN' },
];

const subCategoryTabs = [
  { label: 'All', value: 'ALL' },
  { label: 'Novel', value: 'NOVEL' },
  { label: 'Self help', value: 'SELF_HELP' },
  { label: 'Computer', value: 'COMPUTER' },
  { label: 'Hobby', value: 'HOBBY' },
  { label: 'Travel', value: 'TRAVEL' },
  { label: 'Language', value: 'FOREIGN_LANG' },
];

const pageRange = computed(() => {
  const current = productStore.currentPage;
  const total = productStore.totalPages;
  const start = Math.max(0, current - 2);
  const end = Math.min(total - 1, current + 2);
  const range = [];
  for (let i = start; i <= end; i++) range.push(i);
  return range;
});

function selectCategory(value) {
  selectedCategory.value = value;
  productStore.fetchProducts(0);
}

function selectSubCategory(value) {
  selectedSubCategory.value = value;
  productStore.fetchProducts(0);
}

function handleSearch() {
  productStore.fetchProducts(0);
}

function changePage(page) {
  if (page < 0 || page >= productStore.totalPages) return;
  productStore.fetchProducts(page);
  window.scrollTo(0, 0);
}

function goToDetail(productId) {
  router.push(`/product/${productId}`);
}

async function addToCart(productId) {
  if (!authStore.isLoggedIn) {
    router.push('/signin');
    return;
  }
  try {
    await cartStore.addItem(productId, 1);
  } catch (e) {
    console.error(e);
  }
}

watch(
  () => route.query.keyword,
  (keyword) => {
    if (keyword) {
      searchKeyword.value = keyword;
      productStore.fetchProducts(0);
    }
  }
);

onMounted(() => {
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword;
  }
  productStore.fetchProducts(0);
});
</script>
