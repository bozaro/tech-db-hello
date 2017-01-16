FROM ubuntu:16.04

MAINTAINER Artem V. Navrotskiy

# Обвновление списка пакетов
RUN apt-get -y update

# Установка golang
RUN apt-get install -y golang git wget unzip

# Выставляем переменную окружения для сборки проекта
ENV GOPATH /opt/go
ENV PATH $GOPATH/bin:/usr/local/go/bin:$PATH

# Добавляем зависимости генератора
RUN go get -v github.com/go-swagger/go-swagger/cmd/swagger
RUN go get -v github.com/jteeuwen/go-bindata/...
RUN go get -v github.com/go-openapi/runtime
RUN go get -v github.com/jessevdk/go-flags
RUN go get -v github.com/tylerb/graceful
RUN go get -v golang.org/x/net/context

# Копируем исходный код в Docker-контейнер
RUN wget https://github.com/bozaro/tech-db-hello/archive/master.zip -O /opt/tech-db-hello-master.zip
RUN mkdir -p $GOPATH/src/github.com/bozaro/tech-db-hello/
RUN unzip /opt/tech-db-hello-master.zip -d /opt/ tech-db-hello-master/common/*
RUN mv /opt/tech-db-hello-master/common $GOPATH/src/github.com/bozaro/tech-db-hello/
ADD . $GOPATH/src/github.com/bozaro/tech-db-hello/golang/

# Собираем и устанавливаем пакет
RUN go generate -x github.com/bozaro/tech-db-hello/golang/...
RUN go get -v github.com/bozaro/tech-db-hello/golang/...
RUN go install github.com/bozaro/tech-db-hello/golang/cmd/...

# Запускаем сервер
EXPOSE 5000
CMD hello-server --scheme=http --port=5000 --host=0.0.0.0
