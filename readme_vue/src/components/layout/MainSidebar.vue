<template>
  <aside class="hidden w-60 shrink-0 xl:block">
    <div class="sticky top-28">
      <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">

        <!-- 주요 메뉴 -->
        <p class="border-b border-slate-200 px-4 py-3 text-xs font-bold uppercase tracking-widest text-slate-400">주요 메뉴</p>
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

        <!-- 카테고리 -->
        <p class="border-y border-slate-200 px-4 py-3 text-xs font-bold uppercase tracking-widest text-slate-400">카테고리</p>
        <div v-for="top in categories" :key="top.id" class="border-b border-slate-100 last:border-b-0">
          <button
            class="flex w-full items-center justify-between px-4 py-2.5 text-sm font-semibold transition"
            :class="activeTopId === top.id ? 'text-brand-800' : 'text-slate-700 hover:text-brand-800'"
            @click="toggleTop(top.id)"
          >
            <span>{{ top.name }}</span>
            <span class="text-slate-400 transition-transform duration-200" :class="activeTopId === top.id ? 'rotate-90' : ''">›</span>
          </button>
          <div v-if="activeTopId === top.id" class="space-y-0.5 px-4 pb-2">
            <router-link
              v-for="sub in top.subs"
              :key="sub.id"
              :to="`/product?topId=${top.id}&subId=${sub.id}`"
              class="block rounded-lg px-3 py-1.5 text-xs transition"
              :class="isActiveSub(top.id, sub.id)
                ? 'bg-brand-50 font-bold text-brand-800'
                : 'text-slate-500 hover:bg-slate-50 hover:text-brand-800'"
            >
              {{ sub.name }}
            </router-link>
          </div>
        </div>

        <!-- 고객 서비스 -->
        <p class="border-y border-slate-200 px-4 py-3 text-xs font-bold uppercase tracking-widest text-slate-400">고객 서비스</p>
        <div class="p-2">
          <router-link
            v-for="item in serviceMenus"
            :key="item.label"
            :to="item.to"
            class="block rounded-xl px-3 py-2 text-sm font-medium text-slate-600 transition hover:bg-brand-50 hover:text-brand-800"
            active-class="bg-brand-50 text-brand-800"
          >
            {{ item.label }}
          </router-link>
        </div>

      </div>
    </div>
  </aside>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { categoryApi } from '@/api/category'

const route = useRoute()

const quickMenus = [
  { label: '베스트셀러', to: '/product?sort=bestseller' },
  { label: '신간도서',   to: '/product?sort=new' },
  { label: 'MD 추천',   to: '/product?tag=md' },
]

const serviceMenus = [
  { label: '이벤트',    to: '/product?tag=event' },
  { label: '상품 리뷰', to: '/review' },
  { label: 'QnA',       to: '/qna' },
  { label: '공지사항',   to: '/notice' },
]

const categories = ref([])

const activeTopId = ref(Number(route.query.topId) || 1)

async function fetchCategories() {
  try {
    const { data } = await categoryApi.getTopCategories()
    categories.value = Array.isArray(data)
      ? data.map((top) => ({
          id: Number(top.id),
          name: top.name,
          subs: Array.isArray(top.subCategories)
            ? top.subCategories.map((sub) => ({
                id: Number(sub.id),
                name: sub.name,
              }))
            : [],
        }))
      : []

    if (!route.query.topId && categories.value.length > 0) {
      activeTopId.value = categories.value[0].id
    }
  } catch (error) {
    console.error('사이드바 카테고리 조회 실패', error)
    categories.value = []
  }
}

function toggleTop(id) {
  activeTopId.value = activeTopId.value === id ? null : id
}

function isActiveSub(topId, subId) {
  return Number(route.query.topId) === topId && Number(route.query.subId) === subId
}

watch(
  () => route.query.topId,
  (topId) => {
    activeTopId.value = topId ? Number(topId) : (categories.value[0]?.id ?? null)
  },
  { immediate: true },
)

onMounted(() => {
  fetchCategories()
})
</script>
