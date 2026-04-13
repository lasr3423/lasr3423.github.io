<template>
  <footer class="mt-12 border-t border-slate-200 bg-slate-50">

    <!-- 상단: 브랜드 + 링크 -->
    <div class="mx-auto flex max-w-[1440px] flex-col items-center gap-10 px-6 py-10 text-center">

      <!-- 브랜드 -->
      <div class="w-52 shrink-0">
        <p class="text-lg font-bold text-brand-800">📚 ReadMe</p>
        <p class="mt-2 text-xs leading-[1.9] text-slate-400">
          도서 검색과 주문, 배송 조회를<br />이용할 수 있는 온라인 서점 ReadMe입니다.<br />국내외 도서를 확인하실 수 있습니다.
        </p>
        <div class="mt-4 flex flex-wrap justify-center gap-2">
          <a
            href="#"
            class="rounded border border-slate-300 px-3 py-1.5 text-[11px] text-slate-500 transition hover:border-brand-800 hover:text-brand-800"
          >🍎 App Store</a>
          <a
            href="#"
            class="rounded border border-slate-300 px-3 py-1.5 text-[11px] text-slate-500 transition hover:border-brand-800 hover:text-brand-800"
          >▶ Google Play</a>
        </div>
      </div>

      <!-- 링크 컬럼 4개 -->
      <div class="flex flex-wrap justify-center gap-8">

        <div class="flex min-w-[100px] flex-col items-center gap-2">
          <p class="mb-1 text-sm font-bold text-brand-800">고객 서비스</p>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">공지사항</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">자주 묻는 질문</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">1:1 문의</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">교환/반품 안내</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">배송 조회</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">이용 가이드</a>
        </div>

        <div class="flex min-w-[100px] flex-col items-center gap-2">
          <p class="mb-1 text-sm font-bold text-brand-800">회사 정보</p>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">회사 소개</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">채용 정보</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">언론 보도</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">투자자 관계</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">제휴 문의</a>
        </div>

        <div class="flex min-w-[100px] flex-col items-center gap-2">
          <p class="mb-1 text-sm font-bold text-brand-800">서비스</p>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">프리미엄 구독</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">전자책</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">오디오북</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">북클럽</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">기업 솔루션</a>
        </div>

        <div class="flex min-w-[100px] flex-col items-center gap-2">
          <p class="mb-1 text-sm font-bold text-brand-800">정책</p>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">개인정보처리방침</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">이용약관</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">저작권 정책</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">청소년 보호</a>
          <a href="#" class="text-xs text-slate-400 transition hover:text-accent-500">쿠키 정책</a>
        </div>

      </div>
    </div>

    <!-- 하단: 회사 정보 -->
    <div class="border-t border-slate-200 px-6 py-4">
      <div class="mx-auto max-w-[1440px] space-y-1 text-center">
        <p class="text-[11px] text-slate-400">
          (주)리드미 &nbsp;|&nbsp; 대표이사: 홍길동 &nbsp;|&nbsp;
          사업자등록번호: 123-45-67890 &nbsp;|&nbsp;
          통신판매업신고: 제2024-서울강남-12345호
        </p>
        <p class="text-[11px] text-slate-400">
          주소: 서울특별시 강남구 테헤란로 123, ReadMe빌딩 10층 &nbsp;|&nbsp;
          고객센터: 1588-0000 (평일 09:00~18:00) &nbsp;|&nbsp;
          이메일: help@readme.co.kr
        </p>
        <p class="text-[11px] text-slate-400">ⓒ 2025 ReadMe Corp. All rights reserved.</p>
      </div>
    </div>

  </footer>
</template>

<script setup>
// 별도 로직 없음
</script>
