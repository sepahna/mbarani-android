package com.v2ray.ang.util

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.OutboundTrafficStat

/** Proxy bytes only. Direct and blocked outbounds stay off the home traffic line. */
fun proxyTransferBytes(stats: List<OutboundTrafficStat>): Pair<Long, Long> {
    var upload = 0L
    var download = 0L
    for (stat in stats) {
        if (stat.tag == AppConfig.TAG_DIRECT || stat.tag == AppConfig.TAG_BLOCKED) continue
        when (stat.direction) {
            AppConfig.UPLINK -> upload += stat.value
            AppConfig.DOWNLINK -> download += stat.value
        }
    }
    return upload to download
}
