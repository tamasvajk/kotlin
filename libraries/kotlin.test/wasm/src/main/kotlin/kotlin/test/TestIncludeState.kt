/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

internal class TestIncludeState(testArguments: FrameworkTestArguments) {
    private var pathIndex: Int = 0
    private var includeMatches = testArguments.include
    private var excludeMatches = testArguments.exclude

    inline fun enterIfIncluded(name: String, body: () -> Unit) {
        val newExcludedMatches = excludeMatches.filter { it.lastIndex >= pathIndex && name.maskedEquals(it[pathIndex]) }
        if (newExcludedMatches.any { it.lastIndex == pathIndex }) return

        val newIncludedMatches = includeMatches.filter { it.lastIndex < pathIndex || name.maskedEquals(it[pathIndex]) }
        if (newIncludedMatches.isEmpty()) return

        val oldIncludeMatches = includeMatches
        val oldExcludeMatches = excludeMatches
        includeMatches = newIncludedMatches
        excludeMatches = newExcludedMatches
        pathIndex++
        try {
            body()
        } finally {
            pathIndex--
            includeMatches = oldIncludeMatches
            excludeMatches = oldExcludeMatches
        }
    }

    fun String.maskedEquals(prefix: String): Boolean {
        if (this.isEmpty() || prefix.isEmpty()) return true

        val sourceIterator = this.iterator()
        val maskIterator = prefix.iterator()

        var onStar = false
        maskLoop@while (maskIterator.hasNext()) {
            val maskSymbol = maskIterator.nextChar()
            if (maskSymbol == '*') {
                onStar = true
                continue
            }

            while (sourceIterator.hasNext()) {
                val sourceSymbol = sourceIterator.nextChar()
                if (sourceSymbol == '*') return false
                if (maskSymbol == sourceSymbol) {
                    onStar = false
                    continue@maskLoop
                } else {
                    if (!onStar) return false
                }
            }
            return !maskIterator.hasNext() && !onStar
        }

        return !sourceIterator.hasNext() || onStar
    }
}
