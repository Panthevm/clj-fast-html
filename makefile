repl:
	clojure -M:test:repl

bench:
	clojure -X:bench :mode :average :warmup 2 :measurement 2 :fork 1 :threads 1 :status true :output-time-unit :ns 
