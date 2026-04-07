<template>
  <aside class="hidden w-72 shrink-0 lg:block">
    <div class="sticky top-24 overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
      <div class="border-b border-slate-200 bg-slate-950 px-5 py-4 text-sm font-semibold uppercase tracking-[0.2em] text-white">
        Admin Menu
      </div>

      <div class="space-y-2 p-3">
        <router-link
          to="/admin"
          class="block rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-100"
          active-class="bg-brand-50 text-brand-800"
        >
          Dashboard
        </router-link>

        <section v-for="section in sections" :key="section.key">
          <button
            class="flex w-full items-center justify-between rounded-2xl px-4 py-3 text-left text-sm font-semibold text-slate-800 transition hover:bg-slate-100"
            @click="toggle(section.key)"
          >
            <span>{{ section.title }}</span>
            <span class="text-slate-400">{{ open[section.key] ? '-' : '+' }}</span>
          </button>

          <div v-show="open[section.key]" class="space-y-1 px-4 pb-3">
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
  adminOrder: true,
  adminProduct: false,
  adminBoard: false,
  adminMember: false,
});

const sections = [
  {
    key: 'adminOrder',
    title: 'Orders',
    items: [
      { label: 'Order list', to: '/admin/order/list' },
      { label: 'Delivery list', to: '/admin/delivery/list' },
      { label: 'Category list', to: '/admin/category/list' },
      { label: 'Payment list', to: '/admin/payment/list' },
    ],
  },
  {
    key: 'adminProduct',
    title: 'Products',
    items: [
      { label: 'Product list', to: '/admin/product/list' },
      { label: 'Create product', to: '/admin/product/insert' },
    ],
  },
  {
    key: 'adminBoard',
    title: 'Boards',
    items: [
      { label: 'Notice', to: '/admin/notice/list' },
      { label: 'QnA', to: '/admin/qna/list' },
      { label: 'Review', to: '/admin/review/list' },
    ],
  },
  {
    key: 'adminMember',
    title: 'Members',
    items: [
      { label: 'Member list', to: '/admin/member/list' },
      { label: 'Role control', to: '/admin/member/role' },
    ],
  },
];

function toggle(key) {
  open[key] = !open[key];
}
</script>
