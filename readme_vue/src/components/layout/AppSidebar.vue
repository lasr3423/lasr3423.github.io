PDF 스타일 가이드 확인했어요. Primary #092C4C, Secondary #F2994A 기준으로 만들게요.
vue<template>
  <aside class="app-sidebar">
    <div class="sidebar-section">
      <p class="sidebar-title">카테고리</p>

      <div v-for="top in categories" :key="top.name" class="category-group">
        <!-- Top 카테고리 -->
        <button
          class="top-menu"
          :class="{ active: activeTop === top.name }"
          @click="toggleTop(top.name)"
        >
          {{ top.name }}
          <span class="arrow" :class="{ open: activeTop === top.name }">›</span>
        </button>

        <!-- Sub 카테고리 (Top 활성화 시에만 노출) -->
        <div v-if="activeTop === top.name" class="sub-menu">
          <router-link
            v-for="sub in top.subs"
            :key="sub"
            :to="`/product?category=${sub}`"
            :class="{ active: isActiveSub(sub) }"
          >
            {{ sub }}
          </router-link>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const categories = [
  { name: '국내도서', subs: ['IT/컴퓨터', '자기개발', '외국어', '소설', '만화'] },
  { name: '해외도서', subs: ['IT/컴퓨터', '자기개발', '외국어', '소설', '만화'] },
  { name: '일본도서', subs: ['IT/컴퓨터', '자기개발', '외국어', '소설', '만화'] },
]

const activeTop = ref(null)

function toggleTop(name) {
  activeTop.value = activeTop.value === name ? null : name
}

function isActiveSub(sub) {
  return route.query.category === sub
}
</script>

<style scoped>
.app-sidebar {
  width: 180px;
  flex-shrink: 0;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-gray5);
  border-radius: 8px;
  padding: var(--space-2) 12px;
  position: sticky;
  top: 72px;
  background: var(--color-white1);
  box-shadow: var(--shadow-sm);
}

.sidebar-title {
  font-weight: 700;
  font-size: 14px;
  color: var(--color-primary);
  margin-bottom: var(--space-1);
  padding-bottom: var(--space-1);
  border-bottom: 2px solid var(--color-primary);
  font-family: var(--font-sans);
}

.category-group {
  display: flex;
  flex-direction: column;
}

.top-menu {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: none;
  border: none;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-black2);
  padding: 8px 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: color 0.2s;
  font-family: var(--font-sans);
}
.top-menu:hover { color: var(--color-secondary); }
.top-menu.active { color: var(--color-primary); }

.arrow {
  font-size: 16px;
  transition: transform 0.2s;
  display: inline-block;
  color: var(--color-gray3);
}
.arrow.open { transform: rotate(90deg); }

.sub-menu {
  display: flex;
  flex-direction: column;
  padding-left: 12px;
  margin-bottom: 4px;
}

.sub-menu a {
  font-size: 12px;
  color: var(--color-gray3);
  text-decoration: none;
  padding: 5px 4px;
  border-radius: 4px;
  transition: color 0.2s;
  font-family: var(--font-sans);
}
.sub-menu a:hover { color: var(--color-secondary); }
.sub-menu a.active {
  color: var(--color-secondary);
  font-weight: 700;
}
</style>