package org.mjdev.desktop.helpers.fuzzywuzzy.diffutils

object DiffUtils {
    private fun levEditDistance(s1: String, s2: String, xcost: Int): Int {
        var i: Int
        val half: Int
        var c1 = s1.toCharArray()
        var c2 = s2.toCharArray()
        var str1 = 0
        var str2 = 0
        var len1 = s1.length
        var len2 = s2.length
        while (len1 > 0 && len2 > 0 && c1[str1] == c2[str2]) {
            len1--
            len2--
            str1++
            str2++
        }
        while (len1 > 0 && len2 > 0 && c1[str1 + len1 - 1] == c2[str2 + len2 - 1]) {
            len1--
            len2--
        }
        if (len1 == 0) return len2
        if (len2 == 0) return len1
        if (len1 > len2) {
            val nx = len1
            val temp = str1
            len1 = len2
            len2 = nx
            str1 = str2
            str2 = temp
            val t = c2
            c2 = c1
            c1 = t
        }
        if (len1 == 1) {
            return if (xcost != 0) {
                len2 + 1 - 2 * memchr(c2, str2, c1[str1], len2)
            } else {
                len2 - memchr(c2, str2, c1[str1], len2)
            }
        }
        len1++
        len2++
        half = len1 shr 1
        val row = IntArray(len2)
        var end = len2 - 1
        i = 0
        while (i < len2 - if (xcost != 0) 0 else half) {
            row[i] = i
            i++
        }
        if (xcost != 0) {
            i = 1
            while (i < len1) {
                var p = 1
                val ch1 = c1[str1 + i - 1]
                var c2p = str2
                var D = i
                var x = i
                while (p <= end) {
                    if (ch1 == c2[c2p++]) {
                        x = --D
                    } else {
                        x++
                    }
                    D = row[p]
                    D++
                    if (x > D) x = D
                    row[p++] = x
                }
                i++
            }
        } else {
            row[0] = len1 - half - 1
            i = 1
            while (i < len1) {
                var p: Int
                val ch1 = c1[str1 + i - 1]
                var c2p: Int
                var D: Int
                var x: Int
                if (i >= len1 - half) {
                    val offset = i - (len1 - half)
                    var c3: Int
                    c2p = str2 + offset
                    p = offset
                    c3 = row[p++] + if (ch1 != c2[c2p++]) 1 else 0
                    x = row[p]
                    x++
                    D = x
                    if (x > c3) {
                        x = c3
                    }
                    row[p++] = x
                } else {
                    p = 1
                    c2p = str2
                    x = i
                    D = x
                }
                if (i <= half + 1) end = len2 + i - half - 2
                while (p <= end) {
                    val c3 = --D + if (ch1 != c2[c2p++]) 1 else 0
                    x++
                    if (x > c3) {
                        x = c3
                    }
                    D = row[p]
                    D++
                    if (x > D) x = D
                    row[p++] = x
                }
                if (i <= half) {
                    val c3 = --D + if (ch1 != c2[c2p]) 1 else 0
                    x++
                    if (x > c3) {
                        x = c3
                    }
                    row[p] = x
                }
                i++
            }
        }
        i = row[end]
        return i
    }

    private fun memchr(haystack: CharArray, offset: Int, needle: Char, inum: Int): Int {
        var num = inum
        if (num != 0) {
            var p = 0
            do {
                if (haystack[offset + p] == needle) return 1
                p++
            } while (--num != 0)
        }
        return 0
    }

    fun getRatio(s1: String, s2: String): Double {
        val len1 = s1.length
        val len2 = s2.length
        val lensum = len1 + len2
        val editDistance = levEditDistance(s1, s2, 1)
        return (lensum - editDistance) / lensum.toDouble()
    }
}