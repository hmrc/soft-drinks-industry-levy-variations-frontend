#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$                       controllers.$className$Controller.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.panel.message = $className;format="decap"$ panel message" >> ../conf/messages.en
echo "$className;format="decap"$.whatNextText = $className;format="decap"$ what next text" >> ../conf/messages.en
echo "$className;format="decap"$.detailsSummary = $className;format="decap"$ Details of x" >> ../conf/messages.en

echo "Migration $className;format="snake"$ completed"
