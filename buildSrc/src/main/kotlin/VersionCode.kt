/**
 * Android versionCode from a "major.minor.patch" version name: major * 10000 + minor * 100 + patch, so every later
 * version gets a larger code. Until 8 Oct 2026 the dots were removed, and 1.0.10 (1010) outranked a later 1.1.0 (110),
 * which phones then refused as an update.
 */
object VersionCode {
    /** Weight of the major number. */
    private const val MAJOR_WEIGHT = 10_000

    /** Weight of the minor number. */
    private const val MINOR_WEIGHT = 100

    /** Highest minor or patch number that still fits its two digits. */
    private const val PART_MAX = 99

    /** Number of parts of a version name. */
    private const val PARTS = 3

    /** Separator of the version parts. */
    private const val SEPARATOR = "."

    /** versionCode of [versionName]; missing parts count as 0, a part over [PART_MAX] stops the build. */
    fun of(versionName: String): Int {
        val given = versionName.split(SEPARATOR).map { it.toInt() }
        require(given.size <= PARTS) { "version $versionName has more than $PARTS parts" }
        val (major, minor, patch) = given + List(PARTS - given.size) { 0 }
        require(minor <= PART_MAX && patch <= PART_MAX) { "minor and patch of $versionName must be at most $PART_MAX" }
        return major * MAJOR_WEIGHT + minor * MINOR_WEIGHT + patch
    }
}
