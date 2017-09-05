export UID:=$(shell id -u)
export PWD=$(shell pwd)

## Testing
#################################################
check: test integration-test

test: prepare
	./scripts/ruby bundle exec rspec -c spec

integration-test: prepare
	docker-compose run --rm ruby bundle exec cucumber -c --tags ~@wip
#################################################


## Dependency management
#################################################
prepare: Gemfile build/phantomjs
	docker-compose run --rm --no-deps ruby bundle install --path=vendor/gems

build/phantomjs:
	mkdir -p build
	(cd build && \
			wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
			tar -xjf phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
			mv phantomjs-2.1.1-linux-x86_64 phantomjs)
#################################################


## Container management
#################################################
build:
	docker run --rm -v $(PWD):/opt/accountapp/ -u $(UID):$(UID) -w /opt/accountapp --entrypoint ./gradlew openjdk:8-jdk --no-daemon --info war
	docker-compose build run

clean:
	docker run --rm -v $(PWD):/opt/accountapp/ -u $(UID):$(UID) -w /opt/accountapp --entrypoint ./gradlew openjdk:8-jdk clean
	docker-compose rm -f run
#################################################

run:
	docker-compose up run

.PHONY: build clean run prepare test integration-test check
