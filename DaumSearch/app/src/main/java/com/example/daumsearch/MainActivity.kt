package com.example.daumsearch

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.SparseArray
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import coil.compose.AsyncImage
import com.example.daumsearch.model.blog.BlogDocument
import com.example.daumsearch.model.image.ImageDocument
import com.example.daumsearch.model.video.VideoDocument
import com.example.daumsearch.nav.MainRoute
import com.example.daumsearch.nav.WebViewRoute
import com.example.daumsearch.ui.theme.DaumSearchTheme
import com.example.daumsearch.ui.theme.MyYellow
import com.example.daumsearch.ui.theme.Purple500
import com.example.daumsearch.viewmodel.SearchViewModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

val LocalSearchViewModel = staticCompositionLocalOf<SearchViewModel> {
    error("No MusicViewModel provided")
}

val LocalNavController = staticCompositionLocalOf<NavController?> { null }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalSearchViewModel provides searchViewModel) {
                DaumSearchTheme {
                    AppNavigation()
                }
            }

        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DaumSearchTheme {
        Greeting("Android")
    }
}



@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = MainRoute.route
        ) {
            composable(MainRoute.route) {
                MainScreen()
            }
            composable(
                route = "${WebViewRoute.route}/{url}",
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url")
                println(url)
                if (url != null) {
                    WebViewScreen(url)
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        RoundedSearchBar()
        TabRow()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RoundedSearchBar() {
    val searchViewModel = LocalSearchViewModel.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by rememberSaveable  { mutableStateOf("") }
    var showPlaceholder by rememberSaveable { mutableStateOf(true) }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        value = searchQuery,
        onValueChange = { newValue ->
            searchQuery = newValue
            showPlaceholder = newValue.isEmpty()
        },
        placeholder = { if (showPlaceholder) Text(text = "검색어를 입력해주세요.") },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MyYellow, // 포커스 상태일 때 외곽선 색상
            unfocusedBorderColor = MyYellow, // 포커스가 없는 상태일 때 외곽선 색상
            backgroundColor = Color.White, // 배경색
            cursorColor = Color.Black, // 커서 색상

        ),
        singleLine = true,
        leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier.size(24.dp),
            )
        },

        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            softwareKeyboardController?.hide()
            searchViewModel.fetchData(searchQuery)
        })

    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabRow() {
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.fillMaxWidth()
    ) {
        Tab(
            selected = pagerState.currentPage == 0,
            onClick = { coroutineScope.launch { pagerState.scrollToPage(0) }},
            text = { Text("비디오") }
        )
        Tab(
            selected = pagerState.currentPage == 1,
            onClick = { coroutineScope.launch { pagerState.scrollToPage(1) }},
            text = { Text("블로그") }
        )

        Tab(
            selected = pagerState.currentPage == 2,
            onClick = { coroutineScope.launch { pagerState.scrollToPage(2) }},
            text = { Text("이미지") }
        )
    }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), pageCount = 3) { page ->
        when (page) {
            0 -> VideoScreen()
            1 -> BlogScreen()
            2 -> ImageScreen()
        }
    }
}

@Composable
fun VideoItem(video: VideoDocument) {
    val navController = LocalNavController.current
    val showVideoPlayer = rememberSaveable{ mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                val encodedUrl = URLEncoder.encode(video.url, StandardCharsets.UTF_8.toString())
                navController?.navigate(WebViewRoute.route + "/${encodedUrl}")
            },
    ) {
        Row {
            AsyncImage(
                modifier = Modifier
                    .size(width = 100.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                model = video.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = video.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "게시자 : ${video.author}",
                            fontSize = 10.sp,
                        )
                        Text(
                            text = "조회수 : ${video.play_time}",
                            fontSize = 10.sp,
                        )
                        Text(
                            text = "게시일자 : ${video.datetime.slice(0..9)}",
                            fontSize = 10.sp,
                        )
                    }

                    if (video.url.contains("youtube")) {
                        Button(
                            onClick = {
                                showVideoPlayer.value = !showVideoPlayer.value
                            },
                            modifier = Modifier
                                .padding(8.dp)
                        ) {
                            Text(text = if (!showVideoPlayer.value) "미리보기" else "접기")
                        }
                    }
                }
            }
        }

        if (showVideoPlayer.value) {
            VideoPlayer(video.url)
        }
    }
}

@Composable
fun VideoScreen() {
    val searchViewModel = LocalSearchViewModel.current
    val videos = searchViewModel.videosFlow.collectAsLazyPagingItems()


    if (videos.itemCount == 0) {
        NoSearchResult()
    }
    else
        LazyColumn {
            items(videos) { video ->
                if (video != null) {
                    VideoItem(video)
                }
            }
        }
}

@Composable
fun VideoPlayer(url: String) {
    val context = LocalContext.current

    val exoPlayer = remember { SimpleExoPlayer.Builder(context).build() }
    val playerView = remember { PlayerView(context) }

    DisposableEffect(Unit) {

        object : YouTubeExtractor(context) {
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, vMeta: VideoMeta?) {
                val itag = 22
                val videoPath = ytFiles?.get(itag)?.url

                if (videoPath != null) {
                    exoPlayer.setMediaItem(MediaItem.fromUri(videoPath))
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }

        }.extract(url, true, true)

        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { playerView.apply { player = exoPlayer } },
        modifier = Modifier.fillMaxWidth().height(250.dp)
    )


}

@Composable
fun BlogItem(blog: BlogDocument) {
    val navController = LocalNavController.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable {
            val encodedUrl = URLEncoder.encode(blog.url, StandardCharsets.UTF_8.toString())
            navController?.navigate(WebViewRoute.route + "/${encodedUrl}")
        },
        verticalAlignment = Alignment.CenterVertically

    ) {
        AsyncImage(
            modifier = Modifier
                .size(width = 100.dp, height = 80.dp)
                .clip(RoundedCornerShape(8.dp)),
            model = blog.thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = blog.title.toColoredText(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,)

            Spacer(modifier = Modifier.height(5.dp))

            Column {
                Text(
                    text = blog.contents.toColoredText(),
                    fontSize = 10.sp,)
                Text(
                    text = blog.blogname,
                    fontSize = 10.sp,)
                Text(
                    text = "게시일자 : ${blog.datetime.slice(0..9)}",
                    fontSize = 10.sp,)
            }
        }
    }
}

@Composable
fun BlogScreen() {
    val searchViewModel = LocalSearchViewModel.current
    val blogs = searchViewModel.blogsFlow.collectAsLazyPagingItems()


    if (blogs.itemCount == 0) {
        NoSearchResult()
    }
    else
        LazyColumn {
            items(blogs) { blog ->
                if (blog != null) {
                    BlogItem(blog)
                }
            }
        }
}

@Composable
fun ImageItem(image: ImageDocument) {
    val navController = LocalNavController.current

    Box(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable {
                val encodedUrl = URLEncoder.encode(image.doc_url, StandardCharsets.UTF_8.toString())
                navController?.navigate(WebViewRoute.route + "/${encodedUrl}")
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .size(width = 100.dp, height = 100.dp)
                .clip(RoundedCornerShape(8.dp)),
            model = image.thumbnail_url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun ImageScreen() {
    val searchViewModel = LocalSearchViewModel.current
    val images = searchViewModel.imagesFlow.collectAsLazyPagingItems()


    if (images.itemCount == 0) {
        NoSearchResult()
    }
    else {
        LazyVerticalGrid(
            columns  = GridCells.Fixed(3)
        ) {
            items(images.itemCount) { index ->
                images[index]?.let {
                    ImageItem(it)
                }
            }
        }
    }
//    else
//        LazyColumn {
//            items(images) { image ->
//                if (image != null) {
//                    ImageItem(image)
//                }
//            }
//        }
}

@Composable
fun NoSearchResult() {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "검색 결과가 없습니다.")
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    AndroidView(
        factory = {
            webView.apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack() // 이전 페이지로 이동
        } else {
            navController?.popBackStack() // 이전 페이지가 없으면 뒤로가기 동작
        }
    }
}

fun String.toColoredText() = buildAnnotatedString {

    val pattern = Pattern.compile("<b>(.*?)</b>")
    val matcher = pattern.matcher(this@toColoredText)

    var currentPosition = 0

    while (matcher.find()) {
        val matchStart = matcher.start()
        val matchEnd = matcher.end()

        // 일반 텍스트 추가
        append(this@toColoredText.substring(currentPosition, matchStart))

        // 스타일이 적용된 텍스트 추가
        withStyle(style = SpanStyle(color = Purple500)) {
            append(this@toColoredText.substring(matchStart + 3, matchEnd - 4))
        }

        currentPosition = matchEnd
    }

    // 나머지 일반 텍스트 추가
    append(this@toColoredText.substring(currentPosition))
}