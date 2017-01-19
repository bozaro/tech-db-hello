package service

import (
	"fmt"
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
	"github.com/go-openapi/runtime/middleware"
	_ "github.com/lib/pq"
)

type HelloPgSQL struct {
	HelloGeneric
}

func NewHelloPgSQL(dataSourceName string) HelloHandler {
	return HelloPgSQL{HelloGeneric: NewHelloGeneric("postgres", dataSourceName)}
}

func (self HelloPgSQL) AddMulti(params operations.AddMultiParams) middleware.Responder {
	posts := []Post{}
	if len(params.Body) > 0 {
		tx := self.db.MustBegin()
		defer tx.Rollback()

		query := "INSERT INTO tasks (description, completed) VALUES"
		args := []interface{}{}
		for idx, item := range params.Body {
			if idx > 0 {
				query += ","
			}
			query += fmt.Sprintf(" ($%d, $%d)", len(args)+1, len(args)+2)
			args = append(args, item.Description, item.Completed)
		}
		query += " RETURNING id, description, completed"

		check(tx.Select(&posts, query, args...))
		check(tx.Commit())
	}
	return operations.NewAddMultiCreated().WithPayload(PostsPayload(posts))
}
func (self HelloPgSQL) UpdateOne(params operations.UpdateOneParams) middleware.Responder {
	tx := self.db.MustBegin()
	defer tx.Rollback()

	posts := []Post{}
	check(tx.Select(&posts, "UPDATE tasks SET description = $1, completed = $2 WHERE id = $3 RETURNING  id, description, completed", params.Body.Description, params.Body.Completed, params.ID))
	check(tx.Commit())

	if len(posts) == 0 {
		return operations.NewUpdateOneNotFound()
	}

	post := *params.Body
	post.ID = params.ID
	return operations.NewUpdateOneOK().WithPayload(posts[0].Payload())
}
