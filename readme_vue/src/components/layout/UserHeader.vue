<template>
  <header class="sticky top-0 z-50 border-b border-slate-200 bg-white/95 shadow-sm backdrop-blur">
    <div class="app-shell flex items-center gap-4 py-3">
      <router-link
        to="/"
        class="shrink-0 text-xl font-bold tracking-tight text-brand-800 transition hover:text-accent-500"
      >
        ReadMe
      </router-link>

      <div class="mx-auto flex max-w-2xl flex-1 overflow-hidden rounded-lg border border-slate-200 transition focus-within:border-brand-800">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="검색어를 입력하세요 (도서명, 저자)"
          class="flex-1 border-none bg-white px-4 py-2.5 text-sm text-slate-700 outline-none placeholder:text-slate-400"
          @keyup.enter="handleSearch"
        />
        <select
          v-model="searchType"
          class="border-l border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-500 outline-none"
        >
          <option value="title">도서명</option>
          <option value="author">저자</option>
        </select>
        <button
          class="bg-brand-800 px-4 text-white transition hover:bg-brand-700"
          @click="handleSearch"
        >
          검색
        </button>
      </div>

      <nav class="flex shrink-0 items-center gap-4">
        <button
          class="text-sm text-slate-500 transition hover:text-brand-800"
          @click="handleLogout"
        >
          로그아웃
        </button>
        <router-link
          class="text-sm text-slate-600 transition hover:text-brand-800"
          to="/mypage/edit"
        >
          회원정보 변경
        </router-link>
        <router-link
          class="text-sm font-semibold text-slate-700 transition hover:text-brand-800"
          :to="dashboardLink.to"
        >
          {{ dashboardLink.label }}
        </router-link>
        <router-link
          class="relative text-sm font-medium text-slate-600 transition hover:text-brand-800"
          to="/cart"
        >
          장바구니
          <span
            v-if="cartStore.totalCount > 0"
            class="absolute -right-4 -top-2 flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-accent-500 px-1 text-[10px] font-bold text-white"
          >
            {{ cartStore.totalCount }}
          </span>
        </router-link>
      </nav>
    </div>

    <div class="border-t border-slate-100 bg-slate-50/90">
      <div class="app-shell flex items-center gap-6 overflow-x-auto py-2 text-sm font-medium text-slate-600">
        <router-link
          v-for="item in primaryMenus"
          :key="item.label"
          :to="item.to"
          class="whitespace-nowrap transition hover:text-brand-800"
        >
          {{ item.label }}
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { useCartStore } from '@/store/cart';

const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore();

const searchKeyword = ref('');
const searchType = ref('title');
const dashboardLink = authStore.isAdmin
  ? { label: '대시보드', to: '/admin' }
  : { label: '마이페이지', to: '/mypage' };

const primaryMenus = [
  { label: '베스트셀러', to: '/product?sort=bestseller' },
  { label: '신간도서', to: '/product?sort=new' },
  { label: '이벤트', to: '/product?tag=event' },
  { label: 'MD 추천', to: '/product?tag=md' },
  { label: '공지사항', to: '/notice' },
];

function handleSearch() {
  if (!searchKeyword.value.trim()) return;

  router.push({
    path: '/product',
    query: {
      keyword: searchKeyword.value.trim(),
      searchType: searchType.value,
    },
  });
}

async function handleLogout() {
  await authStore.signout();
  alert('로그아웃 되었습니다.');
  router.push('/');
}
</script>
