package service

import (
	_ "github.com/mattn/go-sqlite3"
)

func NewHelloSQLite(dataSourceName string) HelloHandler {
	generic := NewHelloGeneric("sqlite3", dataSourceName)
	generic.db.SetMaxOpenConns(1)
	return generic
}
