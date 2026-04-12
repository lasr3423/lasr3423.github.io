<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">상품 등록</h1>
      <p class="mt-3 text-sm leading-6 text-slate-500">
        카테고리 관리에서 등록한 카테고리 이름을 기준으로 상품 정보를 입력하고 신규 도서를 등록합니다.
      </p>
    </section>

    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <form class="max-w-4xl space-y-6" @submit.prevent="handleSubmit">
        <div class="grid grid-cols-1 gap-5 sm:grid-cols-2">
          <label class="block space-y-2 sm:col-span-2">
            <span class="text-sm font-medium text-slate-700">도서명 *</span>
            <input
              v-model="form.title"
              type="text"
              required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">저자 *</span>
            <input
              v-model="form.author"
              type="text"
              required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <div class="space-y-2">
            <span class="text-sm font-medium text-slate-700">ISBN</span>
            <div class="flex gap-2">
              <input
                v-model="form.isbn"
                type="text"
                placeholder="978..."
                class="min-w-0 flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
              >
              <button
                type="button"
                :disabled="isbnLoading"
                class="shrink-0 rounded-2xl bg-brand-700 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-600 disabled:bg-slate-300"
                @click="lookupIsbn"
              >
                {{ isbnLoading ? '조회 중...' : 'ISBN 조회' }}
              </button>
            </div>
          </div>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">대분류 *</span>
            <select
              v-model="form.categoryTopId"
              required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
              <option :value="null">대분류 선택</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">소분류 *</span>
            <select
              v-model="form.categorySubId"
              required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
              <option :value="null">소분류 선택</option>
              <option v-for="subCategory in availableSubCategories" :key="subCategory.id" :value="subCategory.id">
                {{ subCategory.name }}
              </option>
            </select>
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">정가(원) *</span>
            <input
              v-model.number="form.price"
              type="number"
              min="0"
              required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">할인율(%)</span>
            <input
              v-model.number="form.discountRate"
              type="number"
              min="0"
              max="100"
              step="0.1"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">재고 수량 *</span>
            <input
              v-model.number="form.stock"
              type="number"
              min="0"
              required
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <label class="block space-y-2 sm:col-span-2">
            <span class="text-sm font-medium text-slate-700">도서 소개</span>
            <textarea
              v-model="form.description"
              rows="5"
              class="w-full resize-none rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            ></textarea>
          </label>

          <label class="block space-y-2 sm:col-span-2">
            <span class="text-sm font-medium text-slate-700">대표 이미지 경로</span>
            <input
              v-model="form.thumbnail"
              type="text"
              class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"
            >
          </label>

          <label class="block space-y-2 sm:col-span-2">
            <span class="text-sm font-medium text-slate-700">대표 이미지 업로드</span>
            <input
              type="file"
              accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
              @change="handleFileChange"
              class="block w-full rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-600 file:mr-4 file:rounded-full file:border-0 file:bg-brand-800 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-brand-700"
            >
          </label>
        </div>

        <div class="space-y-3">
          <button
            type="button"
            :disabled="!selectedFile || uploadLoading"
            class="rounded-2xl bg-accent-500 px-5 py-3 text-sm font-semibold text-white transition hover:bg-accent-400 disabled:cursor-not-allowed disabled:bg-slate-300"
            @click="uploadThumbnail"
          >
            {{ uploadLoading ? '이미지 업로드 중...' : '이미지 업로드' }}
          </button>

          <div v-if="previewUrl" class="overflow-hidden rounded-[1.5rem] border border-slate-200 bg-slate-50 p-4">
            <p class="mb-3 text-sm font-medium text-slate-700">대표 이미지 미리보기</p>
            <img :src="previewUrl" alt="대표 이미지 미리보기" class="h-56 w-40 rounded-2xl object-cover shadow-sm">
          </div>
        </div>

        <div class="rounded-2xl bg-brand-50 px-5 py-4 text-sm">
          <span class="font-medium text-brand-700">예상 판매가:</span>
          <span class="ml-2 text-lg font-bold text-brand-900">{{ previewSalePrice.toLocaleString() }}원</span>
          <span class="ml-1 text-xs text-brand-600">({{ form.discountRate }}% 할인)</span>
        </div>

        <p v-if="errorMsg" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ errorMsg }}</p>
        <p v-if="successMsg" class="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{{ successMsg }}</p>

        <div class="flex gap-3 pt-2">
          <button
            type="submit"
            :disabled="loading || uploadLoading || isbnLoading"
            class="rounded-2xl bg-brand-800 px-6 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:bg-slate-400"
          >
            {{ loading ? '상품 등록 중...' : '상품 등록' }}
          </button>
          <router-link to="/admin/product/list" class="rounded-2xl border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50">
            취소
          </router-link>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const uploadLoading = ref(false)
const isbnLoading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const selectedFile = ref(null)
const previewUrl = ref('')
const categories = ref([])

const form = ref({
  title: '',
  author: '',
  isbn: '',
  description: '',
  thumbnail: '',
  price: 0,
  discountRate: 0,
  stock: 0,
  categoryTopId: null,
  categorySubId: null,
})

const availableSubCategories = computed(() =>
  categories.value.find((category) => category.id === form.value.categoryTopId)?.subCategories || []
)

const previewSalePrice = computed(() => {
  const rate = Number(form.value.discountRate || 0)
  return Math.floor(Number(form.value.price || 0) * (1 - rate / 100))
})

function handleCategoriesUpdated(updatedCategories) {
  categories.value = updatedCategories

  const selectedTop = categories.value.find((category) => category.id === form.value.categoryTopId)
  if (!selectedTop) {
    form.value.categoryTopId = null
    form.value.categorySubId = null
    return
  }

  const matchedSub = selectedTop.subCategories?.some((subCategory) => subCategory.id === form.value.categorySubId)
  if (!matchedSub) {
    form.value.categorySubId = null
  }
}

async function loadCategories() {
  try {
    const { data } = await adminApi.getCategories()
    handleCategoriesUpdated(data || [])
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '카테고리 목록을 불러오지 못했습니다.'
  }
}

function handleFileChange(event) {
  const [file] = event.target.files || []
  selectedFile.value = file || null
  errorMsg.value = ''

  if (!selectedFile.value) {
    previewUrl.value = ''
    return
  }

  previewUrl.value = URL.createObjectURL(selectedFile.value)
}

async function lookupIsbn() {
  if (!form.value.isbn?.trim()) {
    errorMsg.value = 'ISBN을 먼저 입력해 주세요.'
    return
  }

  try {
    isbnLoading.value = true
    errorMsg.value = ''
    successMsg.value = ''

    const { data } = await adminApi.lookupBookByIsbn(form.value.isbn.trim())
    form.value.isbn = data.isbn || form.value.isbn
    form.value.title = data.title || form.value.title
    form.value.author = data.author || form.value.author
    form.value.thumbnail = data.thumbnail || form.value.thumbnail
    previewUrl.value = data.thumbnail || previewUrl.value
    successMsg.value = '도서 검색 결과를 불러왔습니다.'
  } catch (error) {
    errorMsg.value = error.response?.data?.message || 'ISBN 조회에 실패했습니다.'
  } finally {
    isbnLoading.value = false
  }
}

async function uploadThumbnail() {
  if (!selectedFile.value) {
    errorMsg.value = '업로드할 이미지 파일을 선택해 주세요.'
    return
  }

  try {
    uploadLoading.value = true
    errorMsg.value = ''

    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const { data } = await adminApi.uploadProductThumbnail(formData)
    form.value.thumbnail = data.storedPath
    previewUrl.value = data.accessUrl
    successMsg.value = '대표 이미지가 업로드되었습니다.'
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '이미지 업로드에 실패했습니다.'
  } finally {
    uploadLoading.value = false
  }
}

async function handleSubmit() {
  if (!form.value.categoryTopId || !form.value.categorySubId) {
    errorMsg.value = '대분류와 소분류를 모두 선택해 주세요.'
    return
  }

  try {
    loading.value = true
    errorMsg.value = ''
    successMsg.value = ''

    await adminApi.createProduct(form.value)
    successMsg.value = '상품을 등록했습니다.'
    setTimeout(() => router.push('/admin/product/list'), 700)
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '상품 등록에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(loadCategories)
</script>
