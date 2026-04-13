<template>
  <aside class="hidden w-64 shrink-0 xl:block">
    <div class="sticky top-24 guide-sidebar">
      <div class="px-5 py-3 text-sm font-semibold tracking-[0.2em] text-brand-800">
        관리자 메뉴
      </div>

      <nav class="p-3 space-y-3">
        <router-link
          to="/admin/"
          class="block rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-brand-50 hover:text-brand-800"
          active-class="bg-brand-800 text-white shadow-sm"
        >
          대시보드
        </router-link>

        <div>
          <p class="mb-2 px-4 text-[10px] font-bold uppercase tracking-[0.24em] text-slate-400">주문</p>
          <router-link
            v-for="item in orderMenu"
            :key="item.to"
            :to="item.to"
            class="mb-2 block rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition last:mb-0 hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-800 text-white shadow-sm"
          >
            {{ item.label }}
          </router-link>
        </div>

        <div>
          <p class="mb-2 px-4 text-[10px] font-bold uppercase tracking-[0.24em] text-slate-400">상품</p>
          <router-link
            v-for="item in productMenu"
            :key="item.to"
            :to="item.to"
            class="mb-2 block rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition last:mb-0 hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-800 text-white shadow-sm"
          >
            {{ item.label }}
          </router-link>
        </div>

        <div>
          <p class="mb-2 px-4 text-[10px] font-bold uppercase tracking-[0.24em] text-slate-400">게시판</p>
          <router-link
            v-for="item in boardMenu"
            :key="item.to"
            :to="item.to"
            class="mb-2 block rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition last:mb-0 hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-800 text-white shadow-sm"
          >
            {{ item.label }}
          </router-link>
        </div>

        <div>
          <p class="mb-2 px-4 text-[10px] font-bold uppercase tracking-[0.24em] text-slate-400">회원</p>
          <router-link
            v-for="item in memberMenu"
            :key="item.to"
            :to="item.to"
            class="mb-2 block rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition last:mb-0 hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-800 text-white shadow-sm"
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
  { label: '주문 목록', to: '/admin/order/list' },
  { label: '주문 승인', to: '/admin/order/approval' },
  { label: '배송 관리', to: '/admin/delivery/list' },
  { label: '결제 내역', to: '/admin/payment/list' },
]

const productMenu = [
  { label: '상품 목록', to: '/admin/product/list' },
  { label: '상품 등록', to: '/admin/product/insert' },
  { label: '재고/상태 관리', to: '/admin/product/stock' },
  { label: '카테고리', to: '/admin/category/list' },
]

const boardMenu = [
  { label: '공지사항', to: '/admin/notice/list' },
  { label: 'QnA', to: '/admin/qna/list' },
  { label: '상품 리뷰', to: '/admin/review/list' },
]

const memberMenu = [
  { label: '회원 목록', to: '/admin/member/list' },
  { label: '관리자 등급', to: '/admin/member/role' },
]
</script>
