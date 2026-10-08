package com.subtrack.app.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(val title: String, val description: String)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage("Добавь свои подписки", "Вручную, за 30 секунд. Укажи название, цену и дату списания."),
        OnboardingPage("Следи за расходами", "Не забывай про пробные периоды — мы напомним, когда они закончатся."),
        OnboardingPage("Твои данные — только у тебя", "Мы не подключаемся к банку и не продаём твою информацию.")
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    pages[page].title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    pages[page].description,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            Modifier.padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { i ->
                val color = if (i == pagerState.currentPage)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .background(color, CircleShape)
                )
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage == pages.size - 1) onFinish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (pagerState.currentPage == pages.size - 1) "Начать" else "Далее")
        }
    }
}
