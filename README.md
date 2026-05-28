# NIA-Compose-Bridge

一个模仿 [Now in Android](https://github.com/android/nowinandroid) 架构搭建的、开箱即用的 Android 项目模板。

核心定位是 **"双 UI 技术栈桥接"**：同一套数据层之上，同时示范 **Jetpack Compose** 和 **XML ViewBinding** 两种 UI 方案。你可以在同一个 App 中混用两者，也可以选择其中一种长期发展。

项目只包含基础通用模块和 demo 代码，不包含具体业务，你可以直接在这个框架上开发你自己的应用。

---

## 技术栈

- **100% Kotlin** + Jetpack Compose（Material 3）+ XML ViewBinding
- **Kotlin 2.3.20** + **AGP 9.1.1** + **Gradle 9.4.0** + JDK 17
- **KSP2 2.3.6**（替代 KSP1，AGP 9 起必需）
- **Hilt 2.59.2**（已原生支持 AGP 9）
- AGP 9 **built-in Kotlin**（不再需要手动 apply `kotlin-android` 插件）
- 模块化 + `build-logic` convention plugins（nowinandroid 风格）
- `gradle/libs.versions.toml` 统一版本管理
- Jetpack Navigation Compose 类型安全导航（基于 `kotlinx.serialization`）
- Room + DataStore 本地持久化
- Retrofit + OkHttp + kotlinx.serialization 网络层
- Coroutines + Flow + Channel 异步编程
- Coil 图片加载；Timber 日志

---

## 目录结构

```
NIA-Compose-Bridge/
├── app/                                    # 主应用模块
│   └── src/main/kotlin/.../
│       ├── MainActivity.kt                 # Compose 入口（App 级主题 + 导航）
│       ├── navigation/AppNavHost.kt        # Navigation Compose 导航图
│       ├── EmptyAndroidApplication.kt      # @HiltAndroidApp
│       └── demo/
│           ├── ProductCatalogActivity.kt   # ViewBinding Demo
│           ├── ProductCatalogUiState.kt    # UiState / UiEffect 定义
│           ├── ProductCatalogViewModel.kt  # BaseViewModel 子类 Demo
│           ├── ProductDetailFragment.kt    # Fragment + viewBinding() 委托
│           └── adapter/
│               └── ProductAdapter.kt       # BaseMultiAdapter 多类型 Demo
│
├── build-logic/convention/                 # 构建逻辑（Convention Plugins）
│   ├── build.gradle.kts                   # 注册 9 个 plugin
│   └── src/main/kotlin/
│       ├── com/empty/android/convention/
│       │   ├── AndroidCommon.kt            # 通用 Android/Kotlin 配置
│       │   └── AndroidCompose.kt           # Compose 通用配置
│       ├── AndroidApplicationConventionPlugin.kt
│       ├── AndroidApplicationComposeConventionPlugin.kt
│       ├── AndroidLibraryConventionPlugin.kt
│       ├── AndroidLibraryComposeConventionPlugin.kt
│       ├── AndroidFeatureApiConventionPlugin.kt
│       ├── AndroidFeatureImplConventionPlugin.kt
│       ├── AndroidHiltConventionPlugin.kt
│       ├── AndroidRoomConventionPlugin.kt
│       └── JvmLibraryConventionPlugin.kt
│
├── core/                                   # 核心基础层（10 个子模块）
│   ├── base/                               # ViewModel 基础框架
│   │   └── .../mvi/
│   │       ├── BaseContracts.kt            # UiState / UiEffect 接口
│   │       ├── BaseViewModel.kt            # ViewModel 基类
│   │       └── FlowExt.kt                  # 生命周期感知 Flow 收集
│   │
│   ├── viewbinding/                        # ViewBinding 基类组件
│   │   └── .../viewbinding/
│   │       ├── BaseActivity.kt             # Activity 基类
│   │       ├── BaseFragment.kt             # Fragment 基类
│   │       ├── BaseDialogFragment.kt       # DialogFragment 基类
│   │       ├── FragmentViewBinding.kt      # ViewBinding 属性委托
│   │       ├── ViewDiffExt.kt              # diffUpdate 局部刷新
│   │       └── adapter/
│   │           ├── BaseAdapter.kt          # RecyclerView Adapter 基类
│   │           ├── BaseMultiAdapter.kt     # 多条目 Adapter
│   │           ├── BaseSingleAdapter.kt    # 单条目 Adapter
│   │           ├── ItemViewBinder.kt        # 条目-ViewBinding 绑定器
│   │           └── VBViewHolder.kt          # ViewBinding ViewHolder
│   │
│   ├── common/                             # 通用基础设施
│   │   └── .../common/
│   │       ├── CoroutineDispatchers.kt     # 协程调度器
│   │       ├── ApplicationScope.kt         # Application 级协程作用域
│   │       └── Result.kt                   # 统一 Result 封装
│   │
│   ├── model/                              # 纯 Kotlin 领域模型（JVM library）
│   │   └── .../model/
│   │       ├── DemoItem.kt                 # 示例模型
│   │       └── ThemeMode.kt                # 主题模式枚举
│   │
│   ├── designsystem/                       # Compose 设计系统
│   │   └── .../designsystem/
│   │       ├── theme/
│   │       │   ├── Color.kt
│   │       │   ├── Theme.kt
│   │       │   └── Type.kt
│   │       └── component/
│   │           ├── EmptyBackground.kt
│   │           └── EmptyBackgroundIcon.kt
│   │
│   ├── ui/                                 # 跨 feature 通用 UI 组件
│   │   └── .../ui/
│   │       └── DemoItemCard.kt             # Demo 列表卡片组件
│   │
│   ├── data/                               # 仓库层（Repository）
│   │   └── .../data/
│   │       ├── repository/
│   │       │   ├── DemoItemsRepository.kt
│   │       │   └── UserSettingsRepository.kt
│   │       └── di/DataModule.kt            # Hilt @Binds 绑定
│   │
│   ├── database/                           # Room 数据库
│   │   └── .../database/
│   │       └── dao/, entity/, di/          # DAO / Entity / DatabaseModule
│   │
│   ├── datastore/                          # DataStore 偏好设置
│   │   └── .../datastore/
│   │       └── UserPreferencesSerializer.kt
│   │
│   ├── network/                            # Retrofit 网络层
│   │   └── .../network/
│   │       ├── api/DemoApi.kt              # Retrofit API 接口
│   │       └── di/NetworkModule.kt         # Hilt 网络依赖
│   │
│   └── domain/                             # UseCase 示例
│       └── .../domain/
│           └── GetDemoItemsUseCase.kt
│
├── feature/                                # 功能模块（api / impl 分离）
│   ├── home/
│   │   ├── api/                            # HomeRoute + navigateToHome
│   │   └── impl/                           # HomeScreen / HomeViewModel
│   └── settings/
│       ├── api/                            # SettingsRoute + navigateToSettings
│       └── impl/                           # SettingsScreen / SettingsViewModel
│
├── gradle/libs.versions.toml               # 版本目录
├── gradle.properties                       # Gradle 全局配置
├── settings.gradle.kts
└── build.gradle.kts
```

---

## 架构设计

### 核心理念：Compose ↔ ViewBinding 桥接

本项目名称中的 "Bridge" 指 **同一套数据层之上，桥接两种 UI 范式**：

```
┌──────────────────────────────────────────────────┐
│                     UI Layer                      │
│  ┌──────────────┐         ┌────────────────────┐  │
│  │   Compose     │         │  ViewBinding      │  │
│  │  HomeScreen   │         │  ProductCatalog    │  │
│  │  SettingsScreen│         │  Activity/Fragment │  │
│  └──────┬───────┘         └─────────┬──────────┘  │
│         │                           │             │
├─────────┼───────────────────────────┼─────────────┤
│  Shared ViewModel Core              │             │
│  ┌──────┴───────────────────────────┴──────────┐  │
│  │  BaseViewModel<UiState>                     │  │
│  │  StateFlow<UiState> + Channel<UiEffect>     │  │
│  │  Sealed Class State (Loading/Error/Success) │  │
│  └──────────────────────┬──────────────────────┘  │
│                         │                         │
├─────────────────────────┼─────────────────────────┤
│  Data Layer             │                         │
│  ┌──────────────────────┴──────────────────────┐  │
│  │  Repository → Room / DataStore / Retrofit   │  │
│  │  UseCase (Domain Layer)                     │  │
│  └─────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────┘
```

- **Compose 路径**：`ViewModel.uiState` → `collectAsStateWithLifecycle()` → `when(state)` 渲染
- **ViewBinding 路径**：`BaseActivity/BaseFragment` 自动订阅 `uiState` → `render(state)` → `when(state)` + `diffUpdate`

**两者共享同一套 Core 模块**（`core:base`、`core:data`、`core:model` 等），ViewModel 写法完全相同，UI 层各自由不同的基类衔接。

---

### ViewModel 架构

#### 1. BaseViewModel（`core:base`）

```kotlin
abstract class BaseViewModel<S : UiState>(initialState: S) : ViewModel() {
    val uiState: StateFlow<S>       // State：幂等、可重播
    val uiEffect: Flow<UiEffect>     // Effect：一次性消费（Channel）

    protected fun setState(state: S)           // 全量替换
    protected fun updateState(reducer: (S) -> S) // 局部更新（原子性）
    protected fun emitEffect(effect: UiEffect)  // 发出副作用
    protected fun launch(block: suspend () -> Unit) // 安全协程启动
}
```

**无 Intent 密封类**，ViewModel 直接暴露 public 方法作为类型安全的合约。

#### 2. State 设计模式：Sealed Class + Data Class

```kotlin
// 子模块 Data Class（并行共存）
data class CatalogData(
    val pageTitle: String = "Product Catalog",
    val items: List<ProductDisplayItem> = emptyList(),
)

// 页面级 Sealed Class（宏观互斥）
sealed class ProductCatalogUiState : UiState {
    data object Loading : ProductCatalogUiState()
    data class Error(val message: String) : ProductCatalogUiState()
    data class Success(val data: CatalogData) : ProductCatalogUiState()
}
```

#### 3. 两种 UI 渲染方式

**Compose 方式：**
```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        HomeUiState.Loading -> LoadingIndicator()
        is HomeUiState.Error -> ErrorPlaceholder(state.message)
        is HomeUiState.Success -> LazyColumn { items(state.items) { ... } }
    }
}
```

**ViewBinding 方式：**
```kotlin
@AndroidEntryPoint
class ProductCatalogActivity : BaseActivity() {
    override val viewModel by viewModels<ProductCatalogViewModel>()

    override fun render(state: UiState) {
        when (val s = state) {
            is ProductCatalogUiState.Loading -> { /* show loading */ }
            is ProductCatalogUiState.Error -> {
                binding.tvError.diffUpdate(s.message) { text = it }
            }
            is ProductCatalogUiState.Success -> {
                binding.tvPageTitle.diffUpdate(s.data.pageTitle) { text = it }
                adapter.submitList(s.data.items)
            }
        }
    }

    override fun handleEffect(effect: UiEffect) {
        when (effect) {
            is ProductCatalogEffect.ShowToast -> /* toast */
            is ProductCatalogEffect.NavigateToDetail -> /* navigate */
        }
    }
}
```

---

### ViewBinding 基础组件详解（`core:viewbinding`）

| 组件 | 说明 |
|------|------|
| `BaseActivity` | Activity 基类，自动订阅 State/Effect，提供 `render()` / `handleEffect()` 模板方法 |
| `BaseFragment` | Fragment 基类，以 `viewLifecycleOwner` 为宿主，支持 `currentState` / `previousState` |
| `BaseDialogFragment` | DialogFragment 基类，同上模式 |
| `Fragment.viewBinding()` | ViewBinding 属性委托，自动在 `onDestroyView` 时置 null，防内存泄漏 |
| `View.diffUpdate()` | 值不变则跳过 `setText` 等操作，避免无效重绘 |
| `View.diffUpdateNullable()` | 带可见性控制的 diff 更新（null 时自动 GONE） |
| `BaseMultiAdapter<T>` | 多条目 RecyclerView Adapter，`ItemViewBinder` 实现 "类型→ViewBinding→ViewHolder" 映射 |
| `BaseSingleAdapter<T>` | 单条目 RecyclerView Adapter，适合简单列表 |
| `BaseAdapter<T>` | Adapter 抽象基类，统一 `DiffUtil` 支持 |

#### diffUpdate 局部刷新机制

```
每次 render() 触发 → 每个 View 独立 diff：
  - 新旧值相同 → skip（不触发布局）
  - 新旧值不同 → call bind lambda + setTag 缓存新值
```

```kotlin
binding.tvTitle.diffUpdate(state.title) { text = it }
binding.ivAvatar.diffUpdate(state.avatarUrl) { load(it) }
// 同一 View 绑定多个字段时，需要指定不同 tagKey
binding.container.diffUpdate(state.count, tagKey = R.id.tag_count) { tvCount.text = it.toString() }
binding.container.diffUpdate(state.name,  tagKey = R.id.tag_name)  { tvName.text = it }
```

#### BaseMultiAdapter：多类型列表

```kotlin
class ProductAdapter(...) : BaseMultiAdapter<ProductDisplayItem>(...) {
    init {
        addItemType(TYPE_HEADER, ItemHeaderBinding::inflate) { item ->
            tvTitle.text = (item as? Header)?.title
        }
        addItemType(TYPE_BANNER, ItemBannerBinding::inflate) { item ->
            ivBanner.load((item as? Banner)?.imageUrl)
        }
        addItemType(TYPE_PRODUCT, ItemProductBinding::inflate) { item ->
            val p = item as? Product ?: return@addItemType
            tvName.text = p.name
            tvPrice.text = p.price
        }
        addItemType(TYPE_FOOTER, ItemFooterBinding::inflate) { item ->
            tvCount.text = "Total: ${(item as? Footer)?.totalCount}"
        }
    }

    override fun onItemViewType(position: Int, list: List<ProductDisplayItem>): Int {
        return when (list[position]) {
            is ProductDisplayItem.Header -> TYPE_HEADER
            is ProductDisplayItem.Banner -> TYPE_BANNER
            is ProductDisplayItem.Product -> TYPE_PRODUCT
            is ProductDisplayItem.Footer -> TYPE_FOOTER
        }
    }
}
```

---

### Feature 模块为何拆 `api` / `impl`？

参考 nowinandroid 的 [ModularizationLearningJourney](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md)：

- **`api`**：只存放导航 key 以及跳转扩展函数；体量极小，几乎不会变动。
- **`impl`**：存放真实的 UI、ViewModel、Hilt DI；改动频繁。

好处：
1. **构建增量友好**：`impl` 变更时，依赖它的模块不需要重新编译（只有 `api` 变动才会波及下游）；
2. **防止循环依赖**：feature 之间跳转时，`feature:A:impl` 只依赖 `feature:B:api`，绝不依赖 `feature:B:impl`；
3. **多 App 复用**：benchmark、catalog 等独立 App 可以只依赖 feature 的 `api` 来组装所需模块。

### 模块依赖图

```
              ┌─────────────┐
              │    :app     │
              └──────┬──────┘
           ┌─────────┴──────────┐
           ▼                    ▼
   feature:home:impl     feature:settings:impl
         │ │                   │
         │ └──────┐            │
         │        ▼            │
         │  feature:settings:api
         ▼                    │
   feature:home:api            │
         │                    │
         └──────┬─────────────┘
                │
     ┌──────────┴──────────┐
     ▼                     ▼
   :core:ui        :core:domain
   :core:designsystem     ...
                │
                ▼
            :core:data
     ┌─────────┼─────────┬──────────┐
     ▼         ▼         ▼          ▼
  database  network  datastore   (etc.)
                │
                ▼
            :core:model     ← JVM library（kotlinx.serialization）
                │
                ▼
            :core:common    ← Coroutines / Dispatchers / ApplicationScope
```

**依赖规则：**
- `:core:model` → 无依赖（纯 JVM, 仅依赖 `kotlinx-serialization-json`）
- `:core:common` → 仅依赖 Android + Coroutines
- `:core:viewbinding` → 仅依赖 `:core:base`（不依赖 feature/data 模块）
- `:core:base` → 仅依赖 AndroidX ViewModel + Coroutines
- feature `:impl` → 依赖 feature `:api` + core 各模块
- `:app` → 依赖所有 feature `:impl` 模块

---

## Convention Plugins

`build-logic/convention` 下提供了 9 个统一的 Gradle 插件：

| 插件 ID | 作用 |
|---------|------|
| `emptyandroid.android.application` | Application 模块基础配置（compileSdk=36, minSdk=24, targetSdk=36） |
| `emptyandroid.android.application.compose` | Application 模块启用 Compose |
| `emptyandroid.android.library` | Library 模块基础配置（compileSdk=36, minSdk=24，无 targetSdk） |
| `emptyandroid.android.library.compose` | Library 模块启用 Compose |
| **`emptyandroid.android.feature.api`** | feature `api` 模块（只含路由 key，添加 Compose + Hilt + kotlinx-serialization） |
| **`emptyandroid.android.feature.impl`** | feature `impl` 模块（Compose + Hilt + Navigation + core 全系依赖） |
| `emptyandroid.android.hilt` | 添加 Hilt 依赖和 KSP |
| `emptyandroid.android.room` | 添加 Room 依赖和 KSP |
| `emptyandroid.jvm.library` | 纯 JVM library（移除 Android，仅 Kotlin + kotlinx-serialization） |

---

## 运行

前置要求：
- Android Studio Panda 或更新（AGP 9.1 需要 Android Studio 2025.3.x 及以上）
- JDK 17

步骤：
1. 在 Android Studio 打开本项目根目录；
2. 首次 Sync 会自动下载全部依赖；
3. 连接设备或启动模拟器，点击 Run 即可。

命令行构建：

```bash
./gradlew assembleDebug      # 生成 debug APK
./gradlew lintDebug          # 代码检查
./gradlew testDebugUnitTest  # 单元测试
```

---

## 业务开发指引

### 1. 选择 UI 技术栈

| 路径 | 基类 | 适用场景 |
|------|------|----------|
| **Compose** | 标准 `ViewModel` + `collectAsStateWithLifecycle()` | 新页面首选，复杂动画、声明式 UI |
| **ViewBinding** | `BaseActivity` / `BaseFragment` | 已有 XML 页面、混合迁移、RecyclerView 多类型列表 |
| **混合** | 两者共存 | 渐进式迁移：Compose 新页面 + 旧页面保持 XML |

### 2. 新增 Compose Feature 模块

1. 在 `feature/` 下建目录 `feature/yourfeature/api/` 和 `feature/yourfeature/impl/`；
2. 在 `settings.gradle.kts` 加上：

   ```kotlin
   include(":feature:yourfeature:api")
   include(":feature:yourfeature:impl")
   ```

3. `feature/yourfeature/api/build.gradle.kts`：
   ```kotlin
   plugins {
       alias(libs.plugins.emptyandroid.android.feature.api)
   }
   android {
       namespace = "com.empty.android.feature.yourfeature.api"
   }
   ```

4. `api` 里定义路由：
   ```kotlin
   @Serializable
   data object YourFeatureRoute

   fun NavController.navigateToYourFeature(navOptions: NavOptions? = null) =
       navigate(route = YourFeatureRoute, navOptions = navOptions)
   ```

5. `feature/yourfeature/impl/build.gradle.kts`：
   ```kotlin
   plugins {
       alias(libs.plugins.emptyandroid.android.feature.impl)
   }
   android {
       namespace = "com.empty.android.feature.yourfeature.impl"
   }
   dependencies {
       implementation(projects.feature.yourfeature.api)
   }
   ```

6. 在 `impl` 中编写 Screen、ViewModel、NavGraphBuilder 扩展（参考 `feature/home/impl`）；
7. 在 `app/build.gradle.kts` 加 `implementation(projects.feature.yourfeature.impl)`，并在 `AppNavHost.kt` 注册路由。

### 3. 新增 ViewBinding 页面

如果你更偏向 XML + ViewBinding 方案：

```kotlin
// 1. 定义 UiState
sealed class YourUiState : UiState {
    data object Loading : YourUiState()
    data class Error(val msg: String) : YourUiState()
    data class Success(val data: YourData) : YourUiState()
}

// 2. 定义 ViewModel
@HiltViewModel
class YourViewModel @Inject constructor(
    private val repo: YourRepository,
) : BaseViewModel<YourUiState>(YourUiState.Loading) {

    fun load() {
        setState(YourUiState.Loading)
        launch {
            repo.getData()
                .onSuccess { setState(YourUiState.Success(it)) }
                .onFailure { setState(YourUiState.Error(it.message ?: "Error")) }
        }
    }
}

// 3. Activity 继承 BaseActivity
@AndroidEntryPoint
class YourActivity : BaseActivity() {
    override val binding by viewBinding(ActivityYourBinding::inflate)
    override val viewModel by viewModels<YourViewModel>()

    override fun initView() {
        binding.btnRefresh.setOnClickListener { viewModel.load() }
        viewModel.load()
    }

    override fun render(state: UiState) {
        when (val s = state) {
            is YourUiState.Loading -> { /* ... */ }
            is YourUiState.Error -> binding.tvError.diffUpdate(s.msg) { text = it }
            is YourUiState.Success -> {
                binding.tvTitle.diffUpdate(s.data.title) { text = it }
            }
        }
    }
}
```

### 4. 定义新的数据模型 / Repository / Network API

- 纯模型放 `core/model`（`@Serializable` data class）；
- 数据库 Entity 放 `core/database/entity/`，DAO 放 `core/database/dao/`；
- 网络 DTO 放 `core/network/model/`，API 接口放 `core/network/api/`；
- Repository 接口放 `core/data/repository/`，实现放同目录，通过 `core/data/di/DataModule.kt` 用 `@Binds` 绑定；
- UseCase 放 `core/domain/`。

### 5. 主题/组件

- Compose 主题统一在 `core/designsystem/theme/`（Color / Typography / Theme）；
- 全局通用 Compose 组件放 `core/designsystem/component/`；
- 跨 feature 使用的业务相关组件放 `core/ui/`。

---

## AGP 9 迁移要点（已适配）

相比 AGP 8，以下是最容易踩坑的点：

1. **built-in Kotlin**：AGP 9 内置 Kotlin 编译能力，不再允许手动 apply `org.jetbrains.kotlin.android`。
2. **CommonExtension 去参数化**：`CommonExtension<*,*,*,*,*,*>` → `CommonExtension`。
3. **DSL 方法形式被移除**：使用 `xxx.apply { ... }` 代替 lambda 块。
4. **Library 不再有 `targetSdk`**：由消费方 App 决定。
5. **KSP1 停止支持**：必须使用 KSP2（独立版本，本项目用 `2.3.6`）。
6. **Hilt 2.59+**：正式要求 AGP 9.0+。
7. **`android.enableJetifier=false`**：Jetifier 会导致 `ComponentTreeDeps` 异常。
8. **Kotlin 2.3 `-jvm-default` 值变更**：从 `all` → `enable`。
9. **`kotlin.incremental.useClasspathSnapshot` 已 deprecated**：已从 `gradle.properties` 移除。

更多背景参考：
- [AGP 9.0 release notes](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [AGP 9.1 release notes](https://developer.android.com/build/releases/agp-9-1-0-release-notes)
- [nowinandroid AGP 9 PR](https://github.com/android/nowinandroid/pull/1959)

---

## License

本仓库为模板项目，你可以随意使用、修改、发布。
