<template>
  <div class="flex min-h-[60vh] flex-col items-center justify-center gap-6 text-center">
    <p class="text-7xl font-bold text-slate-200">404</p>
    <h1 class="text-2xl font-bold text-slate-800">페이지를 찾을 수 없습니다</h1>
    <p class="text-sm text-slate-500">요청하신 페이지가 존재하지 않거나 이동되었습니다.</p>
    <router-link
      to="/"
      class="rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
    >
      홈으로 돌아가기
    </router-link>
  </div>
</template>
