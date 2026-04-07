<template>
  <div>

    <!-- 로딩 / 에러 -->
    <div v-if="loading" class="status-area">불러오는 중...</div>
    <div v-else-if="error" class="status-area error">{{ error }}</div>

    <!-- 상품 정보 본문 -->
    <template v-else-if="product">

      <!-- 상단: 이미지 + 오른쪽 정보 -->
      <div class="product-top">

        <!-- 책 이미지 -->
        <div class="image-area">
          <img
            :src="product.thumbnail || '/img/no-image.png'"
            :alt="product.title"
            class="product-image"
          />
        </div>

        <!-- 오른쪽 정보 -->
        <div class="info-area">
          <h1 class="product-title">{{ product.title }}</h1>
          <p class="product-author">{{ product.author }}</p>

          <!-- 정가 -->
          <div class="price-row">
            <span class="label">가격</span>
            <span class="original-price">{{ product.price.toLocaleString() }}원</span>
          </div>

          <!-- 할인가 + 별점 + 적립 -->
          <div class="sale-price-row">
            <span class="stars">★★★★☆</span>
            <span class="discount-rate">{{ product.discountRate }}%</span>
            <span class="sale-price">{{ product.salePrice.toLocaleString() }}원</span>
            <span class="point-info">
              {{ Math.floor(product.salePrice * 0.1).toLocaleString() }}원 (10% 적립)
            </span>
          </div>

          <!-- 재고 상태 -->
          <p v-if="product.stock === 0" class="out-of-stock">품절된 상품입니다.</p>
          <p v-else-if="product.stock <= 5" class="stock-warning">
            ⚠️ 재고 {{ product.stock }}개 남았습니다.
          </p>

          <!-- 수량 선택 -->
          <div class="quantity-row">
            <button class="qty-btn" @click="decreaseQty" :disabled="quantity <= 1">-</button>
            <span class="qty-value">{{ quantity }}</span>
            <button class="qty-btn" @click="increaseQty" :disabled="product.stock === 0">+</button>
          </div>

          <!-- 장바구니 버튼 -->
          <button
            class="cart-btn"
            @click="addToCart"
            :disabled="product.stock === 0"
          >
            🛒 장바구니 담기
          </button>
        </div>
      </div>

      <!-- 탭 영역 -->
      <div class="tabs-area">
        <div class="tab-list">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="tab-content">
          <!-- 상품정보 -->
          <div v-if="activeTab === 'info'">
            <p class="description">{{ product.description || '상품 설명이 없습니다.' }}</p>
          </div>

          <!-- 리뷰 (추후 구현) -->
          <div v-if="activeTab === 'review'">
            <p class="tab-placeholder">리뷰 기능은 준비 중입니다.</p>
          </div>

          <!-- QnA (추후 구현) -->
          <div v-if="activeTab === 'qna'">
            <p class="tab-placeholder">QnA 기능은 준비 중입니다.</p>
          </div>

          <!-- 배송/교환 -->
          <div v-if="activeTab === 'delivery'">
            <div class="delivery-info">
              <p>📦 <strong>두밀배송</strong> — 오전 주문 시 당일 배송</p>
              <p>🔄 교환/반품 사유에 따라 비용이 발생할 수 있습니다.</p>
            </div>
          </div>
        </div>
      </div>

    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { useCartStore } from '@/store/cart'
import { useAuthStore } from '@/store/auth'

const route    = useRoute()
const router   = useRouter()
const cartStore  = useCartStore()
const authStore  = useAuthStore()

// ── 상태 ─────────────────────────────────────────────────────
const product  = ref(null)
const loading  = ref(false)
const error    = ref(null)
const quantity = ref(1)

// 탭
const tabs = [
  { key: 'info',     label: '상품정보' },
  { key: 'review',   label: '리뷰' },
  { key: 'qna',      label: 'Q&A' },
  { key: 'delivery', label: '배송/교환' },
]
const activeTab = ref('info')

// ── 수량 조절 ─────────────────────────────────────────────────
function increaseQty() {
  if (product.value && quantity.value < product.value.stock) {
    quantity.value++
  }
}
function decreaseQty() {
  if (quantity.value > 1) quantity.value--
}

// ── 장바구니 담기 ─────────────────────────────────────────────
async function addToCart() {
  if (!authStore.isLoggedIn) {
    alert('로그인이 필요합니다.')
    router.push('/signin')
    return
  }
  try {
    await cartStore.addItem(product.value.id, quantity.value)
    alert(`장바구니에 ${quantity.value}권 담겼습니다.`)
  } catch (e) {
    alert('장바구니 담기에 실패했습니다.')
  }
}

// ── 데이터 로드 ───────────────────────────────────────────────
async function fetchProduct() {
  loading.value = true
  error.value   = null
  try {
    const { data } = await productApi.getDetail(route.params.productId)
    product.value  = data
  } catch (e) {
    error.value = e.response?.status === 404
      ? '존재하지 않는 상품입니다.'
      : '상품 정보를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchProduct() })
</script>

<style scoped>
.status-area {
  text-align: center;
  padding: 80px 0;
  color: var(--color-gray3);
  font-size: 14px;
  font-family: var(--font-sans);
}
.status-area.error { color: var(--color-error); }

/* 상단 상품 영역 */
.product-top {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

/* 이미지 */
.image-area { flex-shrink: 0; }
.product-image {
  width: 220px;
  aspect-ratio: 3 / 4;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: var(--shadow-md);
}

/* 오른쪽 정보 */
.info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.product-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-black2);
  margin: 0;
  font-family: var(--font-sans);
  line-height: 1.3;
}
.product-author {
  font-size: 14px;
  color: var(--color-gray2);
  font-family: var(--font-sans);
}

/* 가격 */
.price-row { display: flex; align-items: center; gap: 12px; }
.label { font-size: 13px; color: var(--color-gray3); width: 40px; font-family: var(--font-sans); }
.original-price {
  font-size: 14px;
  color: var(--color-gray4);
  text-decoration: line-through;
  font-family: var(--font-sans);
}

.sale-price-row {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-wrap: wrap;
}
.stars { color: var(--color-secondary); font-size: 16px; }
.discount-rate {
  color: var(--color-secondary);
  font-size: 22px;
  font-weight: 700;
  font-family: var(--font-sans);
}
.sale-price {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-black2);
  font-family: var(--font-sans);
}
.point-info {
  font-size: 12px;
  color: var(--color-info);
  font-family: var(--font-sans);
}

/* 재고 */
.stock-warning {
  font-size: 13px;
  color: var(--color-warning);
  font-family: var(--font-sans);
}
.out-of-stock {
  font-size: 13px;
  color: var(--color-error);
  font-weight: 700;
  font-family: var(--font-sans);
}

/* 수량 */
.quantity-row {
  display: flex;
  align-items: center;
  border: 1.5px solid var(--color-gray5);
  border-radius: 6px;
  width: fit-content;
  overflow: hidden;
  transition: border-color 0.2s;
}
.quantity-row:focus-within { border-color: var(--color-primary); }
.qty-btn {
  background: var(--color-white2);
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 6px 14px;
  color: var(--color-gray1);
  transition: background 0.2s, color 0.2s;
  font-family: var(--font-sans);
}
.qty-btn:disabled { color: var(--color-gray4); cursor: default; }
.qty-btn:not(:disabled):hover {
  background: var(--color-primary);
  color: var(--color-white1);
}
.qty-value {
  font-size: 16px;
  font-weight: 600;
  min-width: 36px;
  text-align: center;
  padding: 6px 4px;
  border-left: 1px solid var(--color-gray5);
  border-right: 1px solid var(--color-gray5);
  color: var(--color-black2);
  font-family: var(--font-sans);
}

/* 장바구니 버튼 */
.cart-btn {
  padding: 14px 32px;
  background: var(--color-primary);
  color: var(--color-white1);
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  width: fit-content;
  font-family: var(--font-sans);
  transition: background 0.2s;
}
.cart-btn:hover:not(:disabled) { background: var(--color-secondary); }
.cart-btn:disabled { background: var(--color-gray4); cursor: default; }

/* 탭 */
.tabs-area { border-top: 1px solid var(--color-gray5); padding-top: var(--space-3); }
.tab-list {
  display: flex;
  border-bottom: 2px solid var(--color-gray5);
  margin-bottom: var(--space-3);
}
.tab-btn {
  padding: 12px 20px;
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  color: var(--color-gray3);
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  font-family: var(--font-sans);
  font-weight: 500;
  transition: color 0.2s;
}
.tab-btn:hover { color: var(--color-secondary); }
.tab-btn.active {
  color: var(--color-primary);
  font-weight: 700;
  border-bottom-color: var(--color-primary);
}

/* 탭 내용 */
.tab-content { min-height: 200px; }
.description {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-gray1);
  white-space: pre-line;
  font-family: var(--font-sans);
}
.tab-placeholder {
  text-align: center;
  padding: 40px 0;
  color: var(--color-gray4);
  font-size: 14px;
  font-family: var(--font-sans);
}
.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
  color: var(--color-gray2);
  font-family: var(--font-sans);
}

/* 반응형 */
@media (max-width: 600px) {
  .product-top { flex-direction: column; }
  .product-image { width: 160px; }
}
</style>