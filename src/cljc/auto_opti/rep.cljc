(ns auto-opti.rep
  "Representations and the schemas that validate them.

  An evaluation (see `auto-opti.eval`) declares the `::opti/rep-type` it consumes.
  This namespace maps each rep-type keyword to the Malli schema a representation
  of that type must satisfy, and offers `valid-rep` to check a representation
  value against its rep-type."
  (:require
   [auto-core.schema :as opt-schema]
   [auto-opti        :as-alias opti]))

(def id "Malli schema to name a representation type." :keyword)

(def registry
  "Registry of representation types to the Malli schema validating their value.

  * `:seed` - a map holding a `:seed` key with a uuid value, e.g.
    `{:seed #uuid \"54b9758a-906f-4ec9-b1eb-1efef7f67e3b\"}`."
  {:seed [:map [:seed :uuid]]})

(def registry-schema
  "Malli schema for a representation registry: a rep-type keyword to a (Malli) schema."
  [:map-of id :any])

(defn valid-rep
  "Check that representation `rep` is compatible with `rep-type`.

  Returns `nil` when valid; otherwise a map describing the problem: `{:rep
  <humanized-errors>}` when the value does not match the rep-type schema, or
  `{:error :not-found :rep-type rep-type}` when the rep-type is absent from
  `registry`."
  ([rep-type rep] (valid-rep registry rep-type rep))
  ([registry rep-type rep]
   (if-let [schema (get registry rep-type)]
     (when-let [errors (opt-schema/validate-data-humanize schema rep)] {:rep errors})
     {:error :not-found
      :rep-type rep-type})))
