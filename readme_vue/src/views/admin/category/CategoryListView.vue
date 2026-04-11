<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">카테고리 관리</h1>
      <p class="mt-1 text-sm text-slate-400">대분류·소분류 카테고리 구조를 확인합니다.</p>
    </section>

    <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

    <div v-else-if="categories.length > 0" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      <section
        v-for="top in categories"
        :key="top.id"
        class="rounded-[2rem] border border-slate-200 bg-white shadow-sm"
      >
        <!-- 대분류 헤더 -->
        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <div>
            <p class="font-bold text-slate-800">{{ top.name }}</p>
            <p class="text-xs text-slate-400">ID: {{ top.id }}</p>
          </div>
          <span class="rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-800">
            {{ top.subCategories?.length ?? 0 }}개 소분류
          </span>
        </div>
        <!-- 소분류 목록 -->
        <ul class="divide-y divide-slate-50 px-6 py-2">
          <li
            v-for="sub in top.subCategories"
            :key="sub.id"
            class="flex items-center justify-between py-2.5 text-sm"
          >
            <span class="text-slate-700">{{ sub.name }}</span>
            <span class="text-xs text-slate-400">ID: {{ sub.id }}</span>
          </li>
          <li v-if="!top.subCategories?.length" class="py-3 text-xs text-slate-400">소분류 없음</li>
        </ul>
      </section>
    </div>

    <div v-else class="flex items-center justify-center rounded-[2rem] border border-slate-200 bg-white py-16 text-sm text-slate-400">
      카테고리가 없습니다.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const categories = ref([])
const loading    = ref(true)

onMounted(async () => {
  try {
    const { data } = await adminApi.getCategories()
    categories.value = data
  } catch (e) {
    console.error('카테고리 로드 실패', e)
  } finally {
    loading.value = false
  }
})
</script>
