/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.util

import org.jetbrains.concurrency.*
import java.util.concurrent.atomic.AtomicReference

object PromiseUtil {
  fun runChain(tasks: List<() -> Promise<Boolean>>): Promise<Boolean> {
    var promise: Promise<Boolean> = resolvedPromise(true)
    tasks.forEach { task ->
      promise = promise.thenAsync {
        if (it) {
          task()
        } else {
          resolvedPromise(false)
        }
      }
    }
    return promise
  }

}

fun <T, R> CancellablePromise<T>.thenCancellable(f: (T) -> R): CancellablePromise<R> {
  val result = AsyncPromise<R>()
  this.onSuccess {
    try {
      result.setResult(f(it))
    } catch (e: Throwable) {
      result.setError(e)
    }
  }.onError { result.setError(it) }
  result.onError { if (this.isPending) this.cancel() }
  return result
}

fun <T, R> CancellablePromise<T>.thenAsyncCancellable(f: (T) -> CancellablePromise<R>): CancellablePromise<R> {
  val result = AsyncPromise<R>()
  val secondPromise = AtomicReference<CancellablePromise<R>>(null)
  this.onError { result.setError(it) }
    .onSuccess {
      val promise = try {
        f(it)
      } catch (e: Throwable) {
        result.setError(e)
        return@onSuccess
      }
      if (secondPromise.compareAndSet(null, promise)) {
        promise.processed(result)
      } else {
        promise.cancel()
      }
    }
  result.onError {
    if (secondPromise.compareAndSet(null, result)) {
      this.cancel()
    } else {
      secondPromise.get()?.cancel()
    }
  }
  return result
}
