package restapi

import (
	"crypto/tls"
	"log"
	"net/http"
	"strings"

	"github.com/bozaro/tech-db-hello/golang/modules/assets/assets_ui"
	"github.com/bozaro/tech-db-hello/golang/modules/service"
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
	"github.com/dre1080/recover"
	assetfs "github.com/elazarl/go-bindata-assetfs"
	"github.com/go-openapi/errors"
	"github.com/go-openapi/runtime"
	"github.com/go-openapi/swag"
)

// This file is safe to edit. Once it exists it will not be overwritten

type DatabaseFlags struct {
	Database string `long:"database" description:"database connection parameters" default:"sqlite3:tech-db-hello.db"`
}

var dbFlags DatabaseFlags

func configureFlags(api *operations.HelloAPI) {
	api.CommandLineOptionsGroups = []swag.CommandLineOptionsGroup{
		{"database", "database connection parameters", &dbFlags},
	}
}

func configureAPI(api *operations.HelloAPI) http.Handler {
	// configure the api here
	api.ServeError = errors.ServeError

	// Set your custom logger if needed. Default one is log.Printf
	// Expected interface func(string, ...interface{})
	//
	// Example:
	// s.api.Logger = log.Printf

	api.JSONConsumer = runtime.JSONConsumer()

	api.JSONProducer = runtime.JSONProducer()

	var handler service.HelloHandler = service.NewHello(dbFlags.Database)
	api.AddMultiHandler = operations.AddMultiHandlerFunc(handler.AddMulti)
	api.DestroyOneHandler = operations.DestroyOneHandlerFunc(handler.DestroyOne)
	api.FindHandler = operations.FindHandlerFunc(handler.Find)
	api.GetOneHandler = operations.GetOneHandlerFunc(handler.GetOne)
	api.UpdateOneHandler = operations.UpdateOneHandlerFunc(handler.UpdateOne)
	api.ServerShutdown = func() {}

	return setupGlobalMiddleware(api.Serve(setupMiddlewares))
}

// The TLS configuration before HTTPS server starts.
func configureTLS(tlsConfig *tls.Config) {
	// Make all necessary changes to the TLS configuration here.
}

// As soon as server is initialized but not run yet, this function will be called.
// If you need to modify a config, store server instance to stop it individually later, this is the place.
// This function can be called multiple times, depending on the number of serving schemes.
// scheme value will be set accordingly: "http", "https" or "unix"
func configureServer(s *http.Server, scheme, addr string) {
}

// The middleware configuration is for the handler executors. These do not apply to the swagger.json document.
// The middleware executes after routing but before authentication, binding and validation
func setupMiddlewares(handler http.Handler) http.Handler {
	return handler
}

// The middleware configuration happens before anything, this middleware also applies to serving the swagger.json document.
// So this is a good place to plug in a panic handling middleware, logging and metrics
func setupGlobalMiddleware(handler http.Handler) http.Handler {
	recovery := recover.New(&recover.Options{
		Log: log.Print,
	})
	return recovery(uiMiddleware(handler))
}

func uiMiddleware(handler http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/swagger.json" || r.URL.Path == "/swagger.yml" {
			w.Header().Add("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			_, _ = w.Write(SwaggerJSON)
			return
		}
		// Serving Swagger UI
		if r.URL.Path == "/api/" {
			r.URL.Path = "/api"
		}
		if r.URL.Path != "/api" && !strings.HasPrefix(r.URL.Path, "/api/") {
			http.FileServer(&assetfs.AssetFS{
				Asset:    assets_ui.Asset,
				AssetDir: assets_ui.AssetDir,
			}).ServeHTTP(w, r)
			return
		}
		handler.ServeHTTP(w, r)
	})
}
