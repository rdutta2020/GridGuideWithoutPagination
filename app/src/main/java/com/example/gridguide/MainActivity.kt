/*
 * Copyright (c) 2023. Xperi Inc.  All rights reserved.
 */
package com.example.gridguide

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridguide.ui.theme.GridGuideTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

/**
 * This class is responsible for drawing Guide with NG
 */
class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "GuideNGActivity"
    }
    //@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    lateinit var scrollState: ScrollableState

    val maxRows = 400
    val maxColumns = 14 * 24 * 2 // 14 days future data in advance
    val minColumns = -3 * 24 * 2 // 3 days previous data in advance
    val headerHeight = 48
    val rowHeight = 72
    val fixedColumnWidth = 88

    // @Volatile
    lateinit var stateRowX: LazyListState

    //@Volatile
    lateinit var stateRowY: LazyListState

    lateinit var scope: CoroutineScope

    @OptIn(ExperimentalMaterialApi::class)
    lateinit var programSheetState: ModalBottomSheetState

    @OptIn(ExperimentalMaterialApi::class)
    lateinit var channelSheetState: ModalBottomSheetState

    private val fontFamily = FontFamily(
        Font(R.font.roboto_regular, FontWeight.Normal)
    )

    @OptIn(ExperimentalFoundationApi::class)
    val customPageSize = object : PageSize {
        override fun Density.calculateMainAxisPageSize(
            availableSpace: Int,
            pageSpacing: Int
        ): Int {
            val visiblePageCount = pageCountOnScreen()
            return (availableSpace - (visiblePageCount - 1) * pageSpacing) / visiblePageCount
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @SuppressLint(
        "UnusedMaterial3ScaffoldPaddingParameter",
        "UnusedMaterialScaffoldPaddingParameter"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        setContent {
            scope = rememberCoroutineScope()
            programSheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded },
                skipHalfExpanded = true
            )

            channelSheetState = rememberModalBottomSheetState(
                initialValue = ModalBottomSheetValue.Hidden,
                confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded },
                skipHalfExpanded = true
            )

            stateRowX = rememberLazyListState() // State for the first Row, X
            stateRowY = rememberLazyListState() // State for the second Row, Y

            val scope = rememberCoroutineScope()
            scrollState = rememberScrollableState { delta ->
                Timber.tag(TAG).i("scrollState changed by %s", delta)
                scope.launch {
                    //Log.i(TAG,"Before stateRowX ${stateRowX.firstVisibleItemIndex} stateRowY ${stateRowY.firstVisibleItemIndex}")
                    stateRowX.scrollBy(-delta)
                    /* Below line does not work from 2nd page onwards, so added alternative approach under "LaunchedEffect" temporarily */
                    stateRowY.scrollBy(-delta)
                    // Log.i(TAG,"After stateRowX scrollBy ${stateRowX.scrollBy(-delta)} stateRowY scrollBy ${stateRowY.scrollBy(-delta)}")
                    /* for(state in stateYList) {
                          state.scrollBy(-delta)
                      }*/
                }
                delta
            }

            /* Below two LaunchedEffects are added to sync stateRowX & stateRowY from 2nd page onwards */
            LaunchedEffect(stateRowX.firstVisibleItemScrollOffset) {
                if (!stateRowY.isScrollInProgress) {
                    stateRowY.scrollToItem(
                        stateRowX.firstVisibleItemIndex,
                        stateRowX.firstVisibleItemScrollOffset
                    )
                }
            }

            LaunchedEffect(stateRowY.firstVisibleItemScrollOffset) {
                if (!stateRowX.isScrollInProgress) {
                    stateRowX.scrollToItem(
                        stateRowY.firstVisibleItemIndex,
                        stateRowY.firstVisibleItemScrollOffset
                    )
                }
            }

            Scaffold(topBar = {
            },
                content = {
                    if (isPhoneUi()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(.6f)
                                .background(Color.Green)
                        ) {
                            MainContent()
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Blue)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(.48f)
                                    .background(color = colorResource(id = R.color.tivo_dark_surface))
                            ) {
                                InfoPane()
                            }
                            Box(
                                modifier = Modifier.background(Color.Green),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                MainContent()
                            }
                        }
                    }
                    if (isPhoneUi()) {
                        ProgramBottomSheetLayout()
                        ChannelBottomSheetLayout()
                    }
                }
            )
            //   }
        }
    }

    @Composable
    fun InfoPane() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Green
                        ), startX = 1000f
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.demo),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.BottomEnd),
                contentDescription = ""
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                colorResource(id = R.color.black_00)
                            ), startX = 1500f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Golden State Warriors vs. Philadelphia 76ers",
                    color = Color.White,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Basketball", color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ".", color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "2 hr 20 min", color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ".", color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live on ESPN", color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The Philadelphia 76ers' home stand continues as they prepare to take on the Golden State Warriors at Wells Fargo Center",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_status_not_recordable),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "This channel does not allow recording", color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        onClick = { }, colors = ButtonDefaults.buttonColors(
                            backgroundColor = colorResource(
                                id = R.color.tivo_system_dark_primary
                            )
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_watch_live),
                            contentDescription = ""
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Watch Live on ESPN", color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = colorResource(id = R.color.tivo_dark_surface),
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_info_info),
                            contentDescription = ""
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Details and Watch Options", color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MainContent() {
        Column(
            modifier = Modifier
                // .padding(top = padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollable(scrollState, Orientation.Vertical)
                    .background(color = colorResource(id = R.color.tivo_dark_surface))
            ) {
                ChannelHeaderWithList()
                Spacer(modifier = Modifier.width(2.dp))
                ProgramList()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ProgramList(modifier: Modifier = Modifier) {
        val pagerState = rememberPagerState(initialPage = -(minColumns))
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            HorizontalPager(
                pageCount = maxColumns + (-1) * minColumns,
                pageSize = customPageSize,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { currentPage ->
                Timber.tag(TAG).i("Drawing pager for page : %s", currentPage)
                val displayPageNumber = currentPage + minColumns
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(Modifier.padding(end = 2.dp))
                ) {
                    TimeSlotCell(headerHeight, "T $displayPageNumber")
                    LazyColumn(
                        state = stateRowY,
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(channelProgramData) { index, item ->
                            Timber.tag(TAG).i("LazyColumn of program drawing row %s", index)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row() {
                                if (((index + 1) + abs(displayPageNumber)) % 5 == 0) {
                                    //ProgramItemCell(rowHeight, "P - ${index} - ${displayPageNumber}", index, 0.25f)
                                    ProgramItemCell(
                                        rowHeight,
                                        getProgramName(index, displayPageNumber),
                                        index,
                                        0.25f
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                //ProgramItemCell(rowHeight, "P - ${index} - ${displayPageNumber}", index, 1.0f)
                                ProgramItemCell(
                                    rowHeight,
                                    getProgramName(index, displayPageNumber),
                                    index,
                                    1.0f
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ProgramItemCell(height: Int, content: String, rowNumber: Int, widthFraction: Float) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(height.dp)
                .clip(RoundedCornerShape(5.dp))
                .clickable {
                    if (isPhoneUi()) {
                        scope.launch {
                            if (programSheetState.isVisible) {
                                programSheetState.hide()
                            } else {
                                programSheetState.show()
                            }
                        }
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.program_cell_bg),
                contentScale = ContentScale.FillBounds,
                contentDescription = "",
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                if (widthFraction != 0.25f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        if (rowNumber % 4 == 0) {
                            Text(
                                text = "New",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = "${rowNumber * 400 / 30}m left",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }

    @Composable
    fun TimeSlotCell(height: Int, content: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .height(height.dp)
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = R.drawable.program_cell_bg),
                contentScale = ContentScale.FillBounds,
                contentDescription = ""
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }

    @Composable
    fun ChannelHeaderWithList(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .width(fixedColumnWidth.dp)
        ) {
            ChannelHeaderCell(headerHeight, "Channel Filter")
            ChannelList()
        }
    }

    @Composable
    fun ChannelList() {
        LazyColumn(
            state = stateRowX,
            userScrollEnabled = false
        ) {
            Timber.tag(TAG).i("LazyColumn of channel redrawing")
            items(channelProgramData.size) { index ->
                // Log.d("TAG","DrawaingRowNumber==$index")
                Timber.tag(TAG).i("LazyColumn of channel drawing row %s", index)
                Spacer(modifier = Modifier.height(2.dp))
                ChannelItemCell(rowHeight, index)
            }
        }
    }

    @Composable
    fun ChannelHeaderCell(height: Int, content: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .height(height.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.program_cell_bg),
                contentScale = ContentScale.FillBounds,
                contentDescription = ""
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ChannelItemCell(height: Int, rowNumber: Int) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clickable {
                    if (isPhoneUi()) {
                        scope.launch {
                            if (channelSheetState.isVisible) {
                                channelSheetState.hide()
                            } else {
                                channelSheetState.show()
                            }
                        }
                    }
                }
        ) {
            Image(
                painter = painterResource(id = getIconDrawable(rowNumber)),
                contentDescription = "",
                modifier = Modifier
                    .width(65.dp)
                    .height(60.dp)
                    .align(Alignment.Center)
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ProgramBottomSheetLayout() {
        val roundedCornerRadius = 12.dp
        val modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)

        BackHandler(programSheetState.isVisible) {
            scope.launch { programSheetState.hide() }
        }

        ModalBottomSheetLayout(
            sheetState = programSheetState,
            sheetShape = RoundedCornerShape(
                topStart = roundedCornerRadius,
                topEnd = roundedCornerRadius
            ),
            sheetBackgroundColor = Color.Transparent,
            sheetContent = {
                // Sheet Content here
                Box(
                    modifier = modifier
                        .paint(
                            // TODO bottom_sheet_background this need to replace
                            painterResource(id = R.drawable.bg_bottom_sheet),
                            contentScale = ContentScale.FillBounds
                        ), contentAlignment = Alignment.Center

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(28.dp))
                        // TODO Need to remove the Hardcoded Value
                        XperiTitleLarge(
                            title = "Gilmore Girls",
                            textColor = colorResource(id = R.color.tivo_dark_on_surface)
                        )
                        // TODO Need to remove the Hardcoded Value
                        XperiBodyMedium(
                            title = "S7 E4 'S Wonderful, 'S Marvelous",
                            textColor = colorResource(
                                id = R.color.tivo_dark_on_surface
                            )
                        )
                        // TODO Need to remove the Hardcoded Value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 3.dp, bottom = 3.dp)
                        ) {
                            XperiBodySmall(
                                title = "Comedy, Drama",
                                textColor = colorResource(id = R.color.tivo_dark_on_surface_varient)
                            )
                            XperiBodySmall(
                                title = ".",
                                textColor = colorResource(id = R.color.tivo_dark_on_surface_varient)
                            )

                            XperiBodySmall(
                                title = "Sun 2/10  6:00 - 6:45 PM on ABC",
                                textColor = colorResource(id = R.color.tivo_dark_on_surface_varient)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Image(
                            // TODO Remove this image from drawable when API integration done
                            painter = painterResource(id = R.drawable.ic_demo),
                            contentDescription = "",
                            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_info),
                                contentDescription = ""
                            )
                            Spacer(modifier = Modifier.width(26.dp))
                            XperiBodyLarge(
                                title = "Details and Watch Options", textColor = colorResource(
                                    id = R.color.tivo_dark_on_surface
                                )
                            )
                        }
                    }
                }
            },
        ) {
            if (programSheetState.isVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {

                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ChannelBottomSheetLayout() {
        val roundedCornerRadius = 12.dp
        val modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)

        BackHandler(channelSheetState.isVisible) {
            scope.launch { channelSheetState.hide() }
        }

        ModalBottomSheetLayout(
            sheetState = channelSheetState,
            sheetShape = RoundedCornerShape(
                topStart = roundedCornerRadius,
                topEnd = roundedCornerRadius
            ),
            sheetBackgroundColor = Color.Transparent,
            sheetContent = {
                // Sheet Content here
                Box(
                    modifier = modifier
                        .paint(
                            // TODO bottom_sheet_background this need to replace
                            painterResource(id = R.drawable.bottom_sheet_background),
                            contentScale = ContentScale.FillBounds
                        ), contentAlignment = Alignment.TopEnd

                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 36.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 20.dp, start = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier
                                    .width(75.dp)
                                    .height(64.dp),
                                painter = painterResource(id = R.drawable.ic_1),
                                contentDescription = ""
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            XperiTitleMedium(
                                "ESPN",
                                colorResource(id = R.color.tivo_dark_on_surface)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            XperiBodySmall(
                                title = "Channel 8681 ESPN",
                                colorResource(id = R.color.tivo_dark_on_surface_varient)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(id = R.color.tivo_dark_on_surface_varient))
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "HD",
                                color = colorResource(id = R.color.tivo_dark_on_surface_varient),
                                fontSize = 12.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(
                                        width = 1.dp, color = colorResource(
                                            id = R.color.tivo_dark_outline
                                        )
                                    )
                                    .padding(
                                        start = 6.dp,
                                        end = 6.dp,
                                        top = 2.dp,
                                        bottom = 2.dp
                                    )
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp, bottom = 18.dp, start = 24.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_status_not_recordable),
                                contentDescription = ""
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            XperiBodyMedium(
                                title = "This channel does not allow recording",
                                colorResource(id = R.color.tivo_dark_on_surface_varient)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 16.dp, start = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_view_schedule),
                                contentDescription = ""
                            )
                            Spacer(modifier = Modifier.width(19.dp))
                            XperiBodyLarge(
                                title = "See schedule and shows", textColor = colorResource(
                                    id = R.color.tivo_dark_on_surface
                                )
                            )
                        }
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.3.dp)
                                .padding(start = 8.dp, end = 8.dp)
                                .background(color = colorResource(id = R.color.tivo_dark_outline_opacity))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_watch_liveplay),
                                contentDescription = ""
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            XperiBodyLarge(
                                title = "Watch live", textColor = colorResource(
                                    id = R.color.tivo_dark_on_surface
                                )
                            )
                        }
                    }
                }
            },
        ) {
            if (channelSheetState.isVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                }
            }
        }
    }

    @Composable
    fun XperiTitleLarge(title: String, textColor: Color) {
        Text(
            text = title,
            fontSize = 22.sp,
            color = textColor,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
        )
    }

    @Composable
    fun XperiTitleMedium(title: String, textColor: Color) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = textColor,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
        )
    }

    @Composable
    fun XperiBodyLarge(title: String, textColor: Color) {
        Text(
            text = title, fontSize = 16.sp, color = textColor, fontFamily = fontFamily,
            fontWeight = FontWeight.Normal
        )
    }

    @Composable
    fun XperiBodyMedium(title: String, textColor: Color) {
        Text(
            text = title, fontSize = 14.sp, color = textColor,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
        )
    }

    @Composable
    fun XperiBodySmall(title: String, textColor: Color) {
        Text(
            text = title, fontSize = 12.sp, color = textColor, fontFamily = fontFamily,
            fontWeight = FontWeight.Normal
        )
    }

    private fun getIconDrawable(rowNumber: Int): Int =
        when (rowNumber % 10) {
            1 -> R.drawable.ic_1
            2 -> R.drawable.ic_2
            3 -> R.drawable.ic_3
            4 -> R.drawable.ic_4
            5 -> R.drawable.ic_5
            6 -> R.drawable.ic_6
            7 -> R.drawable.ic_7
            8 -> R.drawable.ic_8
            9 -> R.drawable.ic_9
            else -> R.drawable.ic_10
        }

    private fun getProgramName(rowNumber: Int, timeslotNumber: Int): String =
        when ((rowNumber + 1) * (Math.abs(timeslotNumber) + 1) % 10) {
            1 -> "Golden State Warrior"
            2 -> "La Reina Del Sur"
            3 -> "Mi Camino Es Amarte"
            4 -> "Roads War"
            5 -> "DateLine"
            6 -> "Nature Cat"
            7 -> "Donkey Hodie"
            8 -> "In the Heart of the Night"
            9 -> "Moon 101"
            else -> "NBA Today"
        }


    private var channelProgramData: ArrayList<ChannelProgramData> = ArrayList()

    private fun initialize() {
        loadLargeData()
    }

    private fun loadLargeData() {
        for (row in 0..maxRows - 1) {
            val name = "C$row"
            val programList: ArrayList<CellItemData> = ArrayList()
            // for (col in minColumns..maxColumns) {
            // for (col in 0..1) { // making less as this is not used now
            //  if (row % 4 == 0 && col % 2 != 0) {
            //      programList.add(CellItemData(String.format("P-%d-%d", row, col), 15))
            // } else {
            //   programList.add(CellItemData(String.format("P-%d-%d", row, col), 30))
            //  }
            //  }
            channelProgramData.add(ChannelProgramData(name, programList))
        }
        /* for (t in minColumns..maxColumns) {
             timeslots.add(CellItemData("T$t", 30))
         }*/
    }

    private fun isPhoneUi(): Boolean {
        return applicationContext.resources.getBoolean(R.bool.is_phone)
    }

    private fun isPortrait(): Boolean {
        return applicationContext.resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun pageCountOnScreen(): Int {
        var count = 1
        if (!isPhoneUi()) {
            if (isPortrait()) count = 2 else count = 3
        }
        return count
    }
}


val TOP_BAR_HEIGHT = 56.dp
val LazyListState.isScrolled: Boolean
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

data class ChannelProgramData(
    val name: String,
    val programList: ArrayList<CellItemData>
)

data class CellItemData(
    val name: String,
    val duration: Int
)