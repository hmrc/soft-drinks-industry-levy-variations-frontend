#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$className;format="decap"$                        controllers.$packageName$.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes
echo "POST       /$className;format="decap"$                        controllers.$packageName$.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.$packageName$.routes

echo "GET        /change$className$                  controllers.$packageName$.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes
echo "POST       /change$className$                  controllers.$packageName$.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.$option1key;format="decap"$ = $option1msg$" >> ../conf/messages.en
echo "$className;format="decap"$.$option2key;format="decap"$ = $option2msg$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Select $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$className$UserAnswersEntry: Arbitrary[($packageName$.$className$Page.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[$packageName$.$className$Page.type]";\
    print "        value <- arbitrary[$packageName$.$className$].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$packageName;format="cap"$.$className$Page: Arbitrary[$packageName$.$className$Page.type] =";\
    print "    Arbitrary($packageName$.$className$Page)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$packageName;format="cap"$.$className$: Arbitrary[$packageName$.$className$] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf($packageName$.$className$.values)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[($packageName$.$className$Page.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

awk '/private val normalRoutes/ {\
    print;\
    print "    case $packageName$.$className$Page => userAnswers => $nextPage$";\
    next }1' ../app/navigation/Navigator.scala > tmp && mv tmp ../app/navigation/Navigator.scala

echo "Migration $className;format="snake"$ completed"
