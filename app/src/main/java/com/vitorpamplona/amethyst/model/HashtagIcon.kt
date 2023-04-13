package com.vitorpamplona.amethyst.model
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.R
fun checkForHashtagWithIcon(tag: String): HashtagIcon? {
    return when (tag.lowercase()) {
        "bitcoin", "btc", "timechain" -> HashtagIcon(R.drawable.ht_btc, "Bitcoin", Color.Unspecified, Modifier.padding(2.dp, 2.dp, 0.dp, 0.dp))
        "nostr", "nostrich", "nostriches" -> HashtagIcon(R.drawable.ht_nostr, "Nostr", Color.Unspecified, Modifier.padding(1.dp, 2.dp, 0.dp, 0.dp))
        "lightning", "lightningnetwork" -> HashtagIcon(R.drawable.ht_lightning, "Lightning", Color.Unspecified, Modifier.padding(1.dp, 3.dp, 0.dp, 0.dp))
        "zap", "zaps", "zapper", "zappers", "zapping", "zapped", "zapathon", "zapraiser", "zaplife", "zapchain" -> HashtagIcon(R.drawable.zap, "Zap", Color.Unspecified, Modifier.padding(1.dp, 3.dp, 0.dp, 0.dp))
        "amethyst" -> HashtagIcon(R.drawable.amethyst, "Amethyst", Color.Unspecified, Modifier.padding(3.dp, 2.dp, 0.dp, 0.dp))
        "plebs", "pleb", "plebchain" -> HashtagIcon(R.drawable.plebs, "Pleb", Color.Unspecified, Modifier.padding(2.dp, 2.dp, 0.dp, 1.dp))
        "coffee", "coffeechain", "cafe" -> HashtagIcon(R.drawable.coffee, "Coffee", Color.Unspecified, Modifier.padding(2.dp, 2.dp, 0.dp, 0.dp))
        "skullofsatoshi" -> HashtagIcon(R.drawable.skull, "SkullofSatoshi", Color.Unspecified, Modifier.padding(2.dp, 1.dp, 0.dp, 0.dp))
        "foodstr", "pizza" -> HashtagIcon(R.drawable.foodstr, "Foodstr", Color.Unspecified, Modifier.padding(2.dp, 1.dp, 0.dp, 0.dp))
        "dogstr", "npups" -> HashtagIcon(R.drawable.dogstr, "Dogstr", Color.Unspecified, Modifier.padding(0.dp, 1.dp, 0.dp, 0.dp))
        "catstr", "catestr" -> HashtagIcon(R.drawable.catstr, "Catstr", Color.Unspecified, Modifier.padding(1.dp, 4.dp, 0.dp, 1.dp))
        "footstr", "pawstr" -> HashtagIcon(R.drawable.footstr, "Footstr", Color.Unspecified, Modifier.padding(1.dp, 1.dp, 0.dp, 0.dp))
        "bookstr" -> HashtagIcon(R.drawable.bookstr, "Bookstr", Color.Unspecified, Modifier.padding(1.dp, 0.dp, 0.dp, 0.dp))
        "babechain", "kiss" -> HashtagIcon(R.drawable.kiss, "Babechain", Color.Unspecified, Modifier.padding(1.dp, 0.dp, 0.dp, 0.dp))
        "grownostr", "#permies", "#perma", "#permaculture", "#gardening", "#garden" -> HashtagIcon(R.drawable.grownostr, "GrowNostr", Color.Unspecified, Modifier.padding(0.dp, 1.dp, 0.dp, 1.dp))

        else -> null
    }
}
class HashtagIcon(
    val icon: Int,
    val description: String,
    val color: Color,
    val modifier: Modifier
)
