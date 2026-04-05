<template>
  <div>

    <h2 class="page-title">장바구니</h2>

    <!-- 로딩 -->
    <div v-if="cartStore.loading" class="status-area">불러오는 중...</div>
    <!-- 에러 -->
    <div v-else-if="cartStore.error" class="status-area error">{{ cartStore.error }}</div>

    <!-- 장바구니 비었을 때 -->
    <div v-else-if="cartStore.items.length === 0" class="empty-cart">
      <p>장바구니가 비어 있습니다.</p>
      <button class="go-shop-btn" @click="router.push('/product')">쇼핑 계속하기</button>
    </div>

    <!-- 장바구니 본문 -->
    <div v-else class="cart-body">

      <!-- 왼쪽: 상품 테이블 -->
      <div class="cart-table-area">

        <!-- 헤더 (전체선택 / 선택삭제) -->
        <div class="cart-header">
          <label class="check-label">
            <input
              type="checkbox"
              :checked="isAllChecked"
              @change="toggleAll"
            />
            전체선택 · 항목 {{ cartStore.items.length }}개
          </label>
          <button class="delete-btn" @click="removeCheckedItems">선택항목 삭제</button>
        </div>

        <!-- 컬럼 헤더 -->
        <div class="table-head">
          <div class="col-check"></div>
          <div class="col-info">상품정보</div>
          <div class="col-price">판매가</div>
          <div class="col-qty">수량</div>
          <div class="col-total">합계</div>
        </div>

        <!-- 상품 행 -->
        <div
          v-for="item in cartStore.items"
          :key="item.cartItemId"
          class="cart-row"
        >
          <!-- 체크박스 -->
          <div class="col-check">
            <input
              type="checkbox"
              :checked="item.isChecked"
              @change="cartStore.toggleCheck(item.cartItemId)"
            />
          </div>

          <!-- 상품 정보 -->
          <div class="col-info">
            <img
              :src="item.thumbnail || '/img/no-image.png'"
              :alt="item.title"
              class="item-thumbnail"
              @click="router.push(`/product/${item.productId}`)"
            />
            <div class="item-meta">
              <p class="item-title" @click="router.push(`/product/${item.productId}`)">
                {{ item.title }}
              </p>
              <p class="item-author">{{ item.author }}</p>
              <p class="item-delivery">📦 두밀배송</p>
            </div>
          </div>

          <!-- 판매가 -->
          <div class="col-price">
            {{ item.salePrice.toLocaleString() }}원
          </div>

          <!-- 수량 조절 -->
          <div class="col-qty">
            <div class="qty-control">
              <button
                class="qty-btn"
                @click="handleUpdateQty(item, item.quantity - 1)"
                :disabled="item.quantity <= 1"
              >-</button>
              <span class="qty-value">{{ item.quantity }}</span>
              <button
                class="qty-btn"
                @click="handleUpdateQty(item, item.quantity + 1)"
              >+</button>
            </div>
          </div>

          <!-- 합계 -->
          <div class="col-total">
            {{ (item.salePrice * item.quantity).toLocaleString() }}원
          </div>
        </div>

        <!-- 하단 버튼 -->
        <div class="cart-footer">
          <button class="delete-btn" @click="removeCheckedItems">선택항목 삭제</button>
        </div>

      </div>

      <!-- 오른쪽: 주문 내역 패널 -->
      <div class="order-panel">
        <h3 class="panel-title">주문 내역</h3>

        <div class="panel-row">
          <span>총 상품금액</span>
          <span>{{ cartStore.checkedOriginalTotal.toLocaleString() }}원</span>
        </div>
        <div class="panel-row">
          <span>총 할인금액</span>
          <!-- 현재 CartItemResponse에 정가(price) 없어서 0원 고정 -->
          <!-- 추후 백엔드에서 price 필드 추가하면 수정 -->
          <span class="discount-text">-0원</span>
        </div>
        <div class="panel-row">
          <span>정 배송비</span>
          <span class="free-text">무료</span>
        </div>

        <hr class="panel-divider" />

        <div class="panel-row total-row">
          <span>결제예상금액</span>
          <span class="total-price">{{ cartStore.checkedTotal.toLocaleString() }}원</span>
        </div>

        <button
          class="order-btn"
          :disabled="checkedCount === 0"
          @click="goToOrder"
        >
          주문하기
        </button>

        <p class="checked-count">{{ checkedCount }}개 상품 선택됨</p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore }  from '@/store/cart'
import { useOrderStore } from '@/store/order'

const router     = useRouter()
const cartStore  = useCartStore()
const orderStore = useOrderStore()

// ── computed ─────────────────────────────────────────────────
const isAllChecked = computed(() =>
  cartStore.items.length > 0 && cartStore.items.every(i => i.isChecked)
)

const checkedCount = computed(() =>
  cartStore.items.filter(i => i.isChecked).length
)

// ── 이벤트 핸들러 ─────────────────────────────────────────────

// 전체 선택 / 전체 해제
function toggleAll(e) {
  cartStore.toggleAll(e.target.checked)
}

// 수량 변경
async function handleUpdateQty(item, newQty) {
  if (newQty < 1) {
    if (confirm('이 상품을 장바구니에서 삭제할까요?')) {
      await cartStore.removeItem(item.cartItemId)
    }
    return
  }
  try {
    await cartStore.updateItem(item.cartItemId, newQty)
  } catch (e) {
    alert('수량 변경에 실패했습니다.')
  }
}

// 선택 항목 삭제
async function removeCheckedItems() {
  const checked = cartStore.items.filter(i => i.isChecked)
  if (checked.length === 0) {
    alert('삭제할 항목을 선택하세요.')
    return
  }
  if (!confirm(`선택한 ${checked.length}개 상품을 삭제할까요?`)) return

  for (const item of checked) {
    await cartStore.removeItem(item.cartItemId)
  }
}

// 주문하기
function goToOrder() {
  if (checkedCount.value === 0) {
    alert('주문할 상품을 선택하세요.')
    return
  }
  const checkedItems = cartStore.items.filter(i => i.isChecked)

  // 상품명 "첫 번째 상품명 외 N건" 형식
  const itemName = checkedItems.length === 1
    ? checkedItems[0].title
    : `${checkedItems[0].title} 외 ${checkedItems.length - 1}건`

  orderStore.setOrder(null, cartStore.checkedTotal, itemName)

  // 체크된 cartItemId 목록을 쉼표로 이어서 query로 전달
  const ids = checkedItems.map(i => i.cartItemId).join(',')
  router.push({ path: '/order', query: { cartItemIds: ids } })
}

// 마운트 시 장바구니 로드
onMounted(() => {
  cartStore.fetchCart()
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: var(--space-3);
  color: var(--color-black2);
  font-family: var(--font-sans);
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

/* 빈 장바구니 */
.empty-cart {
  text-align: center;
  padding: 80px 0;
  color: var(--color-gray3);
  font-family: var(--font-sans);
}
.go-shop-btn {
  margin-top: var(--space-2);
  padding: 10px 24px;
  background: var(--color-primary);
  color: var(--color-white1);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-family: var(--font-sans);
  font-weight: 600;
  transition: background 0.2s;
}
.go-shop-btn:hover { background: var(--color-secondary); }

/* 본문 레이아웃 (테이블 + 패널) */
.cart-body {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}
.cart-table-area { flex: 1; min-width: 0; }

/* 테이블 헤더 */
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 2px solid var(--color-primary);
  margin-bottom: var(--space-1);
  font-size: 14px;
  font-family: var(--font-sans);
  color: var(--color-black2);
}
.check-label {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  cursor: pointer;
}
.delete-btn {
  background: none;
  border: 1px solid var(--color-gray4);
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
  color: var(--color-gray2);
  font-family: var(--font-sans);
  transition: border-color 0.2s, color 0.2s;
}
.delete-btn:hover { border-color: var(--color-error); color: var(--color-error); }

/* 컬럼 헤더 */
.table-head {
  display: flex;
  align-items: center;
  padding: var(--space-1) 0;
  background: var(--color-white2);
  border: 1px solid var(--color-gray5);
  font-size: 12px;
  color: var(--color-gray3);
  font-weight: 600;
  text-align: center;
  font-family: var(--font-sans);
}

/* 컬럼 너비 — 헤더와 본문 행에서 공통으로 사용 */
.col-check { width: 44px; display: flex; justify-content: center; }
.col-info  { flex: 1; text-align: left; padding-left: var(--space-1); }
.col-price { width: 100px; text-align: center; }
.col-qty   { width: 110px; display: flex; justify-content: center; }
.col-total { width: 100px; text-align: center; }

/* 상품 행 */
.cart-row {
  display: flex;
  align-items: center;
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-gray5);
  font-size: 13px;
  font-family: var(--font-sans);
}

/* 썸네일 */
.item-thumbnail {
  width: 70px;
  height: 90px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.2s;
}
.item-thumbnail:hover { opacity: 0.85; }
.item-meta { display: flex; flex-direction: column; gap: 4px; margin-left: 12px; }
.item-title {
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  color: var(--color-black2);
  font-family: var(--font-sans);
  transition: color 0.2s;
}
.item-title:hover { color: var(--color-secondary); }
.item-author   { font-size: 12px; color: var(--color-gray3); font-family: var(--font-sans); }
.item-delivery { font-size: 11px; color: var(--color-info); font-family: var(--font-sans); }

/* 수량 조절 */
.qty-control {
  display: flex;
  align-items: center;
  border: 1.5px solid var(--color-gray5);
  border-radius: 4px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.qty-control:focus-within { border-color: var(--color-primary); }
.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: var(--color-white2);
  cursor: pointer;
  font-size: 16px;
  color: var(--color-gray1);
  font-family: var(--font-sans);
  transition: background 0.2s, color 0.2s;
}
.qty-btn:disabled { color: var(--color-gray4); cursor: default; }
.qty-btn:not(:disabled):hover {
  background: var(--color-primary);
  color: var(--color-white1);
}
.qty-value {
  min-width: 32px;
  text-align: center;
  font-size: 14px;
  padding: 0 4px;
  border-left: 1px solid var(--color-gray5);
  border-right: 1px solid var(--color-gray5);
  color: var(--color-black2);
  font-family: var(--font-sans);
}

/* 테이블 하단 버튼 */
.cart-footer {
  padding: 12px 0;
  border-top: 1px solid var(--color-gray5);
}

/* 주문 패널 */
.order-panel {
  width: 230px;
  flex-shrink: 0;
  border: 1px solid var(--color-gray5);
  border-radius: 8px;
  padding: var(--space-3);
  position: sticky;
  top: 80px;
  background: var(--color-white1);
  box-shadow: var(--shadow-sm);
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: var(--space-2);
  color: var(--color-black2);
  font-family: var(--font-sans);
}
.panel-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 10px;
  color: var(--color-gray2);
  font-family: var(--font-sans);
}
.discount-text { color: var(--color-info); }
.free-text     { color: var(--color-success); }
.panel-divider { border: none; border-top: 1px solid var(--color-gray5); margin: 12px 0; }
.total-row     { font-size: 14px; font-weight: 700; color: var(--color-black2); }
.total-price   { color: var(--color-primary); font-size: 18px; font-weight: 700; }

/* 주문 버튼 */
.order-btn {
  width: 100%;
  padding: 14px 0;
  background: var(--color-primary);
  color: var(--color-white1);
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 12px;
  font-family: var(--font-sans);
  transition: background 0.2s;
}
.order-btn:disabled { background: var(--color-gray4); cursor: default; }
.order-btn:not(:disabled):hover { background: var(--color-secondary); }

.checked-count {
  text-align: center;
  font-size: 12px;
  color: var(--color-gray3);
  margin-top: var(--space-1);
  font-family: var(--font-sans);
}

/* 반응형 */
@media (max-width: 760px) {
  .cart-body { flex-direction: column; }
  .order-panel { width: 100%; position: static; }
}
</style>