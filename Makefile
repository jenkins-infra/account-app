IMAGENAME=jenkinsciinfra/account-app
TAG=$(shell date '+%Y%m%d_%H%M%S')

target/accountapp.war :
	mvn install

bin/jetty-runner.jar :
	wget -O bin/jetty-runner.jar 'http://search.maven.org/remotecontent?filepath=org/mortbay/jetty/jetty-runner/8.1.15.v20140411/jetty-runner-8.1.15.v20140411.jar'

image : target/accountapp.war bin/jetty-runner.jar
	docker build -t ${IMAGENAME} .

run :
	docker run -P --rm -i -t -v ${PWD}:/etc/accountapp ${IMAGENAME}

tag :
	docker tag ${IMAGENAME} ${IMAGENAME}:${TAG}

push :
	docker push ${IMAGENAME}


