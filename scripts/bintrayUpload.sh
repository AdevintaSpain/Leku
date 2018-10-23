#!/bin/bash

./gradlew -Puser="${BINTRAY_USER}" -Pkey="${BINTRAY_KEY}" bintrayUpload