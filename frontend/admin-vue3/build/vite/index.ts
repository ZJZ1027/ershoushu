import { resolve } from 'path'
import Vue from '@vitejs/plugin-vue'
import VueJsx from '@vitejs/plugin-vue-jsx'
import progress from 'vite-plugin-progress'
import EslintPlugin from 'vite-plugin-eslint'
import PurgeIcons from 'vite-plugin-purge-icons'
import { ViteEjsPlugin } from 'vite-plugin-ejs'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ArcoResolver } from 'unplugin-vue-components/resolvers'
import viteCompression from 'vite-plugin-compression'
import topLevelAwait from 'vite-plugin-top-level-await'
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons-ng'
import UnoCSS from 'unocss/vite'

export function createVitePlugins() {
  const root = process.cwd()

  // 路径查找
  function pathResolve(dir: string) {
    return resolve(root, '.', dir)
  }

  return [
    Vue(),
    VueJsx(),
    UnoCSS(),
    progress(),
    // iconSource: 'local' —— 只从本地 @iconify/json 解析图标，禁用在线回退，
    // 否则误报/缺失的图标名会走 api.iconify.design 拉取，网络抖动即 ETIMEDOUT 致构建随机失败
    PurgeIcons({ iconSource: 'local' }),
    AutoImport({
      include: [
        /\.[tj]sx?$/, // .ts, .tsx, .js, .jsx
        /\.vue$/,
        /\.vue\?vue/, // .vue
        /\.md$/ // .md
      ],
      imports: [
        'vue',
        'vue-router',
        // 可额外添加需要 autoImport 的组件
        {
          '@/hooks/web/useI18n': ['useI18n'],
          '@/hooks/web/useMessage': ['useMessage'],
          '@/utils/formRules': ['required'],
          '@/utils/dict': ['DICT_TYPE']
        }
      ],
      dts: 'src/types/auto-imports.d.ts',
      resolvers: [ArcoResolver()],
      eslintrc: {
        enabled: false, // Default `false`
        filepath: './.eslintrc-auto-import.json', // Default `./.eslintrc-auto-import.json`
        globalsPropValue: true // Default `true`, (true | false | 'readonly' | 'readable' | 'writable' | 'writeable')
      }
    }),
    Components({
      // 生成自定义 `auto-components.d.ts` 全局声明
      dts: 'src/types/auto-components.d.ts',
      // 组件解析器：Arco Design Vue（Element Plus 已退役）
      // Arco 组件以 <a-xxx> 形式使用
      resolvers: [
        ArcoResolver({
          sideEffect: true,
          resolveIcons: true
        })
      ],
      globs: ["src/components/**/**.{vue, md}", '!src/components/DiyEditor/components/mobile/**']
    }),
    EslintPlugin({
      // 开启缓存：该插件会在每次 Vite 转换 .vue/.ts/.tsx 时同步跑 ESLint，cache:false 意味着
      // 首屏与每次热更都要完整 lint 命中的文件，是 dev 变慢的主要来源。ESLint 缓存会按
      // 文件内容 + 配置 hash 自动失效，不会误报；需要强制清空时用 pnpm clean:cache。
      cache: true,
      include: ['src/**/*.vue', 'src/**/*.ts', 'src/**/*.tsx'] // 检查的文件
    }),
    VueI18nPlugin({
      runtimeOnly: true,
      compositionOnly: true,
      include: [resolve(__dirname, 'src/locales/**')]
    }),
    createSvgIconsPlugin({
      iconDirs: [pathResolve('src/assets/svgs')],
      symbolId: 'icon-[dir]-[name]',
    }),
    viteCompression({
      verbose: true, // 是否在控制台输出压缩结果
      disable: false, // 是否禁用
      threshold: 10240, // 体积大于 threshold 才会被压缩,单位 b
      algorithm: 'gzip', // 压缩算法,可选 [ 'gzip' , 'brotliCompress' ,'deflate' , 'deflateRaw']
      ext: '.gz', // 生成的压缩包后缀
      deleteOriginFile: false //压缩后是否删除源文件
    }),
    ViteEjsPlugin(),
    topLevelAwait({
      // https://juejin.cn/post/7152191742513512485
      // The export name of top-level await promise for each chunk module
      promiseExportName: '__tla',
      // The function to generate import names of top-level await promise in each chunk module
      promiseImportName: (i) => `__tla_${i}`
    })
  ]
}
