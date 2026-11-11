package com.v2ray.ang.ui.main

/**
 * Index to pass to [com.v2ray.ang.extension.moveItem] so [from] lands immediately
 * before [before]. A null [before] means the end of the list. Returns null when
 * the move would not change order.
 */
internal fun reorderDestination(size: Int, from: Int, before: Int?): Int? {
    if (size <= 1 || from !in 0 until size) return null
    val to = when {
        before == null -> size - 1
        before !in 0 until size || before == from -> return null
        from < before -> before - 1
        else -> before
    }
    if (to !in 0 until size || to == from) return null
    return to
}
