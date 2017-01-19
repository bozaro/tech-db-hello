package service

import (
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
	"github.com/go-openapi/runtime/middleware"
)

type HelloImpl struct {
}

func NewHello() HelloHandler {
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
