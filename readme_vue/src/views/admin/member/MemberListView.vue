<template>
  <div class="space-y-6">
    <section class="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-sm">
      <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin</p>
      <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">회원 관리</h1>
    </section>

    <!-- 검색 / 필터 -->
    <section class="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex flex-wrap gap-3">
        <input v-model="keyword" type="text" placeholder="이름 또는 이메일 검색"
          class="flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
          @keyup.enter="search" />
        <select v-model="statusFilter"
          class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm outline-none focus:border-brand-400">
          <option value="">전체 상태</option>
          <option value="ACTIVATE">활성</option>
          <option value="DEACTIVATE">정지</option>
          <option value="DELETE">탈퇴</option>
        </select>
        <button @click="search"
          class="rounded-2xl bg-brand-800 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-brand-700">
          검색
        </button>
      </div>
    </section>

    <!-- 테이블 -->
    <section class="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
      <div v-if="loading" class="flex items-center justify-center py-16 text-sm text-slate-400">불러오는 중...</div>

      <template v-else-if="members.length > 0">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="border-b border-slate-200 bg-slate-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">ID</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">이름</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">이메일</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">상태</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">등급</th>
                <th class="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">가입방식</th>
                <th class="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">관리</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="m in members" :key="m.id" class="transition hover:bg-slate-50">
                <td class="px-6 py-4 text-xs text-slate-400">{{ m.id }}</td>
                <td class="px-6 py-4 font-semibold text-slate-900">{{ m.name }}</td>
                <td class="px-6 py-4 text-slate-600">{{ m.email }}</td>
                <td class="px-6 py-4">
                  <span :class="statusClass(m.memberStatus)" class="rounded-full px-3 py-1 text-xs font-semibold">
                    {{ statusLabel(m.memberStatus) }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <select :value="m.memberRole"
                    class="rounded-xl border border-slate-200 bg-white px-2 py-1 text-xs"
                    @change="handleRoleChange(m.id, $event.target.value)">
                    <option value="USER">USER</option>
                    <option value="MANAGER">MANAGER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td class="px-6 py-4 text-xs text-slate-500">{{ m.provider }}</td>
                <td class="px-6 py-4 text-right">
                  <select :value="m.memberStatus"
                    class="rounded-xl border border-slate-200 bg-white px-2 py-1 text-xs"
                    @change="handleStatusChange(m.id, $event.target.value)">
                    <option value="ACTIVATE">활성화</option>
                    <option value="DEACTIVATE">정지</option>
                    <option value="DELETE">탈퇴처리</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex items-center justify-center gap-2 border-t border-slate-100 p-4">
          <button :disabled="page === 0" @click="page--; fetchMembers()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm text-slate-600 transition hover:bg-slate-50 disabled:opacity-40">이전</button>
          <span class="text-sm text-slate-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="page++; fetchMembers()"
            class="rounded-xl border border-slate-200 px-3 py-1.5 text-sm text-slate-600 transition hover:bg-slate-50 disabled:opacity-40">다음</button>
        </div>
      </template>

      <div v-else class="flex items-center justify-center py-16 text-sm text-slate-400">회원이 없습니다.</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { adminApi } from '@/api/admin';

const members = ref([]);
const loading = ref(true);
const keyword = ref('');
const statusFilter = ref('');
const page = ref(0);
const totalPages = ref(1);

const statusLabel = (s) => ({ ACTIVATE: '활성', DEACTIVATE: '정지', DELETE: '탈퇴' }[s] ?? s);
const statusClass = (s) => ({
  ACTIVATE:   'bg-green-50 text-green-700',
  DEACTIVATE: 'bg-yellow-50 text-yellow-700',
  DELETE:     'bg-rose-50 text-rose-700',
}[s] ?? 'bg-slate-100 text-slate-600');

async function fetchMembers() {
  try {
    loading.value = true;
    const { data } = await adminApi.getMembers({ page: page.value, size: 20, keyword: keyword.value || undefined, status: statusFilter.value || undefined });
    members.value = data.content;
    totalPages.value = data.totalPages || 1;
  } catch (e) {
    console.error('회원 목록 로드 실패:', e);
  } finally {
    loading.value = false;
  }
}

function search() { page.value = 0; fetchMembers(); }

async function handleStatusChange(id, status) {
  try {
    await adminApi.updateMemberStatus(id, status);
    fetchMembers();
  } catch (e) {
    alert(e.response?.data?.message || '상태 변경 실패');
  }
}

async function handleRoleChange(id, role) {
  try {
    await adminApi.updateMemberRole(id, role);
    fetchMembers();
  } catch (e) {
    alert(e.response?.data?.message || '등급 변경 실패');
  }
}

onMounted(fetchMembers);
</script>
