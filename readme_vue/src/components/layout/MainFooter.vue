<template>
  <footer class="mt-12 border-t border-slate-200 bg-white/80">
    <div class="mx-auto grid w-full max-w-7xl gap-10 px-4 py-10 lg:grid-cols-[1.4fr_2fr] lg:px-6">
      <div class="space-y-4">
        <div>
          <p class="text-lg font-extrabold tracking-tight text-brand-800">ReadMe</p>
          <p class="mt-2 max-w-sm text-sm leading-6 text-slate-500">
            A modern bookstore experience for readers who want discovery, clean checkout, and a calm dashboard.
          </p>
        </div>

        <div class="flex flex-wrap gap-2">
          <a class="rounded-full border border-slate-200 px-4 py-2 text-sm text-slate-600 transition hover:border-brand-200 hover:bg-brand-50 hover:text-brand-800" href="#">
            App Store
          </a>
          <a class="rounded-full border border-slate-200 px-4 py-2 text-sm text-slate-600 transition hover:border-brand-200 hover:bg-brand-50 hover:text-brand-800" href="#">
            Google Play
          </a>
        </div>
      </div>

      <div class="grid gap-6 sm:grid-cols-2 xl:grid-cols-4">
        <div v-for="group in linkGroups" :key="group.title" class="space-y-3">
          <p class="text-sm font-semibold uppercase tracking-[0.16em] text-brand-800">{{ group.title }}</p>
          <div class="space-y-2">
            <a
              v-for="link in group.links"
              :key="link"
              href="#"
              class="block text-sm text-slate-500 transition hover:text-accent-600"
            >
              {{ link }}
            </a>
          </div>
        </div>
      </div>
    </div>

    <div class="border-t border-slate-200">
      <div class="mx-auto flex w-full max-w-7xl flex-col gap-2 px-4 py-5 text-xs leading-5 text-slate-400 lg:px-6">
        <p>ReadMe Corp. | Business ID 123-45-67890 | Seoul, Republic of Korea</p>
        <p>Customer Center 1588-0000 | help@readme.co.kr</p>
        <p>© 2026 ReadMe Corp. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
const linkGroups = [
  {
    title: 'Support',
    links: ['Notice', 'FAQ', '1:1 Inquiry', 'Returns', 'Delivery'],
  },
  {
    title: 'Company',
    links: ['About', 'Careers', 'Press', 'Partnerships'],
  },
  {
    title: 'Service',
    links: ['Premium', 'eBook', 'Audio Book', 'Gift Cards'],
  },
  {
    title: 'Policy',
    links: ['Privacy', 'Terms', 'Youth Safety', 'Cookies'],
  },
];
</script>
