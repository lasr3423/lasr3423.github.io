<template>
  <section class="w-full max-w-2xl rounded-[2rem] border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60">
    <div class="mb-8 space-y-2">
      <p class="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">Create account</p>
      <h1 class="text-3xl font-bold tracking-tight text-slate-900">Join ReadMe</h1>
      <p class="text-sm text-slate-500">Set up your member profile to start shopping.</p>
    </div>

    <form class="space-y-6" @submit.prevent="handleSignup">
      <div class="grid gap-4 md:grid-cols-2">
        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">Email</span>
          <input v-model="form.email" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" type="email" placeholder="you@example.com" required>
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">Name</span>
          <input v-model="form.name" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" type="text" placeholder="Your name" required>
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">Password</span>
          <input v-model="form.password" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" type="password" placeholder="At least 8 characters" required>
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">Confirm password</span>
          <input v-model="pwconfirm" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" type="password" placeholder="Repeat your password" required>
        </label>

        <label class="block space-y-2">
          <span class="text-sm font-medium text-slate-700">Phone</span>
          <input v-model="form.phone" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" type="tel" placeholder="01012345678" required>
        </label>

        <label class="block space-y-2 md:col-span-2">
          <span class="text-sm font-medium text-slate-700">Address</span>
          <input v-model="form.address" class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100" type="text" placeholder="Street, city, and postal code" required>
        </label>
      </div>

      <div class="rounded-3xl border border-slate-200 bg-slate-50 p-5">
        <div class="space-y-3 text-sm text-slate-600">
          <label class="flex items-center gap-3">
            <input v-model="agreeAll" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span class="font-semibold text-slate-800">Agree to all</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="agreeTerms" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span>Required terms of service</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="agreePrivacy" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span>Required privacy policy</span>
          </label>
          <label class="flex items-center gap-3">
            <input v-model="form.marketingAgreed" class="h-4 w-4 rounded border-slate-300 text-brand-700 focus:ring-brand-400" type="checkbox">
            <span>Optional marketing consent</span>
          </label>
        </div>
      </div>

      <p v-if="errorMsg" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
        {{ errorMsg }}
      </p>

      <div class="flex flex-col gap-3 sm:flex-row sm:justify-end">
        <router-link class="inline-flex items-center justify-center rounded-2xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-600 transition hover:bg-slate-100" to="/">
          Cancel
        </router-link>
        <button class="inline-flex items-center justify-center rounded-2xl bg-brand-800 px-5 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400" type="submit" :disabled="loading">
          {{ loading ? 'Creating account...' : 'Create account' }}
        </button>
      </div>
    </form>

    <p class="mt-6 text-center text-sm text-slate-500">
      Already have an account?
      <router-link class="font-semibold text-brand-700 hover:text-accent-600" to="/signin">
        Sign in
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
    errorMsg.value = 'Password must be at least 8 characters.';
    return;
  }
  if (form.password !== pwconfirm.value) {
    errorMsg.value = 'Passwords do not match.';
    return;
  }
  if (!PHONE_REGEX.test(form.phone)) {
    errorMsg.value = 'Use a valid phone number format like 01012345678.';
    return;
  }
  if (!agreeTerms.value || !agreePrivacy.value) {
    errorMsg.value = 'Required agreements must be accepted.';
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
      'Sign up failed.';
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
