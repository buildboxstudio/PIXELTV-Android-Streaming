package com.idlix.tv.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.idlix.tv.data.model.MovieItem
import com.idlix.tv.ui.theme.NetflixWhite

@Composable
fun TvSwimlaneRow(
    title: String,
    items: List<MovieItem>,
    modifier: Modifier = Modifier,
    onItemFocus: ((MovieItem) -> Unit)? = null,
    onItemClick: (MovieItem) -> Unit
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = NetflixWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)
        ) {
            items(items, key = { it.id }) { movie ->
                TvMovieCard(
                    title = movie.title,
                    posterUrl = movie.fullPosterUrl,
                    rating = movie.formattedRating,
                    quality = movie.quality,
                    modifier = Modifier.padding(end = 12.dp),
                    onFocus = { onItemFocus?.invoke(movie) },
                    onClick = { onItemClick(movie) }
                )
            }
        }
    }
}
