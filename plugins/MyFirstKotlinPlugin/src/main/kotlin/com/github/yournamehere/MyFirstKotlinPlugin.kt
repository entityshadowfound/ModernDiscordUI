package com.github.yournamehere

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils
import com.discord.app.AppActivity
import com.discord.widgets.channels.list.WidgetChannelsList
import com.discord.widgets.channels.memberlist.WidgetChannelMembersList
import com.discord.widgets.chat.input.WidgetChatInputEditText
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.guilds.list.WidgetGuildsListAdapterItemGuild
import com.discord.widgets.tabs.WidgetTabsHost
import com.discord.widgets.user.usersheet.WidgetUserSheet

@AliucordPlugin
class ModernDiscordUI : Plugin() {

    // Modern Discord Mobile Color Palette (Onyx / Dark)
    private val modernBg = Color.parseColor("#1C1D22")
    private val modernSidebarBg = Color.parseColor("#141519")
    private val modernSurface = Color.parseColor("#26272F")
    private val modernMessageCard = Color.parseColor("#212228")
    private val modernStroke = Color.parseColor("#32343F")

    override fun start(context: Context) {
        // 1. App-Wide Window, Status Bar & Nav Bar Palette
        patcher.after<AppActivity>(
            "onCreate",
            Bundle::class.java
        ) { param ->
            val activity = param.thisObject as Activity
            activity.window?.setBackgroundDrawable(ColorDrawable(modernBg))
            activity.window?.statusBarColor = modernSidebarBg
            activity.window?.navigationBarColor = modernSidebarBg
        }

        // 2. Modern Bottom Navigation Bar (Servers / Messages / Notifications / You)
        patcher.after<WidgetTabsHost>(
            "onViewBound",
            View::class.java
        ) { param ->
            val view = param.args as? View ?: return@after
            val navBarId = Utils.getResId("tabs_host_bottom_nav", "id")
            val navBar = if (navBarId != 0) view.findViewById<View>(navBarId) else null
            navBar?.setBackgroundColor(modernSidebarBg)
        }

        // 3. Modern "Squircle" Server Icons in Left Rail
        patcher.after<WidgetGuildsListAdapterItemGuild>(
            "onConfigure",
            Int::class.java,
            Any::class.java
        ) { param ->
            val itemView = (param.thisObject as WidgetGuildsListAdapterItemGuild).itemView
            val radius = DimenUtils.dpToPx(16).toFloat()
            itemView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, radius)
                }
            }
            itemView.clipToOutline = true
        }

        // 4. Floating Rounded Channel Sidebar
        patcher.after<WidgetChannelsList>(
            "onViewBound",
            View::class.java
        ) { param ->
            val view = param.args as? View ?: return@after
            val sidebarBg = GradientDrawable().apply {
                setColor(modernSidebarBg)
                cornerRadii = floatArrayOf(
                    DimenUtils.dpToPx(22).toFloat(), DimenUtils.dpToPx(22).toFloat(),
                    DimenUtils.dpToPx(22).toFloat(), DimenUtils.dpToPx(22).toFloat(),
                    0f, 0f, 0f, 0f
                )
            }
            view.background = sidebarBg
        }

        // 5. Modern Member List Panel
        patcher.after<WidgetChannelMembersList>(
            "onViewBound",
            View::class.java
        ) { param ->
            val view = param.args as? View ?: return@after
            view.setBackgroundColor(modernSidebarBg)
        }

        // 6. Modern Rounded User Profile Popup Sheet
        patcher.after<WidgetUserSheet>(
            "onViewBound",
            View::class.java
        ) { param ->
            val view = param.args as? View ?: return@after
            val sheetBg = GradientDrawable().apply {
                setColor(modernSurface)
                cornerRadii = floatArrayOf(
                    DimenUtils.dpToPx(24).toFloat(), DimenUtils.dpToPx(24).toFloat(),
                    DimenUtils.dpToPx(24).toFloat(), DimenUtils.dpToPx(24).toFloat(),
                    0f, 0f, 0f, 0f
                )
            }
            view.background = sheetBg
        }

        // 7. Modern Chat Message Containers
        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java
        ) { param ->
            val itemView = (param.thisObject as WidgetChatListAdapterItemMessage).itemView
            val msgBg = GradientDrawable().apply {
                setColor(modernMessageCard)
                cornerRadius = DimenUtils.dpToPx(14).toFloat()
            }
            itemView.background = msgBg
            val padH = DimenUtils.dpToPx(10)
            val padV = DimenUtils.dpToPx(6)
            itemView.setPadding(padH, padV, padH, padV)
        }

        // 8. Rounded Embed Cards with Modern Border
        patcher.after<WidgetChatListAdapterItemEmbed>(
            "onConfigure",
            Int::class.java,
            ChatListEntry::class.java
        ) { param ->
            val itemView = (param.thisObject as WidgetChatListAdapterItemEmbed).itemView
            val cardBg = GradientDrawable().apply {
                setColor(modernSurface)
                cornerRadius = DimenUtils.dpToPx(16).toFloat()
                setStroke(DimenUtils.dpToPx(1), modernStroke)
            }
            itemView.background = cardBg
        }

        // 9. Pill-Shaped Floating Chat Input & Decluttered Toolbar
        patcher.after<WidgetChatInputEditText>(
            "onViewBound",
            View::class.java
        ) { param ->
            val rootView = param.args as? View ?: return@after
            val parent = rootView.parent as? ViewGroup ?: return@after

            val pillBackground = GradientDrawable().apply {
                setColor(modernSurface)
                cornerRadius = DimenUtils.dpToPx(24).toFloat()
                setStroke(DimenUtils.dpToPx(1), modernStroke)
            }

            parent.background = pillBackground
            val padH = DimenUtils.dpToPx(14)
            val padV = DimenUtils.dpToPx(4)
            parent.setPadding(padH, padV, padH, padV)

            // Hide Nitro Gift button
            val giftBtnId = Utils.getResId("chat_input_gift_button", "id")
            if (giftBtnId != 0) {
                parent.rootView.findViewById<View>(giftBtnId)?.visibility = View.GONE
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
