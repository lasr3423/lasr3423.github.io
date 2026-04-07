<template>
  <div class="space-y-6">
    <section class="flex items-center justify-between rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <div>
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">상품 관리</h1>
      </div>
      <router-link to="/admin/product/insert"
        class="rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700">
        + 상품 등록
      </router-link>
    </section>

    <!-- 필터 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <select v-model="statusFilter" @change="search"
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400">
          <option value="">전체 상태</option>
          <option value="ACTIVATE">판매중</option>
          <option value="DEACTIVATE">판매중단</option>
          <option value="DELETE">삭제</option>
        </select>
      </div>
    </section>

    <!-- 테이블 -->
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="products.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">도서명</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">저자</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">판매가</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">재고</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">판매량</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="p in products" :key="p.id" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 font-semibold text-slate-900 max-w-xs truncate">{{ p.title }}</td>
                <td class="px-6 py-4 text-slate-600">{{ p.author }}</td>
                <td class="px-6 py-4 text-right text-slate-700">{{ p.salePrice.toLocaleString() }}원</td>
                <td class="px-6 py-4 text-right">
                  <span :class="p.stock < 10 ? 'text-rose-600 font-semibold' : 'text-slate-700'">{{ p.stock }}</span>
                </td>
                <td class="px-6 py-4 text-right text-slate-600">{{ p.salesCount }}</td>
                <td class="px-6 py-4 text-center">
                  <span :class="productStatusClass(p.productStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ productStatusLabel(p.productStatus) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-right">
                  <button class="rounded-xl bg-rose-50 px-3 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100"
                    @click="handleDelete(p.id)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchProducts()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchProducts()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">상품이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { adminApi } from '@/api/admin';

const products = ref([]);
const loading = ref(true);
const statusFilter = ref('');
const page = ref(0);
const totalPages = ref(1);

const productStatusLabel = (s) => ({ ACTIVATE: '판매중', DEACTIVATE: '판매중단', DELETE: '삭제' }[s] ?? s);
const productStatusClass = (s) => ({
  ACTIVATE:   'bg-green-50 text-green-700',
  DEACTIVATE: 'bg-yellow-50 text-yellow-700',
  DELETE:     'bg-rose-50 text-rose-700',
}[s] ?? 'bg-slate-100 text-slate-600');

async function fetchProducts() {
  try {
    loading.value = true;
    const { data } = await adminApi.getProducts({ page: page.value, size: 20, status: statusFilter.value || undefined });
    products.value = data.content;
    totalPages.value = data.totalPages || 1;
  } catch (e) {
    console.error('상품 목록 로드 실패:', e);
  } finally {
    loading.value = false;
  }
}

function search() { page.value = 0; fetchProducts(); }

async function handleDelete(id) {
  if (!confirm('상품을 삭제하시겠습니까?')) return;
  try {
    await adminApi.deleteProduct(id);
    fetchProducts();
  } catch (e) {
    alert(e.response?.data?.message || '삭제 실패');
  }
}

onMounted(fetchProducts);
</script>
