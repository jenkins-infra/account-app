.PHONY: build clean sandbox

current_dir := $(shell pwd)
current_user := $(shell id -u)

build:
	docker-compose build

clean:
	rm -Rf ./fake_data

generate_data:
	@if [ ! -d fake_data ]; then mkdir fake_data && chmod 0777 fake_data;fi
	@./voteGenerator.py

# Deploy an ldap and the accountapp with election open and fake data
fake: generate_data
	docker-compose up --build fake

# Deploy a ldap and the accountapp with election open and without fake data
sandbox:
	docker-compose up --build sandbox

# Deploy the accountapp on the host network and without ldap
local:
	docker-compose up --build local
