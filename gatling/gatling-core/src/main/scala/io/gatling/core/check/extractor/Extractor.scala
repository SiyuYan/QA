/**
 * Copyright 2011-2015 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.core.check.extractor

import io.gatling.commons.validation.Validation

trait Extractor[P, X] {
  def name: String
  def arity: String
  def apply(prepared: P): Validation[Option[X]]
}

abstract class CriterionExtractor[P, T, X] extends Extractor[P, X] {
  def criterion: T
  def extract(prepared: P): Validation[Option[X]]
  def criterionName: String
  def name = s"$criterionName($criterion)"
  def apply(prepared: P): Validation[Option[X]] =
    for {
      extracted <- extract(prepared).mapError(message => s" could not extract : $message")
    } yield extracted
}

trait SingleArity {
  this: Extractor[_, _] =>

  val arity = "find"
}

trait FindArity {
  this: Extractor[_, _] =>

  def occurrence: Int
  def arity = occurrence match {
    case 1 => "find"
    case i => s"find($i)"
  }
}

trait FindAllArity {
  this: Extractor[_, _] =>

  val arity = "findAll"
}

trait CountArity {
  this: Extractor[_, _] =>

  val arity = "count"
}
