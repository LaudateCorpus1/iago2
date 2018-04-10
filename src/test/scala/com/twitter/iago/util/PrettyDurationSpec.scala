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
package com.twitter.iago.util

import org.junit.runner.RunWith
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.junit.JUnitRunner

import com.twitter.conversions.time.intToTimeableNumber

@RunWith(classOf[JUnitRunner])
class PrettyDurationSpec extends WordSpec with MustMatchers {
  "PrettyDuration" should {
    "be pretty" in {
      PrettyDuration(1.minute + 59.seconds + 999.milliseconds) must equal("2.00 minutes")
    }
  }
}
