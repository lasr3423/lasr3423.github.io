<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">배송 관리</h1>
    </section>

    <!-- 필터 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <select v-model="statusFilter" @change="search"
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400">
          <option value="">전체 상태</option>
          <option value="READY">배송준비</option>
          <option value="SHIPPED">배송중</option>
          <option value="DELIVERED">배송완료</option>
        </select>
      </div>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="deliveries.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">배송 ID</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">택배사</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">운송장 번호</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">출고일</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">수정</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="d in deliveries" :key="d.id" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 text-xs text-slate-400">{{ d.id }}</td>
                <td class="px-6 py-4 text-slate-700">{{ d.courier || '-' }}</td>
                <td class="px-6 py-4 font-mono text-xs text-slate-600">{{ d.trackingNumber || '-' }}</td>
                <td class="px-6 py-4 text-center">
                  <span :class="deliveryStatusClass(d.deliveryStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ deliveryStatusLabel(d.deliveryStatus) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-xs text-slate-500">{{ formatDate(d.shippedAt) }}</td>
                <td class="px-6 py-4 text-right">
                  <button @click="openEdit(d)"
                    class="rounded-xl bg-brand-50 px-3 py-1.5 text-xs font-semibold text-brand-700 transition hover:bg-brand-100">
                    수정
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchDeliveries()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchDeliveries()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">배송 내역이 없습니다.</div>
    </section>

    <!-- 수정 모달 -->
    <div v-if="editTarget" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-sm">
      <div class="w-full max-w-md rounded-[2rem] bg-white p-8 shadow-2xl">
        <h2 class="text-lg font-bold text-slate-900">배송 정보 수정</h2>
        <form class="mt-5 space-y-4" @submit.prevent="handleUpdate">
          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">택배사</span>
            <input v-model="editForm.courier" type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>
          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">운송장 번호</span>
            <input v-model="editForm.trackingNumber" type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" />
          </label>
          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">배송 상태</span>
            <select v-model="editForm.deliveryStatus"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400">
              <option value="READY">배송준비</option>
              <option value="SHIPPED">배송중</option>
              <option value="DELIVERED">배송완료</option>
            </select>
          </label>
          <div class="flex gap-3 pt-2">
            <button type="submit" :disabled="updating"
              class="flex-1 rounded-2xl bg-brand-800 py-3 text-sm font-semibold text-white hover:bg-brand-700 disabled:bg-slate-400">
              {{ updating ? '저장 중...' : '저장' }}
            </button>
            <button type="button" @click="editTarget = null"
              class="flex-1 rounded-2xl border border-slate-200 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50">
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { adminApi } from '@/api/admin';

const deliveries = ref([]);
const loading = ref(true);
const statusFilter = ref('');
const page = ref(0);
const totalPages = ref(1);
const editTarget = ref(null);
const editForm = ref({ courier: '', trackingNumber: '', deliveryStatus: '' });
const updating = ref(false);

const deliveryStatusLabel = (s) => ({ READY: '배송준비', SHIPPED: '배송중', DELIVERED: '배송완료' }[s] ?? s);
const deliveryStatusClass = (s) => ({
  READY:     'bg-yellow-50 text-yellow-700',
  SHIPPED:   'bg-brand-50 text-brand-700',
  DELIVERED: 'bg-green-50 text-green-700',
}[s] ?? 'bg-slate-100 text-slate-600');
const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR') : '-';

async function fetchDeliveries() {
  try {
    loading.value = true;
    const { data } = await adminApi.getDeliveries({ page: page.value, size: 20, status: statusFilter.value || undefined });
    deliveries.value = data.content;
    totalPages.value = data.totalPages || 1;
  } catch (e) {
    console.error('배송 목록 로드 실패:', e);
  } finally {
    loading.value = false;
  }
}

function search() { page.value = 0; fetchDeliveries(); }

function openEdit(d) {
  editTarget.value = d;
  editForm.value = { courier: d.courier || '', trackingNumber: d.trackingNumber || '', deliveryStatus: d.deliveryStatus };
}

async function handleUpdate() {
  try {
    updating.value = true;
    await adminApi.updateDelivery(editTarget.value.id, editForm.value);
    editTarget.value = null;
    fetchDeliveries();
  } catch (e) {
    alert(e.response?.data?.message || '수정 실패');
  } finally {
    updating.value = false;
  }
}

onMounted(fetchDeliveries);
</script>
