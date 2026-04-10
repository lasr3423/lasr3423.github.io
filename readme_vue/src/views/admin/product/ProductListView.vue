<template>
  <div class="space-y-6">
    <section class="flex flex-col gap-4 rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm lg:flex-row lg:items-center lg:justify-between">
      <div>
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">상품 관리</h1>
        <p class="mt-2 text-sm leading-6 text-slate-500">
          상품의 판매 상태, 재고, 대표 이미지와 기본 정보를 확인하고 바로 수정할 수 있습니다.
        </p>
      </div>
      <router-link
        to="/admin/product/insert"
        class="inline-flex items-center justify-center rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700"
      >
        + 상품 등록
      </router-link>
    </section>

    <section class="grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_420px]">
      <div class="space-y-6">
        <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <h2 class="text-lg font-semibold text-slate-900">필터</h2>
              <p class="mt-1 text-sm text-slate-500">판매 상태와 재고 위험 상품을 빠르게 확인할 수 있어요.</p>
            </div>

            <div class="flex flex-col gap-3 sm:flex-row">
              <label class="space-y-2">
                <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">판매 상태</span>
                <select
                  v-model="statusFilter"
                  class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400"
                  @change="search"
                >
                  <option value="">전체 상태</option>
                  <option value="ACTIVATE">판매 중</option>
                  <option value="DEACTIVATE">판매 중단</option>
                  <option value="DELETE">삭제됨</option>
                </select>
              </label>

              <label class="space-y-2">
                <span class="text-xs font-semibold uppercase tracking-wider text-slate-500">재고 상태</span>
                <select
                  v-model="stockFilter"
                  class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400"
                  @change="search"
                >
                  <option value="">전체 재고</option>
                  <option value="LOW">재고 부족만</option>
                </select>
              </label>
            </div>
          </div>
        </section>

        <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
          <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
            <div>
              <h2 class="text-lg font-semibold text-slate-900">상품 목록</h2>
              <p class="mt-1 text-sm text-slate-500">상품을 선택하면 우측에서 상세와 수정 폼을 확인할 수 있습니다.</p>
            </div>
            <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
              총 {{ totalElements.toLocaleString() }}건
            </span>
          </div>

          <div v-if="loading" class="flex items-center justify-center py-20 text-sm text-slate-400">
            상품 목록을 불러오는 중입니다.
          </div>

          <template v-else-if="filteredProducts.length > 0">
            <div class="overflow-x-auto">
              <table class="w-full min-w-[960px] text-sm">
                <thead class="border-b border-slate-200 bg-slate-50">
                  <tr>
                    <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">상품</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">저자</th>
                    <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">판매가</th>
                    <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">재고</th>
                    <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">판매수</th>
                    <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                    <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr
                    v-for="product in filteredProducts"
                    :key="product.id"
                    class="cursor-pointer transition hover:bg-slate-50"
                    @click="selectProduct(product.id)"
                  >
                    <td class="px-6 py-4">
                      <div class="flex items-center gap-4">
                        <img :src="resolveAssetUrl(product.thumbnail)" :alt="product.title" class="h-16 w-12 rounded-xl border border-slate-200 object-cover" />
                        <div class="min-w-0">
                          <p class="truncate font-semibold text-slate-900">{{ product.title }}</p>
                          <p class="mt-1 text-xs text-slate-400">ISBN {{ product.isbn || '-' }}</p>
                        </div>
                      </div>
                    </td>
                    <td class="px-6 py-4 text-slate-600">{{ product.author }}</td>
                    <td class="px-6 py-4 text-right text-slate-700">{{ formatPrice(product.salePrice) }}</td>
                    <td class="px-6 py-4 text-right">
                      <span :class="product.stock < 10 ? 'font-semibold text-rose-600' : 'text-slate-700'">{{ product.stock }}</span>
                    </td>
                    <td class="px-6 py-4 text-right text-slate-600">{{ product.salesCount }}</td>
                    <td class="px-6 py-4 text-center">
                      <span :class="statusClass(product.productStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                        {{ statusLabel(product.productStatus) }}
                      </span>
                    </td>
                    <td class="px-6 py-4 text-right">
                      <div class="flex justify-end gap-2">
                        <button type="button" class="rounded-xl border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100" @click.stop="selectProduct(product.id)">
                          수정
                        </button>
                        <button type="button" class="rounded-xl bg-rose-50 px-3 py-1.5 text-xs font-semibold text-rose-700 transition hover:bg-rose-100" @click.stop="handleDelete(product.id)">
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
              <button type="button" :disabled="page === 0" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="changePage(page - 1)">
                이전
              </button>
              <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
              <button type="button" :disabled="page >= totalPages - 1" class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40" @click="changePage(page + 1)">
                다음
              </button>
            </div>
          </template>

          <div v-else class="flex items-center justify-center py-20 text-sm text-slate-400">
            조건에 맞는 상품이 없습니다.
          </div>
        </section>
      </div>

      <aside class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
        <template v-if="detailLoading">
          <div class="flex min-h-[420px] items-center justify-center text-sm text-slate-400">
            상품 정보를 불러오는 중입니다.
          </div>
        </template>

        <template v-else-if="selectedProduct">
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.2em] text-brand-700">Product Detail</p>
              <h2 class="mt-2 text-2xl font-bold tracking-tight text-slate-900">상품 수정</h2>
            </div>
            <span :class="statusClass(form.productStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
              {{ statusLabel(form.productStatus) }}
            </span>
          </div>

          <div class="mt-6 flex items-start gap-4 rounded-[1.5rem] bg-slate-50 p-4">
            <img :src="previewUrl || resolveAssetUrl(form.thumbnail)" :alt="form.title || '상품 이미지'" class="h-32 w-24 rounded-2xl border border-slate-200 object-cover" />
            <div class="min-w-0 flex-1">
              <p class="text-lg font-semibold text-slate-900">{{ form.title }}</p>
              <p class="mt-1 text-sm text-slate-500">{{ form.author }}</p>
              <p class="mt-3 text-xs text-slate-500">현재 판매가 {{ formatPrice(previewSalePrice) }}</p>
              <p class="mt-1 text-xs text-slate-500">조회수 {{ selectedProduct.viewCount }} · 판매수 {{ selectedProduct.salesCount }}</p>
            </div>
          </div>

          <form class="mt-6 space-y-4" @submit.prevent="handleUpdate">
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

            <label class="block space-y-2">
              <span class="text-sm font-medium text-slate-700">도서 소개</span>
              <textarea v-model="form.description" rows="5" class="w-full resize-none rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400"></textarea>
            </label>

            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <label class="block space-y-2">
                <span class="text-sm font-medium text-slate-700">정가</span>
                <input v-model.number="form.price" type="number" min="0" required class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-brand-400" />
              </label>
              <label class="block space-y-2">
                <span class="text-sm font-medium text-slate-700">할인율 (%)</span>
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
                  <option value="DELETE">삭제됨</option>
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
              />
              <button type="button" :disabled="!selectedFile || uploadLoading" class="rounded-2xl bg-accent-500 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-accent-400 disabled:cursor-not-allowed disabled:bg-slate-300" @click="uploadThumbnail">
                {{ uploadLoading ? '이미지 업로드 중...' : '이미지 업로드' }}
              </button>
            </div>

            <div class="rounded-2xl bg-brand-50 px-4 py-3 text-sm text-brand-800">
              예상 판매가 <span class="ml-2 text-lg font-bold text-brand-900">{{ formatPrice(previewSalePrice) }}</span>
            </div>

            <p v-if="feedbackError" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{{ feedbackError }}</p>
            <p v-if="feedbackSuccess" class="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700">{{ feedbackSuccess }}</p>

            <div class="flex gap-3 pt-2">
              <button type="submit" :disabled="updateLoading || uploadLoading" class="flex-1 rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:bg-slate-400">
                {{ updateLoading ? '저장 중...' : '상품 수정 저장' }}
              </button>
              <button type="button" class="rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50" @click="resetFeedback">
                안내 지우기
              </button>
            </div>
          </form>
        </template>

        <div v-else class="flex min-h-[420px] items-center justify-center rounded-[1.5rem] bg-slate-50 px-6 text-center text-sm text-slate-500">
          왼쪽 목록에서 상품을 선택하면 상세 정보와 수정 폼이 표시됩니다.
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'

const products = ref([])
const selectedProduct = ref(null)
const loading = ref(true)
const detailLoading = ref(false)
const updateLoading = ref(false)
const uploadLoading = ref(false)
const statusFilter = ref('')
const stockFilter = ref('')
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
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
})

const filteredProducts = computed(() => {
  if (stockFilter.value !== 'LOW') return products.value
  return products.value.filter((product) => product.stock < 10)
})

const previewSalePrice = computed(() => {
  const price = Number(form.value.price || 0)
  const discountRate = Number(form.value.discountRate || 0)
  return Math.floor(price * (1 - discountRate / 100))
})

const statusLabel = (status) => ({ ACTIVATE: '판매 중', DEACTIVATE: '판매 중단', DELETE: '삭제됨' }[status] ?? status)
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
  }
  previewUrl.value = ''
  selectedFile.value = null
  resetFeedback()
}

async function fetchProducts() {
  loading.value = true
  try {
    const { data } = await adminApi.getProducts({ page: page.value, size: 20, status: statusFilter.value || undefined })
    products.value = data.content ?? []
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || products.value.length

    if (!selectedProduct.value && products.value.length > 0) {
      await selectProduct(products.value[0].id)
      return
    }

    if (selectedProduct.value) {
      const stillExists = products.value.some((product) => product.id === selectedProduct.value.id)
      if (!stillExists) selectedProduct.value = null
    }
  } catch (error) {
    console.error('상품 목록 조회 실패:', error)
  } finally {
    loading.value = false
  }
}

async function selectProduct(productId) {
  detailLoading.value = true
  try {
    const { data } = await adminApi.getProduct(productId)
    selectedProduct.value = data
    populateForm(data)
  } catch (error) {
    console.error('상품 상세 조회 실패:', error)
  } finally {
    detailLoading.value = false
  }
}

function search() {
  page.value = 0
  selectedProduct.value = null
  fetchProducts()
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

  try {
    updateLoading.value = true
    resetFeedback()
    await adminApi.updateProduct(selectedProduct.value.id, form.value)
    feedbackSuccess.value = '상품 정보가 수정되었습니다.'
    await fetchProducts()
    await selectProduct(selectedProduct.value.id)
  } catch (error) {
    feedbackError.value = error.response?.data?.message || '상품 수정에 실패했습니다.'
  } finally {
    updateLoading.value = false
  }
}

async function handleDelete(productId) {
  if (!confirm('선택한 상품을 삭제하시겠습니까?')) return
  try {
    await adminApi.deleteProduct(productId)
    if (selectedProduct.value?.id === productId) selectedProduct.value = null
    await fetchProducts()
  } catch (error) {
    alert(error.response?.data?.message || '상품 삭제에 실패했습니다.')
  }
}

async function changePage(nextPage) {
  page.value = nextPage
  selectedProduct.value = null
  await fetchProducts()
}

onMounted(fetchProducts)
</script>
