<template>
  <header class="sticky top-0 z-40 border-b border-slate-200/80 bg-white/90 backdrop-blur">
    <div class="mx-auto flex w-full max-w-7xl items-center gap-3 px-4 py-3 lg:px-6">
      <router-link
        to="/"
        class="shrink-0 text-xl font-extrabold tracking-tight text-brand-800 transition hover:text-accent-500"
      >
        ReadMe
      </router-link>

      <div class="hidden flex-1 items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-2 py-2 shadow-sm md:flex">
        <input
          v-model="searchKeyword"
          class="min-w-0 flex-1 bg-transparent px-3 text-sm text-slate-700 outline-none placeholder:text-slate-400"
          type="text"
          placeholder="Search by title, author, or ISBN"
          @keyup.enter="handleSearch"
        >
        <select
          v-model="searchType"
          class="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-600 outline-none"
        >
          <option value="all">All</option>
          <option value="title">Title</option>
          <option value="author">Author</option>
          <option value="isbn">ISBN</option>
        </select>
        <button
          class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
          @click="handleSearch"
        >
          Search
        </button>
      </div>

      <nav class="ml-auto flex items-center gap-2 text-sm font-medium text-slate-600">
        <template v-if="!authStore.isLoggedIn">
          <router-link class="rounded-full px-3 py-2 transition hover:bg-slate-100 hover:text-brand-800" to="/signin">
            Sign in
          </router-link>
          <router-link class="rounded-full bg-accent-500 px-4 py-2 text-white transition hover:bg-accent-600" to="/signup">
            Sign up
          </router-link>
        </template>

        <template v-else>
          <router-link class="hidden rounded-full px-3 py-2 transition hover:bg-slate-100 hover:text-brand-800 md:inline-flex" to="/mypage/edit">
            Profile
          </router-link>
          <router-link class="rounded-full px-3 py-2 transition hover:bg-slate-100 hover:text-brand-800" to="/mypage">
            My Page
          </router-link>
          <router-link class="relative rounded-full px-3 py-2 transition hover:bg-slate-100 hover:text-brand-800" to="/cart">
            Cart
            <span
              v-if="cartStore.totalCount > 0"
              class="absolute -right-1 -top-1 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-accent-500 px-1 text-[11px] font-bold text-white"
            >
              {{ cartStore.totalCount }}
            </span>
          </router-link>
          <button
            class="rounded-full border border-slate-200 px-4 py-2 text-slate-600 transition hover:border-brand-200 hover:bg-brand-50 hover:text-brand-800"
            @click="handleLogout"
          >
            Sign out
          </button>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { useCartStore } from '@/store/cart';

const authStore = useAuthStore();
const cartStore = useCartStore();
const router = useRouter();

const searchKeyword = ref('');
const searchType = ref('all');

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
  router.push('/');
}
</script>
