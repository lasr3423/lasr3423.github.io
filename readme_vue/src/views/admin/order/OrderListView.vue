<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">주문 관리</h1>
      <p class="mt-1 text-sm text-slate-400">주문 상태를 확인하고 승인 또는 후속 처리가 필요한 주문을 정리합니다.</p>
    </section>

    <!-- 상태 필터 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white px-6 py-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <button
          v-for="f in filterOptions"
          :key="f.value"
          class="rounded-full border px-4 py-1.5 text-xs font-semibold transition"
          :class="selectedFilter === f.value
            ? 'border-brand-800 bg-brand-800 text-white'
            : 'border-slate-200 bg-white text-slate-600 hover:border-brand-300 hover:text-brand-700'"
          @click="applyFilter(f.value)"
        >
          {{ f.label }}
        </button>

        <label class="ml-auto flex items-center gap-2 text-xs font-semibold text-slate-600">
          <input
            v-model="outOfStockOnly"
            type="checkbox"
            class="h-4 w-4 rounded border-slate-300 text-rose-600"
            @change="applyOutOfStockFilter"
          >
          재고 부족 상품 주문만 보기
        </label>
      </div>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="orders.length > 0">
        <div class="flex flex-col gap-3 border-b border-slate-100 px-6 py-4 md:flex-row md:items-center md:justify-between">
          <div class="flex items-center gap-3">
            <input
              type="checkbox"
              class="h-4 w-4 rounded border-slate-300 text-brand-700"
              :checked="isAllChecked"
              :indeterminate.prop="isSomeChecked"
              @change="toggleAll"
            >
            <span class="text-sm text-slate-500">
              {{ checkedIds.size > 0 ? `${checkedIds.size}건 선택됨` : `전체 ${orders.length}건` }}
            </span>
          </div>

          <div class="flex flex-wrap items-center gap-2">
            <button
              v-if="canBulkApprove"
              class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:bg-slate-300"
              :disabled="checkedIds.size === 0 || bulkUpdating"
              @click="handleBulkApprove"
            >
              {{ bulkUpdating ? '승인 중...' : `선택 주문 일괄 승인 (${checkedIds.size})` }}
            </button>
            <button
              v-if="hasOutOfStockOrders || checkedIds.size > 0"
              class="rounded-xl bg-rose-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-500 disabled:bg-slate-300"
              :disabled="checkedIds.size === 0 || bulkUpdating || !canBulkOutOfStockCancel"
              @click="handleBulkOutOfStockCancel"
            >
              {{ bulkUpdating ? '취소 중...' : `선택 주문 재고 부족 취소 (${checkedIds.size})` }}
            </button>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">선택</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문번호</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문 회원</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문 도서</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">결제금액</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">현재 상태</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">주문일</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">상태 변경</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="order in orders" :key="order.orderId" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 text-center">
                  <input
                    type="checkbox"
                    class="h-4 w-4 rounded border-slate-300 text-brand-700"
                    :checked="checkedIds.has(order.orderId)"
                    @change="toggleItem(order.orderId)"
                  >
                </td>
                <td class="px-6 py-4 font-mono text-xs text-slate-600">{{ order.number }}</td>
                <td class="px-6 py-4 text-slate-700">{{ order.memberName || '-' }}</td>
                <td class="px-6 py-4 text-slate-700">
                  <div class="flex items-center gap-2">
                    <span>{{ order.itemSummary || '-' }}</span>
                    <span
                      v-if="order.outOfStock"
                      class="rounded-full bg-rose-50 px-2 py-0.5 text-[11px] font-semibold text-rose-600"
                    >
                      재고 부족
                    </span>
                  </div>
                </td>
                <td class="px-6 py-4 text-right font-semibold text-slate-900">{{ order.finalPrice.toLocaleString() }}원</td>
                <td class="px-6 py-4 text-center">
                  <span :class="statusClass(order.orderStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ statusLabel(order.orderStatus) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-slate-500">{{ formatDate(order.orderAt) }}</td>
                <td class="px-6 py-4 text-right">
                  <select
                    class="w-28 rounded-xl border px-2 py-1.5 text-xs"
                    :class="nextStatusOptions(order.orderStatus).length === 0
                      ? 'cursor-not-allowed border-slate-100 bg-slate-50 text-slate-400'
                      : 'border-slate-200 bg-white text-slate-700 cursor-pointer'"
                    :disabled="nextStatusOptions(order.orderStatus).length === 0"
                    :value="order.orderStatus"
                    @change="handleStatusChange(order.orderId, $event.target.value)"
                  >
                    <option :value="order.orderStatus" disabled>{{ statusLabel(order.orderStatus) }}</option>
                    <option
                      v-for="option in nextStatusOptions(order.orderStatus)"
                      :key="option.value"
                      :value="option.value"
                    >
                      {{ option.label }}
                    </option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchOrders()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchOrders()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">주문이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { adminApi } from '@/api/admin';

const orders = ref([]);
const loading = ref(true);
const page = ref(0);
const totalPages = ref(1);
const bulkUpdating = ref(false);
const checkedIds = ref(new Set());
const selectedFilter = ref('');
const outOfStockOnly = ref(false);

const filterOptions = [
  { value: '',                label: '전체' },
  { value: 'PAYMENT_PENDING', label: '결제 진행 중' },
  { value: 'PENDING',         label: '승인 대기' },
  { value: 'APPROVAL',        label: '배송 준비' },
  { value: 'DELIVERING',      label: '배송 중' },
  { value: 'DELIVERED',       label: '배송 완료' },
  { value: 'CANCELED',        label: '취소' },
];

// 전체 상태 순서 정의 (드롭다운용)
const statusOptions = [
  { value: 'PAYMENT_PENDING', label: '결제 진행 중' },
  { value: 'PENDING',         label: '승인 대기' },
  { value: 'APPROVAL',        label: '배송 준비' },
  { value: 'DELIVERING',      label: '배송 중' },
  { value: 'DELIVERED',       label: '배송 완료' },
  { value: 'CANCELED',        label: '취소' },
];

function nextStatusOptions(currentStatus) {
  const order = ['PAYMENT_PENDING', 'PENDING', 'APPROVAL', 'DELIVERING', 'DELIVERED'];
  const currentIdx = order.indexOf(currentStatus);

  if (currentStatus === 'DELIVERED' || currentStatus === 'CANCELED') return [];

  return statusOptions.filter((opt) => {
    if (opt.value === 'CANCELED') return true;
    return order.indexOf(opt.value) > currentIdx;
  });
}

const statusLabel = (s) => ({
  PAYMENT_PENDING: '결제 진행 중',
  PENDING:    '승인 대기',
  PAYED:      '승인 대기(구)',
  APPROVAL:   '배송 준비',
  DELIVERING: '배송 중',
  DELIVERED:  '배송 완료',
  CANCELED:   '취소',
}[s] ?? s);

const statusClass = (s) => ({
  PAYMENT_PENDING: 'bg-amber-50 text-amber-700',
  PENDING:    'bg-yellow-50 text-yellow-700',
  PAYED:      'bg-yellow-50 text-yellow-700',
  APPROVAL:   'bg-brand-50 text-brand-700',
  DELIVERING: 'bg-sky-50 text-sky-700',
  DELIVERED:  'bg-emerald-50 text-emerald-700',
  CANCELED:   'bg-rose-50 text-rose-700',
}[s] ?? 'bg-slate-100 text-slate-600');

const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR') : '-';

const isAllChecked = computed(() => orders.value.length > 0 && checkedIds.value.size === orders.value.length);
const isSomeChecked = computed(() => checkedIds.value.size > 0 && checkedIds.value.size < orders.value.length);
const checkedOrders = computed(() => orders.value.filter((order) => checkedIds.value.has(order.orderId)));
const hasOutOfStockOrders = computed(() => orders.value.some((order) => order.outOfStock));
const canBulkApprove = computed(() => selectedFilter.value === 'PENDING');
const canBulkOutOfStockCancel = computed(() =>
  checkedOrders.value.length > 0 && checkedOrders.value.every((order) => order.outOfStock)
);

function applyFilter(status) {
  selectedFilter.value = status;
  page.value = 0;
  fetchOrders();
}

function applyOutOfStockFilter() {
  page.value = 0;
  fetchOrders();
}

async function fetchOrders() {
  try {
    loading.value = true;
    const params = { page: page.value, size: 20 };
    if (selectedFilter.value) params.status = selectedFilter.value;
    const { data } = await adminApi.getOrders(params);
    const loadedOrders = data.content || [];
    orders.value = outOfStockOnly.value
      ? loadedOrders.filter((order) => order.outOfStock)
      : loadedOrders;
    totalPages.value = data.totalPages || 1;
    checkedIds.value = new Set();
  } catch (e) {
    console.error('주문 목록 로드 실패:', e);
  } finally {
    loading.value = false;
  }
}

async function handleStatusChange(id, status) {
  try {
    await adminApi.updateOrderStatus(id, status);
    await fetchOrders();
  } catch (e) {
    alert(e.response?.data?.message || '상태 변경 실패');
  }
}

function toggleAll(event) {
  if (event.target.checked) {
    checkedIds.value = new Set(orders.value.map((order) => order.orderId));
    return;
  }
  checkedIds.value = new Set();
}

function toggleItem(orderId) {
  const next = new Set(checkedIds.value);
  next.has(orderId) ? next.delete(orderId) : next.add(orderId);
  checkedIds.value = next;
}

async function handleBulkApprove() {
  if (checkedIds.value.size === 0) return;
  if (!confirm(`선택한 ${checkedIds.value.size}건을 배송 준비(승인) 상태로 변경하시겠습니까?`)) return;

  bulkUpdating.value = true;
  try {
    const orderIds = [...checkedIds.value];
    await adminApi.updateOrderStatuses(orderIds, 'APPROVAL');
    alert(`${orderIds.length}건이 승인되었습니다.`);
    await fetchOrders();
  } catch (e) {
    alert(e.response?.data?.message || '일괄 승인 실패');
  } finally {
    bulkUpdating.value = false;
  }
}

async function handleBulkOutOfStockCancel() {
  if (checkedIds.value.size === 0) return;
  if (!canBulkOutOfStockCancel.value) {
    alert('재고가 주문 수량보다 부족한 상품이 포함된 주문만 선택해서 취소할 수 있습니다.');
    return;
  }
  if (!confirm(`선택한 ${checkedIds.value.size}건을 재고 부족 사유로 취소하시겠습니까?\n결제 완료 주문은 환불도 함께 처리됩니다.`)) return;

  bulkUpdating.value = true;
  try {
    const orderIds = [...checkedIds.value];
    await adminApi.updateOrderStatuses(orderIds, 'CANCELED');
    alert(`${orderIds.length}건이 재고 부족으로 취소되었습니다.`);
    await fetchOrders();
  } catch (e) {
    alert(e.response?.data?.message || '재고 부족 일괄 취소 실패');
  } finally {
    bulkUpdating.value = false;
  }
}

onMounted(fetchOrders);
</script>
