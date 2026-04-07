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

      <!-- 로그인 회원 메뉴 -->
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
          회원정보
        </router-link>
        <router-link
          class="text-sm font-semibold text-slate-700 transition hover:text-brand-800"
          to="/mypage"
        >
          마이페이지
        </router-link>

        <!-- 장바구니 (수량 뱃지) -->
        <router-link
          class="relative text-2xl leading-none transition hover:opacity-70"
          to="/cart"
        >
          🛒
          <span
            v-if="cartStore.totalCount > 0"
            class="absolute -right-2 -top-1.5 flex h-[17px] w-[17px] items-center justify-center rounded-full bg-accent-500 text-[10px] font-bold text-white"
          >
            {{ cartStore.totalCount }}
          </span>
        </router-link>
      </nav>

    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'

const router    = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

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

async function handleLogout() {
  await authStore.signout()
  alert('로그아웃 되었습니다.')
  router.push('/')
}
</script>
