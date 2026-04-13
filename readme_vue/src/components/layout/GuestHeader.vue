<template>
  <header class="sticky top-0 z-50 bg-white/90 shadow-[0_10px_30px_rgba(15,23,42,0.06)] backdrop-blur">
    <div class="app-shell flex items-center gap-4 py-3">
      <router-link
        to="/"
        class="shrink-0 text-xl font-bold tracking-tight text-brand-800 transition hover:text-accent-500"
      >
        ReadMe
      </router-link>

      <div class="mx-auto flex max-w-2xl flex-1 overflow-hidden rounded-2xl bg-white shadow-[inset_0_1px_0_rgba(255,255,255,0.9),0_8px_24px_rgba(15,23,42,0.07)] transition">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="검색어를 입력하세요 (도서명, 저자)"
          class="flex-1 border-none bg-white px-4 py-2.5 text-sm text-slate-700 outline-none placeholder:text-slate-400"
          @keyup.enter="handleSearch"
        />
        <select
          v-model="searchType"
          class="bg-slate-50 px-3 py-2.5 text-sm text-slate-500 outline-none"
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

      <nav class="flex shrink-0 items-center gap-3">
        <router-link class="text-sm text-slate-600 transition hover:text-brand-800" to="/signin">
          로그인
        </router-link>
        <router-link
          class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
          to="/signup"
        >
          회원가입
        </router-link>
        <router-link
          class="text-sm font-medium text-slate-600 transition hover:text-brand-800"
          to="/cart"
        >
          장바구니
        </router-link>
      </nav>
    </div>

    <div class="app-shell flex items-center gap-6 overflow-x-auto py-2 pb-3 text-sm font-medium text-slate-600">
        <router-link
          v-for="item in primaryMenus"
          :key="item.label"
          :to="item.to"
          class="whitespace-nowrap transition hover:text-brand-800"
        >
          {{ item.label }}
        </router-link>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const searchKeyword = ref('');
const searchType = ref('title');

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
</script>
