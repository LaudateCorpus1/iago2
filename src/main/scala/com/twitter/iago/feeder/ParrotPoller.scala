/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.twitter.iago.feeder

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

import com.twitter.iago.util.{ParrotCluster, RemoteParrot}
import com.twitter.logging.Logger

object ParrotPoller {
  val pollRate = 1000
}

class ParrotPoller(cluster: ParrotCluster, serverLatch: CountDownLatch) extends Thread {
  val log = Logger.get(getClass)
  val isRunning = new AtomicBoolean(true)
  var parrotsSnap = 0

  override def start() {
    log.info("Starting ParrotPoller")
    super.setName("ParrotPoller")
    super.setDaemon(false)
    super.start()
  }

  override def run() {
    while (isRunning.get) {
      pollParrots()
      Thread.sleep(ParrotPoller.pollRate)
    }
  }

  def shutdown() {
    isRunning.set(false)
  }

  private[this] def pollParrots() {
    cluster.parrots foreach { parrot =>
      try {
        pollParrot(parrot)
      } catch {
        case t: Throwable =>
          log.error(
            t,
            "Exception polling parrot[%s] - %s",
            parrot.address,
            Option(t.getMessage).getOrElse(t.toString)
          )
      }
    }

    // Note that cluster.parrots can change during execution of this block!
    val curParrots = cluster.parrots.size
    val newParrots = curParrots - parrotsSnap;
    for (_ <- 1 to newParrots) serverLatch.countDown()
    parrotsSnap = curParrots
  }

  private[this] def pollParrot(parrot: RemoteParrot) {
    val status = parrot.getStatus
    parrot.queueDepth = status.queueDepth getOrElse 0d
    log.debug("pollParrot: depth is %f for %s", parrot.queueDepth, parrot.address)
  }
}
