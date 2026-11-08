package com.v2ray.ang.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.dto.ServerSortMode
import com.v2ray.ang.ui.compose.AppDropdownMenuItems
import com.v2ray.ang.ui.compose.AppTopBar
import com.v2ray.ang.ui.compose.LocalDarkTheme
import com.v2ray.ang.ui.compose.ThemeManager
import com.v2ray.ang.ui.compose.verticalScrollbar

@Composable
fun MainTopBar(
    isLoading: Boolean,
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    onAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit
) {
    var showImportMenu by remember { mutableStateOf(false) }
    var importMenuExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val importMenuScrollState = rememberScrollState()
    val moreMenuScrollState = rememberScrollState()
    val sortMenuScrollState = rememberScrollState()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val maxMenuHeight = LocalConfiguration.current.screenHeightDp.dp - statusBarHeight - navBarHeight - 20.dp
    val channelTitle = stringResource(R.string.title_site_channel)
    val buyTitle = stringResource(R.string.title_site_buy)
    val isDarkTheme = LocalDarkTheme.current
    val addDescription = stringResource(R.string.acc_add)

    AppTopBar(
        title = "",
        onBackClick = {},
        isLoading = isLoading,
        isSearchActive = showSearch,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onSearchClose = onSearchClose,
        searchPlaceholder = stringResource(R.string.menu_item_search),
        centerAligned = true,
        titleContent = {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopCenter)) {
                CenterAddButton(
                    description = addDescription,
                    onClick = {
                        importMenuExpanded = false
                        showImportMenu = true
                    }
                )
                DropdownMenu(
                    expanded = showImportMenu,
                    onDismissRequest = {
                        showImportMenu = false
                        importMenuExpanded = false
                    },
                    scrollState = importMenuScrollState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .widthIn(min = 320.dp)
                        .heightIn(max = maxMenuHeight)
                        .verticalScrollbar(importMenuScrollState)
                ) {
                    ImportMenuContent(
                        showAllItems = importMenuExpanded,
                        onShowMore = { importMenuExpanded = true },
                        onAction = { action ->
                            showImportMenu = false
                            importMenuExpanded = false
                            onAction(action)
                        }
                    )
                }
            }
        },
        navigationIcon = {
            if (showSearch) {
                IconButton(onClick = onSearchClose) {
                    Icon(painterResource(R.drawable.ic_arrow_back_24dp), contentDescription = stringResource(R.string.acc_back))
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BarIcon(R.drawable.ic_menu_24dp, stringResource(R.string.acc_open_menu), onMenuClick)
                    BarIcon(
                        if (isDarkTheme) R.drawable.ic_sun_24dp else R.drawable.ic_moon_24dp,
                        stringResource(R.string.acc_toggle_theme)
                    ) {
                        ThemeManager.setThemeMode(if (isDarkTheme) "1" else "2")
                    }
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                        BarIcon(R.drawable.ic_sort_24dp, stringResource(R.string.acc_sort_servers)) {
                            showSortMenu = true
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            scrollState = sortMenuScrollState,
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .widthIn(min = 220.dp)
                                .heightIn(max = maxMenuHeight)
                                .verticalScrollbar(sortMenuScrollState)
                        ) {
                            AppDropdownMenuItems(
                                items = ServerSortMode.entries,
                                labelRes = { mode ->
                                    when (mode) {
                                        ServerSortMode.Ping -> R.string.title_sort_by_ping
                                        ServerSortMode.Name -> R.string.title_sort_by_name
                                        ServerSortMode.Newest -> R.string.title_sort_by_newest
                                        ServerSortMode.Oldest -> R.string.title_sort_by_oldest
                                    }
                                },
                                onSelected = { mode ->
                                    showSortMenu = false
                                    onAction(MainAction.SortServers(mode))
                                }
                            )
                        }
                    }
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                        BarIcon(R.drawable.ic_more_vert_24dp, stringResource(R.string.acc_more)) {
                            showMenu = true
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            scrollState = moreMenuScrollState,
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .widthIn(min = 240.dp)
                                .heightIn(max = maxMenuHeight)
                                .verticalScrollbar(moreMenuScrollState)
                        ) {
                            MoreMenuContent { action ->
                                showMenu = false
                                onMoreMenuAction(action)
                            }
                        }
                    }
                }
            }
        },
        actions = {
            if (!showSearch) {
                BarIcon(R.drawable.ic_channel_24dp, channelTitle) {
                    onAction(MainAction.OpenSite("${AppConfig.SITE_URL}/channel/", channelTitle))
                }
                BarIcon(R.drawable.ic_cart_24dp, buyTitle) {
                    onAction(MainAction.OpenSite("${AppConfig.SITE_URL}/plans", buyTitle))
                }
                BarIcon(R.drawable.ic_search_24dp, stringResource(R.string.acc_search)) {
                    onSearchToggle(true)
                }
                BarIcon(
                    R.drawable.ic_gauge_24dp,
                    stringResource(R.string.title_real_ping_all_server)
                ) {
                    onAction(MainAction.TestRealAllServers)
                }
            }
        }
    )
}

@Composable
private fun CenterAddButton(
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .semantics {
                role = Role.Button
                contentDescription = description
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun BarIcon(
    @DrawableRes iconRes: Int,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .semantics {
                role = Role.Button
                contentDescription = description
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
