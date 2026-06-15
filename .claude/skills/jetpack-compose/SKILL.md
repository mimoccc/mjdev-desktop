---
name: jetpack-compose
description: Write idiomatic Jetpack Compose UI — stateless composables, state hoisting, slot API, Modifier ordering, side effects, previews, accessibility, and custom layouts
argument-hint: "<component or screen to build>"
user-invocable: true
allowed-tools: ["Read", "Write", "Edit", "Glob", "Grep"]
---

# Jetpack Compose — Best Practices

## Composable Design

### Stateless vs Stateful — Always Hoist State

```kotlin
// Stateless — preferred: reusable, testable, previewable
@Composable
fun WordCard(
    word: Word,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) { ... }

// Stateful — only at ViewModel boundary
// viewModel.state() is a project-level extension:
//   fun <S> ViewModel.state(): State<S> = uiState.collectAsStateWithLifecycle()
@Composable
fun WordCardScreen() {
    val viewModel = koinViewModel<WordCardViewModel>()
    val state by viewModel.state()
    WordCard(
        word      = state.word,
        isFlipped = state.isFlipped,
        onFlip    = viewModel::flip,
        onDelete  = viewModel::delete,
    )
}
```

**State hoisting rule**: hoist state to the lowest common ancestor of all composables that need to read or write it.

### Slot API

Design containers with content slots instead of bloated parameter lists:

```kotlin
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    header: @Composable (() -> Unit)? = null,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            header?.invoke()
            content()
            footer?.invoke()
        }
    }
}

// Usage — flexible without combinatorial parameter explosion
SectionCard(
    header = { Text("Recent Words", style = MaterialTheme.typography.titleMedium) },
    footer = { TextButton(onClick = { }) { Text("See all") } },
) {
    words.forEach { WordRow(it) }
}
```

---

## Modifier

### Always Accept `modifier: Modifier = Modifier`

Every composable that renders UI must accept and forward a modifier parameter:

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,   // always last named param before content
    enabled: Boolean = true,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text)
    }
}
```

### Modifier Ordering Matters

Applied left-to-right — order changes visual and touch behaviour:

```kotlin
// Clickable area INCLUDES the padding
Modifier
    .clickable { onClick() }
    .padding(16.dp)

// Padding is OUTSIDE the clickable area
Modifier
    .padding(16.dp)
    .clickable { onClick() }

// Canonical ordering for a card-like component
Modifier
    .fillMaxWidth()
    .clip(RoundedCornerShape(12.dp))
    .background(containerColor)
    .clickable { onClick() }
    .padding(horizontal = 16.dp, vertical = 12.dp)
```

---

## Lazy Lists

Always supply `key` and `contentType` to help the Compose runtime skip unnecessary work:

```kotlin
LazyColumn {
    items(
        items       = words,
        key         = { it.id },          // stable identity — enables animated reordering
        contentType = { "word" },         // same type → runtime reuses composition nodes
    ) { word ->
        WordRow(
            word     = word,
            modifier = Modifier.animateItem(),  // smooth add/remove animations for free
        )
    }
}

// key() in non-lazy forEach — same principle
Column {
    words.forEach { word ->
        key(word.id) {
            WordRow(word)
        }
    }
}
```

Rules:
- `key` must be **stable and unique** — avoid array indices as keys
- Omitting `key` forces the runtime to diff by position; inserting at the top recomposes everything
- `contentType` matters when a list mixes heterogeneous item types (headers, ads, rows)

---

## State Management

### remember and Keys

```kotlin
// No key — computed once
val painter = remember { loadPainter(url) }

// Key — recomputed when id changes
val formatted = remember(id) { expensiveFormat(id) }

// Multiple keys
val result = remember(a, b) { compute(a, b) }
```

### derivedStateOf — Avoid Redundant Recomposition

```kotlin
// Only recomposes when the boolean flips, not on every scroll delta
val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

// Multiple conditions
val isFormValid by remember {
    derivedStateOf { name.isNotBlank() && email.contains("@") && password.length >= 8 }
}
```

### rememberSaveable — Survive Process Death

```kotlin
// Survives recomposition AND process death (uses Bundle)
var searchQuery by rememberSaveable { mutableStateOf("") }

// Custom saver for complex types
var selection by rememberSaveable(stateSaver = SelectionSaver) { mutableStateOf(Selection.None) }
```

### Deferred State Reads — Lambda Modifiers

Reading state inside a lambda modifier defers the read to the draw/layout phase, bypassing recomposition entirely for continuous animations:

```kotlin
// BAD — state read triggers recomposition on every frame
val offsetY by animateFloatAsState(if (expanded) 0f else -100f)
Box(Modifier.offset(y = offsetY.dp))

// GOOD — state read deferred to layout phase, no recomposition
val offsetY by animateFloatAsState(if (expanded) 0f else -100f)
Box(Modifier.offset { IntOffset(0, offsetY.roundToInt()) })

// Same pattern for graphicsLayer
Box(Modifier.graphicsLayer {
    alpha       = alphaState.value
    scaleX      = scaleState.value
    scaleY      = scaleState.value
    translationY = translationState.value
})
```

---

## Stability — @Stable, @Immutable, ImmutableList

The Compose compiler skips recomposition only if it can prove all parameters are **stable**. Unstable parameters force recomposition even when values haven't changed.

```kotlin
// @Immutable — all public properties are deeply immutable; compiler treats as stable
@Immutable
data class Word(val id: String, val text: String, val definition: String)

// @Stable — mutable but notifies Compose when values change (e.g., custom observable)
@Stable
class WordSelection {
    var selectedId by mutableStateOf<String?>(null)
}

// ImmutableList — List<T> is unstable because its interface allows mutation
// Use kotlinx.collections.immutable
@Immutable
data class WordListState(
    val words: ImmutableList<Word>,    // stable ✓
    val isLoading: Boolean,
)

// Building ImmutableList
val state = WordListState(
    words     = words.toImmutableList(),
    isLoading = false,
)
```

Rules:
- `data class` with only `val` primitive/`@Immutable` properties is automatically stable — no annotation needed
- `List<T>`, `Map<K, V>` are **never stable** — always wrap with `ImmutableList` / `ImmutableMap` in state classes
- Use the Compose compiler metrics (`./gradlew assembleRelease -PcomposeCompilerReports=true`) to verify stability

---

## Side Effects

| Effect | Use case |
|---|---|
| `LaunchedEffect(key)` | Launch coroutine, re-launch when key changes |
| `DisposableEffect(key)` | Register + cleanup (listeners, observers) |
| `SideEffect` | Sync Compose state to non-Compose system every recomposition |
| `rememberUpdatedState` | Capture latest value inside long-lived effect |
| `produceState` | Convert callback/non-Compose source to State |
| `snapshotFlow` | Convert Compose `State` to `Flow` |

```kotlin
// LaunchedEffect — load data when id changes
LaunchedEffect(wordId) {
    viewModel.loadWord(wordId)
}

// DisposableEffect — register/unregister lifecycle observer
DisposableEffect(lifecycle) {
    val observer = LifecycleEventObserver { _, event -> onEvent(event) }
    lifecycle.addObserver(observer)
    onDispose { lifecycle.removeObserver(observer) }
}

// rememberUpdatedState — always use latest callback in a running effect
val currentOnTimeout by rememberUpdatedState(onTimeout)
LaunchedEffect(Unit) {
    delay(5_000)
    currentOnTimeout()   // calls latest lambda, not the one captured at launch
}

// produceState — wrap callback API
val bitmap by produceState<Bitmap?>(initialValue = null, url) {
    value = loadBitmapAsync(url)
}
```

---

## ViewModel Effects — One-Shot Events

**Never** use `LaunchedEffect` to observe a navigation or snackbar event — it can fire multiple times on recomposition. Use an `OnEvents` pattern that consumes each event exactly once:

```kotlin
// In ViewModel — emit to SharedFlow
class WordCardViewModel : ViewModel() {
    private val _effects = MutableSharedFlow<WordEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<WordEffect> = _effects.asSharedFlow()

    fun delete() {
        viewModelScope.launch {
            repository.delete(state.word.id)
            _effects.emit(WordEffect.NavigateBack)
        }
    }
}

sealed interface WordEffect {
    data object NavigateBack : WordEffect
    data class ShowError(val message: String) : WordEffect
}

// In composable — collect effects exactly once
@Composable
fun WordCardScreen(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val viewModel = koinViewModel<WordCardViewModel>()

    // OnEvents: collect SharedFlow without a key so it never re-subscribes
    val effects = viewModel.effects
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                WordEffect.NavigateBack        -> onNavigateBack()
                is WordEffect.ShowError        -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val state by viewModel.state()
    WordCard(state = state, onDelete = viewModel::delete)
}
```

---

## Scaffold + SnackbarHost

The canonical screen skeleton — always wire `SnackbarHostState` through the call stack, not via `CompositionLocal`:

```kotlin
@Composable
fun WordListScreen(onWordClick: (String) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel = koinViewModel<WordListViewModel>()

    // Consume effects (see ViewModel Effects section)
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WordListEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Words") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::addWord) {
                Icon(Icons.Default.Add, contentDescription = "Add word")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val state by viewModel.state()
        WordList(
            words       = state.words,
            onWordClick = onWordClick,
            modifier    = Modifier.padding(innerPadding),
        )
    }
}
```

---

## Animation

Prefer high-level animation APIs before reaching for `Animatable` or `Transition`:

```kotlin
// AnimatedVisibility — show/hide with enter/exit transitions
AnimatedVisibility(
    visible = showBanner,
    enter   = fadeIn() + expandVertically(),
    exit    = fadeOut() + shrinkVertically(),
) {
    ErrorBanner(message)
}

// animateContentSize — smooth size changes without measuring manually
Column(Modifier.animateContentSize()) {
    Text(text, maxLines = if (expanded) Int.MAX_VALUE else 2)
    TextButton(onClick = { expanded = !expanded }) {
        Text(if (expanded) "Show less" else "Show more")
    }
}

// animateFloatAsState — single value animation driven by state
val alpha by animateFloatAsState(
    targetValue = if (isLoading) 0.4f else 1f,
    label       = "content alpha",
)
Box(Modifier.graphicsLayer { this.alpha = alpha }) { ... }

// Crossfade — swap between two composables
Crossfade(targetState = currentScreen, label = "screen") { screen ->
    when (screen) {
        Screen.List   -> WordListScreen()
        Screen.Detail -> WordDetailScreen()
    }
}
```

---

## Previews

Every public composable needs at minimum a light + dark preview:

```kotlin
@Preview(showBackground = true, name = "Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun WordCardPreview() {
    AppTheme {
        WordCard(
            word      = previewWord(),
            isFlipped = false,
            onFlip    = {},
            onDelete  = {},
        )
    }
}

// Device-size previews for adaptive layouts
@Preview(name = "Phone",  device = Devices.PHONE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun WordListPreview() { ... }

// @PreviewParameter — multiple data variants from a single preview function
class WordPreviewProvider : PreviewParameterProvider<Word> {
    override val values = sequenceOf(
        Word(id = "1", text = "Ephemeral", definition = "Lasting for a very short time"),
        Word(id = "2", text = "X", definition = ""),           // edge case: short text
        Word(id = "3", text = "A".repeat(40), definition = "A".repeat(200)), // overflow
    )
}

@Preview(showBackground = true)
@Composable
private fun WordRowPreview(@PreviewParameter(WordPreviewProvider::class) word: Word) {
    AppTheme { WordRow(word) }
}
```

Rules:
- Always wrap in the app theme
- Use realistic preview data — not empty strings or placeholder IDs
- `private` visibility — previews are not API
- Use `@PreviewParameter` for edge cases (empty, overflow, RTL) rather than duplicating preview functions

---

## CompositionLocal

For cross-cutting concerns that would require threading through many composable levels.

**Choose the right variant:**
- `staticCompositionLocalOf` — value is expected to never (or rarely) change; **any** change invalidates the entire subtree. Best for stable dependencies (analytics, feature flags).
- `compositionLocalOf` — tracks reads and only recomposes consumers when the value changes. Use when the value updates at runtime (e.g., a dynamic theme override).

```kotlin
// staticCompositionLocalOf — stable dependency, never changes after app start
val LocalAnalytics = staticCompositionLocalOf<Analytics> {
    error("No Analytics provided — wrap with CompositionLocalProvider")
}

// compositionLocalOf — changes at runtime (only recomposes readers)
val LocalContentAlpha = compositionLocalOf { 1f }

// Provide (at app root or feature entry)
CompositionLocalProvider(LocalAnalytics provides analytics) {
    AppContent()
}

// Consume (anywhere in subtree)
val analytics = LocalAnalytics.current
```

Good candidates: theme, navigation, snackbar host, analytics, feature flags.
Bad candidates: screen-specific state, business data — pass those explicitly.

---

## Accessibility

```kotlin
// Role for custom clickable
Box(
    modifier = Modifier
        .semantics { role = Role.Button }
        .clickable(onClickLabel = "Delete word") { onDelete() }
) { ... }

// contentDescription rules:
//   - Interactive icons (standalone Icon button): always set a description
//   - Decorative icons inside a labeled component: set null so TalkBack skips the icon
Icon(
    imageVector        = Icons.Default.Delete,
    contentDescription = "Delete",   // standalone — screen reader announces this
)

// Merge semantics for compound components (icon + label)
// Set null on the icon so TalkBack reads "Learned" once, not "check mark, Learned"
Row(Modifier.semantics(mergeDescendants = true) {}) {
    Icon(Icons.Default.Check, contentDescription = null)
    Text("Learned")
}

// Custom actions for swipeable items — expose actions to TalkBack without swiping
Box(Modifier.semantics {
    customActions = listOf(
        CustomAccessibilityAction("Mark as learned") { viewModel.markLearned(); true },
        CustomAccessibilityAction("Delete")          { viewModel.delete(); true },
    )
}) { ... }
```

---

## Custom Layouts

For non-standard arrangements not achievable with `Row`/`Column`/`Box`:

```kotlin
@Composable
fun StaggeredRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val width  = constraints.maxWidth
        val height = placeables.maxOf { it.height }

        layout(width, height) {
            var x = 0
            placeables.forEachIndexed { index, placeable ->
                val y = if (index % 2 == 0) 0 else placeable.height / 2
                placeable.placeRelative(x, y)
                x += placeable.width
            }
        }
    }
}
```

---

## Performance Checklist

- [ ] All composables accept `modifier: Modifier = Modifier` — see [Modifier](#modifier)
- [ ] State hoisted to lowest common ancestor — see [Composable Design](#composable-design)
- [ ] `remember` with keys for expensive computations — see [State Management](#state-management)
- [ ] `derivedStateOf` for boolean flags derived from other state — see [State Management](#state-management)
- [ ] `key` and `contentType` in every `LazyColumn`/`LazyRow` — see [Lazy Lists](#lazy-lists)
- [ ] `key()` in non-lazy `forEach` loops — see [Lazy Lists](#lazy-lists)
- [ ] No lambda/object allocation in composable body without `remember`
- [ ] State reads deferred into lambda modifiers (`offset {}`, `graphicsLayer {}`) — see [Deferred State Reads](#deferred-state-reads--lambda-modifiers)
- [ ] State/data classes annotated `@Stable` or `@Immutable` where needed — see [Stability](#stability----stable-immutable-immutablelist)
- [ ] `ImmutableList` / `ImmutableMap` for collections in state classes — see [Stability](#stability----stable-immutable-immutablelist)
- [ ] One-shot effects use `SharedFlow`, not `Channel` or `LaunchedEffect` — see [ViewModel Effects](#viewmodel-effects--one-shot-events)