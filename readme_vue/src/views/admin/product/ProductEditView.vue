<template>
  <div class="space-y-6">
    <section class="flex flex-col gap-4 rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm lg:flex-row lg:items-center lg:justify-between">
      <div>
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">상품 수정</h1>
        <p class="mt-2 text-sm leading-6 text-slate-500">
          등록된 상품 정보를 최신 상태로 수정하고 카테고리, 가격, 재고 정보를 다시 정리합니다.
        </p>
      </div>
      <router-link
        to="/admin/product/list"
        class="inline-flex items-center justify-center rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
      >
        목록으로 돌아가기
      </router-link>
    </section>

    <section v-if="loading" class="surface-panel rounded-[2rem] p-10 text-center text-sm text-slate-400">
      상품 정보를 불러오는 중입니다.
    </section>

    <section v-else-if="selectedProduct" class="grid gap-6 xl:grid-cols-[340px_minmax(0,1fr)]">
      <aside class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex items-start justify-between gap-3">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Product Detail</p>
            <h2 class="mt-2 text-2xl font-bold tracking-tight text-slate-900">상품 미리보기</h2>
          </div>
          <span :class="statusClass(form.productStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
            {{ statusLabel(form.productStatus) }}
          </span>
        </div>

        <div class="mt-6 flex flex-col items-center rounded-[1.5rem] bg-slate-50 p-5 text-center">
          <img
            :src="previewUrl || resolveAssetUrl(form.thumbnail)"
            :alt="form.title || '상품 이미지'"
            class="h-48 w-36 rounded-2xl border border-slate-200 object-cover"
          >
          <p class="mt-4 text-lg font-semibold text-slate-900">{{ form.title }}</p>
          <p class="mt-1 text-sm text-slate-500">{{ form.author }}</p>
          <p class="mt-4 text-xs text-slate-500">ISBN {{ form.isbn || '-' }}</p>
          <p class="mt-2 text-xl font-bold text-brand-900">{{ formatPrice(previewSalePrice) }}</p>
          <p class="mt-2 text-xs text-slate-500">재고 {{ form.stock }}권 / 조회수 {{ selectedProduct.viewCount }} / 판매량 {{ selectedProduct.salesCount }}</p>
          <p class="mt-4 text-xs font-medium text-slate-500">{{ selectedTopName }} / {{ selectedSubName }}</p>
        </div>
      </aside>

      <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
        <form class="space-y-4" @submit.prevent="handleUpdate">
          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">도서명</span>
            <input v-model="form.title" type="text" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
          </label>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">저자</span>
              <input v-model="form.author" type="text" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">ISBN</span>
              <input v-model="form.isbn" type="text" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
            </label>
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">대분류</span>
              <select v-model="form.categoryTopId" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400">
                <option :value="null">대분류 선택</option>
                <option v-for="category in categories" :key="category.id" :value="category.id">
                  {{ category.name }}
                </option>
              </select>
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">소분류</span>
              <select v-model="form.categorySubId" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400">
                <option :value="null">소분류 선택</option>
                <option v-for="subCategory in availableSubCategories" :key="subCategory.id" :value="subCategory.id">
                  {{ subCategory.name }}
                </option>
              </select>
            </label>
          </div>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">도서 소개</span>
            <textarea v-model="form.description" rows="6" class="w-full resize-none rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"></textarea>
          </label>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">정가</span>
              <input v-model.number="form.price" type="number" min="0" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">할인율(%)</span>
              <input v-model.number="form.discountRate" type="number" min="0" max="100" step="0.1" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">재고 수량</span>
              <input v-model.number="form.stock" type="number" min="0" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
            </label>
            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">판매 상태</span>
              <select v-model="form.productStatus" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400">
                <option value="ACTIVATE">판매 중</option>
                <option value="DEACTIVATE">판매 중단</option>
                <option value="DELETE">삭제</option>
              </select>
            </label>
          </div>

          <label class="block space-y-2">
            <span class="text-sm font-medium text-slate-700">대표 이미지 경로</span>
            <input v-model="form.thumbnail" type="text" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
          </label>

          <div class="space-y-3 rounded-[1.5rem] border border-dashed border-slate-300 bg-slate-50 p-4">
            <p class="text-sm font-medium text-slate-700">대표 이미지 새로 업로드</p>
            <input
              type="file"
              accept="image/png,image/jpeg,image/jpg,image/webp,image/gif"
              class="block w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-600 file:mr-4 file:rounded-full file:border-0 file:bg-brand-800 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-brand-700"
              @change="handleFileChange"
            >
            <button type="button" :disabled="!selectedFile || uploadLoading" class="rounded-2xl bg-accent-500 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-accent-400 disabled:cursor-not-allowed disabled:bg-slate-300" @click="uploadThumbnail">
              {{ uploadLoading ? '이미지 업로드 중...' : '이미지 업로드' }}
            </button>
          </div>

          <div class="rounded-2xl bg-brand-50 px-4 py-3 text-sm text-brand-800">
            예상 판매가 <span class="ml-2 text-lg font-bold text-brand-900">{{ formatPrice(previewSalePrice) }}</span>
          </div>

          <p v-if="feedbackError" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ feedbackError }}</p>
          <p v-if="feedbackSuccess" class="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{{ feedbackSuccess }}</p>

          <div class="flex flex-wrap gap-3 pt-2">
            <button type="submit" :disabled="updateLoading || uploadLoading" class="rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:bg-slate-400">
              {{ updateLoading ? '저장 중...' : '상품 수정 저장' }}
            </button>
            <button type="button" class="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50" @click="resetFeedback">
              안내 지우기
            </button>
            <button type="button" class="rounded-2xl border border-rose-200 px-5 py-3 text-sm font-semibold text-rose-700 transition hover:bg-rose-50" @click="handleDelete">
              상품 삭제
            </button>
          </div>
        </form>
      </section>
    </section>

    <section v-else class="surface-panel rounded-[2rem] p-10 text-center text-sm text-slate-500">
      상품 정보를 찾을 수 없습니다.
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'

const route = useRoute()
const router = useRouter()

const selectedProduct = ref(null)
const categories = ref([])
const loading = ref(true)
const updateLoading = ref(false)
const uploadLoading = ref(false)
const selectedFile = ref(null)
const previewUrl = ref('')
const feedbackError = ref('')
const feedbackSuccess = ref('')

const form = ref({
  title: '',
  author: '',
  isbn: '',
  description: '',
  price: 0,
  discountRate: 0,
  stock: 0,
  thumbnail: '',
  productStatus: 'ACTIVATE',
  categoryTopId: null,
  categorySubId: null,
})

const availableSubCategories = computed(() =>
  categories.value.find((category) => category.id === form.value.categoryTopId)?.subCategories || []
)
const selectedTopName = computed(() =>
  categories.value.find((category) => category.id === form.value.categoryTopId)?.name || '-'
)
const selectedSubName = computed(() =>
  availableSubCategories.value.find((subCategory) => subCategory.id === form.value.categorySubId)?.name || '-'
)
const previewSalePrice = computed(() => {
  const price = Number(form.value.price || 0)
  const discountRate = Number(form.value.discountRate || 0)
  return Math.floor(price * (1 - discountRate / 100))
})

const statusLabel = (status) => ({ ACTIVATE: '판매 중', DEACTIVATE: '판매 중단', DELETE: '삭제' }[status] ?? status)
const statusClass = (status) => ({
  ACTIVATE: 'bg-green-50 text-green-700',
  DEACTIVATE: 'bg-yellow-50 text-yellow-700',
  DELETE: 'bg-rose-50 text-rose-700',
}[status] ?? 'bg-slate-100 text-slate-600')
const formatPrice = (value) => `${Number(value || 0).toLocaleString()}원`

function resetFeedback() {
  feedbackError.value = ''
  feedbackSuccess.value = ''
}

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

function populateForm(data) {
  form.value = {
    title: data.title || '',
    author: data.author || '',
    isbn: data.isbn || '',
    description: data.description || '',
    price: Number(data.price || 0),
    discountRate: Number(data.discountRate || 0),
    stock: Number(data.stock || 0),
    thumbnail: data.thumbnail || '',
    productStatus: data.productStatus || 'ACTIVATE',
    categoryTopId: data.categoryTopId || null,
    categorySubId: data.categorySubId || null,
  }
  selectedProduct.value = data
  selectedFile.value = null
  previewUrl.value = ''
  resetFeedback()
}

async function fetchProduct() {
  loading.value = true
  try {
    const { data } = await adminApi.getProduct(route.params.productId)
    populateForm(data)
  } catch (error) {
    console.error('상품 상세 조회 실패:', error)
    selectedProduct.value = null
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const { data } = await adminApi.getCategories()
    handleCategoriesUpdated(data || [])
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '카테고리 목록을 불러오지 못했습니다.'
  }
}

function handleFileChange(event) {
  const [file] = event.target.files || []
  selectedFile.value = file || null
  previewUrl.value = selectedFile.value ? URL.createObjectURL(selectedFile.value) : ''
}

async function uploadThumbnail() {
  if (!selectedFile.value) {
    feedbackError.value = '업로드할 이미지 파일을 선택해 주세요.'
    return
  }

  try {
    uploadLoading.value = true
    resetFeedback()
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const { data } = await adminApi.uploadProductThumbnail(formData)
    form.value.thumbnail = data.storedPath
    previewUrl.value = data.accessUrl
    feedbackSuccess.value = '대표 이미지가 업로드되었습니다.'
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '이미지 업로드에 실패했습니다.'
  } finally {
    uploadLoading.value = false
  }
}

async function handleUpdate() {
  if (!selectedProduct.value) return
  if (!form.value.categoryTopId || !form.value.categorySubId) {
    feedbackError.value = '대분류와 소분류를 모두 선택해 주세요.'
    return
  }

  try {
    updateLoading.value = true
    resetFeedback()
    await adminApi.updateProduct(selectedProduct.value.id, form.value)
    feedbackSuccess.value = '상품 정보가 수정되었습니다.'
    await fetchProduct()
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '상품 수정에 실패했습니다.'
  } finally {
    updateLoading.value = false
  }
}

async function handleDelete() {
  if (!selectedProduct.value) return
  if (!confirm('선택한 상품을 삭제하시겠습니까?')) return

  try {
    await adminApi.deleteProduct(selectedProduct.value.id)
    router.push('/admin/product/list')
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '상품 삭제에 실패했습니다.'
  }
}

onMounted(async () => {
  await loadCategories()
  await fetchProduct()
})
</script>
