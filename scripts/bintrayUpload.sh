#!/bin/bash

./gradlew bintrayUpload -Puser="${BINTRAY_USER}" -Pkey="${BINTRAY_KEY}"