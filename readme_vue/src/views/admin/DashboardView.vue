<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">대시보드</h1>
    </section>

    <!-- 통계 카드 -->
    <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>
    <div v-else class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div v-for="stat in stats" :key="stat.label"
        class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
        <p class="text-xs font-semibold uppercase tracking-widest text-slate-400">{{ stat.label }}</p>
        <p class="mt-2 text-3xl font-bold tracking-tight text-slate-900">{{ stat.value.toLocaleString() }}</p>
        <p class="mt-1 text-xs text-slate-500">{{ stat.sub }}</p>
      </div>
    </div>

    <!-- 빠른 이동 -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <router-link v-for="link in quickLinks" :key="link.to" :to="link.to"
        class="group flex items-center gap-4 rounded-[2rem] border border-slate-200 bg-white p-5 shadow-sm transition hover:border-brand-200 hover:shadow-md">
        <span class="flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-100 text-lg group-hover:bg-brand-50">{{ link.icon }}</span>
        <div>
          <p class="text-sm font-semibold text-slate-800">{{ link.label }}</p>
          <p class="text-xs text-slate-400">{{ link.desc }}</p>
        </div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { adminApi } from '@/api/admin';

const data = ref(null);
const loading = ref(true);

const stats = computed(() => data.value ? [
  { label: '전체 회원',   value: data.value.totalMembers,  sub: `활성 ${data.value.activeMembers}명` },
  { label: '활성 회원',   value: data.value.activeMembers, sub: '정상 계정' },
  { label: '전체 상품',   value: data.value.totalProducts, sub: '등록된 도서' },
  { label: '전체 주문',   value: data.value.totalOrders,   sub: '누적 주문' },
  { label: '결제 대기',   value: data.value.pendingOrders, sub: '처리 필요' },
] : []);

const quickLinks = [
  { to: '/admin/member/list',   icon: '👥', label: '회원 관리',  desc: '회원 목록 / 상태 변경' },
  { to: '/admin/product/list',  icon: '📚', label: '상품 관리',  desc: '상품 목록 / 등록' },
  { to: '/admin/order/list',    icon: '📦', label: '주문 관리',  desc: '주문 목록 / 상태 변경' },
  { to: '/admin/delivery/list', icon: '🚚', label: '배송 관리',  desc: '배송 현황 / 운송장 입력' },
];

onMounted(async () => {
  try {
    const { data: d } = await adminApi.getDashboard();
    data.value = d;
  } catch (e) {
    console.error('대시보드 로드 실패:', e);
  } finally {
    loading.value = false;
  }
});
</script>
