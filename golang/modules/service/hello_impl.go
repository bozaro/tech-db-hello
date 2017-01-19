package service

import (
	"database/sql"
	"github.com/bozaro/tech-db-hello/golang/modules/assets/assets_db"
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
	"github.com/go-openapi/runtime/middleware"
	_ "github.com/lib/pq"
	_ "github.com/mattn/go-sqlite3"
	"github.com/rubenv/sql-migrate"
	"log"
)

type HelloImpl struct {
}

func NewHello() HelloHandler {
	migrations := &migrate.AssetMigrationSource{
		Asset:    assets_db.Asset,
		AssetDir: assets_db.AssetDir,
		Dir:      "db/sqlite3",
	}
	db, err := sql.Open("sqlite3", "tech-db-hello.db")
	if err != nil {
		log.Fatal(err)
	}
	if _, err = migrate.Exec(db, "sqlite3", migrations, migrate.Up); err != nil {
		log.Fatal(err)
	}
	return HelloImpl{}
}

func (HelloImpl) AddMulti(params operations.AddMultiParams) middleware.Responder {
	return middleware.NotImplemented("operation .AddMulti has not yet been implemented")
}

func (HelloImpl) DestroyOne(params operations.DestroyOneParams) middleware.Responder {
	return middleware.NotImplemented("operation .AddMulti has not yet been implemented")
}

func (HelloImpl) Find(params operations.FindParams) middleware.Responder {
	return middleware.NotImplemented("operation .AddMulti has not yet been implemented")
}

func (HelloImpl) GetOne(params operations.GetOneParams) middleware.Responder {
	return middleware.NotImplemented("operation .AddMulti has not yet been implemented")
}

func (HelloImpl) UpdateOne(params operations.UpdateOneParams) middleware.Responder {
	return middleware.NotImplemented("operation .AddMulti has not yet been implemented")
}
