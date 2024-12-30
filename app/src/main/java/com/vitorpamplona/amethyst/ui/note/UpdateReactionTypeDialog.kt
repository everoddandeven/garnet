/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.note

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.AddressableNote
import com.vitorpamplona.amethyst.service.firstFullChar
import com.vitorpamplona.amethyst.ui.actions.CloseButton
import com.vitorpamplona.amethyst.ui.actions.SaveButton
import com.vitorpamplona.amethyst.ui.components.InLineIconRenderer
import com.vitorpamplona.amethyst.ui.navigation.routeFor
import com.vitorpamplona.amethyst.ui.note.types.RenderEmojiPack
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.theme.ButtonBorder
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import com.vitorpamplona.quartz.encoders.ATag
import com.vitorpamplona.quartz.encoders.Nip30CustomEmoji
import com.vitorpamplona.quartz.events.EmojiPackSelectionEvent
import com.vitorpamplona.quartz.events.EmojiUrl
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class UpdateReactionTypeViewModel(val account: Account) : ViewModel() {
    var nextChoice by mutableStateOf(TextFieldValue(""))
    var reactionSet by mutableStateOf(listOf<String>())

    fun load() {
        this.reactionSet = account.reactionChoices
    }

    fun toListOfChoices(commaSeparatedAmounts: String): List<Long> {
        return commaSeparatedAmounts.split(",").map { it.trim().toLongOrNull() ?: 0 }
    }

    fun addChoice() {
        val newValue = nextChoice.text.trim().firstFullChar()
        reactionSet = reactionSet + newValue

        nextChoice = TextFieldValue("")
    }

    fun addChoice(customEmoji: EmojiUrl) {
        reactionSet = reactionSet + (customEmoji.encode())
    }

    fun removeChoice(reaction: String) {
        reactionSet = reactionSet - reaction
    }

    fun sendPost() {
        account.changeReactionTypes(reactionSet)
        nextChoice = TextFieldValue("")
    }

    fun cancel() {
        nextChoice = TextFieldValue("")
    }

    fun hasChanged(): Boolean {
        return reactionSet != account.reactionChoices
    }

    class Factory(val account: Account) : ViewModelProvider.Factory {
        override fun <UpdateReactionTypeViewModel : ViewModel> create(modelClass: Class<UpdateReactionTypeViewModel>): UpdateReactionTypeViewModel {
            return UpdateReactionTypeViewModel(account) as UpdateReactionTypeViewModel
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateReactionTypeDialog(
    onClose: () -> Unit,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    val postViewModel: UpdateReactionTypeViewModel =
        viewModel(
            key = "UpdateReactionTypeViewModel",
            factory = UpdateReactionTypeViewModel.Factory(accountViewModel.account),
        )

    LaunchedEffect(accountViewModel) { postViewModel.load() }

    Dialog(
        onDismissRequest = { onClose() },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(10.dp).imePadding(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CloseButton(
                        onPress = {
                            postViewModel.cancel()
                            onClose()
                        },
                    )

                    SaveButton(
                        onPost = {
                            postViewModel.sendPost()
                            onClose()
                        },
                        isActive = postViewModel.hasChanged(),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.animateContentSize()) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    postViewModel.reactionSet.forEach { reactionType ->
                                        RenderReactionOption(reactionType, postViewModel)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                label = { Text(text = stringResource(R.string.new_reaction_symbol)) },
                                value = postViewModel.nextChoice,
                                onValueChange = { postViewModel.nextChoice = it },
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        capitalization = KeyboardCapitalization.None,
                                        keyboardType = KeyboardType.Text,
                                    ),
                                placeholder = {
                                    Text(
                                        text = "\uD83D\uDCAF, \uD83C\uDF89, \uD83D\uDC4E",
                                        color = MaterialTheme.colorScheme.placeholderText,
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.padding(end = 10.dp).weight(1f),
                            )

                            Button(
                                onClick = { postViewModel.addChoice() },
                                shape = ButtonBorder,
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                            ) {
                                Text(text = stringResource(R.string.add), color = Color.White)
                            }
                        }
                    }
                }

                EmojiSelector(
                    accountViewModel = accountViewModel,
                    nav = nav,
                ) {
                    postViewModel.addChoice(it)
                }
            }
        }
    }
}

@Composable
private fun RenderReactionOption(
    reactionType: String,
    postViewModel: UpdateReactionTypeViewModel,
) {
    Button(
        modifier = Modifier.padding(horizontal = 3.dp),
        shape = ButtonBorder,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        onClick = { postViewModel.removeChoice(reactionType) },
        contentPadding = PaddingValues(horizontal = 5.dp),
    ) {
        if (reactionType.startsWith(":")) {
            val noStartColon = reactionType.removePrefix(":")
            val url = noStartColon.substringAfter(":")

            val renderable =
                persistentListOf(
                    Nip30CustomEmoji.ImageUrlType(url),
                    Nip30CustomEmoji.TextType(" ✖"),
                )

            InLineIconRenderer(
                renderable,
                style = SpanStyle(color = Color.White),
                maxLines = 1,
            )
        } else {
            when (reactionType) {
                "+" -> {
                    Icon(
                        painter = painterResource(R.drawable.ic_liked),
                        null,
                        modifier = remember { Modifier.size(16.dp) },
                        tint = Color.White,
                    )
                    Text(
                        text = " ✖",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
                "-" ->
                    Text(
                        text = "\uD83D\uDC4E ✖",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                else ->
                    Text(
                        text = "$reactionType ✖",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
            }
        }
    }
}

@Composable
private fun EmojiSelector(
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    onClick: ((EmojiUrl) -> Unit)? = null,
) {
    LoadAddressableNote(
        aTag =
            ATag(
                EmojiPackSelectionEvent.KIND,
                accountViewModel.userProfile().pubkeyHex,
                "",
                null,
            ),
        accountViewModel,
    ) { emptyNote ->
        emptyNote?.let { usersEmojiList ->
            val collections by
                usersEmojiList
                    .live()
                    .metadata
                    .map { (it.note.event as? EmojiPackSelectionEvent)?.taggedAddresses()?.toImmutableList() }
                    .distinctUntilChanged()
                    .observeAsState(
                        (usersEmojiList.event as? EmojiPackSelectionEvent)
                            ?.taggedAddresses()
                            ?.toImmutableList(),
                    )

            collections?.let { EmojiCollectionGallery(it, accountViewModel, nav, onClick) }
        }
    }
}

@Composable
fun EmojiCollectionGallery(
    emojiCollections: ImmutableList<ATag>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    onClick: ((EmojiUrl) -> Unit)? = null,
) {
    val color = MaterialTheme.colorScheme.background
    val bgColor = remember { mutableStateOf(color) }

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
    ) {
        itemsIndexed(emojiCollections, key = { _, item -> item.toTag() }) { _, item ->
            LoadAddressableNote(aTag = item, accountViewModel) {
                it?.let { WatchAndRenderNote(it, bgColor, accountViewModel, nav, onClick) }
            }
        }
    }
}

@Composable
private fun WatchAndRenderNote(
    emojiPack: AddressableNote,
    bgColor: MutableState<Color>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
    onClick: ((EmojiUrl) -> Unit)?,
) {
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxWidth().clickable {
            scope.launch { routeFor(emojiPack, accountViewModel.userProfile())?.let { nav(it) } }
        },
    ) {
        RenderEmojiPack(
            baseNote = emojiPack,
            actionable = false,
            backgroundColor = bgColor,
            accountViewModel = accountViewModel,
            onClick = onClick,
        )
    }
}
