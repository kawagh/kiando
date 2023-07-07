.DEFAULT_GOAL := help
.PHONY: help
help: ## show help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: format
format: ## format by detekt
	@./gradlew detekt

.PHONY: test
test: ## run test
	@./gradlew test
	@./gradlew connectedAndroidTest

.PHONY: update-feature-graphic
update-feature-graphic: ## update feature graphic; run after test
	@adb -s emulator-5554 -d shell "run-as jp.kawagh.kiando.debug cat /data/user/0/jp.kawagh.kiando.debug/files/feature_graphic1.png" > pictures/feature_graphic1.png
	@adb -s emulator-5554 -d shell "run-as jp.kawagh.kiando.debug cat /data/user/0/jp.kawagh.kiando.debug/files/feature_graphic2.png" > pictures/feature_graphic2.png
	@adb -s emulator-5554 -d shell "run-as jp.kawagh.kiando.debug cat /data/user/0/jp.kawagh.kiando.debug/files/feature_graphic3.png" > pictures/feature_graphic3.png
	@adb -s emulator-5554 -d shell "run-as jp.kawagh.kiando.debug cat /data/user/0/jp.kawagh.kiando.debug/files/feature_graphic4.png" > pictures/feature_graphic4.png