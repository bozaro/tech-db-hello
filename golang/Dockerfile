FROM ubuntu:16.04

MAINTAINER Artem V. Navrotskiy

# Обвновление списка пакетов
RUN apt-get -y update

#
# Установка postgresql
#
ENV PGVER 9.5
RUN apt-get install -y postgresql-$PGVER

# Run the rest of the commands as the ``postgres`` user created by the ``postgres-$PGVER`` package when it was ``apt-get installed``
USER postgres

# Create a PostgreSQL role named ``docker`` with ``docker`` as the password and
# then create a database `docker` owned by the ``docker`` role.
RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER docker WITH SUPERUSER PASSWORD 'docker';" &&\
    createdb -O docker docker

# Adjust PostgreSQL configuration so that remote connections to the
# database are possible.
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf

# And add ``listen_addresses`` to ``/etc/postgresql/$PGVER/main/postgresql.conf``
RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf

# Expose the PostgreSQL port
EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

# Back to the root user
USER root

#
# Сборка проекта
#

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

# Публикуем порт
EXPOSE 5000

#
# Запускаем PostgreSQL и сервер
#
CMD service postgresql start && hello-server --scheme=http --port=5000 --host=0.0.0.0
