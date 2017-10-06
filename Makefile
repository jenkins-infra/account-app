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

# Deploy an ldap and the accountapp with election open and fake data
open_election: generate_data
	docker-compose up --build open_election

# Deploy an ldap and the accountapp with election closed and fake data
close_election: generate_data
	docker-compose up --build close_election

# Deploy a ldap and the accountapp with election open and without fake data
sandbox:
	docker-compose up --build sandbox

# Deploy the accountapp on the host network and without ldap
local:
	docker-compose up --build local
