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
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { categoryApi } from '@/api/category'

const route = useRoute()

const quickMenus = [
  { label: '베스트셀러', to: '/product?sort=bestseller' },
  { label: '신간도서', to: '/product?sort=new' },
  { label: 'MD 추천', to: '/product?tag=md' },
]

const serviceMenus = [
  { label: '이벤트', to: '/event' },
  { label: '상품 리뷰', to: '/review' },
  { label: 'QnA', to: '/qna' },
  { label: '공지사항', to: '/notice' },
]

const categories = ref([])

const activeTopId = ref(route.query.topId ? Number(route.query.topId) : null)

watch(
  () => route.query.topId,
  (nextTopId) => {
    activeTopId.value = nextTopId ? Number(nextTopId) : null
  },
)

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

function isMenuActive(item) {
  const [path, queryString = ''] = item.to.split('?')

  if (route.path !== path) {
    return false
  }

  if (!queryString) {
    return true
  }

  const params = new URLSearchParams(queryString)
  return Array.from(params.entries()).every(([key, value]) => String(route.query[key] ?? '') === value)
}

function isQuickMenuActive(item) {
  return isMenuActive(item)
}

function isServiceMenuActive(item) {
  return isMenuActive(item)
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
