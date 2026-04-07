<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">상품 등록</h1>
    </section>

    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <form class="max-w-2xl space-y-5" @submit.prevent="handleSubmit">
        <div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
          <label class="block space-y-2 sm:col-span-2">
            <span class="text-sm font-medium text-slate-700">도서명 *</span>
            <input v-model="form.title" type="text" required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">저자 *</span>
            <input v-model="form.author" type="text" required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">썸네일 URL</span>
            <input v-model="form.thumbnail" type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">정가 (원) *</span>
            <input v-model.number="form.price" type="number" min="0" required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">할인율 (%)</span>
            <input v-model.number="form.discountRate" type="number" min="0" max="100" step="0.1"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">재고 수량 *</span>
            <input v-model.number="form.stock" type="number" min="0" required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">대분류 카테고리 ID *</span>
            <input v-model.number="form.categoryTopId" type="number" min="1" required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">소분류 카테고리 ID *</span>
            <input v-model.number="form.categorySubId" type="number" min="1" required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>

          <label class="block space-y-2 sm:col-span-2">
            <span class="text-sm font-medium text-slate-700">도서 소개</span>
            <textarea v-model="form.description" rows="5"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100 resize-none"></textarea>
          </label>
        </div>

        <!-- 판매가 미리보기 -->
        <div class="rounded-2xl bg-brand-50 px-5 py-4 text-sm">
          <span class="text-brand-700 font-medium">예상 판매가:</span>
          <span class="ml-2 text-lg font-bold text-brand-900">{{ previewSalePrice.toLocaleString() }}원</span>
          <span class="ml-1 text-xs text-brand-600">({{ form.discountRate }}% 할인)</span>
        </div>

        <p v-if="errorMsg"   class="rounded-2xl border border-rose-200  bg-rose-50  px-4 py-3 text-sm text-rose-700">{{ errorMsg }}</p>
        <p v-if="successMsg" class="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{{ successMsg }}</p>

        <div class="flex gap-3 pt-2">
          <button type="submit" :disabled="loading"
            class="rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:bg-slate-400">
            {{ loading ? '등록 중...' : '상품 등록' }}
          </button>
          <router-link to="/admin/product/list"
            class="rounded-2xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50">
            취소
          </router-link>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { adminApi } from '@/api/admin';

const router = useRouter();
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

const form = ref({
  title: '', author: '', description: '', thumbnail: '',
  price: 0, discountRate: 0, stock: 0,
  categoryTopId: null, categorySubId: null,
});

const previewSalePrice = computed(() => {
  const rate = form.value.discountRate || 0;
  return Math.floor(form.value.price * (1 - rate / 100));
});

async function handleSubmit() {
  try {
    loading.value = true;
    errorMsg.value = '';
    await adminApi.createProduct(form.value);
    successMsg.value = '상품이 등록되었습니다.';
    setTimeout(() => router.push('/admin/product/list'), 1000);
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '상품 등록에 실패했습니다.';
  } finally {
    loading.value = false;
  }
}
</script>
