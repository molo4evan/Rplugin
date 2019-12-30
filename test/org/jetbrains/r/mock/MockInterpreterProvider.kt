/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.mock

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.concurrency.Promise
import org.jetbrains.r.common.ExpiringList
import org.jetbrains.r.common.emptyExpiringList
import org.jetbrains.r.packages.RPackage
import org.jetbrains.r.packages.remote.RDefaultRepository
import org.jetbrains.r.packages.remote.RMirror
import org.jetbrains.r.packages.remote.RRepoPackage
import org.jetbrains.r.rinterop.RInterop

interface MockInterpreterProvider {
  val interop: RInterop
  val userLibraryPath: String
  val cranMirrors: List<RMirror>
  val libraryPaths: List<VirtualFile>
  val installedPackages: ExpiringList<RPackage>
  val packageDetails: Map<String, RRepoPackage>?
  val defaultRepositories: List<RDefaultRepository>
  fun getAvailablePackages(repoUrls: List<String>): Promise<List<RRepoPackage>>

  companion object {
    val DUMMY = object : MockInterpreterProvider {
      override val interop: RInterop
        get() = throw NotImplementedError()

      override val userLibraryPath: String
        get() = throw NotImplementedError()

      override val cranMirrors: List<RMirror>
        get() = throw NotImplementedError()

      override val libraryPaths: List<VirtualFile>
        get() = throw NotImplementedError()

      override val installedPackages: ExpiringList<RPackage>
        get() = emptyExpiringList(false)  // Note: exception is not thrown intentionally (see `MockInterpreter.installedPackages`)

      override val packageDetails: Map<String, RRepoPackage>?
        get() = throw NotImplementedError()

      override val defaultRepositories: List<RDefaultRepository>
        get() = throw NotImplementedError()

      override fun getAvailablePackages(repoUrls: List<String>): Promise<List<RRepoPackage>> {
        throw NotImplementedError()
      }
    }
  }
}
