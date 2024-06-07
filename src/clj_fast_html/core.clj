(ns clj-fast-html.core
  (:import
   (java.lang StringBuilder Iterable)
   (java.util Iterator HashSet)
   (clojure.lang
    IPersistentMap
    APersistentMap
    IPersistentSet
    Sequential
    MapEntry
    Keyword
    Numbers)))

(set! *warn-on-reflection* true)

(def ^:const ^String element-open-start-tag-string "<")
(def ^:const ^String element-close-start-tag-string ">")
(def ^:const ^String element-open-end-tag-string "</")
(def ^:const ^String element-close-end-tag-string ">")
(def ^:const ^String element-tag-id-string "#")
(def ^:const ^String element-tag-class-string ".")
(def ^:const ^String attribute-separator-string " ")
(def ^:const ^String attribute-value-open-string "='")
(def ^:const ^String attribute-value-close-string "'")
(def ^:const ^String attribute-class-separator-string " ")
(def ^:const ^String attribute-declaration-separator-string ":")
(def ^:const ^String attribute-declaration-end-string ";")
(def ^:const ^String attribute-id-name-string "id")
(def ^:const ^Keyword attribute-id-name-keyword :id)
(def ^:const ^String attribute-class-name-string "class")
(def ^:const ^Keyword attribute-class-name-keyword :class)
(def ^:const ^HashSet unclosed-tags
  (HashSet. #{"area" "base" "br" "col" "embed" "hr" "img" "input" "link" "meta" "param" "source" "track" "wbr"}))


(defprotocol NodeWriter
  "Writing an HTML element and its contents to an appender."
  (write-node [node ^StringBuilder appender]))

(defprotocol AttributeWriter
  "Writing an HTML element attribute to an appender."
  (write-attribute
    [attribute-value ^String attribute-name ^StringBuilder appender]
    [attribute-value ^String attribute-name ^StringBuilder appender ^String additional]))

(defn get-element-tag-id-index
  [^String element-tag]
  (let [index (.indexOf element-tag element-tag-id-string 1)]
    (when (Numbers/isPos index) index)))

(defn get-element-tag-class-index
  [^String element-tag]
  (let [index (.indexOf element-tag element-tag-class-string)]
    (when (Numbers/isPos index) index)))

(defn get-element-tag-name
  [^String element-tag id-index class-index]
  (cond
    id-index    (.substring element-tag 0 ^long id-index)
    class-index (.substring element-tag 0 ^long class-index)
    :else element-tag))

(defn get-element-tag-id
  [^String element-tag id-index class-index]
  (if class-index
    (.substring element-tag (unchecked-inc-int id-index) class-index)
    (.substring element-tag (unchecked-inc-int id-index))))

(defn get-element-tag-class
  [^String element-tag class-index]
  (->
   (.substring element-tag (unchecked-inc-int class-index))
   (.replace element-tag-class-string attribute-class-separator-string)))

(defn write-attribute-boolean
  [^StringBuilder appender ^String attribute-name]
  (.append appender attribute-separator-string)
  (.append appender attribute-name))

(defn write-attribute-string
  [^StringBuilder appender ^String attribute-name ^String attribute-value ^String additional]
  (if (Numbers/isPos (.length attribute-value))
    (do
      (.append appender attribute-separator-string)
      (.append appender attribute-name)
      (.append appender attribute-value-open-string)
      (when additional
        (.append appender additional)
        (.append appender attribute-class-separator-string))
      (.append appender attribute-value)
      (.append appender attribute-value-close-string))
    (when additional
      (.append appender attribute-separator-string)
      (.append appender attribute-name)
      (.append appender attribute-value-open-string)
      (.append appender additional)
      (.append appender attribute-value-close-string))))

(defn write-attribute-map
  [^StringBuilder appender ^String attribute-name ^APersistentMap attribute-value]
  (when-not (.isEmpty attribute-value)
    (let [iterator (.iterator attribute-value)]
      (.append appender attribute-separator-string)
      (.append appender attribute-name)
      (.append appender attribute-value-open-string)
      (while (.hasNext iterator)
        (let [^MapEntry entry (.next iterator)]
          (.append appender (.getName ^Keyword (.key entry)))
          (.append appender attribute-declaration-separator-string)
          (.append appender (.val entry))
          (when (.hasNext iterator)
            (.append appender attribute-declaration-end-string))))
      (.append appender attribute-value-close-string))))

(defn write-attribute-collection
  [^StringBuilder appender ^String attribute-name ^Iterable attribute-value ^String additional]
  (let [iterator (.iterator attribute-value)]
    (if (.hasNext iterator)
      (do 
        (.append appender attribute-separator-string)
        (.append appender attribute-name)
        (.append appender attribute-value-open-string)
        (when additional
          (.append appender additional)
          (.append appender attribute-class-separator-string))
        (while (.hasNext iterator)
          (when-let [item (.next iterator)]
            (.append appender item)
            (when (.hasNext iterator)
              (.append appender attribute-class-separator-string))))
        (.append appender attribute-value-close-string))
      (when additional
        (.append appender attribute-separator-string)
        (.append appender attribute-name)
        (.append appender attribute-value-open-string)
        (.append appender additional)
        (.append appender attribute-value-close-string)))))

(defn write-element-attributes
  [^StringBuilder appender ^APersistentMap attributes ^String tag-id ^String tag-class]
  (let [iterator         (.iterator attributes)
        write-tag-id?    (volatile! true)
        write-tag-class? (volatile! true)]
    (while (.hasNext iterator)
      (let [^MapEntry attribute (.next iterator)]
        (when-let [attribute-value (.val attribute)]
          (let [^Keyword attribute-name (.key attribute)
                attribute-name-string   (.getName attribute-name)]
            (cond
              (and tag-id (identical? attribute-name attribute-id-name-keyword))
              (do (vreset! write-tag-id? false)
                  (write-attribute (or attribute-value tag-id) attribute-name-string appender nil))
              (and tag-class (identical? attribute-name attribute-class-name-keyword))
              (do (vreset! write-tag-class? false)
                  (write-attribute attribute-value attribute-name-string appender tag-class))
              :else (write-attribute attribute-value attribute-name-string appender nil))))))
    (when (and tag-id @write-tag-id?)
      (write-attribute-string appender attribute-id-name-string tag-id nil))
    (when (and tag-class @write-tag-class?)
      (write-attribute-string appender attribute-class-name-string tag-class nil))))

(defn write-element-start-tag
  [^StringBuilder appender ^Iterator element-iterator ^String tag-name ^String tag-id ^String tag-class]
  (.append appender element-open-start-tag-string)
  (.append appender tag-name)
  (if (.hasNext element-iterator)
    (let [item (.next element-iterator)]
      (if (instance? IPersistentMap item)
        (do
          (write-element-attributes appender item tag-id tag-class)
          (.append appender element-close-start-tag-string))
        (do
          (when tag-id
            (write-attribute-string appender attribute-id-name-string tag-id nil))
          (when tag-class
            (write-attribute-string appender attribute-class-name-string tag-class nil))
          (.append appender element-close-start-tag-string)
          (write-node item appender))))
    (do
      (when tag-id
        (write-attribute-string appender attribute-id-name-string tag-id nil))
      (when tag-class
        (write-attribute-string appender attribute-class-name-string tag-class nil))
      (.append appender element-close-start-tag-string))))

(defn write-element-end-tag
  [^StringBuilder appender ^String element-tag-name]
  (.append appender element-open-end-tag-string)
  (.append appender element-tag-name)
  (.append appender element-close-end-tag-string))

(defn write-element
  [^StringBuilder appender ^Iterator element-iterator ^Keyword tag]
  (if (identical? tag :html/raw)
    (when (.hasNext element-iterator)
      (.append appender ^String (.next element-iterator)))
    (let [tag-string      (.getName tag)
          tag-id-index    (get-element-tag-id-index tag-string)
          tag-class-index (get-element-tag-class-index tag-string)
          tag-name        (get-element-tag-name tag-string tag-id-index tag-class-index)
          tag-id          (when tag-id-index
                            (get-element-tag-id tag-string tag-id-index tag-class-index))
          tag-class       (when tag-class-index
                            (get-element-tag-class tag-string tag-class-index))]
      (write-element-start-tag appender element-iterator tag-name tag-id tag-class)
      (when-not (.contains unclosed-tags tag-name)
        (while (.hasNext element-iterator)
          (write-node (.next element-iterator) appender))
        (write-element-end-tag appender tag-name)))))

(defn write-collection
  [^StringBuilder builder ^Iterable collection]
  (let [iterator (.iterator collection)]
    (when (.hasNext iterator)
      (let [item (.next iterator)]
        (if (instance? Keyword item)
          (write-element builder iterator item)
          (do (write-node item builder)
              (while (.hasNext iterator)
                (write-node (.next iterator) builder))))))))

(extend-protocol NodeWriter
  Sequential
  (write-node [node builder]
    (write-collection builder node))
  String
  (write-node [^String node builder]
    (.append ^StringBuilder builder node))
  Object
  (write-node [node builder]
    (.append ^StringBuilder builder node))
  nil
  (write-node [node builder]))

(extend-protocol AttributeWriter
  Boolean
  (write-attribute [attribute-value attribute-name builder additional]
    (write-attribute-boolean builder attribute-name))
  String
  (write-attribute [attribute-value attribute-name builder additional]
    (write-attribute-string builder attribute-name attribute-value additional))
  Sequential
  (write-attribute [attribute-value attribute-name builder additional]
    (write-attribute-collection builder attribute-name attribute-value additional))
  IPersistentSet
  (write-attribute [attribute-value attribute-name builder additional]
    (write-attribute-collection builder attribute-name attribute-value additional))
  IPersistentMap
  (write-attribute [attribute-value attribute-name builder additional]
    (write-attribute-map builder attribute-name attribute-value))
  Object
  (write-attribute [attribute-value attribute-name builder additional]
    (write-attribute-string builder attribute-name (.toString attribute-value) additional)))

(defn to-html-string
  [node]
  (let [builder (StringBuilder.)]
    (write-node node builder)
    (.toString builder)))


(comment
  (require 'clj-async-profiler.core)
  (def data
    )
  (clj-async-profiler.core/serve-ui 8888)
  (clj-async-profiler.core/profile
   (dotimes [_ 1000000]
     (to-html-string data))
   )
     
    )
