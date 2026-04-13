<template>
  <aside class="hidden w-64 shrink-0 xl:block">
    <div class="sticky top-24 guide-sidebar">
      <div class="border-b border-slate-200 bg-brand-800 px-5 py-4 text-sm font-semibold tracking-[0.2em] text-white">
        마이페이지
      </div>

      <div class="p-3">
        <router-link
          v-for="item in menuItems"
          :key="item.to"
          :to="item.to"
          class="mb-2 block rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition last:mb-0 hover:bg-brand-50 hover:text-brand-800"
          active-class="bg-brand-800 text-white shadow-sm"
        >
          {{ item.label }}
        </router-link>
      </div>
    </div>
  </aside>
</template>

<script setup>
const menuItems = [
  { label: '주문 / 배송 조회', to: '/mypage/order' },
  { label: '결제 내역', to: '/mypage/payment' },
  { label: '장바구니', to: '/cart' },
  { label: '내 문의 (QnA)', to: '/mypage/qna' },
  { label: '내 리뷰', to: '/mypage/review' },
  { label: '회원정보 변경', to: '/mypage/edit' },
  { label: '비밀번호 변경', to: '/mypage/password' },
  { label: '회원 탈퇴', to: '/mypage/withdraw' },
]
</script>
