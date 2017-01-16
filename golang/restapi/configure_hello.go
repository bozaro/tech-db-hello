package restapi

import (
	"crypto/tls"
	"log"
	"net/http"
	"strings"

	recover "github.com/dre1080/recover"
	assetfs "github.com/elazarl/go-bindata-assetfs"
	errors "github.com/go-openapi/errors"
	runtime "github.com/go-openapi/runtime"
	middleware "github.com/go-openapi/runtime/middleware"
	graceful "github.com/tylerb/graceful"

	"github.com/bozaro/tech-db-hello/golang/modules/assets/ui"
	"github.com/bozaro/tech-db-hello/golang/restapi/operations"
)

// This file is safe to edit. Once it exists it will not be overwritten

//go:generate swagger generate server --target .. --name hello --spec ../../common/swagger.yml
//go:generate go-bindata -pkg ui -o ../modules/assets/ui/ui.go -prefix ../../common/swagger-ui/ ../../common/swagger-ui/...

func configureFlags(api *operations.HelloAPI) {
	// api.CommandLineOptionsGroups = []swag.CommandLineOptionsGroup{ ... }
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

	api.AddMultiHandler = operations.AddMultiHandlerFunc(func(params operations.AddMultiParams) middleware.Responder {
		return middleware.NotImplemented("operation .AddMulti has not yet been implemented")
	})
	api.DestroyOneHandler = operations.DestroyOneHandlerFunc(func(params operations.DestroyOneParams) middleware.Responder {
		return middleware.NotImplemented("operation .DestroyOne has not yet been implemented")
	})
	api.FindHandler = operations.FindHandlerFunc(func(params operations.FindParams) middleware.Responder {
		return middleware.NotImplemented("operation .Find has not yet been implemented")
	})
	api.GetOneHandler = operations.GetOneHandlerFunc(func(params operations.GetOneParams) middleware.Responder {
		return middleware.NotImplemented("operation .GetOne has not yet been implemented")
	})
	api.UpdateOneHandler = operations.UpdateOneHandlerFunc(func(params operations.UpdateOneParams) middleware.Responder {
		return middleware.NotImplemented("operation .UpdateOne has not yet been implemented")
	})

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
func configureServer(s *graceful.Server, scheme string) {
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
		if r.URL.Path == "/swagger.json" {
			handler.ServeHTTP(w, r)
			return
		}
		// Serving Swagger UI
		if !strings.HasPrefix(r.URL.Path, "/api/") {
			http.FileServer(&assetfs.AssetFS{
				Asset:     ui.Asset,
				AssetDir:  ui.AssetDir,
				AssetInfo: ui.AssetInfo,
			}).ServeHTTP(w, r)
			return
		}
		handler.ServeHTTP(w, r)
	})
}
