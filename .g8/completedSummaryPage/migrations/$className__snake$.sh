#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$className;format="decap"$                       controllers.$packageName$.$className$Controller.onPageLoad()" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $packageName$.$className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.panel.message = $packageName$.$className;format="decap"$ panel message" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.whatNextText = $packageName$.$className;format="decap"$ what next text" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.detailsSummary = $packageName$.$className;format="decap"$ Details of x" >> ../conf/messages.en

echo "Migration $packageName$.$className;format="snake"$ completed"
