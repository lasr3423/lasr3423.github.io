<template>
  <aside class="hidden w-60 shrink-0 xl:block">
    <div class="sticky top-24 guide-sidebar">
      <p class="border-b border-slate-200 px-5 py-4 text-xs font-bold uppercase tracking-[0.24em] text-slate-400">
        주요 메뉴
      </p>
      <div class="p-3">
        <router-link
          v-for="item in quickMenus"
          :key="item.label"
          :to="item.to"
          :class="[
            'mb-2 block rounded-2xl px-4 py-3 text-sm font-semibold transition last:mb-0',
            isQuickMenuActive(item)
              ? 'bg-brand-800 text-white shadow-sm'
              : 'text-slate-700 hover:bg-brand-50 hover:text-brand-800',
          ]"
        >
          {{ item.label }}
        </router-link>
      </div>

      <p class="border-y border-slate-200 px-5 py-4 text-xs font-bold uppercase tracking-[0.24em] text-slate-400">
        카테고리
      </p>
      <div v-for="top in categories" :key="top.id" class="border-b border-slate-100 last:border-b-0">
        <button
          type="button"
          class="flex w-full items-center justify-between px-5 py-4 text-sm font-semibold transition"
          :class="activeTopId === top.id ? 'bg-brand-50 text-brand-800' : 'text-slate-700 hover:bg-slate-50 hover:text-brand-800'"
          @click="toggleTop(top.id)"
        >
          <span>{{ top.name }}</span>
          <span class="text-xs text-slate-400">{{ activeTopId === top.id ? '−' : '+' }}</span>
        </button>
        <div v-if="activeTopId === top.id" class="bg-slate-50 px-4 py-3">
          <router-link
            v-for="sub in top.subs"
            :key="sub.id"
            :to="`/product?topId=${top.id}&subId=${sub.id}`"
            class="mb-2 block rounded-xl px-4 py-3 text-sm font-medium text-slate-600 transition last:mb-0 hover:bg-white hover:text-brand-800"
            :class="isActiveSub(top.id, sub.id) ? 'bg-white text-brand-800 shadow-sm' : ''"
          >
            {{ sub.name }}
          </router-link>
        </div>
      </div>

      <p class="border-y border-slate-200 px-5 py-4 text-xs font-bold uppercase tracking-[0.24em] text-slate-400">
        고객 서비스
      </p>
      <div class="p-3">
        <router-link
          v-for="item in serviceMenus"
          :key="item.label"
          :to="item.to"
          :class="[
            'mb-2 block rounded-2xl px-4 py-3 text-sm font-semibold transition last:mb-0',
            isServiceMenuActive(item)
              ? 'bg-brand-800 text-white shadow-sm'
              : 'text-slate-700 hover:bg-brand-50 hover:text-brand-800',
          ]"
        >
          {{ item.label }}
        </router-link>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const quickMenus = [
  { label: '베스트셀러', to: '/product?sort=bestseller' },
  { label: '신간도서', to: '/product?sort=new' },
  { label: 'MD 추천', to: '/product?tag=md' },
]

const serviceMenus = [
  { label: '이벤트', to: '/product?tag=event' },
  { label: '상품 리뷰', to: '/review' },
  { label: 'QnA', to: '/qna' },
  { label: '공지사항', to: '/notice' },
]

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
]

const activeTopId = ref(route.query.topId ? Number(route.query.topId) : null)

watch(
  () => route.query.topId,
  (nextTopId) => {
    activeTopId.value = nextTopId ? Number(nextTopId) : null
  },
)

function toggleTop(id) {
  activeTopId.value = activeTopId.value === id ? null : id
}

function isActiveSub(topId, subId) {
  return Number(route.query.topId) === topId && Number(route.query.subId) === subId
}

function isQuickMenuActive(item) {
  return route.fullPath === item.to
}

function isServiceMenuActive(item) {
  if (item.to === '/review' || item.to === '/qna' || item.to === '/notice') {
    return route.path === item.to
  }
  return route.fullPath === item.to
}
</script>
