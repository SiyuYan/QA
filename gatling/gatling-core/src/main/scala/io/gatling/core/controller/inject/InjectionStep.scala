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
package io.gatling.core.controller.inject

import java.util.Random
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{ DurationLong, DurationDouble, FiniteDuration }
import scala.math.{ abs, sqrt }

trait InjectionStep {
  /**
   * Iterator of time deltas in between any injected user and the beginning of the simulation
   */
  def chain(iterator: Iterator[FiniteDuration]): Iterator[FiniteDuration]

  /**
   * Number of users to inject
   */
  def users: Int
}

/**
 * Ramp a given number of users over a given duration
 */
case class RampInjection(users: Int, duration: FiniteDuration) extends InjectionStep {
  require(users > 0, "The number of users must be a strictly positive value")

  override def chain(chained: Iterator[FiniteDuration]): Iterator[FiniteDuration] = {
    val interval = duration / users
    Iterator.iterate(0 milliseconds)(_ + interval).take(users) ++ chained.map(_ + duration)
  }
}

/**
 * Inject users at constant rate : an other expression of a RampInjection
 */
case class ConstantRateInjection(rate: Double, duration: FiniteDuration) extends InjectionStep {
  val users = (duration.toSeconds * rate).toInt
  val ramp = RampInjection(users, duration)
  def randomized = PoissonInjection(duration, rate, rate)
  override def chain(chained: Iterator[FiniteDuration]): Iterator[FiniteDuration] = ramp.chain(chained)
}

/**
 * Don't injection any user for a given duration
 */
case class NothingForInjection(duration: FiniteDuration) extends InjectionStep {
  override def chain(chained: Iterator[FiniteDuration]): Iterator[FiniteDuration] = chained.map(_ + duration)
  override def users = 0
}

/**
 * Inject all the users at once
 */
case class AtOnceInjection(users: Int) extends InjectionStep {
  require(users > 0, "The number of users must be a strictly positive value")

  override def chain(chained: Iterator[FiniteDuration]): Iterator[FiniteDuration] =
    Iterator.continually(0 milliseconds).take(users) ++ chained
}

/**
 * The injection scheduling follows this equation
 * u = r1*t + (r2-r1)/(2*duration)*t²
 *
 * @param r1 Initial injection rate in users/seconds
 * @param r2 Final injection rate in users/seconds
 * @param duration Injection duration
 */
case class RampRateInjection(r1: Double, r2: Double, duration: FiniteDuration) extends InjectionStep {
  require(r1 > 0 && r2 > 0, "injection rates must be strictly positive values")

  val users = ((r1 + (r2 - r1) / 2) * duration.toSeconds).toInt
  def randomized = PoissonInjection(duration, r1, r2)

  override def chain(chained: Iterator[FiniteDuration]): Iterator[FiniteDuration] = {
    if ((r2 - r1).abs < 0.0001)
      ConstantRateInjection(r1, duration).chain(chained)
    else {
      val a = (r2 - r1) / (2 * duration.toSeconds)
      val b = r1
      val b2 = r1 * r1

        def userScheduling(u: Int) = {
          val c = -u
          val delta = b2 - 4 * a * c

          val t = (-b + sqrt(delta)) / (2 * a)
          (t * 1000).toLong.milliseconds
        }

      Iterator.range(0, users).map(userScheduling) ++ chained.map(_ + duration)
    }
  }
}

/**
 *  Inject users through separated steps until reaching the closest possible amount of total users.
 *
 *  @param possibleUsers The maximum possible of total users.
 *  @param step The step that will be repeated.
 *  @param separator Will be injected in between the regular injection steps.
 */
case class SplitInjection(possibleUsers: Int, step: InjectionStep, separator: InjectionStep) extends InjectionStep {
  private val stepUsers = step.users
  private lazy val separatorUsers = separator.users

  override def chain(chained: Iterator[FiniteDuration]) = {
    if (possibleUsers > stepUsers) {
      val n = (possibleUsers - stepUsers) / (stepUsers + separatorUsers)
      val lastScheduling = step.chain(chained)
      (1 to n).foldRight(lastScheduling)((_, iterator) => step.chain(separator.chain(iterator)))
    } else
      chained
  }

  def users = {
    if (possibleUsers > stepUsers)
      possibleUsers - (possibleUsers - stepUsers) % (stepUsers + separatorUsers)
    else 0
  }
}

/**
 * Injection rate following a Heaviside distribution function
 *
 * {{{
 * numberOfInjectedUsers(t) = u(t)
 *                          = ∫δ(t)
 *                          = Heaviside(t)
 *                          = 1/2 + 1/2*erf(k*t)
 *                          // (good numerical approximation)
 * }}}
 */
case class HeavisideInjection(users: Int, duration: FiniteDuration) extends InjectionStep {

  override def chain(chained: Iterator[FiniteDuration]) = {
      def heavisideInv(u: Int) = {
        val x = u.toDouble / (users + 2)
        Erf.erfinv(2 * x - 1)
      }

    val t0 = abs(heavisideInv(1))
    val d = t0 * 2
    val k = duration.toMillis / d

    Iterator.range(1, users + 1).map(heavisideInv).map(t => (k * (t + t0)).toLong.milliseconds) ++ chained.map(_ + duration)
  }
}

/**
 * Inject users following a Poisson random process, with a ramped injection rate.
 *
 * A Poisson process models users arriving at a page randomly. You can specify the rate
 * that users arrive at, and this rate can ramp-up.
 *
 * Note that since this injector has an element of randomness, the total number of users
 * may vary from run to run, depending on the seed.
 *
 * @param duration the length of time this injector should run for
 * @param startRate initial injection rate for users
 * @param endRate final injection rate for users
 * @param seed a seed for the randomization. If the same seed is re-used, the same timings will be obtained
 */
case class PoissonInjection(duration: FiniteDuration, startRate: Double, endRate: Double, seed: Long = System.nanoTime) extends InjectionStep {
  private val durationSecs = duration.toUnit(TimeUnit.SECONDS)

  override def chain(chained: Iterator[FiniteDuration]): Iterator[FiniteDuration] = {

    val rand = new Random(seed)

    // Uses Lewis and Shedler's thinning algorithm: http://www.dtic.mil/dtic/tr/fulltext/u2/a059904.pdf
    val maxLambda = startRate max endRate
      def shouldKeep(d: Double) = {
        val actualLambda = startRate + (endRate - startRate) * d / durationSecs
        rand.nextDouble() < actualLambda / maxLambda
      }

    val rawIntervals = Iterator.continually {
      val u = rand.nextDouble()
      -math.log(u) / maxLambda
    }

    rawIntervals
      .scanLeft(0.0)(_ + _) // Rolling sum
      .drop(1) // Throw away first value of 0.0. It is not random, but a quirk of using scanLeft
      .takeWhile(_ < durationSecs)
      .filter(shouldKeep)
      .map(_.seconds) ++ chained.map(_ + duration)
  }

  val users = chain(Iterator.empty).size
}
