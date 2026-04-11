<template>
  <aside class="hidden w-64 shrink-0 xl:block">
    <div class="sticky top-24 overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
      <div class="border-b border-slate-200 px-5 py-4 text-sm font-semibold tracking-[0.2em] text-brand-800">
        관리자 메뉴
      </div>

      <nav class="p-3 space-y-1">
        <!-- 대시보드 -->
        <router-link
          to="/admin/dashboard"
          class="flex items-center rounded-2xl px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
          active-class="bg-brand-50 font-semibold text-brand-800"
        >
          대시보드
        </router-link>

        <!-- 주문 관리 -->
        <div class="pt-2">
          <p class="mb-1 px-4 text-[10px] font-bold uppercase tracking-widest text-slate-400">주문</p>
          <router-link
            v-for="item in orderMenu"
            :key="item.to"
            :to="item.to"
            class="flex items-center rounded-2xl px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-50 font-semibold text-brand-800"
          >
            {{ item.label }}
          </router-link>
        </div>

        <!-- 상품 관리 -->
        <div class="pt-2">
          <p class="mb-1 px-4 text-[10px] font-bold uppercase tracking-widest text-slate-400">상품</p>
          <router-link
            v-for="item in productMenu"
            :key="item.to"
            :to="item.to"
            class="flex items-center rounded-2xl px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-50 font-semibold text-brand-800"
          >
            {{ item.label }}
          </router-link>
        </div>

        <!-- 게시판 -->
        <div class="pt-2">
          <p class="mb-1 px-4 text-[10px] font-bold uppercase tracking-widest text-slate-400">게시판</p>
          <router-link
            v-for="item in boardMenu"
            :key="item.to"
            :to="item.to"
            class="flex items-center rounded-2xl px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-50 font-semibold text-brand-800"
          >
            {{ item.label }}
          </router-link>
        </div>

        <!-- 회원 관리 -->
        <div class="pt-2">
          <p class="mb-1 px-4 text-[10px] font-bold uppercase tracking-widest text-slate-400">회원</p>
          <router-link
            v-for="item in memberMenu"
            :key="item.to"
            :to="item.to"
            class="flex items-center rounded-2xl px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-50 font-semibold text-brand-800"
          >
            {{ item.label }}
          </router-link>
        </div>
      </nav>
    </div>
  </aside>
</template>

<script setup>
const orderMenu = [
  { label: '주문 목록',    to: '/admin/order/list' },
  { label: '주문 승인',    to: '/admin/order/approval' },
  { label: '배송 관리',    to: '/admin/delivery/list' },
  { label: '결제 내역',    to: '/admin/payment/list' },
]

const productMenu = [
  { label: '상품 목록',      to: '/admin/product/list' },
  { label: '상품 등록',      to: '/admin/product/insert' },
  { label: '재고/상태 관리', to: '/admin/product/stock' },
  { label: '카테고리',       to: '/admin/category/list' },
]

const boardMenu = [
  { label: '공지사항',   to: '/admin/notice/list' },
  { label: 'QnA',       to: '/admin/qna/list' },
  { label: '상품 리뷰', to: '/admin/review/list' },
]

const memberMenu = [
  { label: '회원 목록',   to: '/admin/member/list' },
  { label: '관리자 등급', to: '/admin/member/role' },
]
</script>
