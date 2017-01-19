package service

import (
	"github.com/bozaro/tech-db-hello/golang/models"
	"github.com/bozaro/tech-db-hello/golang/modules/assets/assets_db"
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
	"github.com/go-openapi/runtime/middleware"
	"github.com/jmoiron/sqlx"
	_ "github.com/lib/pq"
	_ "github.com/mattn/go-sqlite3"
	"github.com/rubenv/sql-migrate"
	"log"
)

type HelloImpl struct {
	db *sqlx.DB
}

type Post struct {
	ID          int64  `db:"id"`
	Description string `db:"description"`
	Completed   bool   `db:"completed"`
}

func (self Post) Payload() *models.Item {
	return &models.Item{
		ID:          self.ID,
		Description: self.Description,
		Completed:   self.Completed,
	}
}

func PostsPayload(value []Post) []*models.Item {
	result := make([]*models.Item, len(value))
	for idx, item := range value {
		result[idx] = item.Payload()
	}
	return result
}

func NewHello() HelloHandler {
	migrations := &migrate.AssetMigrationSource{
		Asset:    assets_db.Asset,
		AssetDir: assets_db.AssetDir,
		Dir:      "db/sqlite3",
	}
	db, err := sqlx.Open("sqlite3", "tech-db-hello.db")
	if err != nil {
		log.Fatal(err)
	}
	if _, err = migrate.Exec(db.DB, "sqlite3", migrations, migrate.Up); err != nil {
		log.Fatal(err)
	}
	return HelloImpl{db: db}
}

func (HelloImpl) AddMulti(params operations.AddMultiParams) middleware.Responder {
	return middleware.NotImplemented("operation .AddMulti has not yet been implemented")
}

func (self HelloImpl) DestroyOne(params operations.DestroyOneParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	result := tx.MustExec("DELETE FROM tasks WHERE id = ?", params.ID)

	count, err := result.RowsAffected()
	check(err)

	check(tx.Commit())
	if count == 0 {
		return operations.NewDestroyOneNotFound()
	}
	return operations.NewDestroyOneNoContent()
}

func (self HelloImpl) Find(params operations.FindParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	query := "SELECT id, description, completed FROM tasks"
	args := []interface{}{}

	if params.Since != nil {
		query += " WHERE id > ?"
		args = append(args, *params.Since)
	}
	query += " ORDER BY id"
	if *params.Order == "desc" {
		query += " DESC"
	}
	query += " LIMIT ?"
	args = append(args, *params.Limit)

	posts := []Post{}
	check(tx.Select(&posts, query, args))
	check(tx.Commit())

	return operations.NewFindOK().WithPayload(PostsPayload(posts))
}

func (self HelloImpl) GetOne(params operations.GetOneParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	posts := []Post{}
	check(tx.Select(&posts, "SELECT id, description, completed FROM tasks WHERE id = ?", params.ID))
	check(tx.Commit())
	if len(posts) == 0 {
		return operations.NewGetOneNotFound()
	}
	return operations.NewGetOneOK().WithPayload(posts[0].Payload())
}

func (self HelloImpl) UpdateOne(params operations.UpdateOneParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	result := tx.MustExec("UPDATE tasks SET description = ?, completed = ? WHERE id = ?", params.Body.Description, params.Body.Completed, params.ID)

	count, err := result.RowsAffected()
	check(err)

	check(tx.Commit())
	if count == 0 {
		return operations.NewUpdateOneNotFound()
	}
	return operations.NewUpdateOneOK()
}

func check(err error) {
	if err != nil {
		log.Panic(err)
	}
}
