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

package forms.updateRegisteredDetails

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

import scala.collection.immutable.ArraySeq

class UpdateContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new UpdateContactDetailsFormProvider()()

  ".fullName" - {

    val fieldName = "fullName"
    val requiredKey = "updateRegisteredDetails.updateContactDetails.error.fullName.required"
    val lengthKey = "updateRegisteredDetails.updateContactDetails.error.fullName.length"
    val invalidKey = "updateRegisteredDetails.updateContactDetails.error.fullName.invalid"
    val validNameList = List(
      "Jane Doe",
      "Jane.Doe",
      "Jane`Doe",
      "Jane^Doe",
      "Jane'Doe",
      "Jane&Doe",
      " J a n e D o e",
      "&Jane Doe'",
      "'Jane Doe'",
      "Jane I have many first &middle names Doe",
      "a",
      "Jane Doe "
    )
    val overMaxLengthNameList = List("Jane I have many first & middle names Doe")
    val invalidNameList = List("1", "Jane/Doe", "Jane Parker:Doe", "Jane\\Doe", "Jane Doe The 1st")
    val nameRegex = """^[a-zA-Z &\.\`\'\-\^]+$"""

    "should bind successfully with valid data" in {
      validNameList.foreach(name =>
        form
          .bind(
            Map(
              "fullName"    -> name,
              "position"    -> "CEO",
              "phoneNumber" -> "07700 09900",
              "email"       -> "example@example.com"
            )
          )
          .errors mustBe List.empty
      )
    }

    "should provide the correct Error key when the full name is over 40 characters" in {
      overMaxLengthNameList.foreach(name =>
        form
          .bind(
            Map(
              "fullName"    -> name,
              "position"    -> "CEO",
              "phoneNumber" -> "07700 09900",
              "email"       -> "example@example.com"
            )
          )
          .errors mustBe List(FormError("fullName", List(lengthKey), ArraySeq(40)))
      )
    }

    "should provide the correct Error key when the full name is invalid" in {
      invalidNameList.foreach(name =>
        form
          .bind(
            Map(
              "fullName"    -> name,
              "position"    -> "CEO",
              "phoneNumber" -> "07700 09900",
              "email"       -> "example@example.com"
            )
          )
          .errors mustEqual List(FormError("fullName", List(invalidKey), ArraySeq(nameRegex)))
      )
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".position" - {

    val fieldName = "position"
    val requiredKey = "updateRegisteredDetails.updateContactDetails.error.position.required"
    val lengthKey = "updateRegisteredDetails.updateContactDetails.error.position.length"
    val invalidKey = "updateRegisteredDetails.updateContactDetails.error.position.invalid"
    val validPositionList = List(
      "'`&^-",
      "The best CEO & CTO",
      "Software`Dev",
      "Software^Dev",
      "Software'Dev",
      "Software&Dev",
      " S o f t w a r e D e    v ",
      "&Software`Dev'",
      "'Software`Dev'",
      "a",
      "SOFTWARE DEV",
      "softwaredev",
      "software dev",
      "The best that there ever was - in the whole wide world",
      "Bond. James Bond",
      "The best that there ever was - in the whole wide world - but that isn't enough - maybe even the best in our whole Milky Way universe or even the multiverse"
    )
    val overMaxLengthPositionList = List(
      "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaasaaa",
      "The best Software Dev that there ever was - in the whole wide world-but that isn't enough - maybe even the best in our whole Milky Way universe or even the multiverse"
    )
    val invalidPositionList = List(
      "1",
      "Senior/Lead",
      "Senior:lead",
      "Senior\\lead",
      "Senior Lead The 1st",
      "CEO_and_CTO",
      "The best that there ever was, in the whole wide world",
      "Bond, James Bond"
    )
    val positionRegex = """^[a-zA-Z &\.\`\'\-\^]+$"""

    "should bind successfully with valid data" in {
      validPositionList.foreach(position =>
        form
          .bind(
            Map(
              "fullName"    -> "Jane Doe",
              "position"    -> position,
              "phoneNumber" -> "07700 09900",
              "email"       -> "example@example.com"
            )
          )
          .errors mustBe List.empty
      )
    }

    "should provide the correct Error key when the position is over 155 characters" in {
      overMaxLengthPositionList.foreach(position =>
        form
          .bind(
            Map(
              "fullName"    -> "Jane Doe",
              "position"    -> position,
              "phoneNumber" -> "07700 09900",
              "email"       -> "example@example.com"
            )
          )
          .errors mustBe List(FormError("position", List(lengthKey), ArraySeq(155)))
      )
    }

    "should provide the correct Error key when the position is invalid" in {
      invalidPositionList.foreach(position =>
        form
          .bind(
            Map(
              "fullName"    -> "Jane Doe",
              "position"    -> position,
              "phoneNumber" -> "07700 09900",
              "email"       -> "example@example.com"
            )
          )
          .errors mustEqual List(FormError("position", List(invalidKey), ArraySeq(positionRegex)))
      )
    }
    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".phoneNumber" - {

    val fieldName = "phoneNumber"
    val requiredKey = "updateRegisteredDetails.updateContactDetails.error.phoneNumber.required"
    val lengthKey = "updateRegisteredDetails.updateContactDetails.error.phoneNumber.length"
    val invalidKey = "updateRegisteredDetails.updateContactDetails.error.phoneNumber.invalid"
    val validPhoneNumberList = List(
      "#+*-()/\\",
      "01632 960999",
      "07700 900999",
      ")\\/(\\-\\*\\#\\+",
      "UKAAAAAAA 07700 900999",
      "(+44) 01632 960999",
      "(+44) 0 1 6 3 2 9 6 0999",
      "- 44) 016329 60999",
      "THIS IS MY NUMBER",
      ")/(",
      "44/777/00",
      "44-777-00",
      "44-777-00/TEST",
      "#44 +321*777-",
      "44\\777\\00"
    )
    val overMaxLengthPhoneNumberList = List("(+44) 0 1 6 3 2 9 6 0 999", "(+44) 0 1 6 3 2 9 6 09 9 9")
    val invalidPhoneNumberList =
      List("(+44) 016329, 60999", "(& 44) 016329 60999", "This is my number 07700", "ukaaaaaaa 07700 900999")
    val phoneNumberRegex = """^[A-Z0-9 )/(\\#+*\-]+$"""

    "should bind successfully with valid data" in {
      validPhoneNumberList.foreach(phoneNumber =>
        form
          .bind(
            Map(
              "fullName"    -> "Jane Doe",
              "position"    -> "CEO",
              "phoneNumber" -> phoneNumber,
              "email"       -> "example@example.com"
            )
          )
          .errors mustBe List.empty
      )
    }

    "should provide the correct Error key when the phoneNumber is over 155 characters" in {
      overMaxLengthPhoneNumberList.foreach(phoneNumber =>
        form
          .bind(
            Map(
              "fullName"    -> "Jane Doe",
              "position"    -> "CEO",
              "phoneNumber" -> phoneNumber,
              "email"       -> "example@example.com"
            )
          )
          .errors mustBe List(FormError("phoneNumber", List(lengthKey), ArraySeq(24)))
      )
    }

    "should provide the correct Error key when the phoneNumber is invalid" in {
      invalidPhoneNumberList.foreach(phoneNumber =>
        form
          .bind(
            Map(
              "fullName"    -> "Jane Doe",
              "position"    -> "CEO",
              "phoneNumber" -> phoneNumber,
              "email"       -> "example@example.com"
            )
          )
          .errors mustEqual List(FormError("phoneNumber", List(invalidKey), ArraySeq(phoneNumberRegex)))
      )
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".email" - {
    val validEmailList = List(
      "name@example.com",
      "test@test.com",
      "LongTestNameForEmailExample@example.com",
      "LongTestNameForEmailExampleWith55Characters@example.com",
      "LongTestNameForEmailExampleWith74Characters@WithALongDomainNameExample.com",
      "LongNameWITHCAPITALSForEmailExampleWith82Characters@WithALongDomainNameExample.com",
      "a@a.a",
      "1@a.a",
      "1@1.1",
      "lettersdigitsoranyofthefollowingspecialcharacters.!#$%&'*+/=?^_{|}~-@example.com",
      "youCanPutTwoPeriodsAfterThe@example.com.com",
      "3232-21982.digits.are.allowed?Anywhere*within*the^text@example.com",
      "a@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed",
      "132Total@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed",
      "lettersdigitsoranyofthefollowingspecialcharacters.!#$%&'*+/=?^_{|}~-AreAllowed.portionTheBackPortions61Each@a.a",
      "canEndWithHypen-@example.com"
    )
    val overMaxLengthEmailList = List(
      "lettersdigitsoranyofthefollowingspecialcharacters.!#$%&'*+/=?^_{|}~-AreAllowedButOnly1HypenAfterThe.portionTheBackPortions61Each@a.ab",
      "134IsTotal@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed",
      "aWayTooLong@lettersordigitsfollowedbyoptionalhyphenandmorelettersdigits61.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowed"
    )
    val invalidEmailList = List(
      "1.com",
      "commas,are,not,allowed@example.com",
      "A@b@c@example.com",
      "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com",
      "a@lettersordigitsfollowedbyoptionalhyphenandmoreletters.lettersordigitsfollowedbyoptionalhyphen-UpTo61CharsAreAllowedTooManyHere",
      "invalid.IsNotAllowed@example.lettersordigitsfollowedby-optionalhyphen-UpTo61CharsAreAllowed@",
      "spaces cannotExistOutQuotes@example.com",
      "invalid.IsNotAllowed@example.lettersordigitsfollowedby-optionalhyphen-UpTo61CharsAreAllowed-"
    )

    val fieldName = "email"
    val requiredKey = "updateRegisteredDetails.updateContactDetails.error.email.required"
    val lengthKey = "updateRegisteredDetails.updateContactDetails.error.email.length"
    val invalidKey = "updateRegisteredDetails.updateContactDetails.error.email.invalid"
    val emailRegEx =
      """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""

    "should bind successfully with valid data" in {
      validEmailList.foreach(email =>
        form
          .bind(Map("fullName" -> "Jane Doe", "position" -> "CEO", "phoneNumber" -> "07700 09900", "email" -> email))
          .errors mustBe List.empty
      )
    }

    "should provide the correct Error key when the email address is over 132 characters" in {
      overMaxLengthEmailList.foreach(email =>
        form
          .bind(Map("fullName" -> "Jane Doe", "position" -> "CEO", "phoneNumber" -> "07700 09900", "email" -> email))
          .errors mustBe List(FormError("email", List(lengthKey), ArraySeq(132)))
      )
    }

    "should provide the correct Error key when the email address is invalid" in {
      invalidEmailList.foreach(email =>
        form
          .bind(Map("fullName" -> "Jane Doe", "position" -> "CEO", "phoneNumber" -> "07700 09900", "email" -> email))
          .errors mustEqual List(FormError("email", List(invalidKey), ArraySeq(emailRegEx)))
      )
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
