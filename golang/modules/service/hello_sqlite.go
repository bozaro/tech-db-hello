package service

import (
	_ "github.com/mattn/go-sqlite3"
)

func NewHelloSQLite(dataSourceName string) HelloGeneric {
	return NewHelloGeneric("sqlite3", dataSourceName)
}
