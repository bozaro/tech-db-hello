// +build tools
package forum_server_golang

//go:generate swagger generate server --target . --name hello --spec ../common/swagger.yml
//go:generate go-bindata -pkg assets_ui -o modules/assets/assets_ui/assets_ui.go -prefix ../common/swagger-ui/ ../common/swagger-ui/...
//go:generate go-bindata -pkg assets_db -o modules/assets/assets_db/assets_db.go -prefix assets/ assets/...

import (
  _ "github.com/go-swagger/go-swagger/cmd/swagger"
  _ "github.com/jteeuwen/go-bindata/go-bindata"
  _ "github.com/mailru/easyjson/easyjson"
)
