import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {

  // 장바구니 아이템 배열
  // 각 항목 형태: { cartItemId, productId, title, author, salePrice, thumbnail, quantity, isChecked, itemTotal }
  const items = ref([])

  const loading = ref(false)
  const error = ref(null)

  // 장바구니 총 수량 (헤더 뱃지에 사용)
  const totalCount = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  // 체크된 항목의 결제 예상금액
  const checkedTotal = computed(() =>
    items.value
      .filter(item => item.isChecked)
      .reduce((sum, item) => sum + item.itemTotal, 0)
  )

  // 체크된 항목의 원래 정가 합계 (할인 전)
  // → 화면에서 "총 상품금액"으로 표시할 값
  // 주의: 현재 백엔드 CartItemResponse에 price(정가)가 없음
  //       일단 salePrice로 대체. 나중에 백엔드에서 price 필드 추가하면 여기도 수정
  const checkedOriginalTotal = computed(() =>
    items.value
      .filter(item => item.isChecked)
      .reduce((sum, item) => sum + item.salePrice * item.quantity, 0)
  )

  // 서버에서 장바구니 목록 가져오기
  async function fetchCart() {
    loading.value = true
    error.value = null
    try {
      const { data } = await cartApi.getItems()
      items.value = data
    } catch (e) {
      error.value = '장바구니를 불러오지 못했습니다.'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  // 장바구니 담기
  async function addItem(productId, quantity = 1) {
    await cartApi.addItem(productId, quantity)
    await fetchCart() // 담은 후 목록 갱신
  }

  // 수량 변경
  async function updateItem(cartItemId, quantity) {
    await cartApi.updateItem(cartItemId, quantity)
    await fetchCart()
  }

  // 항목 삭제
  async function removeItem(cartItemId) {
    await cartApi.removeItem(cartItemId)
    await fetchCart()
  }

  // 체크 상태 토글 (서버 저장 없이 화면에서만 관리)
  // 백엔드 CartItem에 isChecked 필드가 있지만
  // 체크 변경마다 서버 호출하는 건 과도함 → 화면 상태만 변경
  function toggleCheck(cartItemId) {
    const item = items.value.find(i => i.cartItemId === cartItemId)
    if (item) item.isChecked = !item.isChecked
  }

  // 전체 체크 / 전체 해제
  function toggleAll(checked) {
    items.value.forEach(item => { item.isChecked = checked })
  }

  return {
    items,
    loading,
    error,
    totalCount,
    checkedTotal,
    checkedOriginalTotal,
    fetchCart,
    addItem,
    updateItem,
    removeItem,
    toggleCheck,
    toggleAll
  }
})