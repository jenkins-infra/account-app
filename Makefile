.PHONY: build shell prod

current_dir := $(shell pwd)
current_user := $(shell id -u)

build:
	docker run --rm -v $(current_dir):/opt/accountapp/ -u $(current_user):$(current_user) -w /opt/accountapp --entrypoint ./gradlew java:8 --no-daemon --info war
	docker-compose build run

clean:
	docker run --rm -v $(current_dir):/opt/accountapp/ -u $(current_user):$(current_user) -w /opt/accountapp --entrypoint ./gradlew java:8 clean
	docker-compose rm -f run

run:
	docker-compose up run
