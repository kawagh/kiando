.DEFAULT_GOAL := help
.PHONY: help
help: ## show help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: all
all: ## do all tasks
	@make format
	@make test
	@make update

.PHONY: format
format: ## format by detekt
	@./gradlew detekt || true
	@./gradlew detekt

.PHONY: test
test: ## run test
	@./gradlew test
	@./gradlew connectedAndroidTes

.PHONY: update
update: ## update all (run all tasks named update-~)
	@make update-pictures
	@make update-changelog

.PHONY: update-pictures
update-pictures: ## update pictures/ (ex. app_icon, feature_graphic; run after `make test`
	@shell/update_pictures.sh

.PHONY: update-changelog
update-changelog: ## update CHANGELOG.md
	@shell/generate_changelog.py
