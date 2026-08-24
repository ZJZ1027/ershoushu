<template>
  <div class="hp">
    <!-- 问候区：把「你是谁、上次什么时候来的」说清楚，比放一句口号有用 -->
    <section class="hp-hero hero-greet">
      <div class="hp-hero-main">
        <h1 class="hero-title">{{ greeting }}，{{ profile?.nickname || '管理员' }}</h1>
        <p class="hp-hero-sub">
          <template v-if="profile">
            <span v-if="profile.dept?.name">{{ profile.dept.name }}</span>
            <span v-if="roleNames">{{ roleNames }}</span>
            <span v-if="profile.loginDate">
              上次登录 {{ formatDate(profile.loginDate) }}
              <template v-if="profile.loginIp">· {{ profile.loginIp }}</template>
            </span>
          </template>
          <span v-else>正在加载账号信息…</span>
        </p>
      </div>
      <div class="hp-hero-side">
        <a-button @click="push('/user/profile')">
          <template #icon><icon-user /></template>
          个人中心
        </a-button>
      </div>
    </section>

    <!-- KPI：只放当前登录人有权限看的几项，无权限的直接不渲染而不是显示「—」 -->
    <section v-if="kpis.length" class="hp-kpis">
      <StatCard
        v-for="k in kpis"
        :key="k.label"
        :label="k.label"
        :value="k.value"
        :icon="k.icon"
        :tone="k.tone"
        :hint="k.hint"
        :loading="loading"
        clickable
        @click="push(k.path)"
      />
    </section>

    <a-row :gutter="16">
      <a-col :xs="24" :lg="14">
        <!-- 快捷入口：新装环境最常走的几步，省得在菜单里翻 -->
        <a-card class="hp-card" title="快捷入口" :bordered="false">
          <div v-if="shortcuts.length" class="hp-links">
            <button
              v-for="s in shortcuts"
              :key="s.path"
              type="button"
              class="hp-link"
              @click="go(s.path)"
            >
              <span class="hp-link-icon"><component :is="s.icon" /></span>
              <span class="hp-link-body">
                <span class="hp-link-title">{{ s.title }}</span>
                <span class="hp-link-desc">{{ s.desc }}</span>
              </span>
            </button>
          </div>
          <EmptyState
            v-else-if="!loading"
            :icon="IconMenu"
            size="sm"
            title="暂无可用入口"
            description="当前账号没有菜单权限。请用 admin 登录，或在「角色管理」里给角色分配菜单"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card v-if="canViewNotice" class="hp-card" title="通知公告" :bordered="false">
          <template #extra>
            <a-link @click="push('/system/notice')">全部</a-link>
          </template>
          <a-list v-if="notices.length" :bordered="false" size="small">
            <a-list-item v-for="n in notices" :key="n.id">
              <a-list-item-meta :title="n.title">
                <template #description>
                  <div class="hp-notice-time">{{ formatDate(n.createTime) }}</div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
          <EmptyState
            v-else-if="!loading"
            :icon="IconMessage"
            size="sm"
            title="暂无公告"
            description="在「系统管理 - 通知公告」中发布后会出现在这里"
          />
        </a-card>

        <a-card v-if="canViewLoginLog" class="hp-card" title="最近登录" :bordered="false">
          <template #extra>
            <a-link @click="push('/system/log/login-log')">全部</a-link>
          </template>
          <a-list v-if="loginLogs.length" :bordered="false" size="small">
            <a-list-item v-for="l in loginLogs" :key="l.id">
              <a-list-item-meta :title="l.username">
                <template #description>
                  <div class="hp-notice-time">
                    {{ formatDate(l.createTime) }}
                    <template v-if="l.userIp">· {{ l.userIp }}</template>
                  </div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
          <EmptyState v-else-if="!loading" :icon="IconMessage" size="sm" title="暂无登录记录" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  IconBook,
  IconIdcard,
  IconFolder,
  IconMenu,
  IconMessage,
  IconMindMapping,
  IconSettings,
  IconUser,
  IconUserGroup
} from '@arco-design/web-vue/es/icon'
import { checkPermi, checkRole } from '@/utils/permission'
import { formatDate } from '@/utils/formatTime'
import { getUserProfile, type ProfileVO } from '@/api/system/user/profile'
import { getUserPage } from '@/api/system/user'
import { getRolePage } from '@/api/system/role'
import { getPostPage } from '@/api/system/post'
import { getSimpleDeptList } from '@/api/system/dept'
import { getNoticePage } from '@/api/system/notice'
import { getLoginLogPage } from '@/api/system/loginlog'
import { useUserStore } from '@/store/modules/user'
import { Message } from '@arco-design/web-vue'

defineOptions({ name: 'Index' })

const { push } = useRouter()
const userStore = useUserStore()

/** 超管或具备对应按钮权限时展示；避免权限未就绪时入口被滤空 */
const canShowShortcut = (permi: string[]) => {
  if (checkRole(['super_admin'])) return true
  if (userStore.permissions.has('*:*:*')) return true
  return checkPermi(permi)
}

const go = (path: string) => {
  push(path).catch(() => {
    Message.warning('无法打开该页面，请重新登录或检查角色菜单权限')
  })
}

const loading = ref(true)
const profile = ref<ProfileVO>()
const notices = ref<any[]>([])
const loginLogs = ref<any[]>([])
const counts = ref<Record<string, number>>({})

const canViewNotice = computed(() => checkPermi(['system:notice:query']))
const canViewLoginLog = computed(() => checkPermi(['system:login-log:query']))
const roleNames = computed(() => (profile.value?.roles || []).map((r) => r.name).join('、'))

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

/** KPI 只取分页接口的 total，不额外要后端加统计接口 */
const KPI_DEFS = [
  {
    key: 'user',
    label: '用户数',
    icon: IconUser,
    tone: 'brand' as const,
    hint: '在职账号总数',
    path: '/system/user',
    permi: ['system:user:query'],
    load: () => getUserPage({ pageNo: 1, pageSize: 1 })
  },
  {
    key: 'role',
    label: '角色数',
    icon: IconUserGroup,
    tone: 'info' as const,
    hint: '含内置角色',
    path: '/system/role',
    permi: ['system:role:query'],
    load: () => getRolePage({ pageNo: 1, pageSize: 1 })
  },
  {
    key: 'dept',
    label: '部门数',
    icon: IconMindMapping,
    tone: 'success' as const,
    hint: '组织架构节点数',
    path: '/system/dept',
    permi: ['system:dept:query'],
    // 部门是树形全量接口，没有分页 total，取节点数量
    load: () => getSimpleDeptList().then((list) => ({ total: list?.length ?? 0 }))
  },
  {
    key: 'post',
    label: '岗位数',
    icon: IconIdcard,
    tone: 'warning' as const,
    hint: '可分配的岗位数',
    path: '/system/post',
    permi: ['system:post:query'],
    load: () => getPostPage({ pageNo: 1, pageSize: 1 })
  }
]

const kpis = computed(() =>
  KPI_DEFS.filter((d) => checkPermi(d.permi)).map((d) => ({
    label: d.label,
    value: counts.value[d.key] ?? 0,
    icon: d.icon,
    tone: d.tone,
    hint: d.hint,
    path: d.path
  }))
)

const ALL_SHORTCUTS = [
  {
    title: '运营工作台',
    desc: '待审、预约与举报一目了然',
    icon: IconBook,
    path: '/business/dashboard',
    permi: ['business:dashboard:query']
  },
  {
    title: '书籍审核',
    desc: '通过或驳回用户发布的书',
    icon: IconFolder,
    path: '/business/book',
    permi: ['business:book:query']
  },
  {
    title: '用户管理',
    desc: '开通账号、分配角色与部门',
    icon: IconUser,
    path: '/system/user',
    permi: ['system:user:query']
  },
  {
    title: '角色权限',
    desc: '配置菜单与按钮权限',
    icon: IconUserGroup,
    path: '/system/role',
    permi: ['system:role:query']
  },
  {
    title: '菜单管理',
    desc: '维护路由、按钮与权限标识',
    icon: IconMenu,
    path: '/system/menu',
    permi: ['system:menu:query']
  },
  {
    title: '部门管理',
    desc: '组织架构与负责人',
    icon: IconMindMapping,
    path: '/system/dept',
    permi: ['system:dept:query']
  },
  {
    title: '岗位管理',
    desc: '岗位编码与显示顺序',
    icon: IconIdcard,
    path: '/system/post',
    permi: ['system:post:query']
  },
  {
    title: '字典管理',
    desc: '下拉枚举统一在这里维护',
    icon: IconBook,
    path: '/system/dict',
    permi: ['system:dict:query']
  },
  {
    title: '参数配置',
    desc: '运行期开关与业务参数',
    icon: IconSettings,
    path: '/infra/config',
    permi: ['infra:config:query']
  },
  {
    title: '文件管理',
    desc: '上传记录与下载地址',
    icon: IconFolder,
    path: '/infra/file',
    permi: ['infra:file:query']
  }
]
const shortcuts = computed(() => ALL_SHORTCUTS.filter((s) => canShowShortcut(s.permi)))

// 各块互不依赖，用 allSettled：任一块 403/超时不该把整页拖成空白
const load = async () => {
  loading.value = true
  const tasks: Promise<void>[] = [
    getUserProfile().then((d) => {
      profile.value = d
    })
  ]
  for (const d of KPI_DEFS) {
    if (!checkPermi(d.permi)) continue
    tasks.push(
      d.load().then((page: any) => {
        counts.value[d.key] = page?.total ?? 0
      })
    )
  }
  if (canViewNotice.value) {
    tasks.push(
      getNoticePage({ pageNo: 1, pageSize: 5 }).then((page: any) => {
        notices.value = page?.list ?? []
      })
    )
  }
  if (canViewLoginLog.value) {
    tasks.push(
      getLoginLogPage({ pageNo: 1, pageSize: 5 }).then((page: any) => {
        loginLogs.value = page?.list ?? []
      })
    )
  }
  await Promise.allSettled(tasks)
  loading.value = false
}

onMounted(load)
</script>

<style lang="scss" scoped>
.hp {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 问候区 ===== */
.hp-hero {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 22px 24px;
  background: var(--bm-bg-card);
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius-lg, 14px);
  box-shadow: var(--bm-shadow-card);
}

.hero-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.3;
  color: var(--bm-text-1);
}

.hp-hero-sub {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 16px;
  margin: 8px 0 0;
  font-size: var(--bm-fs-xs, 12px);
  color: var(--bm-text-3);
}

.hp-hero-side {
  flex: 0 0 auto;
}

/* ===== KPI ===== */

/* auto-fit + minmax：4 张卡在窄屏自动折成 2 列 / 1 列，不用写断点 */
.hp-kpis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
}

.hp-card {
  border-radius: var(--bm-radius-lg, 14px);
  box-shadow: var(--bm-shadow-card);

  & + .hp-card {
    margin-top: 16px;
  }
}

/* ===== 快捷入口 ===== */
.hp-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
}

.hp-link {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px;
  font: inherit;
  color: inherit;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius, 10px);
  transition: border-color var(--bm-dur) var(--bm-ease-out),
    background-color var(--bm-dur) var(--bm-ease-out);

  &:hover {
    background: var(--bm-brand-bg);
    border-color: var(--bm-brand);
  }
}

.hp-link-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: 16px;
  color: var(--bm-brand);
  background: var(--bm-brand-bg);
  border-radius: var(--bm-radius-sm, 8px);
}

.hp-link-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.hp-link-title {
  font-size: var(--bm-fs-md, 14px);
  font-weight: 500;
  color: var(--bm-text-1);
}

.hp-link-desc {
  font-size: var(--bm-fs-2xs, 12px);
  line-height: 1.5;
  color: var(--bm-text-3);
}

/* ===== 公告 / 登录记录 ===== */
.hp-notice-time {
  margin-top: 4px;
  font-size: var(--bm-fs-2xs, 12px);
  color: var(--bm-text-4);
}
</style>
