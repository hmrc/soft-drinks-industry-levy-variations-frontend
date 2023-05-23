#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.$packageName$.routes
echo "GET        /$className;format="decap"$                       controllers.$packageName$.$className$Controller.onPageLoad()" >> ../conf/app.$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$packageName$.$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en

echo "Migration $packageName$.$className;format="snake"$ completed"
