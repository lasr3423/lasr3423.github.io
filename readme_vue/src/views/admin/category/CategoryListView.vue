<template>
  <div class="space-y-6">
    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <p class="point-chip">관리자 카테고리</p>
          <h1 class="section-title mt-3">카드형으로 대분류와 소분류를 함께 관리해요</h1>
          <p class="mt-2 text-sm text-slate-500">
            대분류 카드 안에서 소분류를 한 번에 보고, 등록과 수정은 그대로 이어서 진행할 수 있습니다.
          </p>
        </div>

        <div class="flex flex-wrap gap-3">
          <button
            type="button"
            class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="openCreateTopModal"
          >
            카테고리 등록
          </button>
          <button
            type="button"
            :disabled="!selectedTopId && !selectedSubId"
            class="rounded-xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-60"
            @click="removeSelectedCategory"
          >
            삭제하기
          </button>
        </div>
      </div>
    </section>

    <section class="grid gap-4 md:grid-cols-3">
      <article class="surface-panel rounded-[1.75rem] p-5">
        <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">대분류 수</p>
        <p class="mt-3 text-3xl font-bold text-slate-900">{{ categories.length }}</p>
      </article>
      <article class="surface-panel rounded-[1.75rem] p-5">
        <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">소분류 수</p>
        <p class="mt-3 text-3xl font-bold text-slate-900">{{ totalSubCategoryCount }}</p>
      </article>
      <article class="surface-panel rounded-[1.75rem] p-5">
        <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">현재 선택</p>
        <p class="mt-3 text-lg font-semibold text-slate-900">{{ selectedLabel }}</p>
      </article>
    </section>

    <section class="surface-panel rounded-[2rem] p-6">
      <div class="flex flex-col gap-3 lg:flex-row">
        <label class="flex-1 space-y-2">
          <span class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">검색</span>
          <input
            v-model.trim="keyword"
            type="text"
            placeholder="대분류 또는 소분류 이름으로 검색"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
          >
        </label>
        <div class="flex items-end gap-3">
          <button
            type="button"
            class="rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="loadCategories"
          >
            새로고침
          </button>
        </div>
      </div>
    </section>

    <div v-if="loading" class="surface-panel rounded-[2rem] p-12 text-center text-sm text-slate-500">
      카테고리 목록을 불러오는 중입니다...
    </div>

    <section v-else-if="filteredCategories.length" class="grid gap-5 xl:grid-cols-2">
      <article
        v-for="top in filteredCategories"
        :key="top.id"
        class="surface-panel rounded-[2rem] p-6"
      >
        <div class="flex items-start justify-between gap-4">
          <div class="min-w-0">
            <label class="flex items-center gap-3">
              <input
                type="checkbox"
                :checked="selectedTopId === top.id && !selectedSubId"
                class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400"
                @change="toggleTop(top.id)"
              >
              <span class="truncate text-xl font-bold text-slate-900">{{ top.name }}</span>
            </label>
            <p class="mt-3 text-sm text-slate-500">
              대분류 ID {{ top.id }} · 소분류 {{ top.subCategories?.length ?? 0 }}개
            </p>
          </div>

          <span class="rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700">
            소분류 {{ top.subCategories?.length ?? 0 }}개
          </span>
        </div>

        <div class="mt-5 space-y-3">
          <div
            v-for="sub in top.subCategories || []"
            :key="sub.id"
            class="flex items-center justify-between gap-3 rounded-[1.25rem] border border-slate-200 bg-slate-50/80 px-4 py-3"
          >
            <label class="flex min-w-0 items-center gap-3">
              <input
                type="checkbox"
                :checked="selectedSubId === sub.id"
                class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400"
                @change="toggleSub(top.id, sub.id)"
              >
              <span class="truncate text-sm font-medium text-slate-700">{{ sub.name }}</span>
            </label>
            <button
              type="button"
              class="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-white"
              @click="openEditSubModal(top, sub)"
            >
              수정
            </button>
          </div>

          <p
            v-if="!top.subCategories?.length"
            class="rounded-[1.25rem] border border-dashed border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-400"
          >
            등록된 소분류가 없습니다.
          </p>
        </div>

        <div class="mt-5 flex flex-wrap gap-3">
          <button
            type="button"
            class="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            @click="openEditTopModal(top)"
          >
            대분류 수정
          </button>
          <button
            type="button"
            class="rounded-xl bg-brand-50 px-4 py-2.5 text-sm font-semibold text-brand-700 transition hover:bg-brand-100"
            @click="openCreateSubModal(top)"
          >
            소분류 추가
          </button>
        </div>
      </article>
    </section>

    <div v-else class="surface-panel rounded-[2rem] p-12 text-center text-sm text-slate-500">
      검색 결과에 맞는 카테고리가 없습니다.
    </div>

    <div
      v-if="modal.open"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 px-4"
      @click.self="closeModal"
    >
      <div class="w-full max-w-xl rounded-[2rem] bg-white p-6 shadow-2xl">
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-sm font-semibold text-slate-500">카테고리 관리</p>
            <h2 class="mt-1 text-2xl font-bold text-slate-900">{{ modalTitle }}</h2>
          </div>
          <button
            type="button"
            class="rounded-full border border-slate-200 px-3 py-1 text-sm text-slate-500 transition hover:bg-slate-50"
            @click="closeModal"
          >
            닫기
          </button>
        </div>

        <form class="mt-6 space-y-4" @submit.prevent="submitModal">
          <label v-if="isSubModal" class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">기준 대분류</span>
            <select
              v-model="modalForm.categoryTopId"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            >
              <option :value="null">대분류를 선택하세요</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">카테고리 이름</span>
            <input
              v-model.trim="modalForm.name"
              type="text"
              placeholder="카테고리 이름을 입력하세요"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            >
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">정렬 순서</span>
            <input
              v-model.number="modalForm.sortOrder"
              type="number"
              min="0"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            >
          </label>

          <p v-if="feedbackError" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {{ feedbackError }}
          </p>
          <p v-if="feedbackSuccess" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
            {{ feedbackSuccess }}
          </p>

          <div class="flex justify-end gap-3 pt-2">
            <button
              type="button"
              class="rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              @click="closeModal"
            >
              취소
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="rounded-xl bg-brand-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {{ submitting ? '저장 중...' : '저장하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'

const categories = ref([])
const loading = ref(true)
const submitting = ref(false)
const keyword = ref('')
const selectedTopId = ref(null)
const selectedSubId = ref(null)
const feedbackError = ref('')
const feedbackSuccess = ref('')

const modal = ref({
  open: false,
  type: 'top-create',
  targetId: null,
})

const modalForm = ref({
  name: '',
  sortOrder: 0,
  categoryTopId: null,
})

const filteredCategories = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  if (!search) return categories.value

  return categories.value
    .map((top) => {
      const topMatched = top.name.toLowerCase().includes(search)
      const matchedSubCategories = (top.subCategories || []).filter((sub) =>
        sub.name.toLowerCase().includes(search),
      )

      if (topMatched) return top
      if (matchedSubCategories.length > 0) {
        return {
          ...top,
          subCategories: matchedSubCategories,
        }
      }
      return null
    })
    .filter(Boolean)
})

const totalSubCategoryCount = computed(() =>
  categories.value.reduce((count, top) => count + (top.subCategories?.length ?? 0), 0),
)

const selectedLabel = computed(() => {
  if (selectedSubId.value) {
    const top = categories.value.find((item) => item.id === selectedTopId.value)
    const sub = top?.subCategories?.find((item) => item.id === selectedSubId.value)
    return sub ? `${top.name} > ${sub.name}` : '선택 없음'
  }

  if (selectedTopId.value) {
    const top = categories.value.find((item) => item.id === selectedTopId.value)
    return top?.name ?? '선택 없음'
  }

  return '선택 없음'
})

const modalTitle = computed(() => {
  switch (modal.value.type) {
    case 'top-create':
      return '대분류 등록'
    case 'top-edit':
      return '대분류 수정'
    case 'sub-create':
      return '소분류 등록'
    case 'sub-edit':
      return '소분류 수정'
    default:
      return '카테고리 관리'
  }
})

const isSubModal = computed(() => modal.value.type === 'sub-create' || modal.value.type === 'sub-edit')

function resetFeedback() {
  feedbackError.value = ''
  feedbackSuccess.value = ''
}

async function loadCategories() {
  loading.value = true
  try {
    const { data } = await adminApi.getCategories()
    categories.value = data || []
  } catch (error) {
    console.error('카테고리 목록 조회 실패', error)
  } finally {
    loading.value = false
  }
}

function toggleTop(topId) {
  selectedSubId.value = null
  selectedTopId.value = selectedTopId.value === topId ? null : topId
}

function toggleSub(topId, subId) {
  const isSame = selectedSubId.value === subId
  selectedTopId.value = isSame ? null : topId
  selectedSubId.value = isSame ? null : subId
}

function openCreateTopModal() {
  resetFeedback()
  modal.value = { open: true, type: 'top-create', targetId: null }
  modalForm.value = { name: '', sortOrder: 0, categoryTopId: null }
}

function openEditTopModal(top) {
  resetFeedback()
  selectedTopId.value = top.id
  selectedSubId.value = null
  modal.value = { open: true, type: 'top-edit', targetId: top.id }
  modalForm.value = { name: top.name, sortOrder: 0, categoryTopId: null }
}

function openCreateSubModal(top) {
  resetFeedback()
  selectedTopId.value = top.id
  selectedSubId.value = null
  modal.value = { open: true, type: 'sub-create', targetId: null }
  modalForm.value = { name: '', sortOrder: 0, categoryTopId: top.id }
}

function openEditSubModal(top, sub) {
  resetFeedback()
  selectedTopId.value = top.id
  selectedSubId.value = sub.id
  modal.value = { open: true, type: 'sub-edit', targetId: sub.id }
  modalForm.value = { name: sub.name, sortOrder: 0, categoryTopId: top.id }
}

function openEditModal() {
  if (selectedSubId.value) {
    const top = categories.value.find((item) => item.id === selectedTopId.value)
    const sub = top?.subCategories?.find((item) => item.id === selectedSubId.value)
    if (top && sub) openEditSubModal(top, sub)
    return
  }

  if (selectedTopId.value) {
    const top = categories.value.find((item) => item.id === selectedTopId.value)
    if (top) openEditTopModal(top)
  }
}

async function removeSelectedCategory() {
  const isSubSelected = Boolean(selectedSubId.value)
  const isTopSelected = Boolean(selectedTopId.value) && !isSubSelected

  if (!isSubSelected && !isTopSelected) return

  const selectedName = selectedLabel.value
  const typeLabel = isSubSelected ? '소분류' : '대분류'

  if (!window.confirm(`${selectedName} ${typeLabel}를 삭제하시겠어요?`)) {
    return
  }

  try {
    resetFeedback()

    if (isSubSelected) {
      await adminApi.deleteCategorySub(selectedSubId.value)
      feedbackSuccess.value = '소분류를 삭제했습니다.'
    } else {
      await adminApi.deleteCategoryTop(selectedTopId.value)
      feedbackSuccess.value = '대분류를 삭제했습니다.'
    }

    selectedTopId.value = null
    selectedSubId.value = null
    await loadCategories()
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '카테고리 삭제 중 문제가 발생했습니다.'
  }
}

function closeModal() {
  modal.value.open = false
  resetFeedback()
}

async function submitModal() {
  if (!modalForm.value.name) {
    feedbackError.value = '카테고리 이름을 입력해 주세요.'
    return
  }

  if (isSubModal.value && !modalForm.value.categoryTopId) {
    feedbackError.value = '기준 대분류를 선택해 주세요.'
    return
  }

  try {
    submitting.value = true
    resetFeedback()

    const payload = {
      name: modalForm.value.name,
      sortOrder: Number(modalForm.value.sortOrder || 0),
      categoryTopId: modalForm.value.categoryTopId,
    }

    if (modal.value.type === 'top-create') {
      await adminApi.createCategoryTop(payload)
      feedbackSuccess.value = '대분류를 등록했습니다.'
    } else if (modal.value.type === 'top-edit') {
      await adminApi.updateCategoryTop(modal.value.targetId, payload)
      feedbackSuccess.value = '대분류를 수정했습니다.'
    } else if (modal.value.type === 'sub-create') {
      await adminApi.createCategorySub(payload)
      feedbackSuccess.value = '소분류를 등록했습니다.'
    } else if (modal.value.type === 'sub-edit') {
      await adminApi.updateCategorySub(modal.value.targetId, payload)
      feedbackSuccess.value = '소분류를 수정했습니다.'
    }

    await loadCategories()
    window.setTimeout(() => {
      closeModal()
    }, 400)
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '카테고리 저장 중 문제가 발생했습니다.'
  } finally {
    submitting.value = false
  }
}

onMounted(loadCategories)
</script>
