/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.mappings

import models.{Enumerable, ReturnPeriod}
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{JsSuccess, Json}

import scala.util.{Failure, Success, Try}
import scala.util.control.Exception.nonFatalCatch

trait Formatters {

  private[mappings] def stringFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def returnPeriodFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[ReturnPeriod] = new Formatter[ReturnPeriod] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ReturnPeriod] = {
      data.get(key) match {
        case None => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) => Try(Json.fromJson[ReturnPeriod](Json.parse(s))) match {
          case Success(JsSuccess(returnPeriod, _)) => Right(returnPeriod)
          case _ => Left(Seq(FormError(key, errorKey, args)))
        }
      }
    }
    override def unbind(key: String, value: ReturnPeriod): Map[String, String] = {
      Map(key -> Json.toJson(value).toString())
    }
  }

  private[mappings] def booleanFormatter(requiredKey: String, invalidKey: String, args: Seq[String] = Seq.empty): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .flatMap {
          case "true"  => Right(true)
          case "false" => Right(false)
          case _       => Left(Seq(FormError(key, invalidKey, args)))
        }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(requiredKey: String,
                                     wholeNumberKey: String,
                                     nonNumericKey: String,
                                     invalidLength: String,
                                     args: Seq[String] = Seq.empty): Formatter[Int] =
    new Formatter[Int] {
      val decimalRegexp = """^-?(\d*\.\d*)$"""
      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .flatMap {
            case s if s.matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s => Try(s.toInt) match {
              case Failure(_) => Left(Seq(FormError(key, nonNumericKey, args)))
              case Success(number) if (number > 31 && key == s"$key.day"
                || number > 12 && key == s"$key.month"
                || number.toString.length > 4 && key == s"$key.year") =>
                Left(Seq(FormError(key, invalidLength, args)))
              case Success(number) => Right(number)
            }
          }

      override def unbind(key: String, value: Int): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }


  private[mappings] def enumerableFormatter[A](requiredKey: String, invalidKey: String, args: Seq[String] = Seq.empty)(implicit ev: Enumerable[A]): Formatter[A] =
    new Formatter[A] {
      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap {
          str =>
            ev.withName(str)
              .map(Right.apply)
              .getOrElse(Left(Seq(FormError(key, invalidKey, args))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def litresFormatter(band: String,
                                        args: Seq[String] = Seq.empty): Formatter[Long] =
    new Formatter[Long] {

      val requiredKey = s"litres.error.$band.required"
      val outOfRangeKey = s"litres.error.$band.outOfMaxVal"
      val negativeNumber = s"litres.error.$band.negative"
      val wholeNumberKey = s"litres.error.$band.wholeNumber"
      val nonNumericKey = s"litres.error.$band.nonNumeric"


      val decimalRegexp = """^-?(\d*\.\d*)$"""
      val numberRegexp = """^\d+$*"""
      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .map(_.replace(" ", ""))
          .flatMap {
            case s if s.matches(numberRegexp) =>
              nonFatalCatch
                .either(s.toLong)
                .left.map(_ => Seq(FormError(key, outOfRangeKey, args)))
            case s if s.startsWith("-") =>
              Left(Seq(FormError(key, negativeNumber, args)))
            case s if s.matches(decimalRegexp) =>
              Try(s.split("\\.")(0).toLong)
                .fold(_ => Left(Seq(FormError(key, outOfRangeKey, args))),
                  _ => Left(Seq(FormError(key, wholeNumberKey, args))))
            case s =>
              nonFatalCatch
                .either(s.toLong)
                .left.map(_ => Seq(FormError(key, nonNumericKey, args)))
          }

      override def unbind(key: String, value: Long) =
        baseFormatter.unbind(key, value.toString)
    }

  def sdilReferenceFormatter(requiredKey: String, args: Seq[String]): Formatter[String] = {
    new Formatter[String] {
      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .map(_.replace(" ", ""))
          .map(_.toUpperCase)

      override def unbind(key: String, value: String) =
        baseFormatter.unbind(key, value)
    }
  }
}
