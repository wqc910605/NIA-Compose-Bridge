pluginManagement {
    includeBuild("build-logic")
    repositories {
//        maven {
//            url = uri("https://maven.aliyun.com/repository/google")
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//
//        // 2) Maven Central → 阿里云镜像
//        maven {
//            url = uri("https://maven.aliyun.com/repository/central")
//        }

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // —— 下面两个是兜底，别删 —— //
        google()                // 官方 Google 仓库兜底
        mavenCentral()          // 官方 Maven Central 兜底
    }


}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven {
//            url = uri("https://maven.aliyun.com/repository/google")
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//        // 2) Maven Central → 阿里云镜像
//        maven {
//            url = uri("https://maven.aliyun.com/repository/central")
//        }
        // —— 下面两个是兜底，别删 —— //
        google()                // 官方 Google 仓库兜底
        mavenCentral()          // 官方 Maven Central 兜底
    }
}

rootProject.name = "NIA-Compose-Bridge"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// Core modules
include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:domain")
include(":core:viewbinding")

// Feature modules (api + impl 分离，参考 nowinandroid)
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:settings:api")
include(":feature:settings:impl")
include(":feature:weather:api")
include(":feature:weather:impl")
include(":feature:bookmarks:api")
include(":feature:bookmarks:impl")
include(":feature:interests:api")
include(":feature:interests:impl")
include(":feature:search:api")
include(":feature:search:impl")
include(":feature:topic:api")
include(":feature:topic:impl")
