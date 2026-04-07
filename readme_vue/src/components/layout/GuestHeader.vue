<template>
  <header class="sticky top-0 z-50 border-b border-slate-200 bg-white shadow-sm">
    <div class="mx-auto flex max-w-[1440px] items-center gap-4 px-4 py-3">

      <!-- 로고 -->
      <router-link
        to="/"
        class="shrink-0 text-xl font-bold tracking-tight text-brand-800 transition hover:text-accent-500"
      >
        📖 ReadMe
      </router-link>

      <!-- 검색 바 -->
      <div class="mx-auto flex flex-1 max-w-2xl overflow-hidden rounded-lg border border-slate-200 transition focus-within:border-brand-800">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="책 제목, 저자, ISBN을 입력하세요"
          class="flex-1 border-none bg-white px-4 py-2.5 text-sm text-slate-700 outline-none placeholder:text-slate-400"
          @keyup.enter="handleSearch"
        />
        <select
          v-model="searchType"
          class="border-l border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-500 outline-none"
        >
          <option value="all">전체</option>
          <option value="title">도서명</option>
          <option value="author">저자</option>
          <option value="isbn">ISBN</option>
        </select>
        <button
          class="bg-brand-800 px-4 text-white transition hover:bg-brand-700"
          @click="handleSearch"
        >
          🔍
        </button>
      </div>

      <!-- 비회원 메뉴 -->
      <nav class="flex shrink-0 items-center gap-3">
        <router-link
          class="text-sm text-slate-600 transition hover:text-brand-800"
          to="/signin"
        >
          로그인
        </router-link>
        <router-link
          class="rounded-lg bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
          to="/signup"
        >
          회원가입
        </router-link>
        <!-- 비회원도 장바구니 접근 가능 -->
        <router-link
          class="text-2xl leading-none transition hover:opacity-70"
          to="/cart"
        >
          🛒
        </router-link>
      </nav>

    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router        = useRouter()
const searchKeyword = ref('')
const searchType    = ref('all')

function handleSearch() {
  if (!searchKeyword.value.trim()) return
  router.push({
    path: '/product',
    query: {
      keyword:    searchKeyword.value.trim(),
      searchType: searchType.value,
    },
  })
}
</script>
