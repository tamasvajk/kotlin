/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

@Suppress("EnumEntryName")
internal enum class IgnoredTestSuitesReporting {
    skip, reportAsIgnoredTest, reportAllInnerTestsAsIgnored
}

internal class FrameworkTestArguments(
    val include: List<List<String>>,
    val exclude: List<List<String>>,
    val ignoredTestSuites: IgnoredTestSuitesReporting,
    val dryRun: Boolean
) {
    companion object {
        fun parse(args: List<String>): FrameworkTestArguments {
            val includePaths = mutableListOf<List<String>>()
            val excludePaths = mutableListOf<List<String>>()
            var currentPaths: MutableList<List<String>>? = null
            var ignoredTestSuites: IgnoredTestSuitesReporting = IgnoredTestSuitesReporting.reportAllInnerTestsAsIgnored
            var isIgnoredTestSuites = false
            var dryRun = false

            for (arg in args) {
                if (currentPaths != null) {
                    for (splitArg in arg.split(',')) {
                        currentPaths.add(splitArg.split('.'))
                    }
                    currentPaths = null
                    continue
                }

                if (isIgnoredTestSuites) {
                    val value = IgnoredTestSuitesReporting.values().firstOrNull { it.name == arg }
                    if (value != null) {
                        ignoredTestSuites = value
                    }
                    isIgnoredTestSuites = false
                    continue
                }

                when (arg) {
                    "--include" -> currentPaths = includePaths
                    "--exclude" -> currentPaths = excludePaths
                    "--ignoredTestSuites" -> isIgnoredTestSuites = true
                    "--dryRun" -> dryRun = true
                }
            }

            if (includePaths.isEmpty()) {
                includePaths.add(listOf("*"))
            }
            return FrameworkTestArguments(includePaths, excludePaths, ignoredTestSuites, dryRun)
        }
    }
}