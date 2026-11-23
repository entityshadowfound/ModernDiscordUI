package com.github.yournamehere

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils
import com.discord.app.AppActivity
import com.discord.models.presence.Presence
import com.discord.stores.StoreStream
import com.discord.widgets.channels.list.WidgetChannelsList
import com.discord.widgets.channels.memberlist.WidgetChannelMembersList
import com.discord.widgets.chat.input.WidgetChatInputEditText
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.guilds.list.WidgetGuildsList
import com.discord.widgets.tabs.WidgetTabsHost
import com.discord.widgets.user.usersheet.WidgetUserSheet

@AliucordPlugin
class ModernDiscordUI : Plugin() {

    private val onyxBg = Color.parseColor("#000000")
    private val onyxCard = Color.parseColor("#111216")
    private val onyxPill = Color.parseColor("#1A1B20")
    private val onyxStroke = Color.parseColor("#24262E")
    private val textPrimary = Color.parseColor("#F2F3F5")
    private val textSecondary = Color.parseColor("#949BA4")

    private fun recolorSurfaces(view: View, targetColor: Int) {
        val bg = view.background
        if (bg is ColorDrawable && bg.color != Color.TRANSPARENT && bg.color != onyxBg) {
            view.setBackgroundColor(targetColor)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) recolorSurfaces(view.getChildAt(i), targetColor)
        }
    }

    private fun makePill(color: Int, radiusDp: Int, strokeColor: Int = onyxStroke): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = DimenUtils.dpToPx(radiusDp).toFloat()
            if (strokeColor != 0) setStroke(DimenUtils.dpToPx(1), strokeColor)
        }
    }

    private fun clickBottomTab(rootView: View, tabIndex: Int) {
        val navBarId = Utils.getResId("tabs_host_bottom_nav", "id")
        val navBar = if (navBarId != 0) rootView.rootView.findViewById<ViewGroup>(navBarId) else null
        (navBar?.getChildAt(0) as? ViewGroup)?.getChildAt(tabIndex)?.performClick()
    }
        override fun start(context: Context) {
        patcher.after<AppActivity>("onCreate", Bundle::class.java) { param ->
            val activity = param.thisObject as? Activity ?: return@after
            activity.window?.setBackgroundDrawable(ColorDrawable(onyxBg))
            activity.window?.statusBarColor = onyxBg
            activity.window?.navigationBarColor = onyxBg
        }

        patcher.after<WidgetGuildsList>("onViewBound", View::class.java) { param ->
            val root = param.args.firstOrNull() as? ViewGroup ?: return@after
            recolorSurfaces(root, onyxBg)
            fun applySquircle(v: View) {
                if (v is RecyclerView) {
                    val r = DimenUtils.dpToPx(16).toFloat()
                    v.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                        override fun onChildViewAttachedToWindow(view: View) {
                            view.outlineProvider = object : ViewOutlineProvider() {
                                override fun getOutline(v: View, o: Outline) { o.setRoundRect(0, 0, v.width, v.height, r) }
                            }
                            view.clipToOutline = true
                        }
                        override fun onChildViewDetachedFromWindow(view: View) {}
                    })
                    return
                }
                if (v is ViewGroup) for (i in 0 until v.childCount) applySquircle(v.getChildAt(i))
            }
            applySquircle(root)
        }

        patcher.after<WidgetChannelsList>("onViewBound", View::class.java) { param ->
            val root = param.args.firstOrNull() as? ViewGroup ?: return@after
            recolorSurfaces(root, onyxBg)
            root.background = GradientDrawable().apply {
                setColor(onyxBg)
                val r = DimenUtils.dpToPx(24).toFloat()
                cornerRadii = floatArrayOf(r, r, 0f, 0f, 0f, 0f, 0f, 0f)
            }
            val ctx = root.context
            if (root.findViewWithTag<View>("modern_dm_header") != null) return@after

            var rv: View? = null
            fun findRv(v: View) {
                if (v is RecyclerView) { rv = v; return }
                if (v is ViewGroup) for (i in 0 until v.childCount) findRv(v.getChildAt(i))
            }
            findRv(root)
            val recycler = rv ?: return@after
            val rvParent = recycler.parent as? ViewGroup ?: return@after
            val rvIndex = rvParent.indexOfChild(recycler)

            val header = LinearLayout(ctx).apply {
                tag = "modern_dm_header"
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(onyxBg)
                setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(12), DimenUtils.dpToPx(16), DimenUtils.dpToPx(8))
            }
            header.addView(TextView(ctx).apply {
                text = "Messages"; setTextColor(textPrimary); textSize = 22f
                setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, DimenUtils.dpToPx(10))
            })

            val row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(ctx).apply {
                text = "🔍"; setTextColor(textPrimary); textSize = 15f; gravity = Gravity.CENTER
                background = makePill(onyxPill, 20)
                setPadding(DimenUtils.dpToPx(14), DimenUtils.dpToPx(8), DimenUtils.dpToPx(14), DimenUtils.dpToPx(8))
                setOnClickListener { clickBottomTab(root, 2) }
            })
            row.addView(TextView(ctx).apply {
                text = "👤+  Add Friends"; setTextColor(textPrimary); textSize = 14f
                setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER; background = makePill(onyxPill, 20)
                setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(8), DimenUtils.dpToPx(16), DimenUtils.dpToPx(8))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = DimenUtils.dpToPx(8); marginEnd = DimenUtils.dpToPx(8)
                }
                setOnClickListener { clickBottomTab(root, 1) }
            })
            row.addView(TextView(ctx).apply {
                text = "+"; setTextColor(textPrimary); textSize = 18f; setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER; background = makePill(onyxPill, 20)
                setPadding(DimenUtils.dpToPx(14), DimenUtils.dpToPx(6), DimenUtils.dpToPx(14), DimenUtils.dpToPx(6))
                setOnClickListener { clickBottomTab(root, 1) }
            })
            header.addView(row)

            val scroll = HorizontalScrollView(ctx).apply { isHorizontalScrollBarEnabled = false; setPadding(0, DimenUtils.dpToPx(12), 0, DimenUtils.dpToPx(4)) }
            val activeRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
            val presences: Map<Long, Presence> = StoreStream.getPresences().presences
            val users = StoreStream.getUsers().getUsers()
            var count = 0
            for (entry in presences.entries) {
                if (count >= 8) break
                val user = users[entry.key] ?: continue
                val actObj = entry.value.activities?.firstOrNull() ?: continue
                val actName = try {
                    val f = actObj.javaClass.getDeclaredField("name")
                    f.isAccessible = true
                    f.get(actObj)?.toString()
                } catch (_: Throwable) { null } ?: continue
                val card = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL; background = makePill(onyxCard, 16)
                    setPadding(DimenUtils.dpToPx(12), DimenUtils.dpToPx(10), DimenUtils.dpToPx(12), DimenUtils.dpToPx(10))
                    layoutParams = LinearLayout.LayoutParams(DimenUtils.dpToPx(135), DimenUtils.dpToPx(88)).apply { marginEnd = DimenUtils.dpToPx(8) }
                }
                card.addView(TextView(ctx).apply { text = user.username; setTextColor(textPrimary); textSize = 13f; setTypeface(null, Typeface.BOLD); maxLines = 1; ellipsize = TextUtils.TruncateAt.END })
                card.addView(TextView(ctx).apply { text = "🎮 " + actName; setTextColor(textSecondary); textSize = 12f; maxLines = 2; ellipsize = TextUtils.TruncateAt.END; setPadding(0, DimenUtils.dpToPx(6), 0, 0) })
                activeRow.addView(card); count++
            }
            if (count > 0) { scroll.addView(activeRow); header.addView(scroll) }

            val me = StoreStream.getUsers().me
            if (me != null) {
                val bar = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setBackgroundColor(onyxCard)
                    setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(10), DimenUtils.dpToPx(16), DimenUtils.dpToPx(10))
                    setOnClickListener { clickBottomTab(root, 4) }
                }
                val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
                col.addView(TextView(ctx).apply { text = me.username + " ⌄"; setTextColor(textPrimary); textSize = 15f; setTypeface(null, Typeface.BOLD) })
                col.addView(TextView(ctx).apply { text = "Online • Tap for profile & settings"; setTextColor(textSecondary); textSize = 12f })
                bar.addView(col)
                bar.addView(TextView(ctx).apply {
                    text = "🔔"; textSize = 18f; setPadding(DimenUtils.dpToPx(10), DimenUtils.dpToPx(6), DimenUtils.dpToPx(10), DimenUtils.dpToPx(6))
                    setOnClickListener { clickBottomTab(root, 3) }
                })
                rvParent.addView(bar)
            }
        }

        patcher.after<WidgetTabsHost>("onViewBound", View::class.java) { param ->
            (param.args.firstOrNull() as? View)?.let { recolorSurfaces(it, onyxBg) }
        }
        patcher.after<WidgetChannelMembersList>("onViewBound", View::class.java) { param ->
            (param.args.firstOrNull() as? View)?.let { recolorSurfaces(it, onyxBg) }
        }
        patcher.after<WidgetUserSheet>("onViewBound", View::class.java) { param ->
            val v = param.args.firstOrNull() as? View ?: return@after
            recolorSurfaces(v, onyxCard)
            val r = DimenUtils.dpToPx(24).toFloat()
            v.background = GradientDrawable().apply { setColor(onyxCard); cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f) }
        }
        patcher.after<WidgetChatListAdapterItemMessage>("onConfigure", Int::class.java, ChatListEntry::class.java) { param ->
            val v = (param.thisObject as? WidgetChatListAdapterItemMessage)?.itemView ?: return@after
            v.background = makePill(onyxCard, 14, 0); v.setPadding(DimenUtils.dpToPx(10), DimenUtils.dpToPx(6), DimenUtils.dpToPx(10), DimenUtils.dpToPx(6))
        }
        patcher.after<WidgetChatListAdapterItemEmbed>("onConfigure", Int::class.java, ChatListEntry::class.java) { param ->
            (param.thisObject as? WidgetChatListAdapterItemEmbed)?.itemView?.background = makePill(onyxCard, 16)
        }
        patcher.after<WidgetChatInputEditText>("onViewBound", View::class.java) { param ->
            val root = param.args.firstOrNull() as? View ?: return@after
            val parent = root.parent as? ViewGroup ?: return@after
            parent.background = makePill(onyxPill, 24)
            parent.setPadding(DimenUtils.dpToPx(14), DimenUtils.dpToPx(4), DimenUtils.dpToPx(14), DimenUtils.dpToPx(4))
            val giftId = Utils.getResId("chat_input_gift_button", "id")
            if (giftId != 0) parent.rootView.findViewById<View>(giftId)?.visibility = View.GONE
        }
    }

    override fun stop(context: Context) { patcher.unpatchAll() }
}
