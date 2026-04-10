<template>
  <aside class="hidden w-64 shrink-0 xl:block">
    <div class="sticky top-24 overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
      <div class="border-b border-slate-200 px-5 py-4 text-sm font-semibold tracking-[0.2em] text-brand-800">
        관리자 메뉴
      </div>

      <div class="p-3">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="mb-1 block rounded-2xl px-4 py-3 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
          active-class="bg-brand-50 font-semibold text-brand-800"
        >
          {{ item.label }}
        </router-link>
      </div>
    </div>
  </aside>
</template>

<script setup>
const menuItems = [
  { label: '주문/배송', to: '/admin/order/list' },
  { label: '상품 목록', to: '/admin/product/list' },
  { label: '카테고리', to: '/admin/category/list' },
  { label: '공지사항', to: '/admin/notice/list' },
  { label: 'QnA', to: '/admin/qna/list' },
  { label: '상품 리뷰', to: '/admin/review/list' },
  { label: '회원 검색', to: '/admin/member/list' },
  { label: '관리자 등급', to: '/admin/member/role' },
];
</script>
