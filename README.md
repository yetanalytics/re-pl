# re-pl

Try the [live demo](https://yetanalytics.github.io/re-pl/)!

A toy [re-frame](https://github.com/Day8/re-frame) application that presents the user with a [bootstrapped cljs](https://github.com/clojure/clojurescript/wiki/Bootstrapping-the-Compiler) repl.

Uses [replumb](https://github.com/Lambda-X/replumb) to make read/eval super easy, and [CodeMirror](https://codemirror.net/) for editing/highlighting.

## Development Mode

### Run application:

```
clj -A:fig:build
```

Figwheel will launch a browser for interactive development.

## Production Build

```
make site
```

Copyright © 2019 Yet Analytics, Inc.

Distributed under the Eclipse Public License, the same as Clojure.
