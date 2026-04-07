<template>
  <aside class="w-44 shrink-0">
    <div class="sticky top-[72px] rounded-xl border border-slate-200 bg-white py-3 shadow-sm">

      <p class="border-b-2 border-brand-800 px-4 pb-2 text-sm font-bold text-brand-800">카테고리</p>

      <div v-for="top in categories" :key="top.id" class="mt-1">

        <!-- 대분류 -->
        <button
          class="flex w-full items-center justify-between px-4 py-2 text-sm font-semibold transition"
          :class="activeTopId === top.id ? 'text-brand-800' : 'text-slate-600 hover:text-accent-500'"
          @click="toggleTop(top.id)"
        >
          {{ top.name }}
          <span
            class="text-base text-slate-400 transition-transform duration-200"
            :class="activeTopId === top.id ? 'rotate-90' : ''"
          >›</span>
        </button>

        <!-- 소분류 -->
        <div v-if="activeTopId === top.id" class="mb-1 flex flex-col pl-5">
          <router-link
            v-for="sub in top.subs"
            :key="sub.id"
            :to="`/product?topId=${top.id}&subId=${sub.id}`"
            class="rounded px-3 py-1.5 text-xs transition"
            :class="isActiveSub(top.id, sub.id)
              ? 'font-bold text-accent-500'
              : 'text-slate-500 hover:text-accent-500'"
          >
            {{ sub.name }}
          </router-link>
        </div>

      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// dummy_data.sql 기준 — ID는 DB에서 실제 부여된 값과 일치해야 함
const categories = [
  {
    id: 1,
    name: '국내도서',
    subs: [
      { id: 1,  name: '소설' },
      { id: 2,  name: '자기계발' },
      { id: 3,  name: '경제/경영' },
      { id: 4,  name: '과학/기술' },
      { id: 5,  name: '역사/문화' },
    ],
  },
  {
    id: 2,
    name: '해외도서',
    subs: [
      { id: 6,  name: 'Fiction' },
      { id: 7,  name: 'Self-Help' },
      { id: 8,  name: 'Business' },
      { id: 9,  name: 'Science' },
      { id: 10, name: 'History' },
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

const activeTopId = ref(null)

function toggleTop(id) {
  activeTopId.value = activeTopId.value === id ? null : id
}

function isActiveSub(topId, subId) {
  return Number(route.query.topId) === topId && Number(route.query.subId) === subId
}
</script>
