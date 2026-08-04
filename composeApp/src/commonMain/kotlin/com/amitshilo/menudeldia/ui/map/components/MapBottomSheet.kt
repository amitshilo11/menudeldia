package com.amitshilo.menudeldia.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Stable positions the map sheet settles at, ordered from most to least map visible. */
internal enum class MapSheetValue { Peek, Half, Expanded }

/** How much of the sheet stays on screen at [MapSheetValue.Peek]. */
internal val MapSheetPeekHeight = 160.dp

/** Top edge of the sheet at each anchor, as a fraction of the container height. */
private const val ExpandedTopFraction = 0.1f
private const val HalfTopFraction = 0.5f

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/**
 * Anchored state for the map sheet.
 *
 * The anchors are the Y offset of the sheet's top edge from the container top, so a *smaller*
 * offset means a *more expanded* sheet — the same convention [AnchoredDraggableState] uses for
 * drag deltas, which is what lets the nested-scroll bridge hand raw deltas straight through.
 */
@Composable
internal fun rememberMapSheetState(
    containerHeightPx: Float,
    peekHeight: Dp = MapSheetPeekHeight,
): AnchoredDraggableState<MapSheetValue> {
    val peekHeightPx = with(LocalDensity.current) { peekHeight.toPx() }
    // Anchors are baked into the state at construction so the offset is initialised before the
    // first layout pass. Rebuilding on a size change would otherwise reset the sheet, so the
    // settled value is carried across.
    var lastSettled by remember { mutableStateOf(MapSheetValue.Half) }
    val state = remember(containerHeightPx, peekHeightPx) {
        AnchoredDraggableState(
            initialValue = lastSettled,
            anchors = DraggableAnchors {
                MapSheetValue.Expanded at containerHeightPx * ExpandedTopFraction
                MapSheetValue.Half at containerHeightPx * HalfTopFraction
                MapSheetValue.Peek at containerHeightPx - peekHeightPx
            },
        )
    }
    LaunchedEffect(state) {
        snapshotFlow { state.settledValue }.collect { lastSettled = it }
    }
    return state
}

/**
 * Bottom sheet that shares the screen with the map.
 *
 * The whole surface is draggable — not just the handle — and a [NestedScrollConnection] hands
 * scroll deltas from the content to the sheet whenever the content can't consume them itself, so
 * a single upward flick expands the sheet and keeps going into the list. That drag modifier also
 * gives the sheet a pointer-input node across its full area, which is what stops iOS from routing
 * touches straight through to the `MKMapView` underneath.
 */
@Composable
internal fun MapBottomSheet(
    state: AnchoredDraggableState<MapSheetValue>,
    sheetHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(state)
    val nestedScroll = remember(state, flingBehavior) {
        MapSheetNestedScrollConnection(state, flingBehavior)
    }
    val expandedOffset = state.anchors.positionOf(MapSheetValue.Expanded)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .offset { IntOffset(0, (state.requireOffset() - expandedOffset).roundToInt()) }
            .shadow(8.dp, SheetShape)
            .background(MaterialTheme.colorScheme.surfaceContainer, SheetShape)
            .nestedScroll(nestedScroll)
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Vertical,
                flingBehavior = flingBehavior,
            ),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        scope.launch {
                            val target = if (state.targetValue == MapSheetValue.Expanded) {
                                MapSheetValue.Half
                            } else {
                                MapSheetValue.Expanded
                            }
                            state.animateTo(target)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                SheetDragHandle()
            }
            content()
        }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .size(width = 36.dp, height = 4.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
    )
}

/**
 * Bridges the content's scroll gestures to the sheet: drags that would move the content past its
 * bounds move the sheet instead, and flings are handed to the same [FlingBehavior] the drag
 * modifier uses so both paths settle identically.
 */
private class MapSheetNestedScrollConnection(
    private val state: AnchoredDraggableState<MapSheetValue>,
    private val flingBehavior: FlingBehavior,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // Dragging up expands the sheet; let it grow before the content scrolls.
        val delta = available.y
        return if (delta < 0f && source == NestedScrollSource.UserInput) {
            Offset(0f, state.dispatchRawDelta(delta))
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = if (source == NestedScrollSource.UserInput) {
        // Dragging down only reaches us once the content is scrolled to the top.
        Offset(0f, state.dispatchRawDelta(available.y))
    } else {
        Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.y
        return if (toFling < 0f && state.requireOffset() > state.anchors.minPosition()) {
            fling(toFling)
            // The sheet settles with a tween, so consume everything rather than leaving the
            // content to fling a fraction of the gesture at the same time.
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
        Velocity(consumed.x, fling(available.y))

    private suspend fun fling(initialVelocity: Float): Float {
        var consumedVelocity = 0f
        state.anchoredDrag {
            val scrollScope = object : ScrollScope {
                override fun scrollBy(pixels: Float): Float {
                    val newOffset = (state.offset + pixels)
                        .coerceIn(state.anchors.minPosition(), state.anchors.maxPosition())
                    val consumed = newOffset - state.offset
                    dragTo(newOffset)
                    return consumed
                }
            }
            consumedVelocity = with(flingBehavior) { scrollScope.performFling(initialVelocity) }
        }
        return consumedVelocity
    }
}
