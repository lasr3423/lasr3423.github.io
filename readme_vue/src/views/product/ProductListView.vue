<template>
  <div>

    <h2 class="page-title">도서 목록</h2>

    <!-- 필터 영역 -->
    <div class="filter-bar">
      <!-- 대분류 탭 (전체 / 국내 / 해외) -->
      <div class="filter-tabs">
        <button
          v-for="tab in categoryTabs"
          :key="tab.value"
          :class="['tab-btn', { active: selectedCategory === tab.value }]"
          @click="selectCategory(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 검색 박스 -->
      <div class="search-box">
        <select v-model="searchType" class="search-select">
          <option value="title">도서명</option>
          <option value="author">저자</option>
        </select>
        <input
          v-model="searchKeyword"
          class="search-input"
          type="text"
          placeholder="검색어를 입력하세요"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">검색</button>
      </div>
    </div>

    <!-- 소분류 탭 -->
    <div class="sub-filter-tabs">
      <button
        v-for="sub in subCategoryTabs"
        :key="sub.value"
        :class="['sub-tab-btn', { active: selectedSubCategory === sub.value }]"
        @click="selectSubCategory(sub.value)"
      >
        {{ sub.label }}
      </button>
    </div>

    <!-- 로딩 중 -->
    <div v-if="productStore.loading" class="status-area">
      불러오는 중...
    </div>

    <!-- 에러 -->
    <div v-else-if="productStore.error" class="status-area error">
      {{ productStore.error }}
    </div>

    <!-- 상품 그리드 -->
    <div v-else class="product-grid">
      <div
        v-for="product in productStore.products"
        :key="product.id"
        class="product-card"
      >
        <!-- 뱃지 -->
        <span v-if="product.salesCount > 10000" class="badge badge-best">베스트</span>
        <span v-else-if="Number(product.discountRate) >= 40" class="badge badge-sale">핫딜</span>

        <!-- 상품 이미지 -->
        <img
          :src="product.thumbnail || '/img/no-image.png'"
          :alt="product.title"
          class="product-thumbnail"
          @click="goToDetail(product.id)"
        />

        <!-- 상품 정보 -->
        <div class="product-info">
          <p class="product-title" @click="goToDetail(product.id)">{{ product.title }}</p>
          <p class="product-author">{{ product.author }}</p>
          <div class="product-price">
            <span class="discount-rate">{{ product.discountRate }}%</span>
            <span class="sale-price">{{ product.salePrice.toLocaleString() }}원</span>
          </div>
          <p class="original-price">{{ product.price.toLocaleString() }}원</p>
        </div>

        <!-- 장바구니 버튼 -->
        <button class="cart-btn" @click="addToCart(product.id)">
          🛒 장바구니 담기
        </button>
      </div>
    </div>

    <!-- 상품이 아예 없을 때 -->
    <div v-if="!productStore.loading && productStore.products.length === 0" class="status-area">
      상품이 없습니다.
    </div>

    <!-- 페이지네이션 -->
    <div v-if="productStore.totalPages > 1" class="pagination">
      <button @click="changePage(0)"                        :disabled="productStore.currentPage === 0">&lt;</button>
      <button @click="changePage(productStore.currentPage - 1)" :disabled="productStore.currentPage === 0">&lt;&lt;</button>

      <button
        v-for="p in pageRange"
        :key="p"
        :class="['page-num', { active: p === productStore.currentPage }]"
        @click="changePage(p)"
      >
        {{ p + 1 }}
      </button>

      <button @click="changePage(productStore.currentPage + 1)" :disabled="productStore.currentPage === productStore.totalPages - 1">&gt;&gt;</button>
      <button @click="changePage(productStore.totalPages - 1)"  :disabled="productStore.currentPage === productStore.totalPages - 1">&gt;</button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProductStore } from '@/store/product'
import { useCartStore } from '@/store/cart'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route  = useRoute()
const productStore = useProductStore()
const cartStore    = useCartStore()
const authStore    = useAuthStore()

// ── 필터 상태 ──────────────────────────────────────────────────
const selectedCategory    = ref('ALL')
const selectedSubCategory = ref('ALL')
const searchKeyword = ref('')
const searchType    = ref('title')

// 대분류 탭
const categoryTabs = [
  { label: '전체', value: 'ALL' },
  { label: '국내', value: 'DOMESTIC' },
  { label: '해외', value: 'FOREIGN' },
]

// 소분류 탭
const subCategoryTabs = [
  { label: '전체',   value: 'ALL' },
  { label: '소설',   value: 'NOVEL' },
  { label: '자기계발', value: 'SELF_HELP' },
  { label: '컴퓨터', value: 'COMPUTER' },
  { label: '취미',   value: 'HOBBY' },
  { label: '여행',   value: 'TRAVEL' },
  { label: '외국어', value: 'FOREIGN_LANG' },
]

// ── 페이지네이션 버튼 범위 ──────────────────────────────────────
// 현재 페이지 앞뒤 2개씩만 표시
const pageRange = computed(() => {
  const current = productStore.currentPage
  const total   = productStore.totalPages
  const start   = Math.max(0, current - 2)
  const end     = Math.min(total - 1, current + 2)
  const range   = []
  for (let i = start; i <= end; i++) range.push(i)
  return range
})

// ── 이벤트 핸들러 ──────────────────────────────────────────────

function selectCategory(value) {
  selectedCategory.value = value
  productStore.fetchProducts(0)
}

function selectSubCategory(value) {
  selectedSubCategory.value = value
  productStore.fetchProducts(0)
}

function handleSearch() {
  productStore.fetchProducts(0)
}

function changePage(page) {
  if (page < 0 || page >= productStore.totalPages) return
  productStore.fetchProducts(page)
  window.scrollTo(0, 0)
}

function goToDetail(productId) {
  router.push(`/product/${productId}`)
}

async function addToCart(productId) {
  if (!authStore.isLoggedIn) {
    alert('로그인이 필요합니다.')
    router.push('/signin')
    return
  }
  try {
    await cartStore.addItem(productId, 1)
    alert('장바구니에 담겼습니다.')
  } catch (e) {
    alert('담기에 실패했습니다. 다시 시도해주세요.')
  }
}

// ── 헤더 검색창에서 keyword 넘어올 때 처리 ───────────────────────
// AppHeader에서 router.push({ path:'/product', query:{ keyword:'...' } }) 하면 여기서 감지
watch(
  () => route.query.keyword,
  (keyword) => {
    if (keyword) {
      searchKeyword.value = keyword
      productStore.fetchProducts(0)
    }
  }
)

// ── 마운트 시 목록 로드 ───────────────────────────────────────
onMounted(() => {
  // 헤더에서 검색어 가지고 들어왔으면 그 키워드로, 아니면 그냥 전체 로드
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }
  productStore.fetchProducts(0)
})
</script>

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: var(--space-3);
  color: var(--color-black2);
  font-family: var(--font-sans);
}

/* 필터 바 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
  flex-wrap: wrap;
  gap: var(--space-1);
}
.filter-tabs { display: flex; gap: 4px; }
.tab-btn {
  padding: 6px 16px;
  border: 1.5px solid var(--color-gray5);
  border-radius: 4px;
  background: var(--color-white1);
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font-sans);
  color: var(--color-gray2);
  font-weight: 500;
  transition: all 0.2s;
}
.tab-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.tab-btn.active {
  background: var(--color-primary);
  color: var(--color-white1);
  border-color: var(--color-primary);
}

/* 검색 박스 */
.search-box { display: flex; gap: 4px; }
.search-select {
  border: 1.5px solid var(--color-gray5);
  border-radius: 4px;
  padding: 6px 10px;
  font-size: 13px;
  font-family: var(--font-sans);
  color: var(--color-gray2);
  background: var(--color-white2);
  outline: none;
}
.search-input {
  border: 1.5px solid var(--color-gray5);
  border-radius: 4px;
  padding: 6px 12px;
  width: 200px;
  font-size: 13px;
  font-family: var(--font-sans);
  color: var(--color-black2);
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: var(--color-primary); }
.search-input::placeholder { color: var(--color-gray4); }
.search-btn {
  background: var(--color-primary);
  color: var(--color-white1);
  border: none;
  border-radius: 4px;
  padding: 6px 16px;
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font-sans);
  font-weight: 500;
  transition: background 0.2s;
}
.search-btn:hover { background: var(--color-secondary); }

/* 소분류 탭 */
.sub-filter-tabs {
  display: flex;
  gap: 6px;
  margin-bottom: var(--space-3);
  flex-wrap: wrap;
}
.sub-tab-btn {
  padding: 4px 14px;
  border: 1.5px solid var(--color-gray5);
  border-radius: 20px;
  background: var(--color-white1);
  cursor: pointer;
  font-size: 12px;
  font-family: var(--font-sans);
  color: var(--color-gray2);
  transition: all 0.2s;
}
.sub-tab-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.sub-tab-btn.active {
  background: var(--color-primary);
  color: var(--color-white1);
  border-color: var(--color-primary);
}

/* 로딩 / 에러 */
.status-area {
  text-align: center;
  padding: 60px 0;
  color: var(--color-gray3);
  font-size: 14px;
  font-family: var(--font-sans);
}
.status-area.error { color: var(--color-error); }

/* 상품 그리드 — 4열 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

/* 상품 카드 */
.product-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  border: 1px solid var(--color-gray5);
  border-radius: 8px;
  padding: var(--space-2);
  transition: box-shadow 0.2s, transform 0.2s;
  background: var(--color-white1);
}
.product-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

/* 뱃지 */
.badge {
  position: absolute;
  top: var(--space-1);
  left: var(--space-1);
  font-size: 10px;
  padding: 2px 7px;
  border-radius: 4px;
  font-weight: 700;
  color: var(--color-white1);
  font-family: var(--font-sans);
  letter-spacing: 0.2px;
}
.badge-best { background: var(--color-primary); }
.badge-sale  { background: var(--color-secondary); }

/* 썸네일 */
.product-thumbnail {
  width: 100%;
  aspect-ratio: 3 / 4;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.product-thumbnail:hover { opacity: 0.9; }

/* 상품 정보 */
.product-info { display: flex; flex-direction: column; gap: 4px; }
.product-title {
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  color: var(--color-black2);
  font-family: var(--font-sans);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  transition: color 0.2s;
}
.product-title:hover { color: var(--color-secondary); }
.product-author {
  font-size: 12px;
  color: var(--color-gray3);
  font-family: var(--font-sans);
}
.product-price { display: flex; align-items: center; gap: 6px; }
.discount-rate {
  color: var(--color-secondary);
  font-weight: 700;
  font-size: 14px;
  font-family: var(--font-sans);
}
.sale-price {
  font-weight: 700;
  font-size: 15px;
  color: var(--color-black2);
  font-family: var(--font-sans);
}
.original-price {
  font-size: 11px;
  color: var(--color-gray4);
  text-decoration: line-through;
  font-family: var(--font-sans);
}

/* 장바구니 버튼 */
.cart-btn {
  width: 100%;
  padding: 8px 0;
  background: var(--color-primary);
  color: var(--color-white1);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  font-family: var(--font-sans);
  margin-top: auto;
  transition: background 0.2s;
}
.cart-btn:hover { background: var(--color-secondary); }

/* 페이지네이션 */
.pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: var(--space-3);
}
.pagination button {
  padding: 6px 12px;
  border: 1.5px solid var(--color-gray5);
  border-radius: 4px;
  background: var(--color-white1);
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font-sans);
  color: var(--color-gray2);
  transition: all 0.2s;
}
.pagination button:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.pagination button:disabled { color: var(--color-gray4); cursor: default; }
.page-num.active {
  background: var(--color-primary);
  color: var(--color-white1);
  border-color: var(--color-primary);
}

/* 반응형 */
@media (max-width: 800px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 560px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>