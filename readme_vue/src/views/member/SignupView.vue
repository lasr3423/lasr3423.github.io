<template>
  <section class="w-full max-w-2xl rounded-[2rem] border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60">
    <div class="mb-8 space-y-2">
      <p class="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">Join</p>
      <h1 class="text-3xl font-bold tracking-tight text-slate-900">ReadMe 회원가입</h1>
      <p class="text-sm leading-6 text-slate-500">
        기본 정보를 입력해 회원가입을 진행해 주세요.
      </p>
    </div>

    <form class="space-y-6" @submit.prevent="handleSignup">
      <div class="grid gap-4 md:grid-cols-2">
        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">이메일</span>
          <input
            v-model="form.email"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            type="email"
            placeholder="example@readme.test"
            required
          >
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">이름</span>
          <input
            v-model="form.name"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            type="text"
            placeholder="이름을 입력하세요"
            required
          >
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">비밀번호</span>
          <input
            v-model="form.password"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            type="password"
            placeholder="8자 이상 입력하세요"
            required
          >
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">비밀번호 확인</span>
          <input
            v-model="pwconfirm"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            type="password"
            placeholder="비밀번호를 다시 입력하세요"
            required
          >
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">휴대전화</span>
          <input
            v-model="form.phone"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            type="tel"
            placeholder="01012345678"
            required
          >
        </label>

        <label class="block space-y-2 md:col-span-2">
          <span class="text-sm font-medium text-slate-700">주소</span>
          <input
            v-model="form.address"
            class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
            type="text"
            placeholder="도로명 주소와 상세 정보를 입력하세요"
            required
          >
        </label>
      </div>

      <div class="rounded-3xl border border-slate-200 bg-slate-50 p-5">
        <div class="space-y-3 text-sm text-slate-600">
          <label class="flex items-center gap-3">
            <input v-model="agreeAll" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span class="font-semibold text-slate-800">전체 동의</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="agreeTerms" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span>이용약관 동의 (필수)</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="agreePrivacy" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span>개인정보 처리방침 동의 (필수)</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="form.marketingAgreed" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span>마케팅 정보 수신 동의 (선택)</span>
          </label>
        </div>
      </div>

      <p v-if="errorMsg" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
        {{ errorMsg }}
      </p>

      <div class="flex flex-col gap-3 sm:flex-row sm:justify-end">
        <router-link
          class="inline-flex items-center justify-center rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-100"
          to="/"
        >
          취소
        </router-link>
        <button
          class="inline-flex items-center justify-center rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
          type="submit"
          :disabled="loading"
        >
          {{ loading ? '회원가입 처리 중...' : '회원가입' }}
        </button>
      </div>
    </form>

    <p class="mt-6 text-center text-sm text-slate-500">
      이미 계정이 있으신가요?
      <router-link class="font-semibold text-brand-700 hover:text-accent-600" to="/signin">
        로그인
      </router-link>
    </p>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '@/api/auth';

const router = useRouter();
const pwconfirm = ref('');
const loading = ref(false);
const errorMsg = ref('');
const agreeTerms = ref(false);
const agreePrivacy = ref(false);

const form = reactive({
  email: '',
  password: '',
  name: '',
  phone: '',
  address: '',
  marketingAgreed: false,
});

const PHONE_REGEX = /^01[016789][0-9]{7,8}$/;

async function handleSignup() {
  errorMsg.value = '';

  if (form.password.length < 8) {
    errorMsg.value = '비밀번호는 8자 이상이어야 합니다.';
    return;
  }
  if (form.password !== pwconfirm.value) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.';
    return;
  }
  if (!PHONE_REGEX.test(form.phone)) {
    errorMsg.value = '휴대전화 번호 형식은 01012345678 형태로 입력해주세요.';
    return;
  }
  if (!agreeTerms.value || !agreePrivacy.value) {
    errorMsg.value = '필수 약관에 모두 동의해주세요.';
    return;
  }

  try {
    loading.value = true;
    await authApi.signup(form);
    router.push('/signin');
  } catch (e) {
    errorMsg.value =
      e.response?.data?.message ||
      e.response?.data ||
      '회원가입에 실패했습니다.';
  } finally {
    loading.value = false;
  }
}

const agreeAll = computed({
  get() {
    return agreeTerms.value && agreePrivacy.value && form.marketingAgreed;
  },
  set(value) {
    agreeTerms.value = value;
    agreePrivacy.value = value;
    form.marketingAgreed = value;
  },
});
</script>
