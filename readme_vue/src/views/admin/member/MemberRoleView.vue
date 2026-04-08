<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">회원 권한 관리</h1>
      <p class="mt-1 text-sm text-slate-400">회원의 역할(USER / MANAGER / ADMIN)을 변경합니다.</p>
    </section>

    <!-- 검색 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex gap-3">
        <input v-model="keyword" type="text" placeholder="이름 또는 이메일 검색"
          class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
          @keyup.enter="search" />
        <button @click="search"
          class="rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700">
          검색
        </button>
      </div>
    </section>

    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="members.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">이름</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">이메일</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">현재 권한</th>
                <th class="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">권한 변경</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="m in members" :key="m.id" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 font-semibold text-slate-900">{{ m.name }}</td>
                <td class="px-6 py-4 text-slate-500">{{ m.email }}</td>
                <td class="px-6 py-4 text-center">
                  <span :class="roleClass(m.memberRole)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ m.memberRole }}
                  </span>
                </td>
                <td class="px-6 py-4 text-center">
                  <select :value="m.memberRole"
                    class="rounded-xl border border-slate-200 bg-white px-3 py-1.5 text-xs outline-none focus:border-brand-400"
                    @change="handleRoleChange(m.id, $event.target.value)">
                    <option value="USER">USER</option>
                    <option value="MANAGER">MANAGER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchMembers()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchMembers()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">회원이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const members    = ref([])
const loading    = ref(true)
const keyword    = ref('')
const page       = ref(0)
const totalPages = ref(1)

const roleClass = (r) => ({
  ADMIN:   'bg-rose-50 text-rose-700',
  MANAGER: 'bg-violet-50 text-violet-700',
  USER:    'bg-slate-100 text-slate-600',
}[r] ?? 'bg-slate-100 text-slate-600')

async function fetchMembers() {
  loading.value = true
  try {
    const { data } = await adminApi.getMembers({ page: page.value, size: 20, keyword: keyword.value || undefined })
    members.value = data.content ?? []
    totalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('회원 목록 로드 실패', e)
  } finally {
    loading.value = false
  }
}

function search() { page.value = 0; fetchMembers() }

async function handleRoleChange(id, role) {
  try {
    await adminApi.updateMemberRole(id, role)
    const m = members.value.find(m => m.id === id)
    if (m) m.memberRole = role
  } catch (e) {
    alert(e.response?.data?.message || '권한 변경 실패')
  }
}

onMounted(fetchMembers)
</script>
