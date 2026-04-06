<template>
  <aside class="app-sidebar">
    <div class="sidebar-section">
      <p class="sidebar-title">카테고리</p>

      <div v-for="top in categories" :key="top.id" class="category-group">
        <!-- Top 카테고리 -->
        <button
          class="top-menu"
          :class="{ active: activeTopId === top.id }"
          @click="toggleTop(top.id)"
        >
          {{ top.name }}
          <span class="arrow" :class="{ open: activeTopId === top.id }">›</span>
        </button>

        <!-- Sub 카테고리 -->
        <div v-if="activeTopId === top.id" class="sub-menu">
          <router-link
            v-for="sub in top.subs"
            :key="sub.id"
            :to="`/product?topId=${top.id}&subId=${sub.id}`"
            :class="{ active: isActiveSub(top.id, sub.id) }"
          >
            {{ sub.name }}
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

// dummy_data.sql 기준 — ID는 DB에서 실제 부여된 값과 일치해야 함
const categories = [
  {
    id: 1,
    name: '국내도서',
    subs: [
      { id: 1,  name: '소설' },
      { id: 2,  name: '자기계발' },
      { id: 3,  name: '경제/경영' },
      { id: 4,  name: '과학/기술' },
      { id: 5,  name: '역사/문화' },
    ]
  },
  {
    id: 2,
    name: '해외도서',
    subs: [
      { id: 6,  name: 'Fiction' },
      { id: 7,  name: 'Self-Help' },
      { id: 8,  name: 'Business' },
      { id: 9,  name: 'Science' },
      { id: 10, name: 'History' },
    ]
  },
  {
    id: 3,
    name: '일본도서',
    subs: [
      { id: 11, name: 'Fiction' },
      { id: 12, name: 'Self-Help' },
      { id: 13, name: 'Business' },
      { id: 14, name: 'Science' },
      { id: 15, name: 'History' },
    ]
  },
]

const activeTopId = ref(null)

function toggleTop(id) {
  activeTopId.value = activeTopId.value === id ? null : id
}

// topId와 subId 둘 다 일치해야 active — 다른 top의 같은 이름 sub와 혼동 방지
function isActiveSub(topId, subId) {
  return Number(route.query.topId) === topId && Number(route.query.subId) === subId
}
</script>

<style scoped>
/* 기존 스타일 그대로 유지 */
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