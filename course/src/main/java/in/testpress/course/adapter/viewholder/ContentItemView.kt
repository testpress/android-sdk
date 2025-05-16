package `in`.testpress.course.adapter.viewholder

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.util.TestpressFont
import `in`.testpress.course.util.extension.getContentImage
import `in`.testpress.util.DateUtils

@Composable
fun RunningContentItemView(content: DomainContent) {
    val startDate = DateUtils.convertDateFormat(content.start)
    val endDate = content.end?.let { " - ${DateUtils.convertDateFormat(it)}" } ?: ""
    val dateString = "$startDate$endDate"
    ContentItemLayout(content = content, dateString = dateString)
}

@Composable
fun UpcomingContentItemView(content: DomainContent) {
    val dateString = "Available ${
        DateUtils.getRelativeTimeString(content.start, LocalContext.current)
    }"
    ContentItemLayout(content = content, dateString = dateString)
}

@Composable
private fun ContentItemLayout(
    content: DomainContent,
    dateString: String
) {
    val context = LocalContext.current
    val fontFamily = TestpressFont.getRubikMediumFont(context.assets)

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Image(
            painter = painterResource(id = content.getContentImage()),
            contentDescription = content.contentType,
            modifier = Modifier
                .size(72.dp)
                .padding(16.dp)
        )
        Column {
            Text(
                text = "${content.title}",
                color = Color.Black,
                fontFamily = fontFamily,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = dateString,
                color = colorResource(id = R.color.testpress_text_gray),
                fontFamily = fontFamily,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${content.treePath}",
                color = colorResource(id = R.color.testpress_text_gray),
                fontFamily = fontFamily,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview
@Composable
fun RunningContentItemViewPreview() {
    val content = DomainContent(
        id = 0,
        title = "Big Buck Bunny",
        treePath = "Course 1 > Chapter 1 > Sub Chapter",
        start = "2025-05-08T13:21:58Z",
        end = "2025-05-15T13:21:58Z",
        isLocked = null,
        isScheduled = null, active = null,
        hasStarted = null,
        isCourseAvailable = null,
        hasEnded = null
    )
    RunningContentItemView(content)
}

@Preview
@Composable
fun UpcomingContentItemViewPreview() {
    val content = DomainContent(
        id = 0,
        title = "Big Buck Bunny",
        treePath = "Course 1 > Chapter 1 > Sub Chapter",
        start = "2025-05-18T13:21:58Z",
        end = "2025-05-15T13:21:58Z",
        isLocked = null,
        isScheduled = null, active = null,
        hasStarted = null,
        isCourseAvailable = null,
        hasEnded = null
    )
    UpcomingContentItemView(content)
}