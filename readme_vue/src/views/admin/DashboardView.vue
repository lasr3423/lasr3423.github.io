<template>
  <div class="space-y-6">
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-[#203455] text-white shadow-sm">
      <div class="grid gap-6 px-8 py-8 xl:grid-cols-[minmax(0,1.3fr)_380px]">
        <div>
          <p class="text-sm font-semibold uppercase tracking-[0.24em] text-sky-200">Admin Dashboard</p>
          <h1 class="mt-3 text-3xl font-bold tracking-tight">관리자 대시보드</h1>
          <p class="mt-3 max-w-2xl text-sm leading-6 text-slate-200">
            오늘 처리해야 할 주문, 배송, 문의 현황을 한 번에 확인하고 바로 필요한 관리 화면으로 이동할 수 있습니다.
          </p>

          <div class="mt-6 grid gap-4 sm:grid-cols-3">
            <div class="rounded-3xl border border-white/10 bg-white/10 p-4 backdrop-blur">
              <p class="text-xs font-semibold uppercase tracking-[0.16em] text-sky-100">미승인 주문</p>
              <p class="mt-2 text-3xl font-bold">{{ formatNumber(dashboard?.payedOrders) }}</p>
              <p class="mt-1 text-xs text-slate-200">배송 승인 대기 건수</p>
            </div>
            <div class="rounded-3xl border border-white/10 bg-white/10 p-4 backdrop-blur">
              <p class="text-xs font-semibold uppercase tracking-[0.16em] text-sky-100">미답변 문의</p>
              <p class="mt-2 text-3xl font-bold">{{ formatNumber(dashboard?.unansweredQnaCount) }}</p>
              <p class="mt-1 text-xs text-slate-200">우선 확인이 필요한 문의</p>
            </div>
            <div class="rounded-3xl border border-white/10 bg-white/10 p-4 backdrop-blur">
              <p class="text-xs font-semibold uppercase tracking-[0.16em] text-sky-100">배송 준비</p>
              <p class="mt-2 text-3xl font-bold">{{ formatNumber(dashboard?.readyDeliveryCount) }}</p>
              <p class="mt-1 text-xs text-slate-200">출고 전 배송 건수</p>
            </div>
          </div>
        </div>

        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-1">
          <div class="rounded-3xl border border-white/10 bg-white/10 p-5 backdrop-blur">
            <p class="text-xs font-semibold uppercase tracking-[0.16em] text-sky-100">오늘 운영 요약</p>
            <dl class="mt-4 space-y-3 text-sm">
              <div class="flex items-center justify-between">
                <dt class="text-slate-200">오늘 신규 회원</dt>
                <dd class="text-lg font-bold text-white">{{ formatNumber(dashboard?.todayNewMembers) }}명</dd>
              </div>
              <div class="flex items-center justify-between">
                <dt class="text-slate-200">오늘 주문</dt>
                <dd class="text-lg font-bold text-white">{{ formatNumber(dashboard?.todayOrders) }}건</dd>
              </div>
              <div class="flex items-center justify-between">
                <dt class="text-slate-200">오늘 매출</dt>
                <dd class="text-lg font-bold text-white">{{ formatCurrency(dashboard?.todaySales) }}</dd>
              </div>
            </dl>
          </div>

          <div class="rounded-3xl border border-white/10 bg-white/10 p-5 backdrop-blur">
            <p class="text-xs font-semibold uppercase tracking-[0.16em] text-sky-100">재고 경고</p>
            <p class="mt-4 text-4xl font-bold text-white">{{ formatNumber(dashboard?.lowStockProducts) }}</p>
            <p class="mt-2 text-sm text-slate-200">재고 10권 이하 상품</p>
            <router-link
              to="/admin/product/stock"
              class="mt-5 inline-flex rounded-2xl bg-white/15 px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/20"
            >
              재고 확인하기
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <div v-if="loading" class="surface-panel rounded-[2rem] p-12 text-center text-sm text-slate-500">
      관리자 데이터를 불러오는 중입니다...
    </div>

    <template v-else>
      <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <div
          v-for="stat in statCards"
          :key="stat.label"
          class="surface-panel rounded-[2rem] p-6"
        >
          <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">{{ stat.label }}</p>
          <p class="mt-2 text-3xl font-bold tracking-tight text-slate-900">{{ stat.value }}</p>
          <p class="mt-2 text-xs text-slate-500">{{ stat.sub }}</p>
        </div>
      </section>

      <section class="grid gap-6 xl:grid-cols-2">
        <article class="surface-panel rounded-[2rem] p-6">
          <div class="mb-5 flex items-center justify-between">
            <div>
              <p class="text-sm font-semibold text-slate-500">주문 상태 분포</p>
              <h2 class="mt-1 text-xl font-bold text-slate-900">주문/처리 그래프</h2>
            </div>
            <router-link to="/admin/order/list" class="text-sm font-semibold text-brand-700">
              전체 보기
            </router-link>
          </div>

          <div class="grid items-center gap-6 lg:grid-cols-[220px_minmax(0,1fr)]">
            <div class="mx-auto h-44 w-44 rounded-full" :style="orderPieStyle"></div>
            <div class="space-y-4">
              <div v-for="item in orderSegments" :key="item.label">
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="font-medium text-slate-700">{{ item.label }}</span>
                  <span class="text-slate-500">{{ item.value }}건</span>
                </div>
                <div class="h-3 overflow-hidden rounded-full bg-slate-100">
                  <div class="h-full rounded-full" :class="item.barClass" :style="{ width: `${item.percent}%` }"></div>
                </div>
              </div>
            </div>
          </div>
        </article>

        <article class="surface-panel rounded-[2rem] p-6">
          <div class="mb-5 flex items-center justify-between">
            <div>
              <p class="text-sm font-semibold text-slate-500">운영 추이</p>
              <h2 class="mt-1 text-xl font-bold text-slate-900">월간/일간 비교</h2>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <div class="rounded-3xl bg-slate-50 p-4">
              <p class="text-sm font-semibold text-slate-700">주문 건수</p>
              <div class="mt-5 flex h-44 items-end gap-4">
                <div v-for="item in orderTrendBars" :key="item.label" class="flex flex-1 flex-col items-center gap-2">
                  <div class="flex h-32 w-full items-end rounded-t-2xl bg-white px-2 py-2">
                    <div class="w-full rounded-t-xl bg-brand-600" :style="{ height: `${item.height}%` }"></div>
                  </div>
                  <span class="text-xs font-semibold text-slate-500">{{ item.label }}</span>
                </div>
              </div>
            </div>

            <div class="rounded-3xl bg-slate-50 p-4">
              <p class="text-sm font-semibold text-slate-700">회원/문의 지표</p>
              <div class="mt-5 space-y-4">
                <div v-for="item in supportBars" :key="item.label">
                  <div class="mb-2 flex items-center justify-between text-sm">
                    <span class="font-medium text-slate-700">{{ item.label }}</span>
                    <span class="text-slate-500">{{ item.value }}</span>
                  </div>
                  <div class="h-3 overflow-hidden rounded-full bg-white">
                    <div class="h-full rounded-full" :class="item.barClass" :style="{ width: `${item.height}%` }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section class="grid gap-6 xl:grid-cols-2">
        <article class="surface-panel rounded-[2rem] p-6">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <p class="text-sm font-semibold text-slate-500">최근 주문 목록</p>
              <h2 class="mt-1 text-xl font-bold text-slate-900">우선 확인할 주문</h2>
            </div>
            <router-link to="/admin/order/list" class="text-sm font-semibold text-brand-700">
              전체 보기
            </router-link>
          </div>

          <div class="overflow-hidden rounded-3xl border border-slate-100">
            <table class="w-full text-sm">
              <thead class="bg-slate-50 text-slate-500">
                <tr>
                  <th class="px-4 py-3 text-left font-semibold">주문번호</th>
                  <th class="px-4 py-3 text-right font-semibold">금액</th>
                  <th class="px-4 py-3 text-center font-semibold">상태</th>
                  <th class="px-4 py-3 text-right font-semibold">주문일</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="order in recentOrders" :key="order.orderId">
                  <td class="px-4 py-3 font-mono text-xs text-slate-700">{{ order.number }}</td>
                  <td class="px-4 py-3 text-right font-semibold text-slate-900">{{ formatCurrency(order.finalPrice) }}</td>
                  <td class="px-4 py-3 text-center">
                    <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="orderStatusClass(order.orderStatus)">
                      {{ orderStatusLabel(order.orderStatus) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-right text-slate-500">{{ formatDate(order.orderAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <article class="surface-panel rounded-[2rem] p-6">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <p class="text-sm font-semibold text-slate-500">최근 배송 현황</p>
              <h2 class="mt-1 text-xl font-bold text-slate-900">배송 진행 상태</h2>
            </div>
            <router-link to="/admin/delivery/list" class="text-sm font-semibold text-brand-700">
              전체 보기
            </router-link>
          </div>

          <div class="overflow-hidden rounded-3xl border border-slate-100">
            <table class="w-full text-sm">
              <thead class="bg-slate-50 text-slate-500">
                <tr>
                  <th class="px-4 py-3 text-left font-semibold">배송 ID</th>
                  <th class="px-4 py-3 text-left font-semibold">택배사</th>
                  <th class="px-4 py-3 text-center font-semibold">상태</th>
                  <th class="px-4 py-3 text-right font-semibold">출고일</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="delivery in recentDeliveries" :key="delivery.id">
                  <td class="px-4 py-3 font-mono text-xs text-slate-700">#{{ delivery.id }}</td>
                  <td class="px-4 py-3 text-slate-700">{{ delivery.courier || '-' }}</td>
                  <td class="px-4 py-3 text-center">
                    <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="deliveryStatusClass(delivery.deliveryStatus)">
                      {{ deliveryStatusLabel(delivery.deliveryStatus) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-right text-slate-500">{{ formatDate(delivery.shippedAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <router-link
          v-for="link in quickLinks"
          :key="link.to"
          :to="link.to"
          class="surface-panel group rounded-[2rem] p-5 transition hover:-translate-y-0.5 hover:border-brand-200 hover:shadow-md"
        >
          <div class="flex items-center gap-4">
            <span class="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-lg transition group-hover:bg-brand-50">
              {{ link.icon }}
            </span>
            <div>
              <p class="text-sm font-semibold text-slate-900">{{ link.label }}</p>
              <p class="text-xs text-slate-500">{{ link.desc }}</p>
            </div>
          </div>
        </router-link>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'

const dashboard = ref(null)
const recentOrders = ref([])
const recentDeliveries = ref([])
const loading = ref(true)

const statCards = computed(() => [
  {
    label: '전체 회원',
    value: `${formatNumber(dashboard.value?.totalMembers)}명`,
    sub: `활성 회원 ${formatNumber(dashboard.value?.activeMembers)}명`,
  },
  {
    label: '전체 상품',
    value: `${formatNumber(dashboard.value?.totalProducts)}권`,
    sub: `재고 주의 ${formatNumber(dashboard.value?.lowStockProducts)}권`,
  },
  {
    label: '전체 주문',
    value: `${formatNumber(dashboard.value?.totalOrders)}건`,
    sub: `이번 달 ${formatNumber(dashboard.value?.monthOrders)}건`,
  },
  {
    label: '이번 달 매출',
    value: formatCurrency(dashboard.value?.monthSales),
    sub: `오늘 ${formatCurrency(dashboard.value?.todaySales)}`,
  },
  {
    label: '신규 회원',
    value: `${formatNumber(dashboard.value?.monthNewMembers)}명`,
    sub: `오늘 ${formatNumber(dashboard.value?.todayNewMembers)}명 가입`,
  },
])

const orderSegments = computed(() => {
  const pending = Number(dashboard.value?.pendingOrders || 0)
  const payed = Number(dashboard.value?.payedOrders || 0)
  const total = Number(dashboard.value?.totalOrders || 0)
  const completed = Math.max(total - pending - payed, 0)
  const base = Math.max(total, 1)

  return [
    {
      label: '결제 진행 중',
      value: pending,
      percent: Math.round((pending / base) * 100),
      color: '#f59e0b',
      barClass: 'bg-amber-500',
    },
    {
      label: '승인 대기',
      value: payed,
      percent: Math.round((payed / base) * 100),
      color: '#2563eb',
      barClass: 'bg-blue-600',
    },
    {
      label: '완료/기타',
      value: completed,
      percent: Math.round((completed / base) * 100),
      color: '#22c55e',
      barClass: 'bg-emerald-500',
    },
  ]
})

const orderPieStyle = computed(() => {
  const segments = orderSegments.value
  let current = 0
  const stops = segments.map((segment) => {
    const start = current
    current += segment.percent
    return `${segment.color} ${start}% ${current}%`
  })

  return {
    background: `conic-gradient(${stops.join(', ')})`,
  }
})

const orderTrendBars = computed(() => {
  const todayOrders = Number(dashboard.value?.todayOrders || 0)
  const monthOrders = Number(dashboard.value?.monthOrders || 0)
  const totalOrders = Number(dashboard.value?.totalOrders || 0)
  const pendingOrders = Number(dashboard.value?.pendingOrders || 0)
  const values = [todayOrders, monthOrders, totalOrders, pendingOrders]
  const max = Math.max(...values, 1)

  return [
    { label: '오늘 주문', height: Math.max(12, Math.round((todayOrders / max) * 100)) },
    { label: '월간 주문', height: Math.max(12, Math.round((monthOrders / max) * 100)) },
    { label: '누적 주문', height: Math.max(12, Math.round((totalOrders / max) * 100)) },
    { label: '대기 주문', height: Math.max(12, Math.round((pendingOrders / max) * 100)) },
  ]
})

const supportBars = computed(() => {
  const values = [
    Number(dashboard.value?.todayNewMembers || 0),
    Number(dashboard.value?.monthNewMembers || 0),
    Number(dashboard.value?.unansweredQnaCount || 0),
    Number(dashboard.value?.readyDeliveryCount || 0),
  ]
  const max = Math.max(...values, 1)

  return [
    {
      label: '오늘 신규 회원',
      value: `${values[0]}명`,
      height: Math.max(10, Math.round((values[0] / max) * 100)),
      barClass: 'bg-indigo-500',
    },
    {
      label: '이번 달 신규 회원',
      value: `${values[1]}명`,
      height: Math.max(10, Math.round((values[1] / max) * 100)),
      barClass: 'bg-violet-500',
    },
    {
      label: '미답변 QnA',
      value: `${values[2]}건`,
      height: Math.max(10, Math.round((values[2] / max) * 100)),
      barClass: 'bg-rose-500',
    },
    {
      label: '배송 준비',
      value: `${values[3]}건`,
      height: Math.max(10, Math.round((values[3] / max) * 100)),
      barClass: 'bg-emerald-500',
    },
  ]
})

const quickLinks = [
  { to: '/admin/order/approval', icon: '승', label: '주문 승인', desc: '승인 대기 주문 처리' },
  { to: '/admin/product/list', icon: '상', label: '상품 관리', desc: '상품 조회와 등록' },
  { to: '/admin/member/list', icon: '회', label: '회원 관리', desc: '회원 상태와 권한 관리' },
  { to: '/admin/qna/list', icon: '문', label: '문의 관리', desc: '미답변 문의 확인' },
]

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function formatCurrency(value) {
  return `${Number(value || 0).toLocaleString()}원`
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' })
}

function orderStatusLabel(status) {
  return {
    PAYMENT_PENDING: '결제 진행 중',
    PENDING: '승인 대기',
    PAYED: '승인 대기(구)',
    APPROVAL: '배송 준비',
    DELIVERING: '배송 중',
    DELIVERED: '배송 완료',
    CANCELED: '취소',
  }[status] || status
}

function orderStatusClass(status) {
  return {
    PAYMENT_PENDING: 'bg-amber-50 text-amber-700',
    PENDING: 'bg-yellow-50 text-yellow-700',
    PAYED: 'bg-blue-50 text-blue-700',
    APPROVAL: 'bg-emerald-50 text-emerald-700',
    DELIVERING: 'bg-sky-50 text-sky-700',
    DELIVERED: 'bg-violet-50 text-violet-700',
    CANCELED: 'bg-rose-50 text-rose-700',
  }[status] || 'bg-slate-100 text-slate-600'
}

function deliveryStatusLabel(status) {
  return {
    READY: '배송 준비',
    SHIPPED: '배송 중',
    DELIVERED: '배송 완료',
  }[status] || status
}

function deliveryStatusClass(status) {
  return {
    READY: 'bg-amber-50 text-amber-700',
    SHIPPED: 'bg-blue-50 text-blue-700',
    DELIVERED: 'bg-emerald-50 text-emerald-700',
  }[status] || 'bg-slate-100 text-slate-600'
}

async function fetchDashboard() {
  const [{ data: dashboardData }, { data: orderData }, { data: deliveryData }] = await Promise.all([
    adminApi.getDashboard(),
    adminApi.getOrders({ page: 0, size: 6 }),
    adminApi.getDeliveries({ page: 0, size: 6 }),
  ])

  dashboard.value = dashboardData
  recentOrders.value = orderData.content ?? []
  recentDeliveries.value = deliveryData.content ?? []
}

onMounted(async () => {
  try {
    await fetchDashboard()
  } catch (error) {
    console.error('관리자 대시보드 로드 실패:', error)
  } finally {
    loading.value = false
  }
})
</script>
