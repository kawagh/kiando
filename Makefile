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

.PHONY: update-pictures
update-pictures: ## update pictures/ (ex. app_icon, feature_graphic; run after `make test`
	@shell/update_pictures.sh