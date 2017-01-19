package service

import (
	_ "github.com/mattn/go-sqlite3"
)

func NewHelloSQLite(dataSourceName string) HelloHandler {
	return NewHelloGeneric("sqlite3", dataSourceName)
}
