.PHONY: build clean sandbox

current_dir := $(shell pwd)
current_user := $(shell id -u)

build:
	docker-compose build

clean:
	rm -Rf ./fake_data

# Deploy an ldap and the accountapp with fake data
fake:
	docker-compose up --build fake

# Deploy the accountapp on the host network and without ldap
local:
	docker-compose up --build local
