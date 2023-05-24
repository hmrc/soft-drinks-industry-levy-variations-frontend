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
echo "$packageName$.$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.hint = Enter a maximum of $maxLength$ characters" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.error.length = $className$ must be $maxLength$ characters or less" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrary$packageName;format="cap"$$className$UserAnswersEntry: Arbitrary[($className$Page.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[$className$Page.type]";\
    print "        value <- arbitrary[String].map(Json.toJson(_))";\
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

echo "Adding to Navigator$packageName;format="cap"$"

awk '/override val normalRoutes/ {\
    print;\
    print "    case $className$Page => userAnswers => $nextPage$";\
    next }1' ../app/navigation/NavigatorFor$packageName;format="cap"$.scala > tmp && mv tmp ../app/navigation/NavigatorFor$packageName;format="cap"$.scala

echo "Migration $className;format="snake"$ completed"