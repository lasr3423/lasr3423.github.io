<template>
  <header class="app-header">
    <div class="header-inner">

      <!-- 로고 -->
      <router-link to="/" class="logo">
        📖 ReadMe
      </router-link>

      <!-- 검색 바 -->
      <div class="search-bar">
        <input
          v-model="searchKeyword"
          class="search-input"
          type="text"
          placeholder="책제목을 입력하세요 (도서명, 저자, ISBN)"
          @keyup.enter="handleSearch"
        />
        <select v-model="searchType" class="search-select">
          <option value="all">전체</option>
          <option value="title">도서명</option>
          <option value="author">저자</option>
          <option value="isbn">ISBN</option>
        </select>
        <button class="search-btn" @click="handleSearch">🔍</button>
      </div>

      <!-- 오른쪽 메뉴 -->
      <nav class="header-nav">
        <!-- 로그인 상태 -->
        <template v-if="authStore.isLoggedIn">
          <button class="nav-link" @click="handleLogout">로그아웃</button>
          <router-link class="nav-link" to="/mypage">회원정보 변경</router-link>
          <router-link class="nav-link" to="/mypage">마이페이지</router-link>
          <!-- 장바구니 아이콘 + 담긴 수량 뱃지 -->
          <router-link class="nav-link cart-icon" to="/cart">
            🛒
            <span v-if="cartStore.totalCount > 0" class="cart-badge">
              {{ cartStore.totalCount }}
            </span>
          </router-link>
        </template>

        <!-- 비로그인 상태 -->
        <template v-else>
          <router-link class="nav-link" to="/signin">로그인</router-link>
          <router-link class="nav-link" to="/signup">회원가입</router-link>
          <router-link class="nav-link cart-icon" to="/cart">🛒</router-link>
        </template>
      </nav>

    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const searchType = ref('all')

// 검색 실행 — 상품 목록 페이지로 이동하면서 keyword를 query로 넘김
function handleSearch() {
  if (!searchKeyword.value.trim()) return
  router.push({
    path: '/product',
    query: {
      keyword: searchKeyword.value.trim(),
      searchType: searchType.value
    }
  })
}

// 로그아웃
async function handleLogout() {
  await authStore.signout()
  alert('로그아웃 되었습니다.')
  router.push('/')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--color-white1);
  border-bottom: 1px solid var(--color-gray5);
  box-shadow: var(--shadow-sm);
}
.header-inner {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  max-width: 1440px;
  margin: 0 auto;
  padding: 12px var(--space-2);
}

/* 로고 */
.logo {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  text-decoration: none;
  white-space: nowrap;
  flex-shrink: 0;
  letter-spacing: -0.3px;
  font-family: var(--font-sans);
}
.logo:hover { color: var(--color-secondary); }

/* 검색 바 */
.search-bar {
  flex: 1;
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  border: 1.5px solid var(--color-gray5);
  border-radius: 6px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.search-bar:focus-within { border-color: var(--color-primary); }

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 9px 14px;
  font-size: 14px;
  font-family: var(--font-sans);
  color: var(--color-black2);
  background: var(--color-white1);
}
.search-input::placeholder { color: var(--color-gray4); }

.search-select {
  border: none;
  border-left: 1px solid var(--color-gray5);
  background: var(--color-white2);
  padding: 9px 10px;
  font-size: 13px;
  font-family: var(--font-sans);
  color: var(--color-gray2);
  cursor: pointer;
  outline: none;
}

.search-btn {
  border: none;
  background: var(--color-primary);
  color: var(--color-white1);
  padding: 9px 16px;
  cursor: pointer;
  font-size: 15px;
  transition: background 0.2s;
}
.search-btn:hover { background: var(--color-secondary); }

/* 오른쪽 메뉴 */
.header-nav {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}
.nav-link {
  font-size: 13px;
  font-family: var(--font-sans);
  color: var(--color-gray2);
  text-decoration: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  white-space: nowrap;
  transition: color 0.2s;
}
.nav-link:hover { color: var(--color-secondary); }

/* 장바구니 아이콘 뱃지 */
.cart-icon {
  position: relative;
  font-size: 22px;
  line-height: 1;
}
.cart-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: var(--color-secondary);
  color: var(--color-white1);
  font-size: 10px;
  font-weight: 700;
  border-radius: 50%;
  width: 17px;
  height: 17px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .search-bar { display: none; }
  .nav-link { font-size: 12px; }
  .header-nav { gap: var(--space-1); }
}
</style>