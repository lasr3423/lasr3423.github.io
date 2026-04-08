<template>
  <div class="flex min-h-screen flex-col font-sans">
    <GuestHeader />

    <main class="flex flex-1 items-center justify-center bg-[radial-gradient(circle_at_top,#f8fbff_0%,#eef4ff_35%,#f8fafc_100%)] px-4 py-12">
      <div class="grid w-full max-w-6xl gap-10 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
        <section class="hidden rounded-[2.5rem] bg-brand-800 p-10 text-white shadow-2xl shadow-brand-900/20 lg:block">
          <p class="text-sm font-semibold uppercase tracking-[0.28em] text-brand-200">ReadMe Membership</p>
          <h1 class="mt-4 text-4xl font-bold leading-tight">
            읽고 싶은 책을
            <br>
            가장 빠르게 만나는 방법
          </h1>
          <p class="mt-5 max-w-xl text-sm leading-7 text-brand-100">
            베스트셀러, 신간도서, 이벤트 도서까지 한 곳에서 찾고
            주문과 결제, 배송 조회까지 편리하게 이용해보세요.
          </p>

          <div class="mt-10 grid gap-4 sm:grid-cols-3">
            <article class="rounded-3xl border border-white/15 bg-white/10 p-5 backdrop-blur">
              <p class="text-xs font-semibold uppercase tracking-[0.18em] text-brand-100">혜택</p>
              <p class="mt-2 text-lg font-bold">첫 구매 쿠폰</p>
              <p class="mt-2 text-xs leading-6 text-brand-100">신규 회원 전용 할인과 추천 도서를 확인할 수 있어요.</p>
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
