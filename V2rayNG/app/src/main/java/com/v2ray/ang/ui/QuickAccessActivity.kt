package com.v2ray.ang.ui

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.ui.base.BaseComponentActivity
import com.v2ray.ang.ui.compose.AppTopBar
import com.v2ray.ang.ui.compose.NavigationBarsSpacer
import com.v2ray.ang.util.Utils

private data class SiteLink(
    val title: String,
    val subtitle: String,
    val path: String,
    val badge: String = ""
)

private val quickSections = listOf(
    "دسترسی سریع" to listOf(
        SiteLink("خرید", "خرید سرویس", "/plans"),
        SiteLink("سفارش‌ها", "پیگیری خرید", "/orders"),
        SiteLink("تیکت", "پیگیری پیام", "/tickets", "جدید"),
        SiteLink("پشتیبانی", "تماس و واتساپ", "/support"),
        SiteLink("برنامه‌ها", "دانلود کلاینت", "/#apps"),
        SiteLink("ویدیو", "آموزش ویدیویی", "/video"),
        SiteLink("بلاگ", "مطالب آموزشی", "/blog"),
        SiteLink("کانال", "کانال اطلاع‌رسانی", "/channel"),
        SiteLink("کیف پول", "شارژ و خرید سریع", "/wallet"),
        SiteLink("اپلیکیشن", "", "/app", "جدید"),
        SiteLink("بازی", "بازی و جایزه", "/games", "جدید"),
    ),
    "امکانات دیگر" to listOf(
        SiteLink("قیمت ارز و طلا", "دلار، تتر، طلا و ...", "/market"),
        SiteLink("وضعیت سرور", "سرورها", "/", "آزمایشی"),
    )
)

class QuickAccessActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        QuickAccessScreen(onBackClick = { finish() })
    }
}

@Composable
private fun QuickAccessScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_quick_access),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_barani_logo),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Text(
                    text = AppConfig.SITE_URL.removePrefix("https://"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            quickSections.forEach { (title, links) ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                links.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { link ->
                            SiteLinkCard(
                                link = link,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    Utils.openUri(context, AppConfig.SITE_URL + link.path)
                                }
                            )
                        }
                        repeat(2 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            NavigationBarsSpacer()
        }
    }
}

@Composable
private fun SiteLinkCard(
    link: SiteLink,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (link.badge.isNotEmpty()) {
                Text(
                    text = link.badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = link.title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (link.subtitle.isNotEmpty()) {
                Text(
                    text = link.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
