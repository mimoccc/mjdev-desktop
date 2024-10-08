package eu.mjdev.desktop.managers

//import io.github.irgaly.kfswatch.KfsDirectoryWatcher
//import io.github.irgaly.kfswatch.KfsDirectoryWatcherEvent
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch

//@Suppress("MemberVisibilityCanBePrivate")
//class FileSystemWatcher(
//    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
//    val mountpoints: List<String> = listOf("/mnt", "/media"),
//    val watcher: KfsDirectoryWatcher = KfsDirectoryWatcher(scope),
//    val onEvent: KfsDirectoryWatcherEvent.() -> Unit = {}
//) {
//    fun init() = scope.launch(Dispatchers.IO) {
//        mountpoints.forEach {
//            watcher.add(it)
//        }
//        watcher.onEventFlow.collect { event ->
//            onEvent(event)
//        }
//    }
//
//    fun dispose() = scope.launch {
//        watcher.removeAll()
//        watcher.close()
//    }
//}
