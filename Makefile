.PHONY: build clean run

current_dir := $(shell pwd)
current_user := $(shell id -u)

build:
	docker run --rm -v $(current_dir):/opt/accountapp/ -u $(current_user):$(current_user) -w /opt/accountapp --entrypoint ./gradlew openjdk:8-jdk --no-daemon --info war

clean:
	docker run --rm -v $(current_dir):/opt/accountapp/ -u $(current_user):$(current_user) -w /opt/accountapp --entrypoint ./gradlew openjdk:8-jdk clean
	rm -Rf ./fake_data

generate_data:
	@if [ ! -d fake_data ]; then mkdir fake_data ;fi
	@./voteGenerator.py

open_election: generate_data
	docker-compose up --build open_election

close_election: generate_data
	docker-compose up --build close_election

local:
	docker-compose up --build local
