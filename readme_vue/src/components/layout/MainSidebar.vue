<template>
  <aside class="hidden w-60 shrink-0 xl:block">
    <div class="sticky top-28 space-y-4">
      <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <p class="border-b border-slate-200 px-4 py-3 text-sm font-bold text-brand-800">
          주요 메뉴
        </p>
        <div class="p-2">
          <router-link
            v-for="item in quickMenus"
            :key="item.label"
            :to="item.to"
            class="block rounded-xl px-3 py-2 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-50 text-brand-800"
          >
            {{ item.label }}
          </router-link>
        </div>
      </div>

      <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <p class="border-b border-slate-200 px-4 py-3 text-sm font-bold text-brand-800">
          카테고리
        </p>

        <div v-for="top in categories" :key="top.id" class="border-b border-slate-100 last:border-b-0">
          <button
            class="flex w-full items-center justify-between px-4 py-3 text-sm font-semibold transition"
            :class="activeTopId === top.id ? 'text-brand-800' : 'text-slate-700 hover:text-brand-800'"
            @click="toggleTop(top.id)"
          >
            <span>{{ top.name }}</span>
            <span class="text-slate-400 transition-transform" :class="activeTopId === top.id ? 'rotate-90' : ''">
              >
            </span>
          </button>

          <div v-if="activeTopId === top.id" class="space-y-1 px-4 pb-3">
            <router-link
              v-for="sub in top.subs"
              :key="sub.id"
              :to="`/product?topId=${top.id}&subId=${sub.id}`"
              class="block rounded-lg px-3 py-2 text-xs transition"
              :class="isActiveSub(top.id, sub.id)
                ? 'bg-brand-50 font-bold text-brand-800'
                : 'text-slate-500 hover:bg-slate-50 hover:text-brand-800'"
            >
              {{ sub.name }}
            </router-link>
          </div>
        </div>
      </div>

      <div class="rounded-2xl border border-amber-200 bg-amber-50 p-4 shadow-sm">
        <p class="text-xs font-semibold uppercase tracking-[0.18em] text-amber-700">이벤트</p>
        <p class="mt-2 text-sm font-bold text-slate-800">첫 구매 회원 10% 쿠폰</p>
        <p class="mt-1 text-xs leading-5 text-slate-500">
          회원가입 후 메인 페이지에서 바로 사용할 수 있는 웰컴 쿠폰을 확인해보세요.
        </p>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const quickMenus = [
  { label: '베스트셀러', to: '/product?sort=bestseller' },
  { label: '신간도서', to: '/product?sort=new' },
  { label: 'MD 추천', to: '/product?tag=md' },
  { label: '이벤트', to: '/product?tag=event' },
  { label: '공지사항', to: '/notice' },
];

const categories = [
  {
    id: 1,
    name: '국내도서',
    subs: [
      { id: 1, name: '소설' },
      { id: 2, name: 'IT/컴퓨터' },
      { id: 3, name: '자기계발' },
      { id: 4, name: '경제/경영' },
      { id: 5, name: '역사/문화' },
    ],
  },
  {
    id: 2,
    name: '외국도서',
    subs: [
      { id: 6, name: '소설' },
      { id: 7, name: '자기계발' },
      { id: 8, name: '경제/경영' },
      { id: 9, name: '과학' },
      { id: 10, name: '역사' },
    ],
  },
  {
    id: 3,
    name: '일본도서',
    subs: [
      { id: 11, name: '소설' },
      { id: 12, name: '자기계발' },
      { id: 13, name: '비즈니스' },
      { id: 14, name: '과학' },
      { id: 15, name: '역사' },
    ],
  },
];

const activeTopId = ref(Number(route.query.topId) || 1);

function toggleTop(id) {
  activeTopId.value = activeTopId.value === id ? null : id;
}

function isActiveSub(topId, subId) {
  return Number(route.query.topId) === topId && Number(route.query.subId) === subId;
}
</script>
