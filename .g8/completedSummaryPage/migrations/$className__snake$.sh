#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$url$                       controllers.$packageName$.$className$Controller.onPageLoad()" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $packageName$.$className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $heading$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.panel.message = $panelMessage$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.whatNextText = $packageName$.$className;format="decap"$ what next text" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.detailsSummary = $detailsLinkText$" >> ../conf/messages.en

echo "Migration $packageName$.$className;format="snake"$ completed"
