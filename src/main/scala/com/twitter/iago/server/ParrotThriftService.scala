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

import com.twitter.finagle.Service
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.util.{Future, Promise, Time}

class ParrotThriftService(queue: RequestQueue[ParrotRequest, Array[Byte]])
    extends ParrotService[ParrotRequest, Array[Byte]](queue)

class ParrotThriftServiceWrapper(val service: ParrotService[ParrotRequest, Array[Byte]])
    extends Service[ThriftClientRequest, Array[Byte]] {
  def apply(req: ThriftClientRequest): Future[Array[Byte]] = {
    val request = new ParrotRequest(message = req)
    service(request)
  }

  override def close(deadline: Time) = service.close(deadline)

  override def status = service.status
}
