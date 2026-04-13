<template>
  <section class="page-section">
    <div class="mx-auto max-w-xl">
      <div class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
        <!-- 상단 배너 -->
        <div class="bg-gradient-to-br from-brand-800 to-brand-600 px-8 py-12 text-center text-white">
          <p class="text-xs font-bold uppercase tracking-[0.3em] text-white/60">Event</p>
          <h1 class="mt-3 text-3xl font-black tracking-tight">이벤트</h1>
          <p class="mt-2 text-sm text-white/70">ReadMe의 특별한 혜택을 만나보세요</p>
        </div>

        <!-- 준비중 안내 -->
        <div class="px-8 py-12 text-center">
          <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 text-2xl">
            🛠️
          </div>
          <h2 class="mt-5 text-xl font-bold text-slate-900">페이지 준비 중입니다</h2>
          <p class="mt-3 text-sm leading-6 text-slate-500">
            더 좋은 이벤트와 혜택을 준비하고 있습니다.<br />
            조금만 기다려 주세요!
          </p>

          <div class="mt-8 rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-6 py-5 text-left space-y-3">
            <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">예정 콘텐츠</p>
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <span class="text-base">🎁</span> 신규 회원 웰컴 쿠폰 이벤트
            </div>
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <span class="text-base">📚</span> 월별 MD 추천 도서 특가전
            </div>
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <span class="text-base">⭐</span> 리뷰 작성 시 적립금 증정
            </div>
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <span class="text-base">🏷️</span> 시즌 한정 할인 이벤트
            </div>
          </div>

          <div class="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
            <button
              class="rounded-xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
              @click="router.push('/')"
            >
              홈으로 돌아가기
            </button>
            <button
              class="rounded-xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              @click="router.push('/product')"
            >
              상품 둘러보기
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>
