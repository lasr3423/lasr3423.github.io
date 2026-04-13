<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">My Account</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">마이페이지</h1>
      <p class="mt-2 text-sm text-slate-500">내 정보와 주문, 결제, 활동 내역을 한곳에서 편하게 관리해 보세요.</p>
    </section>

    <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

    <template v-else-if="member">
      <!-- 프로필 카드 -->
      <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
        <div class="flex items-center gap-6">
          <div class="flex h-20 w-20 shrink-0 items-center justify-center rounded-full bg-brand-800 text-2xl font-bold text-white">
            {{ member.name?.[0]?.toUpperCase() ?? '?' }}
          </div>
          <div>
            <p class="text-xl font-bold text-slate-900">{{ member.name }}</p>
            <p class="mt-1 text-sm text-slate-500">{{ member.email }}</p>
            <span class="mt-2 inline-block rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-700">
              {{ member.memberRole }}
            </span>
          </div>
        </div>
        <div class="mt-6 grid grid-cols-1 gap-4 border-t border-slate-100 pt-6 sm:grid-cols-3">
          <div>
            <p class="text-xs font-medium uppercase tracking-widest text-slate-400">전화번호</p>
            <p class="mt-1 text-sm font-semibold text-slate-700">{{ member.phone || '미등록' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase tracking-widest text-slate-400">주소</p>
            <p class="mt-1 text-sm font-semibold text-slate-700">{{ member.address || '미등록' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase tracking-widest text-slate-400">가입 방식</p>
            <p class="mt-1 text-sm font-semibold text-slate-700">{{ member.provider }}</p>
          </div>
        </div>
      </section>

      <!-- 퀵메뉴 -->
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <router-link
          v-for="card in menuCards" :key="card.to" :to="card.to"
          class="group flex items-start gap-4 rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm transition hover:border-brand-200 hover:shadow-md"
        >
          <span class="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-brand-50 text-xl group-hover:bg-brand-100">{{ card.icon }}</span>
          <div>
            <p class="font-semibold text-slate-900">{{ card.title }}</p>
            <p class="mt-1 text-sm text-slate-500">{{ card.desc }}</p>
          </div>
        </router-link>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { memberApi } from '@/api/member';

const member = ref(null);
const loading = ref(true);

const menuCards = [
  { to: '/mypage/order',    icon: '📦', title: '주문 내역',     desc: '나의 주문 목록을 확인하세요' },
  { to: '/mypage/payment',  icon: '💳', title: '결제 내역',     desc: '결제 기록을 확인하세요' },
  { to: '/mypage/qna',      icon: '💬', title: '내 문의 (QnA)', desc: '내가 작성한 문의를 확인하세요' },
  { to: '/mypage/review',   icon: '⭐', title: '내 리뷰',       desc: '내가 남긴 상품 리뷰를 관리하세요' },
  { to: '/mypage/edit',     icon: '✏️', title: '정보 수정',     desc: '이름, 연락처, 주소를 변경하세요' },
  { to: '/mypage/password', icon: '🔒', title: '비밀번호 변경', desc: '현재 비밀번호를 변경하세요' },
  { to: '/cart',            icon: '🛒', title: '장바구니',      desc: '담아둔 상품을 확인하세요' },
  { to: '/mypage/withdraw', icon: '⚠️', title: '회원 탈퇴',     desc: '계정을 삭제합니다' },
];

onMounted(async () => {
  try {
    const { data } = await memberApi.getMe();
    member.value = data;
  } catch (e) {
    console.error('내 정보 로드 실패:', e);
  } finally {
    loading.value = false;
  }
});
</script>
