#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$className;format="decap"$                        controllers.$packageName$.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes
echo "POST       /$className;format="decap"$                        controllers.$packageName$.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes

echo "GET        /change$className$                  controllers.$packageName$.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes
echo "POST       /change$className$                  controllers.$packageName$.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes

echo "GET        /howMany$className$                        controllers.$packageName$.HowMany$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes
echo "POST       /howMany$className$                        controllers.$packageName$.HowMany$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes

echo "GET        /changeHowMany$className$                  controllers.$packageName$.HowMany$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes
echo "POST       /changeHowMany$className$                  controllers.$packageName$.HowMany$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.detailsLink = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.detailsContent = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.error.required = Select yes if $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.change.hidden = Change $className$" >> ../conf/messages.en

echo "howMany$className$.title = howMany$className$" >> ../conf/messages.en
echo "howMany$className$.heading = howMany$className$" >> ../conf/messages.en
echo "howMany$className$.subtext = howMany$className$" >> ../conf/messages.en
echo "$className;format="decap"$.lowband.litres.hidden = change amount of litres in lowband for $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.highband.litres.hidden = change amount of litres in highband for $className;format="decap"$" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$packageName;format="cap"$$className$UserAnswersEntry: Arbitrary[($className$Page.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[$className$Page.type]";\
    print "        value <- arbitrary[$className$].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$packageName;format="cap"$$className$Page: Arbitrary[$className$Page.type] =";\
    print "    Arbitrary($className$Page)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[($className$Page.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Adding to NavigatorFor$packageName;format="cap"$"
awk '/class NavigatorFor$packageName;format="cap"$/ {\
    print;\
    print "";\
    print "  private def navigationFor$className$(userAnswers: UserAnswers, mode: Mode): Call = {";\
    print "    if (userAnswers.get(page = $className$Page).contains(true)) {";\
    print "      routes.HowMany$className$Controller.onPageLoad(mode)";\
    print "    } else if(mode == CheckMode){";\
    print "        routes.CheckYourAnswersController.onPageLoad";\
    print "    } else {";\
    print "        $nextPage$";\
    print "    }";\
    print "  }";\
    next }1' ../app/navigation/NavigatorFor$packageName;format="cap"$.scala > tmp && mv tmp ../app/navigation/NavigatorFor$packageName;format="cap"$.scala

awk '/private val normalRoutes/ {\
    print;\
    print "    case $className$Page => userAnswers => navigationFor$className$(userAnswers, NormalMode)";\
    print "    case HowMany$className$Page => userAnswers => $nextPage$";\
    next }1' ../app/navigation/NavigatorFor$packageName;format="cap"$.scala > tmp && mv tmp ../app/navigation/NavigatorFor$packageName;format="cap"$.scala

awk '/private val checkRouteMap/ {\
    print;\
    print "    case $className$Page => userAnswers => navigationFor$className$(userAnswers, CheckMode)";\
    next }1' ../app/navigation/NavigatorFor$packageName;format="cap"$.scala > tmp && mv tmp ../app/navigation/NavigatorFor$packageName;format="cap"$.scala

echo "Adding to ITCoreTestDataFor$packageName;format="cap"$"
awk '/trait ITCoreTestDataFor$packageName;format="cap"$/ {\
    print;\
    print "";\
    print "  val userAnswersFor$packageName;format="cap"$$className$Page: Map[String, UserAnswers] = {";\
    print "    val yesSelected = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, true).success.value";\
    print "    val noSelected = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, false).success.value";\
    print "    Map(\"yes\" -> yesSelected, \"no\" -> noSelected)";\
    print "    }";\
    next }1' ../it/testSupport/ITCoreTestDataFor$packageName;format="cap"$.scala > tmp && mv tmp ../it/testSupport/ITCoreTestDataFor$packageName;format="cap"$.scala

echo "Migration $className;format="snake"$ completed"
