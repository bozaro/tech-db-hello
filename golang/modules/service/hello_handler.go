package service

import (
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
	"github.com/go-openapi/runtime/middleware"
)

type HelloHandler interface {
	AddMulti(params operations.AddMultiParams) middleware.Responder
	DestroyOne(params operations.DestroyOneParams) middleware.Responder
	Find(params operations.FindParams) middleware.Responder
	GetOne(params operations.GetOneParams) middleware.Responder
	UpdateOne(params operations.UpdateOneParams) middleware.Responder
}
