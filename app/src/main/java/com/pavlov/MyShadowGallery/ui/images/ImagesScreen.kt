package com.pavlov.MyShadowGallery.ui.images

import com.pavlov.MyShadowGallery.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.*
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesScreen(
    itemLoaderScreen: @Composable () -> Unit,
    extractedImagesScreen: @Composable () -> Unit,
    viewModel: ImagesViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val receivedImages by viewModel.receivedFromOutside.collectAsState()
    val tempImages by viewModel.tempImages.collectAsState()

    LaunchedEffect(tempImages) {
        if (tempImages.isNotEmpty()) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .padding(top = 0.dp)
                    .height(60.dp),
                backgroundColor = Color.Black,
                contentColor = Color.White,
            ) {

                @Composable
                fun createTab(title: String, page: Int) {
                    Tab(
                        selected = pagerState.currentPage == page,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                createTab(stringResource(id = R.string.uploaded_by_me), 0)
                createTab(stringResource(id = R.string.received_from_outside), 1)
            }

            HorizontalPager(
                count = 2,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> itemLoaderScreen()
                    1 -> extractedImagesScreen()
                }
            }
        }
    }
}
