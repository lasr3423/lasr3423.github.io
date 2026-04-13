<template>
  <section class="rounded-[1.5rem] border border-slate-200 bg-slate-50 p-5">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h3 class="text-base font-semibold text-slate-900">카테고리 관리</h3>
        <p class="mt-1 text-sm text-slate-500">대분류와 소분류를 추가하거나 이름을 수정할 수 있습니다.</p>
      </div>
      <button
        type="button"
        class="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
        @click="loadCategories"
      >
        새로고침
      </button>
    </div>

    <div class="mt-5 grid gap-4 xl:grid-cols-2">
      <div class="rounded-[1.25rem] border border-slate-200 bg-white p-4">
        <h4 class="text-sm font-semibold text-slate-800">대분류</h4>
        <div class="mt-3 space-y-3">
          <label class="block space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">선택한 대분류</span>
            <select
              v-model="selectedTopId"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
              <option :value="null">대분류 선택</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">대분류 이름</span>
            <input
              v-model.trim="topForm.name"
              type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
              placeholder="예: 국내도서"
            >
          </label>

          <label class="block space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">정렬 순서</span>
            <input
              v-model.number="topForm.sortOrder"
              type="number"
              min="0"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <div class="flex flex-wrap gap-2">
            <button
              type="button"
              class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
              @click="createTopCategory"
            >
              대분류 추가
            </button>
            <button
              type="button"
              :disabled="!selectedTopId"
              class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
              @click="updateTopCategory"
            >
              이름 수정
            </button>
          </div>
        </div>
      </div>

      <div class="rounded-[1.25rem] border border-slate-200 bg-white p-4">
        <h4 class="text-sm font-semibold text-slate-800">소분류</h4>
        <div class="mt-3 space-y-3">
          <label class="block space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">기준 대분류</span>
            <select
              v-model="selectedTopId"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
              <option :value="null">대분류 선택</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">선택한 소분류</span>
            <select
              v-model="selectedSubId"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
              <option :value="null">소분류 선택</option>
              <option v-for="subCategory in selectedTopSubCategories" :key="subCategory.id" :value="subCategory.id">
                {{ subCategory.name }}
              </option>
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">소분류 이름</span>
            <input
              v-model.trim="subForm.name"
              type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
              placeholder="예: 소설"
            >
          </label>

          <label class="block space-y-2">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">정렬 순서</span>
            <input
              v-model.number="subForm.sortOrder"
              type="number"
              min="0"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <div class="flex flex-wrap gap-2">
            <button
              type="button"
              :disabled="!selectedTopId"
              class="rounded-xl bg-brand-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-50"
              @click="createSubCategory"
            >
              소분류 추가
            </button>
            <button
              type="button"
              :disabled="!selectedSubId"
              class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
              @click="updateSubCategory"
            >
              이름 수정
            </button>
          </div>
        </div>
      </div>
    </div>

    <p v-if="feedbackError" class="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ feedbackError }}</p>
    <p v-if="feedbackSuccess" class="mt-4 rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{{ feedbackSuccess }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { adminApi } from '@/api/admin'

const emit = defineEmits(['updated'])

const categories = ref([])
const selectedTopId = ref(null)
const selectedSubId = ref(null)
const feedbackError = ref('')
const feedbackSuccess = ref('')

const topForm = ref({
  name: '',
  sortOrder: 0,
})

const subForm = ref({
  name: '',
  sortOrder: 0,
})

const selectedTop = computed(() => categories.value.find((category) => category.id === selectedTopId.value) || null)
const selectedTopSubCategories = computed(() => selectedTop.value?.subCategories || [])
const selectedSub = computed(() => selectedTopSubCategories.value.find((subCategory) => subCategory.id === selectedSubId.value) || null)

watch(selectedTop, (value) => {
  selectedSubId.value = null
  if (value) {
    topForm.value.name = value.name || ''
  }
})

watch(selectedSub, (value) => {
  if (value) {
    subForm.value.name = value.name || ''
  }
})

function resetFeedback() {
  feedbackError.value = ''
  feedbackSuccess.value = ''
}

async function loadCategories() {
  try {
    const { data } = await adminApi.getCategories()
    categories.value = data || []
    emit('updated', data || [])
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '카테고리 목록을 불러오지 못했습니다.'
  }
}

async function createTopCategory() {
  if (!topForm.value.name) {
    feedbackError.value = '대분류 이름을 입력해 주세요.'
    return
  }

  try {
    resetFeedback()
    const { data: createdId } = await adminApi.createCategoryTop({
      name: topForm.value.name,
      sortOrder: Number(topForm.value.sortOrder || 0),
      categoryTopId: null,
    })
    await loadCategories()
    selectedTopId.value = createdId
    feedbackSuccess.value = '대분류를 추가했습니다.'
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '대분류 추가에 실패했습니다.'
  }
}

async function updateTopCategory() {
  if (!selectedTopId.value || !topForm.value.name) {
    feedbackError.value = '수정할 대분류와 이름을 확인해 주세요.'
    return
  }

  try {
    resetFeedback()
    await adminApi.updateCategoryTop(selectedTopId.value, {
      name: topForm.value.name,
      sortOrder: Number(topForm.value.sortOrder || 0),
      categoryTopId: null,
    })
    await loadCategories()
    feedbackSuccess.value = '대분류 이름을 수정했습니다.'
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '대분류 수정에 실패했습니다.'
  }
}

async function createSubCategory() {
  if (!selectedTopId.value || !subForm.value.name) {
    feedbackError.value = '기준 대분류와 소분류 이름을 확인해 주세요.'
    return
  }

  try {
    resetFeedback()
    const { data: createdId } = await adminApi.createCategorySub({
      name: subForm.value.name,
      sortOrder: Number(subForm.value.sortOrder || 0),
      categoryTopId: selectedTopId.value,
    })
    await loadCategories()
    selectedSubId.value = createdId
    feedbackSuccess.value = '소분류를 추가했습니다.'
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '소분류 추가에 실패했습니다.'
  }
}

async function updateSubCategory() {
  if (!selectedSubId.value || !subForm.value.name || !selectedTopId.value) {
    feedbackError.value = '수정할 소분류와 이름을 확인해 주세요.'
    return
  }

  try {
    resetFeedback()
    await adminApi.updateCategorySub(selectedSubId.value, {
      name: subForm.value.name,
      sortOrder: Number(subForm.value.sortOrder || 0),
      categoryTopId: selectedTopId.value,
    })
    await loadCategories()
    feedbackSuccess.value = '소분류 이름을 수정했습니다.'
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '소분류 수정에 실패했습니다.'
  }
}

onMounted(loadCategories)
</script>
