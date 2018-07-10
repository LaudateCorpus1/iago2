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

trait LogSource extends Iterator[String] {
  def next(): String
  def hasNext: Boolean
  def reset()
}

class LogSourceImpl(file: String) extends LogSource {
  protected var source = init()

  private lazy val logLines = {
    val inputSource = scala.io.Source.fromFile(file)("UTF-8")
    val inputLines = inputSource.getLines().toList
    inputSource.close()
    inputLines
  }

  protected def init(): Iterator[String] = {
    logLines.toIterator
  }

  def next(): String = source.next()
  def hasNext: Boolean = source.hasNext

  def reset() {
    source = init()
  }
}
