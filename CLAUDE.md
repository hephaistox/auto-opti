# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## Project Overview

`auto-opti` is a Clojure(Script) library that gathers common features for discrete
optimization and simulation. It is deliberately **agnostic of any rendering or solver
technology**: it provides building blocks (PRNGs, probability distributions, statistics,
criteria comparison, evaluations, time-based variables, routings) that the user assembles
into an optimization chain.

Two guiding design decisions (see `docs/wiki/design_decisions.org`):

- **Flexibility over raw speed.** The optimization chain is configured with open parameter
  maps and registries that users enrich, rather than hard-coded pipelines.
- **Runs on JVM and JS.** Code is written in `.cljc` so it runs on both Clojure (JVM) and
  ClojureScript.

## Build / Test / Lint Commands

All workflows go through [Babashka](https://babashka.org) tasks defined in `bb.edn`:

| Command                | Purpose                                                                                     |
|------------------------|---------------------------------------------------------------------------------------------|
| `bb bp`                | **Before-push gate**: format → lint → clj tests → cljs node tests. Run this before pushing. |
| `bb clj-test`          | Run Clojure (JVM) tests via cognitect test-runner. Append `all` to run everything.          |
| `bb cljs-node-test`    | Run ClojureScript tests on Node via shadow-cljs (`:ltest` build).                           |
| `bb cljs-browser-test` | Run ClojureScript tests in a browser (`:browser-test` build).                               |
| `bb gha`               | Exact test sequence CI runs: `bb clj-test test-clj -v` then `bb cljs-node-test all -v`.     |
| `bb format`            | Format all sources with zprint (config in `.zprintrc`).                                     |
| `bb lint`              | Lint `src` and `test` with clj-kondo.                                                       |
| `bb repl`              | Start a Clojure/ClojureScript REPL (nREPL port 7003).                                       |
| `bb docs`              | Generate API docs with codox and publish to the `gh-pages` branch.                          |
| `bb deploy`            | Deploy the library.                                                                         |
| `bb deps`              | Update dependencies (`clj -M:antq` + `npm upgrade`).                                        |
| `bb clean`             | Remove `.cpcache`, `.shadow-cljs`, `target`, `node_modules`.                                |
| `bb copy`              | Sync shared files from source projects per `ext_src.edn`.                                   |
| `bb init`              | One-time: add the wiki repo (`auto-opti.wiki`) under `docs/`.                               |

CI (`.github/workflows/commit_validation.yml`) runs `bb lint`, re-runs `bb format` and
**fails if the pushed code was not already formatted**, then runs `bb gha`. Always run
`bb bp` (or at least `bb format`) before pushing.

To run a single namespace's tests, prefer the REPL or pass the namespace regexp to the
underlying runner; the test runner matches files named `*-test*`.

## Source Layout

- `src/cljc/auto_opti/` — the cross-platform library (the vast majority of the code). This
  is the only path on `:paths` in `deps.edn`.
- `src/clj/auto_opti/` — JVM-only implementations (e.g. native xoroshiro128/256 PRNGs under
  `prng/impl/`). Used for benchmarking/reference work.
- `test/cljc/` — cross-platform tests (run by both clj and cljs runners).
- `test/clj/` — JVM-only tests, including reference implementations.
- `test/c/` — a small C/C++ reference (`xoro_test.cpp`, build+run via `test/c/run`) used to
  validate PRNG output against the canonical algorithm.
- `docs/wiki/` — the project wiki (a separate git repo) with per-feature `.org` docs.

Each feature follows the same structure: a top-level public namespace
(e.g. `auto_opti/prng.cljc`) re-exporting a small API, with implementation details under an
`impl/` subdirectory marked `{:no-doc true}`.

## Architecture & Conventions

### Registry + factory pattern

Most features expose a **default registry** (a map from a keyword name to a builder
function) plus a public constructor that takes a parameter map. Users override the default
by passing their own `registry`. Examples:

- `auto-opti.prng/prng-registry` (`:xoroshiro128`, `:built-in`) → `prng`
- `auto-opti.proba-dist/distribution-registry` (`:uniform`, `:normal`, `:exponential`,
  `:categorical`, …) → `distribution`
- `auto-opti.eval/registry` (`:montecarlo-pi`, …)
- `auto-opti.crit-comp/default-registry` (`:hierarchise`, …)

Builders are looked up by keyword and invoked with the parameter map; constructors apply
sensible defaults (e.g. `prng` defaults to `:xoroshiro128`, `distribution` to `:uniform`).

### Namespaced keys

Parameter maps use keys qualified under the `auto-opti` namespace, aliased as `opti` via
`[auto-opti :as-alias opti]`. So you will see `::opti/seed`, `::opti/dstb-name`,
`::opti/prng`, `#::opti{:prng-name ... :seed ...}`, etc. Validation schemas use
[Malli](https://github.com/metosin/malli) (`registry-schema` defs).

### Protocols

Behavior is defined with `defprotocol` in dedicated namespaces and implemented in `impl/`:

- `auto-opti.prng.stateful/PRNG` — stateful, thread-safe random generation
  (`rnd-int`, `rnd-double`, `jump`, `duplicate`, `reset`, `uuid-seed`).
- `auto-opti.proba-dist.distribution-protocol` — `draw` and friends.
- `auto-opti.tb-var.protocol` — time-based variable storage.

### Feature namespaces (public API)

- `auto-opti.prng` — pseudo-random number generators (xoroshiro128, host built-in).
- `auto-opti.proba-dist` — probability distributions (uniform, normal, exponential,
  categorical, integer variants), backed by a PRNG; `kixi-stats` available.
- `auto-opti.sample` — statistics over collections (average, variance, std-dev, median, …).
- `auto-opti.maths` (+ `maths/gamma`, `maths/weighted-sum`) — cross-platform math helpers.
- `auto-opti.crit-comp` — compare two solutions' criteria (e.g. hierarchical ordering).
- `auto-opti.eval` (+ `eval/montecarlo`) — evaluate a representation into a solution.
- `auto-opti.tb-var` — time-bucketed variables with pluggable semantics
  (additive/latest/aggregated) and storage strategies (deltas/contiguous).
- `auto-opti.routings` — jobshop-style routes of operations (machine + processing time).
- `auto-opti.iterative.descent` — iterative optimization (WIP, `{:no-doc true}`).

### Style

- Format with zprint per `.zprintrc` (community style, sorted/justified requires,
  100-column width). Do not hand-format requires — let `bb format` do it.
- Keep cross-platform code in `.cljc`; use reader conditionals (`#?(:clj ... :cljs ...)`)
  for host differences.
- Public namespaces carry docstrings used by codox; mark implementation namespaces
  `{:no-doc true}`.

## Dependencies

Managed in `deps.edn` (Clojure) and `package.json` (shadow-cljs / Node tooling). Notable:
`com.github.hephaistox/auto-core` (shared Hephaistox utilities), `kixi/stats`,
`criterium` (benchmarking), `clj-xchart` (charts), `xoroshiro128`. The build tooling
(`bb.edn`) depends on `com.github.hephaistox/auto-build`.
