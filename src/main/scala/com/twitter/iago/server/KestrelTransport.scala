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
package com.twitter.iago.server

import com.twitter.finagle.kestrel.protocol._
import com.twitter.finagle.{Kestrel, Service}
import com.twitter.io.Buf
import com.twitter.util.{Duration, Time}
import java.util.concurrent.TimeUnit

object KestrelCommandExtractor extends MemcacheLikeCommandExtractor[Command] {
  // currently handles get & set only
  def unapply(rawCommand: String): Option[Command] = {
    val delim = rawCommand.indexOf("\r\n")
    val (command, data) =
      if (delim == -1) {
        (rawCommand, None)
      } else {
        (rawCommand.substring(0, delim), Some(rawCommand.substring(delim + 2)))
      }

    val tokens = command.split("\\s+")
    tokens(0).toLowerCase match {
      case "get" => get(tokens.drop(1))
      case "set" => set(tokens.drop(1), data)
      case _ => None
    }
  }

  private def get(rawArgs: Array[String]): Option[GetCommand] = {
    if (rawArgs.length != 1) {
      None
    } else {
      val queueAndArgs = rawArgs(0).split("/")
      val queueName = Buf.Utf8(queueAndArgs.head)
      val args = queueAndArgs.tail

      val (timeouts, flags) = args.partition {
        _.startsWith("t=")
      }
      val timeout = timeouts.headOption.map { timeoutSpec =>
        Duration(timeoutSpec.substring(2).toLong, TimeUnit.MILLISECONDS)
      }

      flags.toSeq.map {
        _.toLowerCase
      } match {
        case Seq() => Some(Get(queueName, timeout))
        case Seq("close", "open") => Some(CloseAndOpen(queueName, timeout))
        case Seq("open", "close") => Some(CloseAndOpen(queueName, timeout))
        case Seq("abort") => Some(Abort(queueName))
        case Seq("close") => Some(Close(queueName))
        case Seq("open") => Some(Open(queueName, timeout))
        case Seq("peek") => Some(Peek(queueName, timeout))
        case _ => None
      }
    }
  }

  private def set(args: Array[String], data: Option[String]): Option[Set] = {
    if (args.length < 4) {
      None
    } else {
      val key = Buf.Utf8(args(0))
      val expiry = Time.fromSeconds(args(2).toInt)
      val byteCount = args(3).toInt
      val bytes = data.getOrElse("").getBytes

      if (bytes.length != byteCount) {
        None
      } else {
        Some(Set(key, expiry, Buf.ByteArray.Owned(bytes)))
      }
    }
  }
}

object KestrelTransportFactory extends MemcacheLikeTransportFactory[Command, Response] {
  override def stack() = Kestrel.client

  override def fromService(service: Service[Command, Response]) = new KestrelTransport(service)
}

class KestrelTransport(service: Service[Command, Response])
    extends MemcacheLikeTransport[Command, Response](KestrelCommandExtractor, service)
