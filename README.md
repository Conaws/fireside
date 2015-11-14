# Minimal Chat

Example project using ClojureScript, Reagent (Re-Frame) and Firebase (Matchbox)
as the basis for creating a persistent, server-less, client-side chat client.

## Getting Started

1. Sign up for a firebase.com account.
2. Make a Firebase application.
3. Clone the project.
4. Change `firebase-io-root` in `core.cljs` to have the URL for your Firebase
   application.
5. Run `lein figwheel` in the project root to start up a local webserver.
6. Point your browser to [http://localhost:3449](http://localhost:3449) and start playing around in the code!

## Developing

To start a local server that causes CSS and ClojureScript changes to
be reflected immediately in your browser run `lein fighweel` and open
up [http://localhost:3449](http://localhost:3449)

To run the ClojureScript tests run `lein auto-test`. It will recompile
the ClojureScript and rerun the tests whenever a file changes.

## Acknowledgement

This project is a fork of [Jake's Clojurescript Firebase Chat Example](https://github.com/jakemcc/clojurescript-firebase-chat-example).

## License

Distributed under the MIT License. Copyright (c) 2015 Paul Lam.

