<template>
  <div class="book-gallery">
    <div class="book-cover-shell">
      <button
        v-show="hasMultiple"
        type="button"
        class="gallery-nav gallery-nav-prev"
        aria-label="上一张"
        @click.stop="shift(-1)"
      >
        ‹
      </button>
      <button type="button" class="book-cover-btn" title="点击查看完整图片" @click="openPreview()">
        <img class="book-cover" :src="currentUrl" alt="" />
        <span v-if="hasMultiple" class="cover-index">{{ currentIndex + 1 }} / {{ images.length }}</span>
        <span class="book-cover-tip">点击查看完整图片</span>
      </button>
      <button
        v-show="hasMultiple"
        type="button"
        class="gallery-nav gallery-nav-next"
        aria-label="下一张"
        @click.stop="shift(1)"
      >
        ›
      </button>
    </div>

    <div v-if="hasMultiple" class="thumbs">
      <img
        v-for="(u, i) in images"
        :key="u + '-' + i"
        :src="resolveUrl(u)"
        :class="{ active: currentRaw === u }"
        alt=""
        @click="currentRaw = u"
        @dblclick="openPreview(u)"
      />
    </div>

    <Teleport to="body">
      <div
        v-if="previewVisible"
        class="image-preview-mask"
        role="dialog"
        aria-modal="true"
        aria-label="查看图片"
        @click="closePreview"
      >
        <button type="button" class="image-preview-close" aria-label="关闭" @click.stop="closePreview">×</button>
        <button
          v-show="hasMultiple"
          type="button"
          class="image-nav image-nav-prev"
          aria-label="上一张"
          @click.stop="shift(-1)"
        >
          ‹
        </button>
        <div class="image-preview-wrap" @click.stop>
          <img :key="previewDisplayUrl" :src="previewDisplayUrl" alt="" />
          <span v-if="hasMultiple" class="image-counter">{{ currentIndex + 1 }} / {{ images.length }}</span>
        </div>
        <button
          v-show="hasMultiple"
          type="button"
          class="image-nav image-nav-next"
          aria-label="下一张"
          @click.stop="shift(1)"
        >
          ›
        </button>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { fileUrl } from '@/api/http'

const props = defineProps<{
  coverUrl?: string | null
  imageUrls?: string[] | null
}>()

const currentRaw = ref('')
const previewVisible = ref(false)

const images = computed(() => {
  const seen = new Set<string>()
  const list: string[] = []
  const add = (url?: string | null) => {
    const value = String(url || '').trim()
    if (!value || seen.has(value)) return
    seen.add(value)
    list.push(value)
  }
  add(props.coverUrl)
  if (Array.isArray(props.imageUrls)) {
    props.imageUrls.forEach(add)
  }
  return list
})

const hasMultiple = computed(() => images.value.length > 1)

const resolveUrl = (url?: string) => fileUrl(url)

const currentIndex = computed(() => {
  const idx = images.value.indexOf(currentRaw.value)
  return idx >= 0 ? idx : 0
})

const currentUrl = computed(() => {
  const raw = currentRaw.value || images.value[0]
  return resolveUrl(raw) || 'https://placehold.co/600x400?text=Book'
})

const previewDisplayUrl = computed(() => currentUrl.value)

watch(
  images,
  (list) => {
    if (!list.length) {
      currentRaw.value = ''
      return
    }
    if (!list.includes(currentRaw.value)) {
      currentRaw.value = list[0]
    }
  },
  { immediate: true }
)

const shift = (delta: number) => {
  if (!hasMultiple.value) return
  const next = (currentIndex.value + delta + images.value.length) % images.value.length
  currentRaw.value = images.value[next]
}

const openPreview = (url?: string) => {
  if (url) currentRaw.value = url
  if (!currentRaw.value && !images.value[0]) return
  previewVisible.value = true
}

const closePreview = () => {
  previewVisible.value = false
}

const onPreviewKey = (e: KeyboardEvent) => {
  if (!previewVisible.value) return
  if (e.key === 'ArrowLeft') {
    e.preventDefault()
    shift(-1)
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    shift(1)
  } else if (e.key === 'Escape') {
    closePreview()
  }
}

watch(previewVisible, (visible) => {
  if (visible) {
    window.addEventListener('keydown', onPreviewKey)
  } else {
    window.removeEventListener('keydown', onPreviewKey)
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', onPreviewKey)
})
</script>

<style scoped>
.book-cover-shell {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.gallery-nav {
  position: absolute;
  top: 50%;
  z-index: 5;
  width: 36px;
  height: 36px;
  margin-top: -18px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.52);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.28);
  transition: background 0.2s ease, transform 0.2s ease;
}

.gallery-nav:hover {
  background: rgba(0, 0, 0, 0.72);
  transform: scale(1.05);
}

.gallery-nav-prev {
  left: 10px;
}

.gallery-nav-next {
  right: 10px;
}

.book-cover-btn {
  position: relative;
  display: block;
  flex: 1;
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  cursor: zoom-in;
  border-radius: var(--radius);
  overflow: hidden;
}

.book-cover-btn:hover .book-cover-tip {
  opacity: 1;
}

.book-cover {
  width: 100%;
  min-height: 240px;
  object-fit: cover;
  display: block;
  border-radius: var(--radius);
  background: var(--mist);
  border: 1px solid var(--line);
}

.cover-index {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 3;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.52);
  color: #fff;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
  pointer-events: none;
}

.book-cover-tip {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 3;
  padding: 8px 12px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.55));
  color: #fff;
  font-size: 12px;
  text-align: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
}

.thumbs {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.thumbs img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  box-sizing: border-box;
}

.thumbs img.active {
  border-color: var(--teal);
}

.image-preview-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 72px;
  background: rgba(20, 35, 28, 0.82);
}

.image-preview-close {
  position: absolute;
  top: 18px;
  right: 22px;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.image-preview-close:hover {
  background: rgba(255, 255, 255, 0.28);
}

.image-nav {
  position: fixed;
  top: 50%;
  z-index: 3001;
  width: 44px;
  height: 44px;
  margin-top: -22px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.52);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.28);
  transition: background 0.2s ease, transform 0.2s ease;
}

.image-nav:hover {
  background: rgba(0, 0, 0, 0.72);
  transform: scale(1.05);
}

.image-nav-prev {
  left: 24px;
}

.image-nav-next {
  right: 24px;
}

.image-preview-wrap {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-width: min(920px, 100%);
  max-height: 86vh;
}

.image-preview-wrap img {
  max-width: min(920px, 100%);
  max-height: 82vh;
  object-fit: contain;
  display: block;
  background: #fff;
  border-radius: 8px;
}

.image-counter {
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
</style>
