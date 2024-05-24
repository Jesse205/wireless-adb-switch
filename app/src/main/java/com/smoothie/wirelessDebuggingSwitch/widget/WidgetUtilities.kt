package com.smoothie.wirelessDebuggingSwitch.widget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.app.PendingIntentCompat
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.SettingsActivity

fun applyRemoteViewsParameters(
    context: Context,
    preferences: SharedPreferences,
    remoteViews: RemoteViews,
    cornerViewIds: Array<Int> = arrayOf(
        R.id.corner_bottom_left,
        R.id.corner_bottom_right,
        R.id.corner_top_left,
        R.id.corner_top_right
    ),
    rectangularViewIds: Array<Int> = arrayOf(
        R.id.side_top,
        R.id.side_bottom,
        R.id.side_left,
        R.id.side_right,
        R.id.center
    )
) {
    val radius = getWidgetCornerRadius(context, preferences)

    val cornerBitmap = generateWidgetCornerBitmap(context, preferences, radius)
    cornerViewIds.forEach { id ->
        remoteViews.setBitmap(id, "setImageBitmap", cornerBitmap)
    }

    val rectangularBitmap = generateRectangleBitmapForWidget(context, preferences)
    rectangularViewIds.forEach { id ->
        remoteViews.setBitmap(id, "setImageBitmap", rectangularBitmap)
    }
}

fun applyPreviewParameters(
    context: Context,
    preferences: SharedPreferences,
    view: View,
    cornerViewIds: Array<Int> = arrayOf(
        R.id.corner_bottom_left,
        R.id.corner_bottom_right,
        R.id.corner_top_left,
        R.id.corner_top_right
    ),
    rectangularViewIds: Array<Int> = arrayOf(
        R.id.side_top,
        R.id.side_bottom,
        R.id.side_left,
        R.id.side_right,
        R.id.center
    )
) {
    val radius =  getWidgetCornerRadius(context, preferences)

    val cornerBitmap = generateWidgetCornerBitmap(context, preferences, radius)
    cornerViewIds.forEach { id ->
        view.findViewById<ImageView>(id).setImageBitmap(cornerBitmap)
    }

    val rectangularBitmap = generateRectangleBitmapForWidget(context, preferences)
    rectangularViewIds.forEach { id ->
        view.findViewById<ImageView>(id).setImageBitmap(rectangularBitmap)
    }
}

private fun generateRectangleBitmapForWidget(
    context: Context,
    preferences: SharedPreferences
): Bitmap {
    val paint = createPaint(context, preferences)
    val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.drawRect(0f, 0f, 512f, 512f, paint)

    return bitmap
}

private fun getWidgetCornerRadius(
    context: Context,
    preferences: SharedPreferences
): Int {
    val key = context.getString(R.string.key_corner_roundness)
    val roundnessModifier = preferences.getFloat(key, 100f) / 100f
    val dimensionId = com.smoothie.widgetFactory.R.dimen.system_appwidget_background_radius
    val systemRoundness = context.resources.getDimensionPixelSize(dimensionId)
    val radius = (systemRoundness * roundnessModifier).toInt()
    return if (radius == 0) 1 else radius
}

private fun createPaint(context: Context, preferences: SharedPreferences): Paint {
    var key = context.getString(R.string.key_use_colorful_background)
    val useColorfulBackground = preferences.getBoolean(key, true)

    key = context.getString(R.string.key_background_transparency)
    val transparency = preferences.getFloat(key, 100f) / 100f

    val colorId =
        if (useColorfulBackground)
            R.color.colorGoogleWidgetBackground
        else
            R.color.colorSurface

    val theme = context.theme
    val color = Color.valueOf(theme.resources.getColor(colorId, theme))
    val colorInt =  Color.argb(transparency, color.red(), color.green(), color.blue())

    val paint = Paint()
    paint.color = colorInt
    paint.style = Paint.Style.FILL
    paint.blendMode = BlendMode.SRC

    return paint
}

private fun generateWidgetCornerBitmap(
    context: Context,
    preferences: SharedPreferences,
    radius: Int
): Bitmap {
    val paint = createPaint(context, preferences)
    val bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val radiusFloat = radius.toFloat()
    canvas.drawCircle(radiusFloat, radiusFloat, radiusFloat, paint)

    return bitmap
}

fun getMissingPrivilegesRemoteViews(context: Context, preferences: SharedPreferences): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_no_privileges)
    views.setOnClickPendingIntent(R.id.root_view, PendingIntentCompat.getActivity(
        context,
        0,
        Intent(context, SettingsActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        0,
        false
    ))
    applyRemoteViewsParameters(context, preferences, views)
    return views
}

fun getDisabledUpdatesRemoteViews(
    context: Context,
    preferences: SharedPreferences
): RemoteViews {
    val views = getMissingPrivilegesRemoteViews(context, preferences)
    val header = context.getString(R.string.title_updates_disabled)
    val description = context.getString(R.string.message_updates_disabled)
    views.setImageViewResource(R.id.image_view_status, R.drawable.round_sync_disabled_24)
    views.setTextViewText(R.id.text_view_header, header)
    views.setTextViewText(R.id.text_view_description, description)
    return views
}
