<template>
  <router-link class="card-book" :to="'/book/' + book.id">
    <div class="card-cover">
      <img class="card-cover-img" :src="cover" alt="" />
    </div>
    <div class="card-body">
      <h3 class="card-title">
        <span v-if="book.campus" class="card-tag">{{ book.campus }}</span>
        {{ book.title }}
      </h3>
      <span v-if="conditionText" class="card-badge">{{ conditionText }}</span>
      <div class="card-price"><small>¥</small>{{ formatPrice(book.price) }}</div>
      <div class="card-seller">
        <div class="card-seller-left">
          <span class="card-seller-avatar">
            <img
              v-if="avatarSrc && !avatarFailed"
              :src="avatarSrc"
              alt=""
              loading="lazy"
              @error="avatarFailed = true"
            />
            <span v-else>{{ sellerLetter }}</span>
          </span>
          <span class="card-seller-name">{{ book.sellerNickname || '同学' }}</span>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { fileUrl } from '@/api/http'

const props = defineProps<{
  book: any
  conditionLabel?: (code?: string) => string
}>()

const avatarFailed = ref(false)

const cover = computed(
  () => fileUrl(props.book.coverUrl) || 'https://placehold.co/400x400?text=Book'
)
const avatarSrc = computed(() => fileUrl(props.book.sellerAvatar))
const sellerLetter = computed(() => {
  const name = props.book.sellerNickname || '同学'
  return String(name).trim().charAt(0).toUpperCase() || '同'
})
const conditionText = computed(() => props.conditionLabel?.(props.book.conditionCode) || '')

const formatPrice = (price: number | string | undefined) => {
  if (price == null || price === '') return '0'
  const num = Number(price)
  if (Number.isNaN(num)) return String(price)
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}
</script>
