<template>
  <section class="space-y-10">

    <!-- 히어로 배너 -->
    <div class="overflow-hidden rounded-[2rem] bg-brand-800 px-8 py-12 text-white shadow-xl">
      <div class="max-w-2xl space-y-4">
        <p class="text-xs font-semibold uppercase tracking-[0.24em] text-brand-200">교보문고 X ReadMe</p>
        <h1 class="text-4xl font-bold leading-tight tracking-tight md:text-5xl">
          오늘도 좋은 책과<br />함께하세요
        </h1>
        <p class="text-base leading-7 text-brand-100">
          국내도서부터 해외원서까지, 원하는 책을 쉽고 빠르게 만나보세요.
        </p>
        <div class="flex flex-wrap gap-3 pt-2">
          <router-link
            class="rounded-full bg-white px-6 py-3 text-sm font-bold text-brand-800 transition hover:bg-brand-50"
            to="/product"
          >
            도서 둘러보기
          </router-link>
          <template v-if="!authStore.isLoggedIn">
            <router-link
              class="rounded-full border border-white/30 px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
              to="/signin"
            >
              로그인
            </router-link>
          </template>
          <template v-else-if="authStore.isAdmin">
            <router-link
              class="rounded-full border border-white/30 px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
              to="/admin"
            >
              관리자 대시보드
            </router-link>
          </template>
          <template v-else>
            <router-link
              class="rounded-full border border-white/30 px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
              to="/mypage"
            >
              마이페이지
            </router-link>
          </template>
        </div>
      </div>
    </div>

    <!-- 카테고리 바로가기 -->
    <div>
      <h2 class="mb-4 text-lg font-bold text-slate-800">카테고리</h2>
      <div class="grid grid-cols-3 gap-3 sm:grid-cols-5">
        <router-link
          v-for="cat in quickCategories"
          :key="cat.id"
          :to="cat.to"
          class="flex flex-col items-center gap-2 rounded-2xl border border-slate-200 bg-white py-4 text-center text-xs font-semibold text-slate-600 shadow-sm transition hover:border-brand-800 hover:text-brand-800"
        >
          <span class="text-2xl">{{ cat.icon }}</span>
          {{ cat.name }}
        </router-link>
      </div>
    </div>

    <!-- 이번 주 추천 -->
    <div>
      <div class="mb-4 flex items-center justify-between">
        <h2 class="text-lg font-bold text-slate-800">이번 주 추천 도서</h2>
        <router-link
          class="text-sm font-medium text-brand-800 transition hover:text-accent-500"
          to="/product"
        >
          전체 보기 →
        </router-link>
      </div>
      <div class="grid gap-4 sm:grid-cols-2 md:grid-cols-4">
        <article
          v-for="book in featuredBooks"
          :key="book.id"
          class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md cursor-pointer"
          @click="$router.push(`/product/${book.id}`)"
        >
          <div class="mb-3 flex h-32 items-center justify-center rounded-xl bg-slate-100 text-4xl">
            📚
          </div>
          <p class="line-clamp-2 text-sm font-semibold text-slate-800">{{ book.title }}</p>
          <p class="mt-1 text-xs text-slate-400">{{ book.author }}</p>
          <p class="mt-2 font-bold text-brand-800">{{ book.price.toLocaleString() }}원</p>
        </article>
      </div>
    </div>

    <!-- 혜택 안내 -->
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div class="mb-2 text-2xl">🚚</div>
        <p class="font-bold text-slate-800">무료 배송</p>
        <p class="mt-1 text-sm text-slate-500">1만원 이상 구매 시 전국 무료 배송</p>
      </div>
      <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div class="mb-2 text-2xl">🔄</div>
        <p class="font-bold text-slate-800">간편 반품</p>
        <p class="mt-1 text-sm text-slate-500">수령 후 7일 이내 반품 가능</p>
      </div>
      <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div class="mb-2 text-2xl">💳</div>
        <p class="font-bold text-slate-800">다양한 결제</p>
        <p class="mt-1 text-sm text-slate-500">카드, 토스페이먼츠 등 간편 결제</p>
      </div>
    </div>

  </section>
</template>

<script setup>
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const $router   = useRouter()

// 카테고리 바로가기 (더미 — DB ID와 맞춰야 함)
const quickCategories = [
  { id: 1,  name: '소설',     icon: '📖', to: '/product?topId=1&subId=1'  },
  { id: 2,  name: '자기계발', icon: '🌱', to: '/product?topId=1&subId=2'  },
  { id: 3,  name: '경제/경영', icon: '💼', to: '/product?topId=1&subId=3' },
  { id: 4,  name: '과학/기술', icon: '🔬', to: '/product?topId=1&subId=4' },
  { id: 5,  name: '해외도서', icon: '🌍', to: '/product?topId=2'          },
]

// 추천 도서 (더미 — 실제 API 연결 후 교체)
const featuredBooks = [
  { id: 1, title: '채식주의자',         author: '한강',          price: 12000 },
  { id: 2, title: '아몬드',             author: '손원평',        price: 14000 },
  { id: 3, title: '82년생 김지영',      author: '조남주',        price: 13000 },
  { id: 4, title: '어린 왕자',          author: '생텍쥐페리',    price: 9800  },
]
</script>
