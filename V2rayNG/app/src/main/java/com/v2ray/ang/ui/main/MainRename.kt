package com.v2ray.ang.ui.main

import kotlinx.coroutines.CancellationException

internal sealed interface RenameOutcome {
    data object Updated : RenameOutcome
    data object Empty : RenameOutcome
    data object NotAllowed : RenameOutcome
    data object Missing : RenameOutcome
    data object Unchanged : RenameOutcome
    data class Failed(val error: Exception) : RenameOutcome
}

/**
 * Trims a requested display name and writes it only when the target exists and the
 * name actually changes. Blank text, a disallowed target, and a missing record do not
 * call [save].
 */
internal fun applyStoredRename(
    requestedName: String,
    allowed: Boolean,
    currentName: () -> String?,
    save: (String) -> Unit,
): RenameOutcome {
    if (!allowed) return RenameOutcome.NotAllowed
    val trimmed = requestedName.trim()
    if (trimmed.isEmpty()) return RenameOutcome.Empty
    val current = try {
        currentName()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        return RenameOutcome.Failed(error)
    } ?: return RenameOutcome.Missing
    if (current == trimmed) return RenameOutcome.Unchanged
    return try {
        save(trimmed)
        RenameOutcome.Updated
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        RenameOutcome.Failed(error)
    }
}
