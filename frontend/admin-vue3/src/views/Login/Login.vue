<template>
  <div :class="prefixCls" class="lp">
    <!-- 左：品牌叙事区。窄屏整块收起（用媒体查询而非 uno 的 lt-lg，后者在本项目里没生效） -->
    <aside class="lp-brand">
      <div class="lp-brand-deco" aria-hidden="true"></div>
      <div class="lp-brand-in">
        <!-- logo 只取图形块，产品名用右侧实文字排一遍，这样才跟得上租户自定义标题。
             图形自带实心蓝底，压在同为蓝色的品牌渐变上会两蓝相撞，故垫一层白片。 -->
        <div class="lp-logo">
          <span class="lp-logo-chip"><BrandLogo :size="40" /></span>
          <span class="lp-logo-wm">
            <span class="lp-logo-text">{{ underlineToHump(appStore.getTitle) }}</span>
            <span class="lp-logo-en">Admin Pro</span>
          </span>
        </div>

        <div class="lp-mid">
          <h1 class="lp-h1">
            开箱即用的<br />
            后台管理基础框架
          </h1>
          <p class="lp-sub">多租户、权限、代码生成与运维监控全部就绪，接手就能写业务。</p>

          <!-- 用它开发一个模块的真实五步，不是装饰性文案。
               连接线用 li::after（挂在前一项末尾），折行时不会把短横带到下一行行首。 -->
          <ol class="lp-rail">
            <li v-for="s in pipeline" :key="s">
              <span class="lp-rail-node">{{ s }}</span>
            </li>
          </ol>

          <ul class="lp-feats">
            <li v-for="f in features" :key="f.title">
              <span class="lp-feat-icon"><component :is="f.icon" /></span>
              <span class="lp-feat-body">
                <span class="lp-feat-title">{{ f.title }}</span>
                <span class="lp-feat-desc">{{ f.desc }}</span>
              </span>
            </li>
          </ul>
        </div>

      </div>
    </aside>

    <!-- 右：登录区 -->
    <section class="lp-side">
      <div class="lp-side-deco" aria-hidden="true"></div>

      <header class="lp-top">
        <div class="lp-top-brand">
          <BrandLogo :size="32" />
          <span class="lp-top-title">{{ underlineToHump(appStore.getTitle) }}</span>
        </div>
        <ThemeSwitch />
      </header>

      <main class="lp-main">
        <div class="lp-card">
          <LoginForm />

          <!-- 默认不开放自助注册（账号由租户管理员在系统里开通），这里把这条规则说清楚，
               否则新用户会一直找那个并不存在的「注册」入口。 -->
          <!-- 拆两段各自 inline-block：中文可在任意字间断行，窄屏下整句会把「开通」劈成两行 -->
          <p class="lp-card-foot">
            <span>账号由所属租户的管理员开通，</span><span>如需开通租户请联系系统管理员</span>
          </p>
        </div>

        <!-- 三条安全属性。原来摆在左侧底部（lp-proof），但左栏讲的是「能干什么」，
             这里讲的是「敢不敢交给它」—— 挨着登录动作说才是它该在的位置，
             顺带把右栏那片空白填成有信息的内容。 -->
        <ul class="lp-trust">
          <li v-for="t in trusts" :key="t.text">
            <component :is="t.icon" class="lp-trust-icon" />
            {{ t.text }}
          </li>
        </ul>
      </main>

      <footer class="lp-bot">© {{ new Date().getFullYear() }} Admin Pro · 后台管理基础框架</footer>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { underlineToHump } from '@/utils'
import { useDesign } from '@/hooks/web/useDesign'
import { useAppStore } from '@/store/modules/app'
import { ThemeSwitch } from '@/layout/components/ThemeSwitch'
import {
  IconUserGroup,
  IconCode,
  IconDashboard,
  IconSafe,
  IconLock,
  IconHistory
} from '@arco-design/web-vue/es/icon'
import BrandLogo from '@/components/BrandLogo/index.vue'
import { LoginForm } from './components'

defineOptions({ name: 'Login' })

const appStore = useAppStore()
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')

// 只写框架真实具备的能力，别写成通用套话——这一屏是二次开发者的第一印象。
const features = [
  {
    icon: IconUserGroup,
    title: '组织与权限开箱可用',
    desc: '部门、岗位、角色、菜单与按钮级鉴权一应俱全'
  },
  {
    icon: IconCode,
    title: '原生 MyBatis-Plus 数据层',
    desc: '分页、逻辑删除、审计字段与多租户全部交给官方插件，没有额外封装'
  },
  {
    icon: IconDashboard,
    title: '审计与文件能力就绪',
    desc: '登录日志、操作日志自动留痕，文件上传下载直接可用'
  }
]
const pipeline = ['写迁移脚本', '建实体与 Mapper', '配置菜单权限', '开发业务', '查审计日志']

// 三条都对应框架里真实存在的机制：租户拦截器 + JWT 无状态鉴权 + 登录/操作双日志。
// 别在这一屏写「等保三级」这类没落地的合规话术。
const trusts = [
  { icon: IconSafe, text: '租户级数据隔离' },
  { icon: IconLock, text: 'JWT 无状态鉴权' },
  { icon: IconHistory, text: '全链路操作留痕' }
]
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

@keyframes lp-rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: none;
  }
}

/* ============ 窄屏：品牌区收起，登录卡居中 ============ */
@media (width <= 1023px) {
  .lp-brand {
    display: none;
  }

  .lp-top {
    justify-content: space-between;
  }

  .lp-top-brand {
    display: flex;
  }

  .lp-side {
    flex-basis: 100%;
  }
}

/* 矮屏（笔记本 + 浏览器缩放）：让品牌区随内容滚动而不是压缩变形 */
@media (height <= 700px) {
  .lp-brand-in {
    padding: 32px 48px;
  }

  .lp-h1 {
    font-size: 32px;
  }

  .lp-rail {
    margin-top: 22px;
  }

  .lp-feats {
    margin-top: 24px;

    li {
      padding: 10px 0;
    }
  }
}

@media (prefers-reduced-motion: no-preference) {
  .lp-logo,
  .lp-h1,
  .lp-sub,
  .lp-rail,
  .lp-feats,
  .lp-card,
  .lp-trust {
    animation: lp-rise 0.5s var(--bm-ease, cubic-bezier(0.22, 1, 0.36, 1)) both;
  }

  .lp-h1 {
    animation-delay: 0.04s;
  }

  .lp-sub {
    animation-delay: 0.08s;
  }

  .lp-rail {
    animation-delay: 0.12s;
  }

  .lp-feats {
    animation-delay: 0.16s;
  }

  .lp-trust {
    animation-delay: 0.2s;
  }
}

.lp {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 100vh;
  overflow-y: auto;
  background: var(--bm-bg-page);
}

/* ============ 左：品牌叙事 ============ */
.lp-brand {
  position: relative;
  flex: 1 1 58%;
  min-width: 0;
  overflow: hidden;
  color: #fff;

  /* --bm-grad 只有「视觉体验」主题定义；其余 7 套主题走 var() 回退，
     用各自的 primary 色阶拼同构渐变，保证换主题时这一屏跟着换。 */

  /* 取 8/7/6 而不是更亮的 7/6/5：白字压在中调蓝上最多只有 3.6:1，
     13px 的说明文字过不了 WCAG AA(4.5)。整体压深一档后，正文与说明都在 6:1 以上。 */
  background-image: var(
    --bm-grad-strong,
    linear-gradient(
      135deg,
      rgb(var(--primary-8)) 0%,
      rgb(var(--primary-7)) 48%,
      rgb(var(--primary-6)) 100%
    )
  );
}

/* 暗色下 Arco 会把整条色阶反转（--primary-8 变成很浅的蓝），
   同一组序号在暗色里会把这块渲染成刺眼的亮蓝，白字直接糊在上面。
   序号换成 3/4/5，反转后取到的正好是明色 8/7/6 那三支色 —— 明暗两套观感一致。 */
body[arco-theme='dark'] .lp-brand {
  background-image: var(
    --bm-grad-strong,
    linear-gradient(
      135deg,
      rgb(var(--primary-3)) 0%,
      rgb(var(--primary-4)) 48%,
      rgb(var(--primary-5)) 100%
    )
  );
}

/* 装饰层：两团柔光 + 细网格 + 压暗罩。放在伪元素同级的独立层里，避免与内容争 z-index。
   压暗罩（最后一层，铺在品牌渐变正上方）是白字能在**任意品牌色**上过 WCAG AA 的关键：
   琥珀金 / 科技青这类亮色相，白字直接压上去只有 2.4~4.3:1；
   加一层随渐变方向由 0.2 → 0.4 加深的暗罩后，色相保留、亮度压住，实测全部 ≥4.6:1。 */
.lp-brand-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    radial-gradient(circle at 18% 12%, rgb(255 255 255 / 18%) 0, transparent 42%),
    radial-gradient(circle at 88% 82%, rgb(255 255 255 / 12%) 0, transparent 46%),
    linear-gradient(rgb(255 255 255 / 5%) 1px, transparent 1px),
    linear-gradient(90deg, rgb(255 255 255 / 5%) 1px, transparent 1px),
    linear-gradient(135deg, rgb(6 8 18 / 20%) 0%, rgb(6 8 18 / 40%) 100%);
  background-size: 100% 100%, 100% 100%, 34px 34px, 34px 34px, 100% 100%;
}

.lp-brand-in {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  height: 100%;

  /* 五段主线实测约 540；左右内距各 50 → 640，再留一点字号/抗锯齿余量 */
  max-width: 640px;
  padding: 48px 50px;
  margin: 0 auto;
  flex-direction: column;
}

.lp-logo {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 12px;
}

/* 白片托底：logo 自带实心蓝，直接压在蓝色品牌渐变上是「两个不同的蓝挨着」，
   垫白片既是品牌资产最保险的呈现方式（浅底 + 原图不动），也把它托成一枚可辨识的徽标 */
.lp-logo-chip {
  display: inline-flex;
  padding: 4px;
  background: #fff;
  border-radius: 13px;
  box-shadow: 0 2px 10px -4px rgb(0 0 0 / 30%);
}

.lp-logo-wm {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.lp-logo-text {
  font-size: 19px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: var(--bm-track-wide);
}

/* 拉丁副名走 display 字体（自托管变量字体），给中文字样垫一条更「品牌」的底线 */
.lp-logo-en {
  font-family: var(--bm-font-display);
  font-size: 10.5px;
  font-weight: 500;
  letter-spacing: var(--bm-track-caps);

  /* 0.72 在琥珀金上只有 3.8:1；这一行虽小但是品牌字样，压到 0.88 才过 AA */
  color: rgb(255 255 255 / 88%);
  text-transform: uppercase;
}

/* logo 钉顶、口号钉底，中间叙事块整体居中。
   叙事必须是连续的一整块——之前用 auto 外距把标题和特性各推一边，
   中间反而空出两条带子，跟右侧那片白一样是"没排明白"。 */
.lp-mid {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  justify-content: center;
  min-height: 0;
}

.lp-h1 {
  margin: 0 0 14px;
  font-size: 42px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: var(--bm-track-display);
}

.lp-sub {
  max-width: 26em;
  margin: 0;
  font-size: 15px;
  line-height: 1.7;

  /* 有色底上的「次要白」不能按灰底的习惯往下压：0.8 在琥珀金那种亮色相上只剩 4 出头。
     统一 0.9，层次交给字号与字重去拉，不靠透明度。 */
  color: rgb(255 255 255 / 90%);
}

/* 五段主线：药丸 + 连接线。药丸底是半透明白，压在渐变上自带玻璃感 */
.lp-rail {
  display: flex;
  flex-wrap: wrap;
  align-items: center;

  /* 折行时行距；列向间距由 ::after 连接线自己撑 */
  row-gap: 8px;
  padding: 0;
  margin: 30px 0 0;
  list-style: none;

  li {
    display: flex;
    align-items: center;
  }

  /* 连接线挂在「前一项」末尾：该项折到下一行时，短横留在上行，不会出现行首孤儿线 */
  li:not(:last-child)::after {
    width: 14px;
    height: 1px;
    margin: 0 4px;
    background: rgb(255 255 255 / 42%);
    content: '';
  }
}

.lp-rail-node {
  padding: 6px 10px;
  font-size: 12.5px;

  /* 半透明底 + 细网格时，ClearType 容易把笔画「描糊」成双影；锁一层合成减轻 */
  -webkit-font-smoothing: antialiased;
  font-weight: 500;
  white-space: nowrap;
  background: rgb(255 255 255 / 16%);
  border-radius: 999px;
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 24%);
}

.lp-feats {
  padding: 0;
  margin: 34px 0 0;
  list-style: none;

  li {
    display: flex;
    gap: 14px;
    padding: 14px 0;

    & + li {
      border-top: 1px solid rgb(255 255 255 / 14%);
    }
  }
}

/* 玻璃质感图标片：半透明白底 + 内高光，压在渐变上不会脏 */
.lp-feat-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  font-size: 17px;
  background: rgb(255 255 255 / 16%);
  border-radius: 10px;
  box-shadow: inset 0 1px 0 rgb(255 255 255 / 28%);
}

.lp-feat-body {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.lp-feat-title {
  font-size: 14.5px;
  font-weight: 600;
}

.lp-feat-desc {
  font-size: 13px;
  line-height: 1.6;
  color: rgb(255 255 255 / 90%);
}

/* ============ 右：登录区 ============ */

/* 40% 而不是 42%：登录卡固定 420 宽，右栏越宽卡片两侧的空白越显眼。
   左栏叙事是可伸缩内容，多给它一点更划算。 */
.lp-side {
  position: relative;
  display: flex;
  flex: 1 1 40%;
  flex-direction: column;
  min-width: 0;
}

/* 右栏原来是一整片纯白，卡片像贴在打印纸上。加两团极淡的品牌柔光 + 一层细网格，
   让它成为「有质感的界面表面」而不是空白页；强度压到几乎察觉不到，别抢卡片。 */
.lp-side-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    radial-gradient(
      circle at 92% 4%,
      color-mix(in srgb, var(--bm-brand) 12%, transparent) 0,
      transparent 46%
    ),
    radial-gradient(
      circle at 4% 100%,
      color-mix(in srgb, var(--bm-brand) 9%, transparent) 0,
      transparent 42%
    ),
    linear-gradient(var(--bm-border-light) 1px, transparent 1px),
    linear-gradient(90deg, var(--bm-border-light) 1px, transparent 1px);
  background-size: 100% 100%, 100% 100%, 34px 34px, 34px 34px;
  opacity: 0.8;
}

.lp-top {
  position: relative;
  display: flex;
  flex: 0 0 auto;
  align-items: center;

  /* 宽屏这里只有一个明暗按钮，space-between 会把它顶到最左边（贴着品牌区），
     看着像放错了位置；靠右才是工具位该在的地方。窄屏出现 logo 时再改回两端对齐。 */
  justify-content: flex-end;
  height: 68px;
  padding: 0 28px;
}

/* 宽屏时左侧已有完整品牌区，这里的 logo 只是重复占位，只在窄屏出现 */
.lp-top-brand {
  display: none;
  align-items: center;
  gap: 10px;
}

.lp-top-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--bm-text-1);
}

/* 卡片 + 卡片下方的安全属性行作为一组内容整体居中，所以这里是竖向 flex */
.lp-main {
  position: relative;
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 8px 28px 28px;
}

/* 表单必须有个「容器」：原先三个输入框直接漂在整片白上，没有落点也没有层级 */
.lp-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  padding: 36px 36px 30px;

  /* 用 auto 外距而不是 align-items: center 做垂直居中：空间不足时 auto 会退成 0
     贴住顶部并交给 .lp 滚动，居中则会把卡片顶部裁掉（矮屏 / 页面缩放时常见）。
     下外距为 0：与其后的安全属性行按固定 18px 相接，两者作为一组整体居中。 */
  margin: auto auto 0;
  background: var(--bm-bg-card);
  border: 1px solid var(--bm-border-light);
  border-radius: var(--bm-radius-lg, 14px);
  box-shadow: var(--bm-shadow-card);

  /* 卡片背后一团很大的品牌柔光：让卡片看起来是「被打亮的」而不是贴在白纸上，
     也把右栏那片空白收成有中心的画面。z-index:-1 会绘制在本元素背景之下、
     祖先背景之上，所以只在卡片外缘露出光晕（.lp-card 不生成层叠上下文，成立）。 */
  &::before {
    position: absolute;
    z-index: -1;
    background: radial-gradient(
      60% 50% at 50% 50%,
      color-mix(in srgb, var(--bm-brand) 13%, transparent) 0,
      transparent 100%
    );
    content: '';
    inset: -90px -70px;
  }
}

/* 卡内收尾说明：与按钮之间用一条 hairline 断开，压成最弱一档字色，不跟主按钮抢注意力 */
.lp-card-foot {
  padding-top: 18px;
  margin: 22px 0 0;
  font-size: 12.5px;
  line-height: 1.6;
  color: var(--bm-text-3);
  text-align: center;
  text-wrap: balance;
  border-top: 1px solid var(--bm-border-light);

  span {
    display: inline-block;
  }
}

.lp-trust {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
  justify-content: center;
  width: 100%;
  max-width: 420px;
  padding: 0;
  margin: 18px auto auto;
  list-style: none;

  li {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: var(--bm-text-3);
  }
}

.lp-trust-icon {
  font-size: 13px;
  color: var(--bm-brand);
  opacity: 0.85;
}

.lp-bot {
  flex: 0 0 auto;
  padding: 0 28px 26px;
  font-size: 12px;
  color: var(--bm-text-3);
  text-align: center;
}
</style>

<style lang="scss">
/* 表单内部件的排版收敛（子组件是 scoped，只能在这里落全局规则，选择器限定在 .lp-card 内） */
.lp-card {
  .arco-form-item {
    margin-bottom: 18px;
  }

  /* 收尾项用显式类名而不是 :last-child：表单末尾若再挂别的组件，:last-child 就落不到按钮上 */
  .lf-submit-item {
    margin-bottom: 0;
  }

  /* 标题块与第一个输入框之间要比行间距更开，否则读起来像并列的四行 */
  .lf-title-item {
    margin-bottom: 26px;
  }

  /* 勾选行本身只有 20px 高，上下都给 18px 会显得它孤零零飘在中间：
     往上收紧贴住密码框，往下留足再接主按钮。 */
  .lf-remember-item {
    margin-top: -4px;
    margin-bottom: 22px;
  }

  /* Arco 的 large 输入框默认 40px、灰底无描边（--color-fill-2）：三个叠在白卡上
     就是三条灰砖，既看不出「可输入」也压不住卡片的层级。改成描边式：
     底色跟卡片一致、1px 描边、聚焦时上品牌色 + 3px 光环。高度抬到 42 与主按钮成一套。 */
  .arco-input-wrapper,
  .arco-select-view-single {
    height: 42px;
    background-color: transparent;
    border: 1px solid var(--bm-border);
    border-radius: var(--bm-radius-sm, 8px);
    transition: border-color var(--bm-dur) var(--bm-ease-out), box-shadow var(--bm-dur) var(--bm-ease-out);

    &:hover {
      background-color: transparent;
      border-color: var(--bm-border-strong, var(--bm-brand));
    }

    &.arco-input-focus {
      background-color: transparent;
      border-color: var(--bm-brand);
      box-shadow: 0 0 0 3px color-mix(in srgb, var(--bm-brand) 16%, transparent);
    }

    &.arco-input-error {
      border-color: var(--bm-danger);
    }
  }

  .arco-input-prefix {
    padding-right: 10px;
    color: var(--bm-text-3);
  }

  .arco-btn-size-large {
    height: 42px;
    border-radius: var(--bm-radius-sm, 8px);
  }
}
</style>
