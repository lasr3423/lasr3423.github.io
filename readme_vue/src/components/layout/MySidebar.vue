<template>
  <aside class="hidden w-72 shrink-0 lg:block">
    <div class="sticky top-24 overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
      <div class="border-b border-slate-200 bg-brand-800 px-5 py-4 text-sm font-semibold uppercase tracking-[0.2em] text-white">
        Browse
      </div>

      <div class="p-3">
        <section v-for="section in sections" :key="section.key" class="rounded-2xl">
          <button
            class="flex w-full items-center justify-between rounded-2xl px-3 py-3 text-left text-sm font-semibold text-slate-800 transition hover:bg-slate-100"
            @click="toggle(section.key)"
          >
            <span>{{ section.title }}</span>
            <span class="text-slate-400">{{ open[section.key] ? '-' : '+' }}</span>
          </button>

          <div v-show="open[section.key]" class="space-y-1 px-3 pb-3">
            <router-link
              v-for="item in section.items"
              :key="item.to"
              :to="item.to"
              class="block rounded-xl px-3 py-2 text-sm text-slate-500 transition hover:bg-brand-50 hover:text-brand-800"
              active-class="bg-brand-50 font-semibold text-brand-800"
            >
              {{ item.label }}
            </router-link>
          </div>
        </section>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { reactive } from 'vue';

const open = reactive({
  order: true,
  board: false,
  myinfo: false,
});

const sections = [
  {
    key: 'order',
    title: 'Orders',
    items: [
      { label: 'Order history', to: '/mypage/order' },
      { label: 'Cart', to: '/cart' },
      { label: 'Payment history', to: '/mypage/payment' },
    ],
  },
  {
    key: 'board',
    title: 'Community',
    items: [
      { label: 'Notice', to: '/notice' },
      { label: 'QnA', to: '/qna' },
      { label: 'Review', to: '/review' },
    ],
  },
  {
    key: 'myinfo',
    title: 'Account',
    items: [
      { label: 'Edit profile', to: '/mypage/edit' },
      { label: 'Change password', to: '/mypage/password' },
      { label: 'Withdraw', to: '/mypage/withdraw' },
    ],
  },
];

function toggle(key) {
  open[key] = !open[key];
}
</script>
