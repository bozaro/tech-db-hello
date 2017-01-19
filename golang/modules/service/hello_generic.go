package service

import (
	"github.com/bozaro/tech-db-hello/golang/models"
	"github.com/bozaro/tech-db-hello/golang/modules/assets/assets_db"
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
	"github.com/go-openapi/runtime/middleware"
	"github.com/jmoiron/sqlx"
	"github.com/rubenv/sql-migrate"
	"log"
	"fmt"
)

type HelloGeneric struct {
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

func NewHelloGeneric(dialect string, dataSourceName string) HelloGeneric {
	migrations := &migrate.AssetMigrationSource{
		Asset:    assets_db.Asset,
		AssetDir: assets_db.AssetDir,
		Dir:      "db/" + dialect,
	}
	db, err := sqlx.Open(dialect, dataSourceName)
	if err != nil {
		log.Fatal(err)
	}
	if _, err = migrate.Exec(db.DB, dialect, migrations, migrate.Up); err != nil {
		log.Fatal(err)
	}
	return HelloGeneric{db: db}
}

func (self HelloGeneric) AddMulti(params operations.AddMultiParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	stmt, err := tx.Preparex("INSERT INTO tasks (description, completed) VALUES ($1, $2)")
	check(err)

	posts := []Post{}
	for _, item := range params.Body {
		result := stmt.MustExec(item.Description, item.Completed)
		id, err := result.LastInsertId()
		check(err)
		posts = append(posts, Post{
			ID:          id,
			Description: item.Description,
			Completed:   item.Completed,
		})
	}
	check(tx.Commit())

	return operations.NewAddMultiCreated().WithPayload(PostsPayload(posts))
}

func (self HelloGeneric) DestroyOne(params operations.DestroyOneParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	result := tx.MustExec("DELETE FROM tasks WHERE id = $1", params.ID)

	count, err := result.RowsAffected()
	check(err)

	check(tx.Commit())
	if count == 0 {
		return operations.NewDestroyOneNotFound()
	}
	return operations.NewDestroyOneNoContent()
}

func (self HelloGeneric) Find(params operations.FindParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	query := "SELECT id, description, completed FROM tasks"
	args := []interface{}{}

	if params.Since != nil {
		query += " WHERE id "
		args = append(args, *params.Since)
		if *params.Order == "desc" {
			query += fmt.Sprintf("< $%d", len(args))
		} else {
			query += fmt.Sprintf("> $%d", len(args))
		}
	}
	query += " ORDER BY id"
	if *params.Order == "desc" {
		query += " DESC"
	}
	args = append(args, *params.Limit)
	query += fmt.Sprintf(" LIMIT $%d", len(args))

	posts := []Post{}
	check(tx.Select(&posts, query, args...))
	check(tx.Commit())

	return operations.NewFindOK().WithPayload(PostsPayload(posts))
}

func (self HelloGeneric) GetOne(params operations.GetOneParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	posts := []Post{}
	check(tx.Select(&posts, "SELECT id, description, completed FROM tasks WHERE id = $1", params.ID))
	check(tx.Commit())
	if len(posts) == 0 {
		return operations.NewGetOneNotFound()
	}
	return operations.NewGetOneOK().WithPayload(posts[0].Payload())
}

func (self HelloGeneric) UpdateOne(params operations.UpdateOneParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	result := tx.MustExec("UPDATE tasks SET description = $1, completed = $2 WHERE id = $3", params.Body.Description, params.Body.Completed, params.ID)

	count, err := result.RowsAffected()
	check(err)

	check(tx.Commit())
	if count == 0 {
		return operations.NewUpdateOneNotFound()
	}

	post := *params.Body
	post.ID = params.ID
	return operations.NewUpdateOneOK().WithPayload(&post)
}

func check(err error) {
	if err != nil {
		log.Panic(err)
	}
}
