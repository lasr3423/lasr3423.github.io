<template>
  <section class="space-y-10">
    <div class="grid gap-6 xl:grid-cols-[1.45fr_0.75fr]">
      <article class="relative overflow-hidden rounded-[2rem] bg-brand-800 px-8 py-10 text-white shadow-2xl shadow-brand-900/20">
        <div class="absolute right-0 top-0 h-52 w-52 rounded-full bg-white/10 blur-3xl"></div>
        <div class="absolute bottom-0 left-16 h-28 w-28 rounded-full bg-accent-400/30 blur-2xl"></div>

        <div class="relative max-w-2xl space-y-5">
          <span class="point-chip !bg-white/10 !text-brand-100">봄 시즌 큐레이션</span>
          <h1 class="text-4xl font-bold leading-tight tracking-tight text-white md:text-5xl">
            이달의 도서를
            <br>
            ReadMe에서 확인하세요
          </h1>
          <p class="max-w-xl text-sm leading-7 text-brand-100 md:text-base">
            베스트셀러와 신간, 이벤트 도서를 둘러보고
            주문부터 배송 조회까지 한곳에서 확인하실 수 있습니다.
          </p>
          <div class="flex flex-wrap gap-3 pt-2">
            <router-link
              class="rounded-full bg-white px-6 py-3 text-sm font-bold text-brand-800 transition hover:bg-brand-50"
              to="/product"
            >
              전체 도서 보러가기
            </router-link>
            <router-link
              class="rounded-full border border-white/25 px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
              :to="authStore.isLoggedIn ? '/mypage' : '/signin'"
            >
              {{ authStore.isLoggedIn ? '마이페이지' : '로그인' }}
            </router-link>
          </div>
        </div>
      </article>

      <aside class="grid gap-4 md:grid-cols-2 xl:grid-cols-1">
      <article class="surface-panel card-fixed p-6">
          <span class="point-chip">이번 주 이벤트</span>
          <h2 class="mt-4 text-xl font-bold text-slate-900">첫 구매 회원 10% 할인</h2>
          <p class="card-copy-2 mt-2 text-sm leading-6 text-slate-500">
            회원가입 후 첫 주문에 사용할 수 있는 할인 혜택 안내입니다.
          </p>
          <router-link class="mt-5 inline-flex text-sm font-semibold text-brand-800 hover:text-accent-500" to="/signup">
            혜택 안내
          </router-link>
        </article>

        <article class="card-fixed overflow-hidden rounded-[2rem] border border-accent-100 bg-gradient-to-br from-accent-50 to-white p-6 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-[0.2em] text-accent-600">도서 안내</p>
          <h2 class="mt-4 text-xl font-bold text-slate-900">카테고리별 도서 안내</h2>
          <ul class="mt-4 space-y-3 text-sm text-slate-600">
            <li>베스트셀러와 신간 도서 확인</li>
            <li>카테고리별 도서 바로가기</li>
            <li>주문 후 배송 상태 조회</li>
          </ul>
        </article>
      </aside>
    </div>

    <section class="grid gap-4 md:grid-cols-3">
      <article v-for="point in servicePoints" :key="point.title" class="surface-panel card-fixed p-5">
        <div class="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-50 text-2xl">
          {{ point.icon }}
        </div>
        <h3 class="text-lg font-bold text-slate-900">{{ point.title }}</h3>
        <p class="card-copy-3 mt-2 text-sm leading-6 text-slate-500">{{ point.description }}</p>
      </article>
    </section>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Quick Category</p>
          <h2 class="section-title mt-2">카테고리 바로가기</h2>
        </div>
        <router-link class="text-sm font-semibold text-brand-800 transition hover:text-accent-500" to="/product">
          전체 카테고리 보기
        </router-link>
      </div>

      <div class="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-5">
        <router-link
          v-for="cat in quickCategories"
          :key="cat.id"
          :to="cat.to"
          class="surface-soft card-fixed min-h-[7.5rem] items-center gap-3 px-4 py-5 text-center transition hover:-translate-y-0.5 hover:border-brand-200 hover:bg-white"
        >
          <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-50 text-2xl">
            {{ cat.icon }}
          </span>
          <span class="text-sm font-semibold text-slate-700">{{ cat.name }}</span>
        </router-link>
      </div>
    </section>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Featured</p>
          <h2 class="section-title mt-2">추천 도서</h2>
        </div>
        <router-link class="text-sm font-semibold text-brand-800 transition hover:text-accent-500" to="/product">
          더 보기
        </router-link>
      </div>

      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <article
          v-for="book in featuredBooks"
          :key="book.id"
          class="surface-soft card-fixed cursor-pointer overflow-hidden p-4 transition hover:-translate-y-0.5 hover:shadow-md"
          @click="router.push(`/product/${book.id}`)"
        >
          <div class="mb-4 flex h-40 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-50 to-slate-100 text-4xl">
            {{ book.icon }}
          </div>
          <span class="point-chip">{{ book.badge }}</span>
          <p class="card-title-2 mt-3 text-base font-bold text-slate-900">{{ book.title }}</p>
          <p class="card-meta-1 mt-1 text-sm text-slate-500">{{ book.author }}</p>
          <p class="numeric-stable mt-auto pt-4 text-lg font-bold text-brand-800">{{ book.price.toLocaleString() }}원</p>
        </article>
      </div>
    </section>

    <section class="surface-panel p-6">
      <div class="mb-5 flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-brand-700">Best Seller</p>
          <h2 class="section-title mt-2">지금 많이 찾는 도서</h2>
        </div>
        <router-link class="text-sm font-semibold text-brand-800 transition hover:text-accent-500" to="/product?sort=bestseller">
          베스트셀러 더 보기
        </router-link>
      </div>

      <div class="grid gap-4 lg:grid-cols-5">
        <article
          v-for="book in bestSellers"
          :key="book.id"
          class="card-fixed rounded-[1.75rem] border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:border-accent-200 hover:shadow-md"
        >
          <div class="mb-3 flex items-center justify-between">
            <span class="flex h-9 w-9 items-center justify-center rounded-full bg-accent-50 text-sm font-bold text-accent-600">
              {{ book.rank }}
            </span>
            <span class="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">BEST</span>
          </div>
          <p class="card-title-2 text-sm font-bold text-slate-900">{{ book.title }}</p>
          <p class="card-meta-1 mt-2 text-xs text-slate-500">{{ book.author }}</p>
          <p class="numeric-stable mt-auto pt-4 text-base font-bold text-brand-800">{{ book.price.toLocaleString() }}원</p>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup>
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const servicePoints = [
  { icon: '🚚', title: '배송 안내', description: '주문 후 배송 준비부터 배송 완료까지 상태를 확인하실 수 있습니다.' },
  { icon: '🎁', title: '회원 혜택', description: '회원가입 후 첫 구매 할인과 이벤트 혜택을 안내합니다.' },
  { icon: '💳', title: '결제 안내', description: '주문 페이지에서 결제 수단과 결제 내역을 확인하실 수 있습니다.' },
];

const quickCategories = [
  { id: 1, name: '한국소설', icon: '📖', to: '/product?topId=1&subId=1' },
  { id: 2, name: '경제/경영', icon: '📈', to: '/product?topId=1&subId=4' },
  { id: 3, name: '외국도서', icon: '🌍', to: '/product?topId=2' },
  { id: 4, name: '그림책', icon: '🧸', to: '/product?topId=3&subId=11' },
  { id: 5, name: '프로그래밍', icon: '💻', to: '/product?topId=4&subId=20' },
];

const featuredBooks = [
  { id: 1, title: '테스트 도서 01', author: '테스트 저자 01', price: 14900, badge: '추천', icon: '🌙' },
  { id: 2, title: '테스트 도서 02', author: '테스트 저자 02', price: 15800, badge: '신간', icon: '📘' },
  { id: 3, title: '테스트 도서 03', author: '테스트 저자 03', price: 16700, badge: 'MD Pick', icon: '🌿' },
  { id: 4, title: '테스트 도서 04', author: '테스트 저자 04', price: 17600, badge: '인기', icon: '🏙️' },
];

const bestSellers = [
  { rank: '1', id: 5, title: '테스트 도서 05', author: '테스트 저자 05', price: 18500 },
  { rank: '2', id: 6, title: '테스트 도서 06', author: '테스트 저자 06', price: 19400 },
  { rank: '3', id: 7, title: '테스트 도서 07', author: '테스트 저자 07', price: 20300 },
  { rank: '4', id: 8, title: '테스트 도서 08', author: '테스트 저자 08', price: 21200 },
  { rank: '5', id: 9, title: '테스트 도서 09', author: '테스트 저자 09', price: 22100 },
];
</script>
