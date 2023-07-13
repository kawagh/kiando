#!/bin/bash

device=$(adb devices | head -n 2 | tail -n 1 | tr '\t' ' ' | cut -d ' ' -f 1)
application_id="jp.kawagh.kiando.debug"

for i in $(seq 1 5); do
  adb -s "${device}" -d shell "run-as ${application_id} cat /data/user/0/${application_id}/files/feature_graphic${i}.png" > "pictures/feature_graphic${i}.png"
done

adb -s "${device}" -d shell "run-as ${application_id} cat /data/user/0/${application_id}/files/app_icon.png" > "pictures/app_icon.png"
