package com.empty.android.core.viewbinding

import android.view.View
import timber.log.Timber

/**
 * View diff 更新工具。
 *
 * 每次 State 变化触发 `render()` 时，通过 [View.setTag] 缓存上次绑定的值，
 * 新旧值相同时跳过更新，避免不必要的布局开销。
 *
 * 同一 View 需绑定多个字段时，各字段应使用不同的 [tagKey]。
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
val DEFAULT_TAG_KEY: Int = R.id.default_tag
