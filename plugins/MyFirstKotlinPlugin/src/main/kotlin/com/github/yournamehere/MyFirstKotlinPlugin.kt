package com.github.entityshadowfound
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
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils
import com.discord.app.AppActivity
import com.discord.stores.StoreStream
import com.discord.widgets.channels.list.WidgetChannelsList
import com.discord.widgets.channels.memberlist.WidgetChannelMembersList
import com.discord.widgets.chat.input.WidgetChatInputEditText
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.guilds.list.WidgetGuildsListAdapterItemGuild
import com.discord.widgets.tabs.NavigationTab
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
if (bg is ColorDrawable) {
val c = bg.color
if (c != Color.TRANSPARENT && c != onyxBg) {
view.setBackgroundColor(targetColor)
}
}
if (view is ViewGroup) {
for (i in 0 until view.childCount) {
recolorSurfaces(view.getChildAt(i), targetColor)
}
}
}
private fun makePill(color: Int, radiusDp: Int, strokeColor: Int = onyxStroke): GradientDrawable {
return GradientDrawable().apply {
setColor(color)
cornerRadius = DimenUtils.dpToPx(radiusDp).toFloat()
if (strokeColor != 0) {
setStroke(DimenUtils.dpToPx(1), strokeColor)
}
}
}
override fun start(context: Context) {
patcher.after(
"onCreate",
Bundle::class.java
) { param ->
val activity = param.thisObject as Activity
activity.window?.setBackgroundDrawable(ColorDrawable(onyxBg))
activity.window?.statusBarColor = onyxBg
activity.window?.navigationBarColor = onyxBg
}
patcher.after(
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
patcher.after(
"onViewBound",
View::class.java
) { param ->
val root = param.args[0] as? ViewGroup ?: return@after
recolorSurfaces(root, onyxBg)
root.background = GradientDrawable().apply {
setColor(onyxBg)
cornerRadii = floatArrayOf(
DimenUtils.dpToPx(24).toFloat(), DimenUtils.dpToPx(24).toFloat(),
0f, 0f, 0f, 0f, 0f, 0f
)
}
val ctx = root.context
if (root.findViewWithTag("modern_dm_header") != null) return@after
var recyclerView: View? = null
fun findRecycler(v: View) {
if (v.javaClass.name.contains("RecyclerView")) {
recyclerView = v
return
}
if (v is ViewGroup) {
for (i in 0 until v.childCount) findRecycler(v.getChildAt(i))
}
}
findRecycler(root)
val rv = recyclerView ?: return@after
val rvParent = rv.parent as? ViewGroup ?: return@after
val rvIndex = rvParent.indexOfChild(rv)
val headerContainer = LinearLayout(ctx).apply {
tag = "modern_dm_header"
orientation = LinearLayout.VERTICAL
setBackgroundColor(onyxBg)
setPadding(
DimenUtils.dpToPx(16),
DimenUtils.dpToPx(12),
DimenUtils.dpToPx(16),
DimenUtils.dpToPx(8)
)
}
val titleText = TextView(ctx).apply {
text = "Messages"
setTextColor(textPrimary)
textSize = 22f
setTypeface(null, Typeface.BOLD)
setPadding(0, 0, 0, DimenUtils.dpToPx(10))
}
headerContainer.addView(titleText)
val actionRow = LinearLayout(ctx).apply {
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
}
val searchPill = TextView(ctx).apply {
text = "🔍"
setTextColor(textPrimary)
textSize = 15f
gravity = Gravity.CENTER
background = makePill(onyxPill, 20)
setPadding(DimenUtils.dpToPx(14), DimenUtils.dpToPx(8), DimenUtils.dpToPx(14), DimenUtils.dpToPx(8))
setOnClickListener {
StoreStream.getTabsNavigation().selectTab(NavigationTab.SEARCH, false)
}
}
val addFriendsPill = TextView(ctx).apply {
text = "👤+  Add Friends"
setTextColor(textPrimary)
textSize = 14f
setTypeface(null, Typeface.BOLD)
gravity = Gravity.CENTER
background = makePill(onyxPill, 20)
setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(8), DimenUtils.dpToPx(16), DimenUtils.dpToPx(8))
layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
marginStart = DimenUtils.dpToPx(8)
marginEnd = DimenUtils.dpToPx(8)
}
setOnClickListener {
StoreStream.getTabsNavigation().selectTab(NavigationTab.FRIENDS, false)
}
}
val newDmPill = TextView(ctx).apply {
text = "+"
setTextColor(textPrimary)
textSize = 18f
setTypeface(null, Typeface.BOLD)
gravity = Gravity.CENTER
background = makePill(onyxPill, 20)
setPadding(DimenUtils.dpToPx(14), DimenUtils.dpToPx(6), DimenUtils.dpToPx(14), DimenUtils.dpToPx(6))
setOnClickListener {
StoreStream.getTabsNavigation().selectTab(NavigationTab.FRIENDS, false)
}
}
actionRow.addView(searchPill)
actionRow.addView(addFriendsPill)
actionRow.addView(newDmPill)
headerContainer.addView(actionRow)
val activeScroll = HorizontalScrollView(ctx).apply {
isHorizontalScrollBarEnabled = false
setPadding(0, DimenUtils.dpToPx(12), 0, DimenUtils.dpToPx(4))
}
val activeRow = LinearLayout(ctx).apply {
orientation = LinearLayout.HORIZONTAL
}
val presences = StoreStream.getPresences().presences
val users = StoreStream.getUsers().users
var cardCount = 0
for ((userId, presence) in presences) {
if (cardCount >= 8) break
val user = users[userId] ?: continue
val activityName = presence.activities?.firstOrNull()?.name ?: continue
val card = LinearLayout(ctx).apply {
orientation = LinearLayout.VERTICAL
background = makePill(onyxCard, 16)
setPadding(DimenUtils.dpToPx(12), DimenUtils.dpToPx(10), DimenUtils.dpToPx(12), DimenUtils.dpToPx(10))
layoutParams = LinearLayout.LayoutParams(DimenUtils.dpToPx(135), DimenUtils.dpToPx(88)).apply {
marginEnd = DimenUtils.dpToPx(8)
}
}
val nameView = TextView(ctx).apply {
text = user.username
setTextColor(textPrimary)
textSize = 13f
setTypeface(null, Typeface.BOLD)
maxLines = 1
ellipsize = TextUtils.TruncateAt.END
}
val gameView = TextView(ctx).apply {
text = "🎮 $activityName"
setTextColor(textSecondary)
textSize = 12f
maxLines = 2
ellipsize = TextUtils.TruncateAt.END
setPadding(0, DimenUtils.dpToPx(6), 0, 0)
}
card.addView(nameView)
card.addView(gameView)
activeRow.addView(card)
cardCount++
}
if (cardCount > 0) {
activeScroll.addView(activeRow)
headerContainer.addView(activeScroll)
}
rvParent.addView(headerContainer, rvIndex)
val me = StoreStream.getUsers().me
if (me != null) {
val userBar = LinearLayout(ctx).apply {
orientation = LinearLayout.HORIZONTAL
gravity = Gravity.CENTER_VERTICAL
setBackgroundColor(onyxCard)
setPadding(
DimenUtils.dpToPx(16),
DimenUtils.dpToPx(10),
DimenUtils.dpToPx(16),
DimenUtils.dpToPx(10)
)
setOnClickListener {
StoreStream.getTabsNavigation().selectTab(NavigationTab.USER_SETTINGS, false)
}
}
val userInfoCol = LinearLayout(ctx).apply {
orientation = LinearLayout.VERTICAL
layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
}
val usernameTv = TextView(ctx).apply {
text = "${me.username} ⌄"
setTextColor(textPrimary)
textSize = 15f
setTypeface(null, Typeface.BOLD)
}
val statusTv = TextView(ctx).apply {
text = "Online • Tap for profile & settings"
setTextColor(textSecondary)
textSize = 12f
}
userInfoCol.addView(usernameTv)
userInfoCol.addView(statusTv)
userBar.addView(userInfoCol)
val bellIcon = TextView(ctx).apply {
text = "🔔"
textSize = 18f
setPadding(DimenUtils.dpToPx(10), DimenUtils.dpToPx(6), DimenUtils.dpToPx(10), DimenUtils.dpToPx(6))
setOnClickListener {
StoreStream.getTabsNavigation().selectTab(NavigationTab.MENTIONS, false)
}
}
userBar.addView(bellIcon)
rvParent.addView(userBar)
}
}
patcher.after(
"onViewBound",
View::class.java
) { param ->
val view = param.args[0] as? View ?: return@after
recolorSurfaces(view, onyxBg)
}
patcher.after(
"onViewBound",
View::class.java
) { param ->
val view = param.args[0] as? View ?: return@after
recolorSurfaces(view, onyxBg)
}
patcher.after(
"onViewBound",
View::class.java
) { param ->
val view = param.args[0] as? View ?: return@after
recolorSurfaces(view, onyxCard)
view.background = GradientDrawable().apply {
setColor(onyxCard)
cornerRadii = floatArrayOf(
DimenUtils.dpToPx(24).toFloat(), DimenUtils.dpToPx(24).toFloat(),
DimenUtils.dpToPx(24).toFloat(), DimenUtils.dpToPx(24).toFloat(),
0f, 0f, 0f, 0f
)
}
}
patcher.after(
"onConfigure",
Int::class.java,
ChatListEntry::class.java
) { param ->
val itemView = (param.thisObject as WidgetChatListAdapterItemMessage).itemView
itemView.background = makePill(onyxCard, 14, 0)
val padH = DimenUtils.dpToPx(10)
val padV = DimenUtils.dpToPx(6)
itemView.setPadding(padH, padV, padH, padV)
}
patcher.after(
"onConfigure",
Int::class.java,
ChatListEntry::class.java
) { param ->
val itemView = (param.thisObject as WidgetChatListAdapterItemEmbed).itemView
itemView.background = makePill(onyxCard, 16)
}
patcher.after(
"onViewBound",
View::class.java
) { param ->
val rootView = param.args[0] as? View ?: return@after
val parent = rootView.parent as? ViewGroup ?: return@after
parent.background = makePill(onyxPill, 24)
parent.setPadding(DimenUtils.dpToPx(14), DimenUtils.dpToPx(4), DimenUtils.dpToPx(14), DimenUtils.dpToPx(4))
val giftBtnId = Utils.getResId("chat_input_gift_button", "id")
if (giftBtnId != 0) {
parent.rootView.findViewById(giftBtnId)?.visibility = View.GONE
}
}
}
override fun stop(context: Context) {
patcher.unpatchAll()
}
}
