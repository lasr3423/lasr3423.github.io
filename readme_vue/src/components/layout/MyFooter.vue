<template>
  <footer class="app-footer">
    <div class="footer-top">
      <div class="footer-brand">
        <p class="brand-logo">📚 ReadMe</p>
        <p class="brand-desc">책을 사랑하는 모든 분들을 위한<br />온라인 서점 ReadMe입니다.<br />국내외 최신 도서를 빠르게 만나보세요.</p>
        <div class="store-links">
          <a href="#" class="store-btn">🍎 App Store</a>
          <a href="#" class="store-btn">▶ Google Play</a>
        </div>
      </div>

      <div class="footer-links">
        <div class="footer-col">
          <p class="col-title">고객 서비스</p>
          <a href="#">공지사항</a>
          <a href="#">자주 묻는 질문</a>
          <a href="#">1:1 문의</a>
          <a href="#">교환/반품 안내</a>
          <a href="#">배송 조회</a>
          <a href="#">이용 가이드</a>
        </div>
        <div class="footer-col">
          <p class="col-title">회사 정보</p>
          <a href="#">회사 소개</a>
          <a href="#">채용 정보</a>
          <a href="#">언론 보도</a>
          <a href="#">투자자 관계</a>
          <a href="#">제휴 문의</a>
        </div>
        <div class="footer-col">
          <p class="col-title">서비스</p>
          <a href="#">프리미엄 구독</a>
          <a href="#">전자책</a>
          <a href="#">오디오북</a>
          <a href="#">북클럽</a>
          <a href="#">기업 솔루션</a>
        </div>
        <div class="footer-col">
          <p class="col-title">정책</p>
          <a href="#">개인정보처리방침</a>
          <a href="#">이용약관</a>
          <a href="#">저작권 정책</a>
          <a href="#">청소년 보호</a>
          <a href="#">쿠키 정책</a>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <p class="company-info">
        (주)리드미 | 대표이사: 홍길동 &nbsp;|&nbsp;
        사업자등록번호: 123-45-67890 &nbsp;|&nbsp;
        통신판매업신고: 제2024-서울강남-12345호
      </p>
      <p class="company-info">
        주소: 서울특별시 강남구 테헤란로 123, ReadMe빌딩 10층 &nbsp;|&nbsp;
        고객센터: 1588-0000 (평일 09:00~18:00) &nbsp;|&nbsp;
        이메일: help@readme.co.kr
      </p>
      <p class="copyright">ⓒ 2025 ReadMe Corp. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup>
// 별도 로직 없음 (임시 푸터)
</script>

<style scoped>
.app-footer {
  background: var(--color-white2);
  border-top: 1px solid var(--color-gray5);
  margin-top: var(--space-6);
}
.footer-top {
  display: flex;
  gap: var(--space-5);
  max-width: 1440px;
  margin: 0 auto;
  padding: var(--space-5) var(--space-2) var(--space-4);
}
.footer-brand {
  width: 220px;
  flex-shrink: 0;
}
.brand-logo {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: var(--space-1);
  font-family: var(--font-sans);
}
.brand-desc {
  font-size: 12px;
  color: var(--color-gray3);
  line-height: 1.8;
  margin-bottom: var(--space-2);
}
.store-links { display: flex; gap: var(--space-1); flex-wrap: wrap; }
.store-btn {
  font-size: 11px;
  padding: 6px 12px;
  border: 1px solid var(--color-gray5);
  border-radius: 4px;
  text-decoration: none;
  color: var(--color-gray2);
  background: var(--color-white1);
  transition: border-color 0.2s, color 0.2s;
  font-family: var(--font-sans);
}
.store-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.footer-links {
  flex: 1;
  display: flex;
  gap: var(--space-4);
  flex-wrap: wrap;
}
.footer-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 100px;
}
.col-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 4px;
  font-family: var(--font-sans);
}
.footer-col a {
  font-size: 12px;
  color: var(--color-gray3);
  text-decoration: none;
  transition: color 0.2s;
  font-family: var(--font-sans);
}
.footer-col a:hover { color: var(--color-secondary); }
.footer-bottom {
  border-top: 1px solid var(--color-gray5);
  max-width: 1440px;
  margin: 0 auto;
  padding: var(--space-2) var(--space-2);
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.company-info { font-size: 11px; color: var(--color-gray4); }
.copyright { font-size: 11px; color: var(--color-gray4); margin-top: 4px; }

@media (max-width: 768px) {
  .footer-top { flex-direction: column; }
  .footer-brand { width: 100%; }
}
</style>
