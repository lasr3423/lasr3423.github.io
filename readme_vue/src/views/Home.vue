<template>
  <section class="space-y-8">
    <div class="overflow-hidden rounded-[2rem] bg-brand-800 px-8 py-10 text-white shadow-xl shadow-brand-900/20">
      <div class="max-w-3xl space-y-4">
        <p class="text-sm font-semibold uppercase tracking-[0.24em] text-brand-100">ReadMe storefront</p>
        <h1 class="text-4xl font-bold tracking-tight md:text-5xl">A cleaner reading commerce experience for this project.</h1>
        <p class="max-w-2xl text-base leading-7 text-brand-100">
          Tailwind is now wired into the frontend, and the main layouts and key storefront screens are ready to extend from the existing deliverable designs.
        </p>
        <div class="flex flex-wrap gap-3">
          <router-link class="rounded-full bg-white px-5 py-3 text-sm font-semibold text-brand-800 transition hover:bg-brand-50" to="/product">
            Browse books
          </router-link>
          <router-link
            v-if="!authStore.isLoggedIn"
            class="rounded-full border border-white/30 px-5 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
            to="/signin"
          >
            Sign in
          </router-link>
          <router-link
            v-else-if="authStore.isAdmin"
            class="rounded-full border border-white/30 px-5 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
            to="/admin"
          >
            Admin dashboard
          </router-link>
          <router-link
            v-else
            class="rounded-full border border-white/30 px-5 py-3 text-sm font-semibold text-white transition hover:bg-white/10"
            to="/mypage"
          >
            My page
          </router-link>
        </div>
      </div>
    </div>

    <div class="grid gap-6 md:grid-cols-3">
      <article class="rounded-[1.75rem] border border-slate-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Member flow</p>
        <h2 class="mt-3 text-xl font-bold text-slate-900">JWT and OAuth already connected</h2>
        <p class="mt-3 text-sm leading-6 text-slate-500">
          Sign in, sign up, token refresh, and Kakao or Google entry points are ready to build on.
        </p>
      </article>

      <article class="rounded-[1.75rem] border border-slate-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Commerce flow</p>
        <h2 class="mt-3 text-xl font-bold text-slate-900">Product and cart screens upgraded</h2>
        <p class="mt-3 text-sm leading-6 text-slate-500">
          Product list, detail, and cart now use Tailwind utility styling and can be expanded from the deliverable wireframes.
        </p>
      </article>

      <article class="rounded-[1.75rem] border border-slate-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-semibold uppercase tracking-[0.2em] text-brand-700">Admin shell</p>
        <h2 class="mt-3 text-xl font-bold text-slate-900">Dashboard layout prepared</h2>
        <p class="mt-3 text-sm leading-6 text-slate-500">
          The admin header and sidebar are now styled with a reusable Tailwind shell for the remaining pages.
        </p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
</script>
