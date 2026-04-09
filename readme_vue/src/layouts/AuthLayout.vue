<template>
  <div class="flex min-h-screen flex-col font-sans">
    <GuestHeader />

    <main class="flex flex-1 items-center justify-center bg-[radial-gradient(circle_at_top,#f8fbff_0%,#eef4ff_35%,#f8fafc_100%)] px-4 py-10 sm:px-6 lg:px-8 lg:py-16">
      <div class="grid w-full max-w-6xl gap-8 lg:grid-cols-[1.05fr_0.95fr] lg:items-center">
        <section class="hidden rounded-[2.5rem] bg-brand-800 p-8 text-white shadow-2xl shadow-brand-900/20 xl:block">
          <p class="text-sm font-semibold uppercase tracking-[0.28em] text-brand-200">ReadMe Membership</p>
          <h1 class="mt-4 text-4xl font-bold leading-tight">
            좋은 책을
            <br>
            더 편안하게 만나는 방법
          </h1>
          <p class="mt-5 max-w-xl text-sm leading-7 text-brand-100">
            베스트셀러 탐색부터 주문과 결제, 배송 조회까지 한 흐름으로 이어지는 온라인 서점 경험을 제공합니다.
          </p>

          <div class="mt-10 grid gap-4 sm:grid-cols-3">
            <article class="rounded-3xl border border-white/15 bg-white/10 p-5 backdrop-blur">
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-100">혜택</p>
              <p class="mt-2 text-lg font-bold">첫 구매 쿠폰</p>
              <p class="mt-2 text-xs leading-6 text-brand-100">신규 회원 대상 할인과 추천 도서를 한눈에 확인할 수 있어요.</p>
            </article>
            <article class="rounded-3xl border border-white/15 bg-white/10 p-5 backdrop-blur">
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-100">주문</p>
              <p class="mt-2 text-lg font-bold">간편한 결제</p>
              <p class="mt-2 text-xs leading-6 text-brand-100">장바구니부터 주문, 결제 내역까지 한 번에 관리할 수 있어요.</p>
            </article>
            <article class="rounded-3xl border border-white/15 bg-white/10 p-5 backdrop-blur">
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-100">관리</p>
              <p class="mt-2 text-lg font-bold">배송 조회</p>
              <p class="mt-2 text-xs leading-6 text-brand-100">마이페이지에서 주문 상태와 배송 현황을 바로 확인할 수 있어요.</p>
            </article>
          </div>
        </section>

        <div class="flex justify-center">
          <RouterView />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import GuestHeader from '@/components/layout/GuestHeader.vue';
</script>
