<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">회원 관리</h1>
      <p class="mt-1 text-sm text-slate-400">회원 상태 변경 및 등급 관리를 합니다.</p>
    </section>

    <!-- 검색 / 필터 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm space-y-4">
      <!-- 검색창 -->
      <div class="flex gap-3">
        <input
          v-model="keyword"
          type="text"
          placeholder="이름 또는 이메일 검색"
          class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none transition focus:border-brand-400 focus:bg-white"
          @keyup.enter="search"
        />
        <button
          class="rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700"
          @click="search"
        >
          검색
        </button>
      </div>

      <!-- 상태 필터 (서버) -->
      <div class="flex flex-wrap items-center gap-2">
        <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">상태</span>
        <button
          v-for="f in statusFilters"
          :key="f.value"
          class="rounded-full px-4 py-1.5 text-xs font-semibold transition"
          :class="statusFilter === f.value
            ? 'bg-brand-800 text-white'
            : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
          @click="setStatusFilter(f.value)"
        >
          {{ f.label }}
        </button>
      </div>

      <!-- 등급 필터 (클라이언트) -->
      <div class="flex flex-wrap items-center gap-2">
        <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">등급</span>
        <button
          v-for="f in roleFilters"
          :key="f.value"
          class="rounded-full px-4 py-1.5 text-xs font-semibold transition"
          :class="roleFilter === f.value
            ? 'bg-brand-800 text-white'
            : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
          @click="roleFilter = f.value"
        >
          {{ f.label }}
        </button>
      </div>
    </section>

    <!-- 일괄 처리 버튼 -->
    <div v-if="checkedIds.size > 0" class="flex items-center gap-3 rounded-2xl border border-brand-200 bg-brand-50 px-5 py-3">
      <span class="text-sm font-semibold text-brand-800">{{ checkedIds.size }}명 선택됨</span>
      <button
        class="rounded-xl bg-emerald-600 px-4 py-1.5 text-xs font-semibold text-white transition hover:bg-emerald-500"
        @click="bulkChangeStatus('ACTIVATE')"
      >활성화</button>
      <button
        class="rounded-xl bg-amber-500 px-4 py-1.5 text-xs font-semibold text-white transition hover:bg-amber-400"
        @click="bulkChangeStatus('DEACTIVATE')"
      >정지</button>
      <button
        class="rounded-xl bg-rose-600 px-4 py-1.5 text-xs font-semibold text-white transition hover:bg-rose-500"
        @click="bulkChangeStatus('DELETE')"
      >탈퇴 처리</button>
      <button
        class="ml-auto text-xs text-slate-400 hover:text-slate-600"
        @click="checkedIds.clear(); checkedIds = new Set()"
      >선택 해제</button>
    </div>

    <!-- 테이블 -->
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="filteredMembers.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-4 py-4 text-center">
                  <input type="checkbox" class="rounded" :checked="allChecked" @change="toggleAll" />
                </th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">이름</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">이메일</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">등급</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">가입일</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">상태 변경</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="m in filteredMembers"
                :key="m.id"
                class="transition hover:bg-slate-50"
                :class="checkedIds.has(m.id) ? 'bg-brand-50' : ''"
              >
                <!-- 체크박스 -->
                <td class="px-4 py-4 text-center">
                  <input
                    type="checkbox"
                    class="rounded"
                    :checked="checkedIds.has(m.id)"
                    @change="toggleCheck(m.id)"
                  />
                </td>

                <!-- 이름 -->
                <td class="px-6 py-4 font-semibold text-slate-900">{{ m.name }}</td>

                <!-- 이메일 -->
                <td class="px-6 py-4 text-slate-600">{{ m.email }}</td>

                <!-- 등급 (표시만) -->
                <td class="px-6 py-4 text-center">
                  <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="roleClass(m.memberRole)">
                    {{ m.memberRole }}
                  </span>
                </td>

                <!-- 상태 배지 -->
                <td class="px-6 py-4 text-center">
                  <span class="rounded-full px-3 py-1 text-xs font-semibold" :class="statusClass(m.memberStatus)">
                    {{ statusLabel(m.memberStatus) }}
                  </span>
                </td>

                <!-- 가입일 -->
                <td class="px-6 py-4 text-center text-xs text-slate-500">
                  {{ formatDate(m.createdAt) }}
                </td>

                <!-- 상태 변경 셀렉트 -->
                <td class="px-6 py-4 text-center">
                  <select
                    :value="m.memberStatus"
                    class="rounded-xl border border-slate-200 bg-white px-2 py-1.5 text-xs outline-none focus:border-brand-400"
                    @change="handleStatusChange(m.id, $event.target.value)"
                  >
                    <option value="ACTIVATE">활성화</option>
                    <option value="DEACTIVATE">정지</option>
                    <option value="DELETE">탈퇴 처리</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button
            :disabled="page === 0"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40"
            @click="page--; fetchMembers()"
          >이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button
            :disabled="page >= totalPages - 1"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40"
            @click="page++; fetchMembers()"
          >다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">회원이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const members    = ref([])
const loading    = ref(true)
const keyword    = ref('')
const statusFilter = ref('')
const roleFilter   = ref('')
const page       = ref(0)
const totalPages = ref(1)
let   checkedIds = ref(new Set())

const statusFilters = [
  { label: '전체',   value: '' },
  { label: '활성',   value: 'ACTIVATE' },
  { label: '정지',   value: 'DEACTIVATE' },
  { label: '탈퇴',   value: 'DELETE' },
]

const roleFilters = [
  { label: '전체',    value: '' },
  { label: 'USER',    value: 'USER' },
  { label: 'MANAGER', value: 'MANAGER' },
  { label: 'ADMIN',   value: 'ADMIN' },
]

// 등급 필터는 클라이언트 사이드 (서버가 status+keyword만 지원)
const filteredMembers = computed(() => {
  if (!roleFilter.value) return members.value
  return members.value.filter(m => m.memberRole === roleFilter.value)
})

const allChecked = computed(() =>
  filteredMembers.value.length > 0 &&
  filteredMembers.value.every(m => checkedIds.value.has(m.id))
)

function toggleAll() {
  if (allChecked.value) {
    filteredMembers.value.forEach(m => checkedIds.value.delete(m.id))
  } else {
    filteredMembers.value.forEach(m => checkedIds.value.add(m.id))
  }
  checkedIds.value = new Set(checkedIds.value)
}

function toggleCheck(id) {
  const s = new Set(checkedIds.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  checkedIds.value = s
}

function setStatusFilter(val) {
  statusFilter.value = val
  page.value = 0
  fetchMembers()
}

async function fetchMembers() {
  loading.value = true
  checkedIds.value = new Set()
  try {
    const { data } = await adminApi.getMembers({
      page: page.value,
      size: 20,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
    })
    members.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('회원 목록 로드 실패:', e)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 0
  fetchMembers()
}

async function handleStatusChange(id, status) {
  try {
    await adminApi.updateMemberStatus(id, status)
    const m = members.value.find(m => m.id === id)
    if (m) m.memberStatus = status
  } catch (e) {
    alert(e.response?.data?.message || '상태 변경 실패')
    fetchMembers()
  }
}

async function bulkChangeStatus(status) {
  const ids = [...checkedIds.value]
  if (!ids.length) return
  const label = { ACTIVATE: '활성화', DEACTIVATE: '정지', DELETE: '탈퇴 처리' }[status]
  if (!confirm(`선택한 ${ids.length}명을 "${label}" 처리하시겠습니까?`)) return

  const results = await Promise.allSettled(
    ids.map(id => adminApi.updateMemberStatus(id, status))
  )
  const failed = results.filter(r => r.status === 'rejected').length
  if (failed > 0) alert(`${failed}건 처리 실패`)

  checkedIds.value = new Set()
  fetchMembers()
}

// helpers
const statusLabel = (s) => ({ ACTIVATE: '활성', DEACTIVATE: '정지', DELETE: '탈퇴' }[s] ?? s)
const statusClass = (s) => ({
  ACTIVATE:   'bg-emerald-50 text-emerald-700',
  DEACTIVATE: 'bg-amber-50 text-amber-700',
  DELETE:     'bg-rose-50 text-rose-700',
}[s] ?? 'bg-slate-100 text-slate-600')

const roleClass = (r) => ({
  ADMIN:   'bg-purple-50 text-purple-700',
  MANAGER: 'bg-blue-50 text-blue-700',
  USER:    'bg-slate-100 text-slate-600',
}[r] ?? 'bg-slate-100 text-slate-600')

function formatDate(dt) {
  if (!dt) return '-'
  return new Date(dt).toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(fetchMembers)
</script>
