package com.empty.android.core.viewbinding

import android.view.View
import timber.log.Timber

/**
 * View diff 更新工具。
 *
 * ## 背景
 * 在 XML + ViewBinding 的 MVI 架构中，每次 State 变化都会触发 `render()` 刷新。
 * 若 View 已绑定相同的值，重复 `setText`/`setImageResource` 仍会触发测量/布局，
 * 产生不必要的 UI 开销。
 *
 * 本工具通过 [View.setTag] / [View.getTag] 将"上一次绑定的值"存储在 View 自身，
 * 只有当新值与旧值不同时才真正调用 [bind] 执行更新。
 *
 * ## 用法
 * ```kotlin
 * // 基础：为单个 View 做 diff
 * tvTitle.diffUpdate(state.title) { text = it }
 * ivAvatar.diffUpdate(state.avatarUrl) { load(it) }
 * btnSubmit.diffUpdate(state.isEnabled) { isEnabled = it }
 *
 * // 带自定义 tag key（同一 View 绑定多个字段时使用）
 * container.diffUpdate(state.count, tagKey = R.id.tag_count) { tvCount.text = it.toString() }
 * container.diffUpdate(state.name,  tagKey = R.id.tag_name)  { tvName.text = it }
 *
 * // 首次强制更新（不管旧值是否相同）
 * tvTitle.diffUpdate(state.title, forceUpdate = true) { text = it }
 * ```
 *
 * ## Tag key 选择
 * - 默认使用 [View.getId] 对应的 key slot（`View.NO_ID` 时退化为 [DEFAULT_TAG_KEY]）。
 * - 同一个 View 需绑定多个独立字段时，必须为每个字段传入不同的 [tagKey]（定义在 `ids.xml`）。
 *
 * @param T      绑定值类型，必须正确实现 [equals]（data class / 基础类型均可）
 * @param value  当前要绑定的新值
 * @param tagKey 用于存储旧值的 tag key；默认值为 [DEFAULT_TAG_KEY]
 * @param forceUpdate 为 `true` 时跳过 diff，强制执行 [bind]
 * @param bind   实际更新 UI 的 lambda，接收新值 [T]
 */
fun <V: View, T> V.diffUpdate(
    value: T,
    tagKey: Int = DEFAULT_TAG_KEY,
    forceUpdate: Boolean = false,
    bind: V.(value: T) -> Unit,
) {
    @Suppress("UNCHECKED_CAST")
    val cached = getTag(tagKey) as? T

    if (!forceUpdate && cached == value) {
        Timber.v("[diffUpdate] skip: ${this::class.simpleName}#$id value unchanged")
        return
    }

    setTag(tagKey, value)
    bind(value)
    Timber.v("[diffUpdate] bind: ${this::class.simpleName}#$id  $cached → $value")
}

/**
 * 带可见性控制的 diff 更新。
 *
 * 当 [value] 为 `null` 时，View 切换为 [hiddenVisibility]（默认 [View.GONE]）并跳过 [bind]；
 * 非空时执行 diff 更新并确保 View 可见。
 *
 * ```kotlin
 * tvSubtitle.diffUpdateNullable(state.subtitle) { text = it }
 * ```
 */
fun <V: View, T> V.diffUpdateNullable(
    value: T?,
    tagKey: Int = DEFAULT_TAG_KEY,
    hiddenVisibility: Int = View.GONE,
    bind: V.(value: T) -> Unit,
) {
    if (value == null) {
        if (visibility != hiddenVisibility) visibility = hiddenVisibility
        return
    }
    if (visibility != View.VISIBLE) visibility = View.VISIBLE
    diffUpdate<V, T>(value, tagKey, bind = bind)
}

/**
 * 清除指定 tag key 下存储的 diff 缓存，下次调用 [diffUpdate] 时将强制更新。
 */
fun View.clearDiffCache(tagKey: Int = DEFAULT_TAG_KEY) {
    setTag(tagKey, null)
}

/**
 * 默认的 diff tag key。
 *
 * 取 `android.R.id.text1`（= 0x0102000b）作为稳定的系统内置 key，
 * 足够大概率不与业务 id 冲突。
 * 如需为同一 View 绑定多个字段，请在 `res/values/ids.xml` 中定义独立 id：
 * ```xml
 * <resources>
 *     <item name="tag_field_a" type="id"/>
 *     <item name="tag_field_b" type="id"/>
 * </resources>
 * ```
 */
const val DEFAULT_TAG_KEY: Int = android.R.id.text1
